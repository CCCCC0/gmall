package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PmsSearchParam;
import com.atguigu.gmall.pojo.PmsSearchSkuInfo;

import java.util.List;

public interface PmsSearchService {

    List<PmsSearchSkuInfo> selectPmsSearchSkuInfo(PmsSearchParam pmsSearchParam);

}
