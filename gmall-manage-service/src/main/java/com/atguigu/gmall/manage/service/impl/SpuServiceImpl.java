package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.pojo.*;
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

    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;

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

        //图片地址的插入
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();

        for (PmsProductImage pmsProductImage : spuImageList) {
            pmsProductImage.setProductId(projectId);
            pmsProductImageMapper.insertSelective(pmsProductImage);
        }

        List<PmsProductSaleAttr> infoSpuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();

        for (PmsProductSaleAttr pmsProductSaleAttr : infoSpuSaleAttrList) {
            pmsProductSaleAttr.setProductId(projectId);
            //销售属性的插入
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);

            //进行值的插入
            //需要插入联合主键 productId + saleAttrId
            String saleAttrId = pmsProductSaleAttr.getSaleAttrId();

            //销售属性值的插入
            List<PmsProductSaleAttrValue> saleAttrSpuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : saleAttrSpuSaleAttrValueList) {
                pmsProductSaleAttrValue.setProductId(projectId);
                pmsProductSaleAttrValue.setSaleAttrId(saleAttrId);
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }
        }
    }

    @Override
    public List<PmsProductImage> getAllPmsProductImage(String spuId) {

        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImageList = pmsProductImageMapper.select(pmsProductImage);

        return pmsProductImageList;
    }

    @Override
    public List<PmsProductSaleAttr> getAllPmsProductSaleAttr(String spuId) {

        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrList = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);

        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrList) {
             PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
             pmsProductSaleAttrValue.setProductId(spuId);
             pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
             List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
             productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValueList);
        }

        return pmsProductSaleAttrList;
    }

    @Override
    public List<PmsProductSaleAttr> getIsChekedSpuAttrList(String skuId, String spuId) {

        List<PmsProductSaleAttr> pmsProductSaleAttrList = pmsProductSaleAttrMapper.getIsChekedSpuAttrList(skuId, spuId);

        return pmsProductSaleAttrList;
    }

}
