package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pojo.*;
import com.atguigu.gmall.service.AttrInfoService;
import com.atguigu.gmall.service.PmsSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Controller
public class SearchController {

    @Reference
    private PmsSearchService pmsSearchService;

    @Reference
    private AttrInfoService attrInfoService;

    @RequestMapping("search.html")
    public String toIndex(){
        return "index";
    }

    @RequestMapping("list.html")
    public String showList(PmsSearchParam pmsSearchParam, ModelMap modelMap){
        //通过list进行查询
        //通过 catalogId  通过关键字或描述进行查询
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = pmsSearchService.selectPmsSearchSkuInfo(pmsSearchParam);

        String[] valueIdList = pmsSearchParam.getValueId();

        //将sku所拥有的平台属性 与 平台属性值存入request域中
        if (pmsSearchSkuInfos != null && pmsSearchSkuInfos.size() > 0){
            HashSet<String> valueIds = new HashSet<>();
            for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
                List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
                if(skuAttrValueList != null && skuAttrValueList.size() > 0){
                    for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                        valueIds.add(pmsSkuAttrValue.getValueId());
                    }
                }
            }
            //查询出所有相关联的平台属性
            List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrInfoService.getBaseAttrListByValueIds(valueIds);

            //创造面包屑集合
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();

            if (valueIdList != null && valueIdList.length > 0) {
                for (String valueId : valueIdList) {
                    PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                    pmsSearchCrumb.setValueId(valueId);
                    pmsSearchCrumb.setUrlParam(getPmsSearchCrumbUrlParam(pmsSearchParam, valueId));

                    //利用迭代器遍历
                    //在删除属性值Id和属性值的内容时 将valueName设置进去
                    Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                    //利用迭代器进行循环
                    while (iterator != null && iterator.hasNext()) {
                        PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                        if (pmsBaseAttrInfo != null) {
                            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                                if (valueId.equals(pmsBaseAttrValue.getId())) {
                                    pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                                    //再进行删除
                                    iterator.remove();
                                }
                            }
                        }
                    }
                    //加入面包屑
                    pmsSearchCrumbs.add(pmsSearchCrumb);
                }
            }
            //将所有的商品属性信息存入request域中
            modelMap.put("attrList",pmsBaseAttrInfos);

            //把elasticsearch中的skuinfo集合存入request域中
            modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

            //将面包屑集合加入到request域中
            modelMap.put("attrValueSelectedList",pmsSearchCrumbs);

            //还需要将keyword  selectedValueIdList  urlParam放入到 当前的集合中
            //modelMap.put("keyword",pmsSearchParam.getKeyword());
            String urlParam = getUrlParam(pmsSearchParam);
            modelMap.put("urlParam",urlParam);
        }

        return "list";
    }

    //这是当前页面的urlParam  显示
    public String getUrlParam(PmsSearchParam pmsSearchParam,String...deleteValueIdS){

    //将urlparam拼接好  放入modelMap并显示
    //采取不一样的策略  要么显示 keyword 要么显示catalog3Id

        String catalog3Id = pmsSearchParam.getCatalog3Id();

        String keyword = pmsSearchParam.getKeyword();

        String[] valueIds = pmsSearchParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(catalog3Id)){
           urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        //前端页面中显示的urlParam
        //面包屑中的urlParam等于页面中显示的 当前的 减去×的id
        if(valueIds != null && valueIds.length > 0){
            for (String valueId : valueIds) {
                urlParam = urlParam + "&valueId=" + valueId;
            }
        }

        return urlParam;
    }

    //创造面包屑Url
    public String getPmsSearchCrumbUrlParam(PmsSearchParam pmsSearchParam,String...deleteValueIds){

        List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();

        String catalog3Id = pmsSearchParam.getCatalog3Id();

        String keyword = pmsSearchParam.getKeyword();

        String[] valueId = pmsSearchParam.getValueId();

        //先制作面包屑的urlParam
        String urlParam = "";

        if (StringUtils.isNotBlank(catalog3Id)){
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (StringUtils.isNotBlank(urlParam)){
            if (StringUtils.isNotBlank(keyword)){
                urlParam = urlParam + "&keyword=" + keyword;
            }
        }

        //完成制造面包屑的urlParam制造
        if(valueId != null && valueId.length > 0){
            for (String id : valueId) {
                if(!(deleteValueIds != null && deleteValueIds.length > 0 && deleteValueIds[0].equals(id))){
                    urlParam = urlParam + "&valueId=" + id;
                }
            }
        }

        return urlParam;
    }

}
