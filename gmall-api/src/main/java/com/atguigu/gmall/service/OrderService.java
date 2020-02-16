package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.OmsCartItem;
import com.atguigu.gmall.pojo.OmsOrder;
import com.atguigu.gmall.pojo.UmsMemberReceiveAddress;


import java.util.List;

public interface OrderService {


    List<UmsMemberReceiveAddress> getUserReceiveAddress(String userId);

    List<OmsCartItem> getIsCheckedCarts(String userId);

    UmsMemberReceiveAddress getUserReceiveAddressById(String addressId);

    void addOrder(OmsOrder omsOrder);

    String putCacheTradeCodeAndReturn(String userId);

    boolean equalsTradeByCatch(String userId, String tradeCode);

    OmsOrder getOmsOrderByOrder_sn(String order_sn);

}
