package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.pojo.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {

    List<PmsProductSaleAttr> getIsChekedSpuAttrList(@Param("skuId") String skuId,@Param("spuId") String spuId);

}
