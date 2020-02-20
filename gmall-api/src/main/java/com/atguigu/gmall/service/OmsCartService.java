package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.OmsCartItem;

import java.util.List;

public interface OmsCartService {

    OmsCartItem selectOmsCartItemByUserIdAndSkuId(String userId,String skuId);

    void addOmsCart(OmsCartItem omsCartItem);

    void updateOmsCart(OmsCartItem omsCartItem);

    List<OmsCartItem> selectOmsCartItemsByRedis(String userId);

    void updateIsCheckedStatus(String userId, String skuId, String isChecked);

    void deleteIsCheckedCartsByUserId(String userId);

    void addOmsCarts(List<OmsCartItem> cartItems,String userId);

}
