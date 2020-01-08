package com.atguigu.web.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pojo.PmsBaseAttrInfo;
import com.atguigu.gmall.service.AttrInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class AttrInfoController {

    @Reference
    private AttrInfoService attrInfoService;

    @ResponseBody
    @RequestMapping("attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){

        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrInfoService.getAllPmsBaseAttrInfo(catalog3Id);

        return pmsBaseAttrInfoList;
    }

    @ResponseBody
    @RequestMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){

        attrInfoService.saveAttrInfo(pmsBaseAttrInfo);

        return "success";
    }


}
