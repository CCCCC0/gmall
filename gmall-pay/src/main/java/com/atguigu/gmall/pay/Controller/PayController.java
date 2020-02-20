package com.atguigu.gmall.pay.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginIsRequiredIntercept;
import com.atguigu.gmall.pay.config.AlipayConfig;
import com.atguigu.gmall.pay.util.HttpClient;
import com.atguigu.gmall.pojo.OmsOrder;
import com.atguigu.gmall.pojo.PaymentInfo;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentInfoService;
import com.github.wxpay.sdk.WXPayUtil;
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

    @LoginIsRequiredIntercept(isRequired = true)
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

        //请求支付宝表单后 - 需要向该支付系统发送一个延迟队列
        //支付系统根据此消息队列 向支付宝系统发送请求 询问交易情况
        paymentInfoService.sendMessageToPaySystemCheckPayResult(paymentInfo,1l);

        //返回form表单
        return form;
    }

    //微信支付
    @LoginIsRequiredIntercept(isRequired = true)
    @RequestMapping("wx/submit")
    @ResponseBody
    public String wxPay(HttpServletRequest request,String order_sn){

        //1:封装参数-封装成xml格式 2:请求微信端-获取支付url 3：利用url生成二维码进行支付
        if(order_sn.length()>32){
            //微信端要求长度为32位 - 本系统时间戳超过32位 需截取
            order_sn = order_sn.substring(0,31);
        }
        //微信端支付 以分为单位
        Map aNativeMap = getWxPayData(order_sn, "1");
        String code_url = (String)aNativeMap.get("code_url");

        //返回支付的url到前端 生成二维码
        return code_url;
    }

    //访问微信端获取 参数Map
    private Map getWxPayData(String  out_order_sn,String totalMoney){

        Map<String,String> map = new HashMap<>();
        map.put("appid","wxf913bfa3a2c7eeeb");
        map.put("mch_id","1543338551");
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        map.put("body","psyDuckGmall");
        map.put("out_trade_no",out_order_sn);
        map.put("spbill_create_ip","127.0.0.1");
        map.put("total_fee",totalMoney);
        map.put("notify_url", " http://2z72m78296.wicp.vip/wx/callback/notify");//回调地址(随便写)
        map.put("trade_type", "NATIVE");//交易类型

        try {
            String mapXml = WXPayUtil.generateSignedXml(map, "atguigu3b0kn9g5v426MKfHQH7X8rKwb");
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setXmlParam(mapXml);
            client.setHttps(true);
            client.post();

            //获取结果
            String content = client.getContent();
            Map<String, String> stringStringMap = WXPayUtil.xmlToMap(content);  //本来需要输出参数 - 看看里面具体有什么
            Map<String, String> m=new HashMap<>();
            map.put("code_url", stringStringMap.get("code_url"));//支付地址
            map.put("total_fee", totalMoney);//总金额
            map.put("out_trade_no",out_order_sn);//订单号

            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    //支付后的同步回调方法
    @LoginIsRequiredIntercept(isRequired = true)
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
        //判断幂等性
        //检查系统是否请求过支付宝已更改支付信息
        boolean flag = paymentInfoService.searPaymentInfoIsUpdate(out_trade_no);
        if(!flag) {
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(request.toString()); //所有的请求内容都存储在请求
            paymentInfoService.updatePatmentInfo(paymentInfo);

            //并且发送消息到订单系统进行订单内容的修改
            paymentInfoService.sendPaySuccessMessage(paymentInfo);
        }
        //3：跳回支付成功页面
        return "finish";
    }
}
