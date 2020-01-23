package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PmsBaseAttrInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface AttrInfoService {

    List<PmsBaseAttrInfo> getAllPmsBaseAttrInfo(String catalog3Id);

    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrInfo> getBaseAttrListByValueIds(HashSet valueIds);
}
