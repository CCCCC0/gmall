package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PmsSkuInfo;

import java.util.List;

public interface SkuService {

    void savePmsSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuInfoById(String skuId);

    List<PmsSkuInfo> getPmsSkuInfoListBySpuId(String spuId);

    //对于分布式锁的测试
    public PmsSkuInfo getSkuInfoByIdCopy1(String skuId);

    List<PmsSkuInfo> getAllPmsSkuInfo();
}
