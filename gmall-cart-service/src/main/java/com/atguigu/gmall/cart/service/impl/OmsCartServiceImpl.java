package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.mapper.OmsCartMapper;
import com.atguigu.gmall.pojo.OmsCartItem;
import com.atguigu.gmall.service.OmsCartService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OmsCartServiceImpl implements OmsCartService {

    @Autowired
    private OmsCartMapper omsCartMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public OmsCartItem selectOmsCartItemByUserIdAndSkuId(String userId, String skuId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(userId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem cartItem = omsCartMapper.selectOne(omsCartItem);

        return cartItem;
    }



    @Override
    public void addOmsCart(OmsCartItem omsCartItem) {

        String memberId = omsCartItem.getMemberId();

        omsCartMapper.insertSelective(omsCartItem);

        toSynchronizedDataToRedis(memberId);
    }

    @Override
    public void updateOmsCart(OmsCartItem omsCartItem) {

        Example example = new Example(OmsCartItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",omsCartItem.getId());
        omsCartMapper.updateByExampleSelective(omsCartItem,example);

        //同步数据到redis
        Jedis jedis = redisUtil.getJedis();
        String key = "user:" + omsCartItem.getMemberId() + ":carts";
        jedis.hset(key,omsCartItem.getProductSkuId(),JSON.toJSONString(omsCartItem));
        jedis.close();
    }

    @Override
    public List<OmsCartItem> selectOmsCartItemsByRedis(String userId) {

        Jedis jedis = redisUtil.getJedis();
        String key = "user:" + userId + ":carts";
        List<String> hvals = jedis.hvals(key);

        List<OmsCartItem> omsCartItems = new ArrayList<>();

        if (hvals != null && hvals.size() > 0){
            for (String hval : hvals) {
                if(StringUtils.isNotBlank(hval)){
                    OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                    omsCartItems.add(omsCartItem);
                }
            }
        }

        return omsCartItems;
    }

    @Override
    public void updateIsCheckedStatus(String userId, String skuId, String isChecked) {

        if(StringUtils.isNotBlank(userId)) {
            //先在DB中进行修改
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setIsChecked(isChecked);

            Example example = new Example(OmsCartItem.class);
            example.createCriteria().andEqualTo("productSkuId",skuId).andEqualTo("memberId",userId);
            //有值就修改 无值的话不修改
            omsCartMapper.updateByExampleSelective(omsCartItem, example);

            //将所有内容同步到redis中
            toSynchronizedDataToRedis(userId);
        }
    }

    @Override
    public void deleteIsCheckedCartsByUserId(String userId) {

        //将DB中的数据删除 mysql - redis
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",userId).andEqualTo("isChecked","1");
        omsCartMapper.deleteByExample(example);

        //将redis中的数据进行删除
       toSynchronizedDataToRedis(userId);

    }

    @Override
    public void addOmsCarts(List<OmsCartItem> cartItems,String userId) {

        for (OmsCartItem cartItem : cartItems) {
            if (cartItem != null) {
                cartItem.setMemberId(userId);
                omsCartMapper.insertSelective(cartItem);
            }
        }

        //同步到Catch
        toSynchronizedDataToRedis(userId);
    }

    private void toSynchronizedDataToRedis(String userId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(userId);
        List<OmsCartItem> omsCartItems = omsCartMapper.select(omsCartItem);
        Jedis jedis = redisUtil.getJedis();
        if(omsCartItems!=null&&omsCartItems.size()>0){

            Map<String,String> map = new HashMap<>();

            for (OmsCartItem cartItem : omsCartItems) {

                String k = cartItem.getProductSkuId();

                String v = JSON.toJSONString(cartItem);

                map.put(k,v);

            }
            jedis.hmset("user:"+userId+":carts",map);
        }else {
            jedis.del("user:"+userId+":carts");
        }
        jedis.close();

    }


}
