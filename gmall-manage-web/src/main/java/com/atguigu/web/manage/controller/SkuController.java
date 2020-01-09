package com.atguigu.web.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pojo.PmsProductImage;
import com.atguigu.gmall.pojo.PmsProductSaleAttr;
import com.atguigu.gmall.service.SkuService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SkuController {

    @Reference
    private SkuService skuService;

    @RequestMapping("spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        List<PmsProductSaleAttr> allPmsProductSaleAttrList = skuService.getAllPmsProductSaleAttr(spuId);

        return allPmsProductSaleAttrList;
    }

    @RequestMapping("spuImageList")
    public List<PmsProductImage> spuImageList(String spuId){

        List<PmsProductImage> pmsProductImageList = skuService.getAllPmsProductImage(spuId);

        return pmsProductImageList;
    }

}
