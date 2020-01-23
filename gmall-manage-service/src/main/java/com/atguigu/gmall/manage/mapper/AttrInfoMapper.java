package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.pojo.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface AttrInfoMapper extends Mapper<PmsBaseAttrInfo> {

    List<PmsBaseAttrInfo> selectAttrInfoListByvalueIds(@Param("valueIds") String valueIds);

}
