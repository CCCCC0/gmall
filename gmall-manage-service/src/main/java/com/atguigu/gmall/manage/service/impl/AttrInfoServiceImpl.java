package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.AttrInfoMapper;
import com.atguigu.gmall.manage.mapper.AttrInfoValueMapper;
import com.atguigu.gmall.pojo.PmsBaseAttrInfo;
import com.atguigu.gmall.pojo.PmsBaseAttrValue;
import com.atguigu.gmall.pojo.PmsProductSaleAttr;
import com.atguigu.gmall.service.AttrInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AttrInfoServiceImpl implements AttrInfoService {

    @Autowired
    private AttrInfoMapper attrInfoMapper;

    @Autowired
    private AttrInfoValueMapper attrInfoValueMapper;

    @Override
    public List<PmsBaseAttrInfo> getAllPmsBaseAttrInfo(String catalog3Id) {

        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrInfoMapper.select(pmsBaseAttrInfo);

        //需要把attrInfoValue的值 注入
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfoList) {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
             List<PmsBaseAttrValue> pmsBaseAttrValueList = attrInfoValueMapper.select(pmsBaseAttrValue);
             baseAttrInfo.setAttrValueList(pmsBaseAttrValueList);
        }

        return pmsBaseAttrInfoList;
    }

    @Override
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        //此方法将修改操作与增加操作
        String id = pmsBaseAttrInfo.getId();

        if(StringUtils.isBlank(id)){
            //进行插入
            attrInfoMapper.insertSelective(pmsBaseAttrInfo);
            id = pmsBaseAttrInfo.getId();
        }else{
            //属性进行修改
            Example example = new Example(PmsBaseAttrInfo.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("id",id);
            attrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);

            //删除属性中的值
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrInfo.setId(id);
            attrInfoValueMapper.delete(pmsBaseAttrValue);
        }

        //进行属性值的注入
        for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrInfo.getAttrValueList()) {
             pmsBaseAttrValue.setAttrId(id);
             attrInfoValueMapper.insertSelective(pmsBaseAttrValue);
        }
    }

    @Override
    public List<PmsBaseAttrInfo> getBaseAttrListByValueIds(HashSet valueIds) {

        //将集合中的内容用逗号拼接在一起
        String valueids = StringUtils.join(valueIds, ",");

        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrInfoMapper.selectAttrInfoListByvalueIds(valueids);

        return pmsBaseAttrInfos;
    }


}
