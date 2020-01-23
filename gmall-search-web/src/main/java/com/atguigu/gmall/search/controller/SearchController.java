package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pojo.PmsSearchParam;
import com.atguigu.gmall.pojo.PmsSearchSkuInfo;
import com.atguigu.gmall.service.PmsSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Controller
public class SearchController {

    @Reference
    private PmsSearchService pmsSearchService;

    @RequestMapping("search.html")
    public String toIndex(){
        return "index";
    }


    @RequestMapping("list.html")
    public String showList(PmsSearchParam pmsSearchParam, ModelMap modelMap){
        //通过list进行查询
        //通过 catalogId  通过关键字或描述进行查询
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = pmsSearchService.selectPmsSearchSkuInfo(pmsSearchParam);

        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

        return "list";
    }

}
