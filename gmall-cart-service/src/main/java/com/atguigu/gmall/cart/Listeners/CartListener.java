package com.atguigu.gmall.cart.Listeners;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pojo.OmsCartItem;
import com.atguigu.gmall.service.OmsCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.List;

@Component
public class CartListener {

    @Autowired
    private OmsCartService omsCartService;

    @JmsListener(destination = "LOGIN_SUCCESS_QUENE",containerFactory = "jmsQueueListener")
    public void CartConsumerLoginSuccessMessage(MapMessage mapMessage) throws JMSException {

        String cookieValue = mapMessage.getString("cookieValue");
        String userId = mapMessage.getString("userId");

        //从cookie中获取购物车内容
        List<OmsCartItem> cartItems = JSON.parseArray(cookieValue, OmsCartItem.class);
        if(cartItems != null && cartItems.size() > 0){
            omsCartService.addOmsCarts(cartItems,userId);
        }

    }


}
