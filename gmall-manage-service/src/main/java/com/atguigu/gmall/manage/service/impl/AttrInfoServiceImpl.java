package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.AttrInfoMapper;
import com.atguigu.gmall.manage.mapper.AttrInfoValueMapper;
import com.atguigu.gmall.pojo.PmsBaseAttrInfo;
import com.atguigu.gmall.pojo.PmsBaseAttrValue;
import com.atguigu.gmall.service.AttrInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

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


}
