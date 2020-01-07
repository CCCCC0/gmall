package com.atguigu.web.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pojo.PmsProductInfo;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    private SpuService spuService;

    @ResponseBody
    @RequestMapping("spuList")
    public List<PmsProductInfo> spuList(String catalog3Id){

        List<PmsProductInfo> pmsProductInfoList = spuService.getAllPmsProductInfo(catalog3Id);

        return pmsProductInfoList;
    }

}
