package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pojo.UmsMember;
import com.atguigu.gmall.service.UmsMemberService;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UmsMemberServiceImpl implements UmsMemberService {

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Autowired
    private RedisUtil redisUtil;

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
    public UmsMember insertUmsMember(UmsMember umsMember) {

        String sourceUid = umsMember.getSourceUid();
        String nickname = umsMember.getNickname();

        UmsMember u = getUmsMemberByUidAndNickName(sourceUid, nickname);
        if(u == null){
            //等于null 进行插入 否则 不进行插入
            //需要生成的主键Id
            umsMemberMapper.insertSelective(umsMember);
            u = getUmsMemberByUidAndNickName(sourceUid, nickname);
        }

        //没有 则返回null
        return u;
    }

    @Override
    public UmsMember getUmsMemberByUserIdAndLoginAccout(String password, String logAccout) {

        UmsMember umsMember = new UmsMember();
        umsMember.setPassword(password);
        umsMember.setUsername(logAccout);
        UmsMember u = umsMemberMapper.selectOne(umsMember);

        return u;
    }

    @Override
    public void sychronizedTokenToRedis(String token,UmsMember umsMember) {

        Jedis jedis = redisUtil.getJedis();
        String key = "user:" + token + ":token";
        String value = JSON.toJSONString(umsMember);
        String setex = jedis.setex(key,  60 * 60 * 24, value);
        jedis.close();
    }

    @Override
    public UmsMember vefiryToken(String token) {

        UmsMember umsMember = null;

        String key = "user:" + token + ":token";
        //从redis中获取value
        Jedis jedis = redisUtil.getJedis();
        String value = jedis.get(key);

        if(StringUtils.isNotBlank(value)){
            umsMember = JSON.parseObject(value,UmsMember.class);
            jedis.expire(key,60*60*24);// 刷新token的过期时间
        }

        return umsMember;
    }

    @Override
    public UmsMember getUmsMemberByUidAndNickName(String uid, String nickName) {

        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid(uid);
        umsMember.setNickname(nickName);

        UmsMember u = umsMemberMapper.selectOne(umsMember);

        return u;
    }


}
