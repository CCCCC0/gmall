package com.atguigu.gmall.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.pay.mapper.PaymentInfoMapper;
import com.atguigu.gmall.pojo.PaymentInfo;
import com.atguigu.gmall.service.PaymentInfoService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Autowired
     private ActiveMQUtil activeMQUtil;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Override
    public void addPaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

    }

    @Override
    public void updatePatmentInfo(PaymentInfo paymentInfo) {

        Example example = new Example(PaymentInfo.class);
        //通过order_sn进行支付信息的修改
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());
        paymentInfoMapper.updateByExampleSelective(paymentInfo, example);

    }

    @Override
    public void sendMessageToPaySystemCheckPayResult(PaymentInfo paymentInfo,Long count) {

        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();

            //创建Session 开启事务
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue successQueue = session.createQueue("PAY_RESULT_CHECK_QUENE");

            MessageProducer producer = session.createProducer(successQueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_order_sn",paymentInfo.getOrderSn());
            mapMessage.setLong("count",count);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,2*1000); //20秒后生效

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);   //设置持久化操作
            producer.send(mapMessage);                           //生产消息

            session.commit();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public PaymentInfo getPayMentInfoByOrder_sn(String out_order_sn) {

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(out_order_sn);
        PaymentInfo pay = paymentInfoMapper.selectOne(paymentInfo);

        return pay;
    }

    @Override
    public PaymentInfo checkPayIsSuccessToAliPay(String out_order_sn) {

        //获取订单号 - 向阿里端发送请求获取支付的内容
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",out_order_sn);
        String jsonString = JSON.toJSONString(map);
        request.setBizContent(jsonString);

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        boolean flag = response.isSuccess();

        PaymentInfo paymentInfo = new PaymentInfo();
        if(flag){
            paymentInfo.setOrderSn(out_order_sn);
            paymentInfo.setCallbackContent(response.toString());
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setAlipayTradeNo(response.getTradeNo());
            String tradeStatus = response.getTradeStatus();
            if("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)){
                paymentInfo.setPaymentStatus("已支付");
            }
            System.out.println("调用成功");
        } else {
            paymentInfo.setPaymentStatus("未支付");
            System.out.println("调用失败");
        }

        return paymentInfo;
    }

    @Override
    public boolean searPaymentInfoIsUpdate(String out_order_sn) {

        PaymentInfo paymentInfo = getPayMentInfoByOrder_sn(out_order_sn);
        String paymentStatus = paymentInfo.getPaymentStatus();
        boolean flag = false;
        if("已支付".equals(paymentStatus)){
            flag = true;
        }

        return flag;
    }

    @Override
    public void sendPaySuccessMessage(PaymentInfo paymentInfo) {

        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue pay_success_quene = session.createQueue("PAY_SUCCESS_QUENE");
            MessageProducer producer = session.createProducer(pay_success_quene);

            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_order_sn",paymentInfo.getOrderSn());
            mapMessage.setString("payStatus",paymentInfo.getPaymentStatus());
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);

            session.commit();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
