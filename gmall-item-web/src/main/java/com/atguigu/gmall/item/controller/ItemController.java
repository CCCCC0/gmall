package com.atguigu.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pojo.PmsProductSaleAttr;
import com.atguigu.gmall.pojo.PmsSkuInfo;
import com.atguigu.gmall.pojo.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class ItemController {

    @Reference
    private SkuService skuService;

    @Reference
    private SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap){

        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfoById(skuId);
        modelMap.put("skuInfo",pmsSkuInfo);

        String spuId = pmsSkuInfo.getProductId();

        List<PmsProductSaleAttr> pmsProductSaleAttrList = spuService.getIsChekedSpuAttrList(skuId, spuId);
        modelMap.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrList);

        //方法2：通过spuId查询出 当前spu下的所有sku
        //在将sku下的 销售属性值 与 sku构建id
        //通过前端点击  通过销售属性值 key  队形 skuId
        //在将skuId发送到后端查询出 sku  再返回
        List<PmsSkuInfo> pmsSkuInfoList = skuService.getPmsSkuInfoListBySpuId(spuId);
        Map<String,String> skuInfoMap = new HashMap<>();

        for (PmsSkuInfo skuInfo : pmsSkuInfoList) {
            List<PmsSkuSaleAttrValue> PmsSkuSaleAttrValue = skuInfo.getSkuSaleAttrValueList();
            String key = "";
            for (PmsSkuSaleAttrValue skuSaleAttrValue : PmsSkuSaleAttrValue) {
                key = key + skuSaleAttrValue.getSaleAttrValueId() + "|";
            }
            String value = skuInfo.getId();
            skuInfoMap.put(key,value);
        }

        String json = JSON.toJSONString(skuInfoMap);    //转成json字符串保存到前端页面 方便进行转换
                                                        //使用ali的fastjson 与 dubbo底层的 hession配合正好
        modelMap.put("skuInfoMap",json);

        return "item";
    }

    //生成文件
//    @RequestMapping("ccc.html")
//    @ResponseBody
//    public String item2(String spuId){
//        //第三种解决方案
//        //将spu下对应的 sku全部生成 创建到一个Map中 作为静态资源放在工程下
//        //当前端每次点击时  通过前端页面中的销售属性组合Id找到skuId  得到结果
//
//        List<PmsSkuInfo> pmsSkuInfos = skuService.getPmsSkuInfoListBySpuId(spuId);
//
//        Map<String,String> skuIdMap = new HashMap();
//
//        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
//            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
//            String k = "";
//            for (PmsSkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
//                k = k + skuSaleAttrValue.getSaleAttrValueId() + "|";
//            }
//            String v = pmsSkuInfo.getId();
//            skuIdMap.put(k,v);
//        }
//
//        String skuIdMapJson = JSON.toJSONString(skuIdMap);
//
//        //将skuIdMapJson写入一个json文件中
//
//        File file = new File("D:/电商文件/static_json/"+ "spuId_" + spuId+".js");
//
//        try {
//            OutputStream out = new FileOutputStream(file);
//            out.write(skuIdMapJson.getBytes());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return skuIdMapJson;
//    }

    //第三种方案访问的路径
    @RequestMapping("{skuId}.htm")
    public String item3(@PathVariable String skuId, ModelMap modelMap) {

        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfoById(skuId);
        modelMap.put("skuInfo", pmsSkuInfo);

        String spuId = pmsSkuInfo.getProductId();

        List<PmsProductSaleAttr> pmsProductSaleAttrList = spuService.getIsChekedSpuAttrList(skuId, spuId);
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrList);

        //将spuId存入
        modelMap.put("spuId",spuId);

        return "item";
    }

    /*
    解决方案1：
    通过页面点击销售属性值，异步传递到后台，根据销售属性值查询对应的skuId
    将对应的skuId返回给前台，前台判断是否需要跳转(skuId有值时跳转，skuId无值时不跳转)
     */

    @ResponseBody
    @RequestMapping("zzz.html")
    public PmsSkuInfo zzz(String skuId){

        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfoByIdCopy1(skuId);

        return pmsSkuInfo;
    }

}
