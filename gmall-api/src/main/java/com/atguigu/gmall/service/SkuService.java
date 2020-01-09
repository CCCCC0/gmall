package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PmsProductImage;
import com.atguigu.gmall.pojo.PmsProductSaleAttr;

import java.util.List;

public interface SkuService {

    List<PmsProductImage> getAllPmsProductImage(String spuId);

    List<PmsProductSaleAttr> getAllPmsProductSaleAttr(String spuId);
}
