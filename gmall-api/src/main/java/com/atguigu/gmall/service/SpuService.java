package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PmsProductInfo;

import java.util.List;

public interface SpuService {

    List<PmsProductInfo> getAllPmsProductInfo(String catalog3Id);

}
