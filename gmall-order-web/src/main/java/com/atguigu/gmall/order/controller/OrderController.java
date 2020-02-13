package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.annotations.LoginIsRequiredIntercept;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OrderController {

    @LoginIsRequiredIntercept
    @RequestMapping("toTrade")
    public String toTrade(){

        //结算页面 - 需要显示购物车中被选中的商品

        //还有一部分用户需要输入的数据 - 如用户的收获地址




        return "trade";
    }


}
