package com.atguigu.gmall.pay.service.impl;

import com.atguigu.gmall.pay.mapper.PaymentInfoMapper;
import com.atguigu.gmall.pojo.PaymentInfo;
import com.atguigu.gmall.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {

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

}
