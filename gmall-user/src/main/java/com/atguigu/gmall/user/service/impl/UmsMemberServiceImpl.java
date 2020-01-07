package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.pojo.UmsMember;
import com.atguigu.gmall.service.UmsMemberService;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UmsMemberServiceImpl implements UmsMemberService {

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Override
    public List<UmsMember> getAllMember() {

       List<UmsMember> memberList = umsMemberMapper.selectAllUmsMembers();

        return memberList;
    }

    @Override
    public UmsMember selectUmsMemberById(String umsMemberId) {

        UmsMember umsMember = umsMemberMapper.selectUmsMemberById(umsMemberId);

        return umsMember;
    }

    @Override
    public void deleteUmsMemberById(String umsMemberId) {

        umsMemberMapper.deleteUmsMemberById(umsMemberId);

    }

    @Override
    public void insertUmsMember(UmsMember umsMember) {
        umsMemberMapper.insertUmsMember(umsMember);
    }
}
