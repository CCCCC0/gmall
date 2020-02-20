package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.order.mapper.UmsReceiveAddressMapper;
import com.atguigu.gmall.pojo.OmsCartItem;
import com.atguigu.gmall.pojo.OmsOrder;
import com.atguigu.gmall.pojo.OmsOrderItem;
import com.atguigu.gmall.pojo.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;


import javax.jms.*;
import javax.jms.Queue;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UmsReceiveAddressMapper umsReceiveAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
    public List<UmsMemberReceiveAddress> getUserReceiveAddress(String userId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(userId);
        List<UmsMemberReceiveAddress> addresses = umsReceiveAddressMapper.select(umsMemberReceiveAddress);
        return addresses;
    }

    @Override
    public List<OmsCartItem> getIsCheckedCarts(String userId) {
        Jedis jedis = redisUtil.getJedis();
        //从缓存中获取被选中的购物车数据 - 减轻DB压力
        String key = "user:" + userId + ":carts";
        List<String> hvals = jedis.hvals(key);    //在 redis中以 map中存储内容
        List<OmsCartItem> cartItemList = new ArrayList<>();

        if(hvals != null && hvals.size() > 0){
            for (String hval : hvals) {
                if(StringUtils.isNotBlank(hval)){
                    OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                    if(omsCartItem != null && "1".equals(omsCartItem.getIsChecked())){
                        cartItemList.add(omsCartItem);
                    }
                }
            }
        }
        return cartItemList;
    }

    @Override
    public UmsMemberReceiveAddress getUserReceiveAddressById(String addressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(addressId);
        UmsMemberReceiveAddress receiveAddress = umsReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return receiveAddress;
    }

    @Override
    public void addOrder(OmsOrder omsOrder) {
        //将order 和 orderItem一起插入到DB中
        if(omsOrder != null){
            omsOrderMapper.insertSelective(omsOrder);
        }

        String orderId = omsOrder.getId();
        if(StringUtils.isNotBlank(orderId)) {
            List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
            for (OmsOrderItem omsOrderItem : omsOrderItems) {
                omsOrderItem.setOrderId(orderId);
                omsOrderItemMapper.insertSelective(omsOrderItem);
            }
        }

    }

    @Override
    public String putCacheTradeCodeAndReturn(String userId) {
        String tradeCode = "psyduckGmall";
        long timeMillis = System.currentTimeMillis();
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        String format = simpleDateFormat.format(date);
        tradeCode = tradeCode + userId + format;

        //存入到redis中
        String key = "user:" + userId + ":tradeCode";
        Jedis jedis = RedisUtil.getJedis();
        jedis.setex(key,60*60*3,tradeCode);
        jedis.close();

        return tradeCode;
    }

    @Override
    public boolean equalsTradeByCatch(String userId, String tradeCode) {
        Jedis jedis = RedisUtil.getJedis();
        String key = "user:" + userId + ":tradeCode";
        String tradeCodeByCatch = jedis.get(key);

        String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object eval = jedis.eval(script, Collections.singletonList(key), Collections.singletonList(tradeCode));

        //利用返回值进行判断
        BigDecimal result = new BigDecimal((Long) eval);
        int i = result.compareTo(new BigDecimal("1"));

        if(i == 0){
            jedis.close();
            return true;
        }else{
            return false;
        }

        //此情况在面对高并发情况会出现tradeCode存在的情况
//        if(StringUtils.isNotBlank(tradeCodeByCatch) && StringUtils.isNotBlank(tradeCode) && tradeCode.equals(tradeCodeByCatch)){
//                //进行DB的删除
//                jedis.del(key);
//                return true;
//            }
    }

    @Override
    public OmsOrder getOmsOrderByOrder_sn(String order_sn) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(order_sn);
        OmsOrder order = omsOrderMapper.selectOne(omsOrder);

        String orderId = order.getId();

        OmsOrderItem omsOrderItem = new OmsOrderItem();
        omsOrderItem.setOrderId(orderId);
        omsOrderItem.setOrderSn(order_sn);
        List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItem);
        order.setOmsOrderItems(omsOrderItems);

        return order;
    }

    @Override
    public void updateOmsOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());
        omsOrderMapper.updateByExampleSelective(omsOrder,example);
    }

    //通知库存系统锁定库存
    @Override
    public void sendPaySuccessMessageToGwareSystem(OmsOrder order) {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue queue = session.createQueue("ORDER_PAID_QUENE");
            MessageProducer producer = session.createProducer(queue);
            TextMessage message = new ActiveMQTextMessage();
            message.setText(JSON.toJSONString(order));
            producer.send(message);

            session.commit();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
