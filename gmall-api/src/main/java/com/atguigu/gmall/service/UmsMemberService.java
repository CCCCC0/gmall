package com.atguigu.gmall.service;

import com.atguigu.gmall.pojo.UmsMember;

import java.util.List;

public interface UmsMemberService {

    List<UmsMember> getAllMember();

    UmsMember selectUmsMemberById(String umsMemberId);

    void deleteUmsMemberById(String umsMemberId);

    UmsMember insertUmsMember(UmsMember umsMember);

    UmsMember getUmsMemberByUserIdAndLoginAccout(String password,String logAccout);

    void sychronizedTokenToRedis(String token,UmsMember umsMember);

    UmsMember vefiryToken(String token);

    UmsMember getUmsMemberByUidAndNickName(String uid,String nickName);

}
