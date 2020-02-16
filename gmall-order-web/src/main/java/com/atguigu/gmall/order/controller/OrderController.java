package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginIsRequiredIntercept;
import com.atguigu.gmall.pojo.OmsCartItem;
import com.atguigu.gmall.pojo.OmsOrder;
import com.atguigu.gmall.pojo.OmsOrderItem;
import com.atguigu.gmall.pojo.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.OmsCartService;
import com.atguigu.gmall.service.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    private OrderService orderService;

    @Reference
    private OmsCartService omsCartService;

    @LoginIsRequiredIntercept
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap modelMap){

        //获取用户id - 查找购买用户的地址
        String userId = (String) request.getAttribute("userId");
        List<UmsMemberReceiveAddress> userReceiveAddress = orderService.getUserReceiveAddress(userId);

        //将购物车中的数据存入
        List<OmsCartItem> omsCartItems = orderService.getIsCheckedCarts(userId);

        //将购物车数据 转化为orderItem数据
        List<OmsOrderItem> orderItemList = changeCartItemToOrderItem(omsCartItems,null);

        //获取订单的总金额
        BigDecimal totalMoney = getTotalMoney(omsCartItems);

        //生成交易码 - 一个放在页面 一个放在redis中
        String tradeCode = orderService.putCacheTradeCodeAndReturn(userId);
        modelMap.put("orderDetailList",orderItemList);
        modelMap.put("userAddressList",userReceiveAddress);
        modelMap.put("totalAmount",totalMoney);
        modelMap.put("tradeCode",tradeCode);

        return "trade";
    }

    @LoginIsRequiredIntercept
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request, ModelMap modelMap,String addressId,String tradeCode) {

        //获取登入的用户信息
        String userId = (String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");

        //进行比较页面中的tradeCode 与 缓存中的比较
        boolean flag = orderService.equalsTradeByCatch(userId, tradeCode);

        if (flag) {
            //接收用户选择的信息 -
            if (StringUtils.isNotBlank(addressId)) {
                //查询出接收用户数据
                UmsMemberReceiveAddress umsMemberReceiveAddress = orderService.getUserReceiveAddressById(addressId);

                //生成大订单对象
                OmsOrder omsOrder = new OmsOrder();

                //生成订单Id
                String order_sn = "";
                String key = "psyduckGmall";
                long timeMillis = System.currentTimeMillis();
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String formatDate = format.format(date);
                order_sn = order_sn + key + timeMillis + formatDate;

                //插入订单中的数据
                omsOrder.setOrderSn(order_sn);
                omsOrder.setNote("可达鸭商城订单");
                omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
                omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
                omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
                omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
                omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
                omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
                omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
                omsOrder.setMemberId(userId);
                omsOrder.setMemberUsername(nickName);
                omsOrder.setCreateTime(new Date());
                omsOrder.setStatus("0");
                //变换时间的工具类
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, 3);
                Date time = calendar.getTime();
                omsOrder.setReceiveTime(time);

                //获取当前用户选择的所用购物车数据
                List<OmsCartItem> carts = orderService.getIsCheckedCarts(userId);
                //订单的总金额
                BigDecimal totalMoney = getTotalMoney(carts);
                omsOrder.setTotalAmount(totalMoney);
                omsOrder.setPayAmount(totalMoney);

                //将购物车数据 转化 为orderItem数据
                List<OmsOrderItem> orderItemList = changeCartItemToOrderItem(carts, order_sn);
                omsOrder.setOmsOrderItems(orderItemList);

                //将orderItem 和 Order存储到DB中
                orderService.addOrder(omsOrder);

                //需要将购物车中已生成订单的数据进行清除
                omsCartService.deleteIsCheckedCartsByUserId(userId);

                //重定向到支付页面 - 参数：订单号 - 总金额
                return "redirect:http://pay.gmall.com:8088/goPay?totalMoney="
                        + totalMoney + "&order_sn=" + order_sn + "&nickName=" + nickName;
            }
        }
        return "tradeFail";
    }


    private BigDecimal getTotalMoney(List<OmsCartItem> cartList) {
        BigDecimal totalMoney = new BigDecimal("0");

        for (OmsCartItem omsCartItem : cartList) {
            if(omsCartItem != null && "1".equals(omsCartItem.getIsChecked())){
                omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
                totalMoney = totalMoney.add(omsCartItem.getTotalPrice());
            }
        }

        return totalMoney;
    }

    //将被选中购物车数据 转化 成orderItem数据
    private List<OmsOrderItem> changeCartItemToOrderItem(List<OmsCartItem> omsCartItems,String order_sn){

        List<OmsOrderItem> orderItemList = new ArrayList<>();

        if(omsCartItems != null && omsCartItems.size() > 0){
            for (OmsCartItem omsCartItem : omsCartItems) {
                if(omsCartItem != null){
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductSkuCode(omsCartItem.getProductSkuCode());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setOrderSn(order_sn);
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());

                    orderItemList.add(omsOrderItem);
                }
            }
        }

        return orderItemList;
    }

}
