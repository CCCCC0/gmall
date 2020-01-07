package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.pojo.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UmsMemberMapper extends Mapper<UmsMember> {

    List<UmsMember> selectAllUmsMembers();

    UmsMember selectUmsMemberById(String umsMemberId);

    void deleteUmsMemberById(String umsMemberId);

    void insertUmsMember(UmsMember umsMember);

}
