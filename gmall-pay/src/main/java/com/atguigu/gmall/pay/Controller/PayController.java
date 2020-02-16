package com.atguigu.gmall.pay.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginIsRequiredIntercept;
import com.atguigu.gmall.pay.config.AlipayConfig;
import com.atguigu.gmall.pojo.OmsOrder;
import com.atguigu.gmall.pojo.PaymentInfo;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PayController {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Reference
    private OrderService orderService;

    @RequestMapping("goPay")
    public String goPay(BigDecimal totalMoney, String order_sn,String nickName, ModelMap modelMap){

        modelMap.put("order_sn",order_sn);
        modelMap.put("totalMoney",totalMoney);
        modelMap.put("nickName",nickName);

        return "index";
    }

    @ResponseBody
    @LoginIsRequiredIntercept(isRequired = true)
    @RequestMapping("alipay/submit")
    public String goAlipay(HttpServletRequest request,String order_sn){

        //业务逻辑
        //点击后 发送请求到阿里 获取from表单后 将表单返回到一个页面  页面自动跳转到支付页面
        //支付成功后 阿里会回跳到本页面

        // 创建请求接口的对象
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        OmsOrder omsOrder = orderService.getOmsOrderByOrder_sn(order_sn);

        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",order_sn);  //订单号
        map.put("total_amount",0.01);    //订单总金额
        map.put("product_code","FAST_INSTANT_TRADE_PAY");    //支付宝产品名
        map.put("subject",omsOrder.getOmsOrderItems().get(0).getProductName());  //订单商品名称
        String jsonString = JSON.toJSONString(map);
        alipayRequest.setBizContent(jsonString);

        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //需生成支付信息 - 进行保存 - 无论请求成功与否 都进行支付信息的保存
        //支付成功后 - 支付宝会与当前支付用户建立一个交易码 并会返回
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(order_sn);
        paymentInfo.setTotalAmount(omsOrder.getTotalAmount());
        paymentInfo.setSubject(omsOrder.getOmsOrderItems().get(0).getProductName());
        paymentInfo.setPaymentStatus("未支付");
        paymentInfoService.addPaymentInfo(paymentInfo);

        //返回form表单
        return form;
    }


    @RequestMapping("alipay/callback/return")
    public String alipaySyncCallBack(HttpServletRequest request){

        //支付宝同步回调的页面
        //需要更新支付订单的信息 - 然后跳回支付成功页面

        /*
        http://pay.gmall.com:8088/alipay/callback/return?charset=utf-8
        &out_trade_no=psyduckGmall158184207257920200216163432
        &method=alipay.trade.page.pay.return
        &total_amount=0.01
        &sign=biUh%2FyGUeeakdQhAgj9d02XYu5dw%2FLAdi4uHP9bsYJQ9H62LdhAFBQGpLXDxUmdBt%2F9ztdwh50UUqdk72OYMPOUuRJGEiIVJalijkUovLzJW35y0SRZPy51%2FRWyxs2lIvQWs9QmFjMWuCxJN02FSPNiRMIkGpWuO1YNzQNnjzMJo72pGwTNgVLiKS4W%2FJAhmovbYckUTWM1y8I4t2nEF2FZQ9rHQImSovll%2B8NpSWpgKJ%2FSfuDpH%2BgHLYDD5AQzwg35o%2FNw4AztPcsBE0r1v010YSvF70MRbXZyiEUPDgF6IYUiKsLp%2FTJF2DuUlpoe4SM%2BaAnOUA0VepC%2BimBjPCw%3D%3D
        &trade_no=2020021622001429291418958991
        &auth_app_id=2018020102122556&version=1.0
        &app_id=2018020102122556
        &sign_type=RSA2
        &seller_id=2088921750292524
        &timestamp=2020-02-16+16%3A34%3A59
         */
        
        //1:解析支付宝回调参数
        String app_id = request.getParameter("app_id");   //该支付应用ID
        String out_trade_no = request.getParameter("out_trade_no"); //返回的商品订单号
        String trade_no = request.getParameter("trade_no");  //与支付宝建立的交易码
        String sign = request.getParameter("sign");     //私钥生成的签名

        //注意: 本身是需要用自身应用的公钥来解析签名获取私钥进行比对 - 本系统不做此操作
        //2：进行支付信息的更改
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setOrderSn(out_trade_no);
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setAlipayTradeNo(trade_no);
        paymentInfo.setCallbackContent(request.toString()); //所有的请求内容都存储在请求
        paymentInfoService.updatePatmentInfo(paymentInfo);

        //3:发送一个消息队列 - 提示order模块完成订单状态的更改
        //等下做 - 把回调跑通

        //4：跳回支付成功页面
        return "finish";
    }
}
