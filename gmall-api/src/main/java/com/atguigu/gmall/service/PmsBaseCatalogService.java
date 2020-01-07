package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.PmsBaseCatalog1;
import com.atguigu.gmall.pojo.PmsBaseCatalog2;
import com.atguigu.gmall.pojo.PmsBaseCatalog3;

import java.util.List;

public interface PmsBaseCatalogService {

    List<PmsBaseCatalog1> getAllPmsBaseCatalog1();

    List<PmsBaseCatalog2> getAllPmsBaseCatalog2(String catalog1Id);

    List<PmsBaseCatalog3> getAllPmsBaseCatalog3(String catalog2Id);
}
