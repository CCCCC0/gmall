package com.atguigu.gmall.passport.service.impl;

import com.atguigu.gmall.service.CasService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.*;

@Service
public class CasServiceImpl implements CasService {

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
    public void sendLoginSuccessMessageToCartSystem(String cookieValue,String userId) {

        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();

        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue successQuene = session.createQueue("LOGIN_SUCCESS_QUENE");
            MessageProducer producer = session.createProducer(successQuene);

            MapMessage message = new ActiveMQMapMessage();
            //对cookie中的内容进行判断
            if (StringUtils.isNotBlank(cookieValue) && !"[]".equals(cookieValue)){
                message.setString("cookieValue",cookieValue);
                message.setString("userId",userId);
            }

            producer.send(message);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            session.commit();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
