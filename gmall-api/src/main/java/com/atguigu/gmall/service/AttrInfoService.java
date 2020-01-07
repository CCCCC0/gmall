package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PmsBaseAttrInfo;

import java.util.List;

public interface AttrInfoService {

    List<PmsBaseAttrInfo> getAllPmsBaseAttrInfo(String catalog3Id);

    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

}
