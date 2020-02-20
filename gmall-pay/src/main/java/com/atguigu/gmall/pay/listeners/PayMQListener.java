package com.atguigu.gmall.pay.listeners;

import com.atguigu.gmall.pojo.PaymentInfo;
import com.atguigu.gmall.service.PaymentInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PayMQListener{

    @Autowired
    private PaymentInfoService paymentInfoService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAY_RESULT_CHECK_QUENE")
    public void PayMessageConsumer(MapMessage mapMessage) throws JMSException {

        //从消息队列中获取消息 - 获取消息后 根据消息 来进行支付信息的修改
        long count = mapMessage.getLong("count");
        String out_order_sn = mapMessage.getString("out_order_sn");

        //进行消息的消费
        PaymentInfo paymentInfo = paymentInfoService.checkPayIsSuccessToAliPay(out_order_sn);

        //进行查询支付的状态是否成功来判断 和 请求的次数进行是否发送延迟队列来向支付宝端询问订单状态
        String paymentStatus = paymentInfo.getPaymentStatus();

        if(StringUtils.isBlank(paymentStatus) || paymentStatus.equals("未支付")){
            count++;
            if(count <= 7){
                //查询次数未到7次
                //支付状态还未支付 - 需要请求支付宝端询问 - 并且继续发送延迟队列
                PaymentInfo info = new PaymentInfo();
                info.setOrderSn(out_order_sn);
                paymentInfoService.sendMessageToPaySystemCheckPayResult(info,count);
                System.out.println("支付未成功 - 请重新支付");
            }else{
                //查询7次之后 - 还未成功 则不进行状态的修改 但是发送消息通知订单系统进行数据更改
                paymentInfo.setOrderSn(out_order_sn);
                paymentInfo.setPaymentStatus("用户未支付");
                paymentInfoService.updatePatmentInfo(paymentInfo);
                paymentInfoService.sendPaySuccessMessage(paymentInfo);
                System.out.println("支付7次都未成功");
            }
        }else{
            //如果查询支付已成功 - 则进行数据的修改 - 并且通知订单系统进行修改数据
            boolean flag = paymentInfoService.searPaymentInfoIsUpdate(out_order_sn);
            if(!flag){
                paymentInfoService.updatePatmentInfo(paymentInfo);
                paymentInfoService.sendPaySuccessMessage(paymentInfo);
                System.out.println("支付成功了");
            }
        }
    }


}
