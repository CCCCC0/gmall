package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pojo.PmsSearchParam;
import com.atguigu.gmall.pojo.PmsSearchSkuInfo;
import com.atguigu.gmall.service.PmsSearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class PmsSearchServiceImpl implements PmsSearchService {

    @Autowired
    private JestClient jestClient;


    public String createSearchConditions(PmsSearchParam pmsSearchParam) {

        //进行查询的条件获取
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] valueId = pmsSearchParam.getValueId();

        //Search
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //keyword - 搜索查询
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        //catalog3Id and valueId 进行过滤操作
        //catalog3Id
        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //valueId
        if(valueId != null && valueId.length > 0)
        for (String id : valueId) {
            if(StringUtils.isNotBlank(id)){
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", id);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //将查询条件转换为字符串并返回
        SearchSourceBuilder query = searchSourceBuilder.query(boolQueryBuilder);

        //一次查询20条  从0开始 到20条时结束
        query.from(0);
        query.size(20);
        String queryStr = query.toString();

        return queryStr;
    }

    public List<PmsSearchSkuInfo> selectPmsSearchSkuInfo(PmsSearchParam pmsSearchParam) {
        //进行PmsSearchSkuinfo的查询   然后返回数据
        // 通过到elasticsearch中进行查询

        //进行执行语句的操作
        String searchCondition = createSearchConditions(pmsSearchParam);
        Search search = new Search.Builder(searchCondition).addIndex("gmall").addType("pmsSearchSkuInfo").build();

        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();

        try {
            SearchResult searchResult = jestClient.execute(search);
            List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);

            if (hits != null && hits.size() > 0){
                for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                    pmsSearchSkuInfoList.add(hit.source);
                }
            }

            return pmsSearchSkuInfoList;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
