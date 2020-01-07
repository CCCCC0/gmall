package com.atguigu.web.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atguigu.gmall.pojo.PmsBaseCatalog1;
import com.atguigu.gmall.pojo.PmsBaseCatalog2;
import com.atguigu.gmall.pojo.PmsBaseCatalog3;
import com.atguigu.gmall.service.PmsBaseCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin   //服务端允许跨域请求的注解
public class PmsBaseCatalogController {

    @Reference
    private PmsBaseCatalogService pmsBaseCatalogService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){

        List<PmsBaseCatalog1> allPmsBaseCatalog1List = pmsBaseCatalogService.getAllPmsBaseCatalog1();

        return allPmsBaseCatalog1List;
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id){

        List<PmsBaseCatalog2> allPmsBaseCatalog2List = pmsBaseCatalogService.getAllPmsBaseCatalog2(catalog1Id);

        return allPmsBaseCatalog2List;
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id){

        List<PmsBaseCatalog3> allPmsBaseCatalog3List = pmsBaseCatalogService.getAllPmsBaseCatalog3(catalog2Id);

        return allPmsBaseCatalog3List;
    }

}
