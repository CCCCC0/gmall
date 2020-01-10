package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PmsBaseSaleAttr;
import com.atguigu.gmall.pojo.PmsProductImage;
import com.atguigu.gmall.pojo.PmsProductInfo;
import com.atguigu.gmall.pojo.PmsProductSaleAttr;


import java.util.List;

public interface SpuService {

    List<PmsProductInfo> getAllPmsProductInfo(String catalog3Id);

    List<PmsBaseSaleAttr> getAllPmsProductSaleAttr();

    void savePmsProductInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductImage> getAllPmsProductImage(String spuId);

    List<PmsProductSaleAttr> getAllPmsProductSaleAttr(String spuId);
}
