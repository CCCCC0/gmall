package com.atguigu.gmall.order.listeners;


import com.atguigu.gmall.pojo.OmsOrder;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderListener {

    @Autowired
    private OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAY_SUCCESS_QUENE")
    public void OrderMessageConsumer(MapMessage mapMessage) throws JMSException {

        String out_order_sn = mapMessage.getString("out_order_sn");
        String payStatus = mapMessage.getString("payStatus");

        OmsOrder omsOrder = new OmsOrder();

        if("用户未支付".equals(payStatus)){
            omsOrder.setStatus("5");
        }else if ("未支付".equals(payStatus)){
            omsOrder.setStatus("0");
        }else{
            omsOrder.setStatus("1");
            //支付成功才通知订单系统进行发货
            omsOrder.setOrderSn(out_order_sn);
            orderService.updateOmsOrder(omsOrder);
            OmsOrder order = orderService.getOmsOrderByOrder_sn(out_order_sn);
            orderService.sendPaySuccessMessageToGwareSystem(order); //将订单信息传入
            return;
        }
        omsOrder.setOrderSn(out_order_sn);
        orderService.updateOmsOrder(omsOrder);
    }

}
