package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.pojo.PmsSkuAttrValue;
import com.atguigu.gmall.pojo.PmsSkuImage;
import com.atguigu.gmall.pojo.PmsSkuInfo;
import com.atguigu.gmall.pojo.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.atguigu.gmall.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void savePmsSkuInfo(PmsSkuInfo pmsSkuInfo) {

        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);

        //获取自增主键Id
        String skuId = pmsSkuInfo.getId();

        //进行图片集合的插入
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        //进行Sku属性的插入
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
             pmsSkuAttrValue.setSkuId(skuId);
             pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        //进行Sku销售属性的插入
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
    }

    @Override
    public PmsSkuInfo getSkuInfoById(String skuId) {

        Jedis jedis = redisUtil.getJedis();

        String key = "PmsSkuInfo" + ":" + skuId + ":" + "info";

        String json = jedis.get(key);

        try {
            if (StringUtils.isNotBlank(json)) {
                PmsSkuInfo pmsSkuInfo = JSON.parseObject(json, PmsSkuInfo.class);
                System.out.println(pmsSkuInfo);
                return pmsSkuInfo;
            } else {
                PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
                pmsSkuInfo.setId(skuId);
                PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

                PmsSkuImage pmsSkuImage = new PmsSkuImage();
                pmsSkuImage.setSkuId(skuId);
                List<PmsSkuImage> pmsSkuImageList = pmsSkuImageMapper.select(pmsSkuImage);
                pmsSkuInfo1.setSkuImageList(pmsSkuImageList);

                //同步数据到redis
                String skuJson = JSON.toJSONString(pmsSkuInfo1);
                jedis.set(key, skuJson);

                return pmsSkuInfo1;
            }
        }catch(Exception e){
            //导引错误信息到系统日志
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return null;
    }

    @Override
    public List<PmsSkuInfo> getPmsSkuInfoListBySpuId(String spuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setProductId(spuId);
        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.select(pmsSkuInfo);

        for (PmsSkuInfo skuInfo : pmsSkuInfoList) {
            PmsSkuSaleAttrValue saleAttrValue = new PmsSkuSaleAttrValue();
            saleAttrValue.setSkuId(skuInfo.getId());
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuSaleAttrValueMapper.select(saleAttrValue);
            skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        }

        return pmsSkuInfoList;
    }

}
