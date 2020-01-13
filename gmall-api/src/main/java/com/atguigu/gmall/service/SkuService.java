package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PmsSkuInfo;

import java.util.List;

public interface SkuService {

    void savePmsSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuInfoById(String skuId);

    List<PmsSkuInfo> getPmsSkuInfoListBySpuId(String spuId);

}
