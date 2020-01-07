package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pojo.UmsMember;
import com.atguigu.gmall.service.UmsMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UmsMemberController {

    @Reference
    private UmsMemberService umsMemberService;

    @ResponseBody
    @RequestMapping("/get/all/umsMember")
    public List<UmsMember> getAllUmsMember(){

        System.out.println(umsMemberService);

        List<UmsMember> memberList = umsMemberService.getAllMember();

        return memberList;

    }

}