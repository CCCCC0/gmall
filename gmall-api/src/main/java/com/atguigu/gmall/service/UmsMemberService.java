package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.UmsMember;

import java.util.List;

public interface UmsMemberService {

    List<UmsMember> getAllMember();

    UmsMember selectUmsMemberById(String umsMemberId);

    void deleteUmsMemberById(String umsMemberId);

    void insertUmsMember(UmsMember umsMember);


}
