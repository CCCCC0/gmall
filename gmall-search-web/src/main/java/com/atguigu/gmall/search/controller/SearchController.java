package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pojo.PmsBaseAttrInfo;
import com.atguigu.gmall.pojo.PmsSearchParam;
import com.atguigu.gmall.pojo.PmsSearchSkuInfo;
import com.atguigu.gmall.pojo.PmsSkuAttrValue;
import com.atguigu.gmall.service.AttrInfoService;
import com.atguigu.gmall.service.PmsSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

        //把elasticsearch中的skuinfo集合存入request域中
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

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
            //存入request域中
            modelMap.put("attrList",pmsBaseAttrInfos);
        }

        return "list";
    }

}
