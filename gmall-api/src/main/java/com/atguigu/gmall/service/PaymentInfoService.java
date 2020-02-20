package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PaymentInfo;

public interface PaymentInfoService {

    void addPaymentInfo(PaymentInfo paymentInfo);

    void updatePatmentInfo(PaymentInfo paymentInfo);

    void sendMessageToPaySystemCheckPayResult(PaymentInfo paymentInfo,Long count);

    PaymentInfo getPayMentInfoByOrder_sn(String out_order_sn);

    PaymentInfo checkPayIsSuccessToAliPay(String out_order_sn);

    boolean searPaymentInfoIsUpdate(String out_order_sn);

    void sendPaySuccessMessage(PaymentInfo paymentInfo);
}
