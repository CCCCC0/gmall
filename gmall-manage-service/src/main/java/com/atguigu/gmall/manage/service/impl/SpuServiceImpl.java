package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.atguigu.gmall.manage.mapper.PmsProductInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.atguigu.gmall.manage.mapper.PmsProductSaleAttrValueMapper;
import com.atguigu.gmall.pojo.PmsBaseSaleAttr;
import com.atguigu.gmall.pojo.PmsProductInfo;
import com.atguigu.gmall.pojo.PmsProductSaleAttr;
import com.atguigu.gmall.pojo.PmsProductSaleAttrValue;
import com.atguigu.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;

    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Override
    public List<PmsProductInfo> getAllPmsProductInfo(String catalog3Id) {

        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfoList = pmsProductInfoMapper.select(pmsProductInfo);

        return pmsProductInfoList;
    }

    @Override
    public List<PmsBaseSaleAttr> getAllPmsProductSaleAttr() {

        List<PmsBaseSaleAttr> pmsProductSaleAttrList = pmsBaseSaleAttrMapper.selectAll();

        return pmsProductSaleAttrList;
    }

    @Override
    public void savePmsProductInfo(PmsProductInfo pmsProductInfo) {

        //先进行PmsProductInfo的保存
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
        //获取主键
        String projectId = pmsProductInfo.getId();

         List<PmsProductSaleAttr> infoSpuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();

        for (PmsProductSaleAttr pmsProductSaleAttr : infoSpuSaleAttrList) {
            pmsProductSaleAttr.setProductId(projectId);
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);

            //进行值的插入
            //需要插入联合主键 productId + saleAttrId
            String saleAttrId = pmsProductSaleAttr.getId();

            List<PmsProductSaleAttrValue> saleAttrSpuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : saleAttrSpuSaleAttrValueList) {
                pmsProductSaleAttrValue.setProductId(projectId);
                pmsProductSaleAttrValue.setSaleAttrId(saleAttrId);
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }
        }
    }

}
