package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pojo.UmsMember;
import com.atguigu.gmall.service.CasService;
import com.atguigu.gmall.service.UmsMemberService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import com.atguigu.gmall.util.JwtUtil;
import com.atguigu.gmall.util.WebConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CasController {

    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private CasService casService;

    @Reference
    private UmsMemberService umsMemberService;

    @RequestMapping("index")
    public String index(String originUrl,String ip, ModelMap modelMap){

        if(StringUtils.isNotBlank(originUrl)) {
            modelMap.put("url", originUrl);
        }

        if(StringUtils.isNotBlank(ip)){
            modelMap.put("ip",ip);
        }

        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, HttpServletResponse response, String ip, String password, String loginName){

        //进行账号密码的验证
        UmsMember member = umsMemberService.getUmsMemberByUserIdAndLoginAccout(password, loginName);

        if(member == null){
            return "fail";
        }

        String userId = member.getId();
        String nickname = member.getNickname();

        //Token的生成   -- 公共部分 + 私人部分 + 服务器部分
        String key = "com-atguigu-gmall";
        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);
        map.put("nickName",nickname);
        String token = JwtUtil.encode(key, map, ip);

        //需要将token存入redis中
        umsMemberService.sychronizedTokenToRedis(token,member);

        //发送消息队列到购物车系统 同步缓存中的内容到购物车
        sendMessageToCartSystem(request,response,userId);

        return token;
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token){

        //对token进行校验
        UmsMember umsMember = umsMemberService.vefiryToken(token);

        if(umsMember != null){
            //校验成功 将userId进行返回
            String id = umsMember.getId();
            String nickName = umsMember.getNickname();
            Map<String,String> map = new HashMap<>();
            map.put("userId",id);
            map.put("nickName",nickName);
            return JSON.toJSONString(map);
        }

        return "";
    }

    @RequestMapping("weibo")
    public String weibo(String code,HttpServletRequest request,HttpServletResponse response){

        //在此中获取code内容 - 通过此code调用开发者平台的内容 获取 accesstoken
        Map<String,String> map = new HashMap<>();
        map.put("client_id","2466601619");
        map.put("client_secret","957174c211ea434e3f1d0d97460a1073");
        map.put("redirect_uri","http://passport.gmall.com:8087/weibo");
        map.put("code",code);
        map.put("grant_type","authorization_code");

        //调用post请求获取access_token 和 用户的id等信息 - 如果登入成功
        String getAccess_TokenUrl = "https://api.weibo.com/oauth2/access_token";
        String jsonMapString = HttpclientUtil.doPost(getAccess_TokenUrl, map);

        if(StringUtils.isBlank(jsonMapString)){
           //如果获取失败 - 继续返回到登入页面
           return "index";
        }

        //不为空 - 则获取成功 - 接下来 获取jsonMapString中的内容
        Map jsonMap = JSON.parseObject(jsonMapString, Map.class);
        String access_token = (String)jsonMap.get("access_token");
        String uid = (String)jsonMap.get("uid");

        //调用get请求获取存储在第三方中的用户内容
        String getUserInfoUrl = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid;
        String userInfoJsonMap = HttpclientUtil.doGet(getUserInfoUrl);

        if(StringUtils.isNotBlank(userInfoJsonMap)){

            Map userInfoMap = JSON.parseObject(userInfoJsonMap, Map.class);
            String screen_name = (String)userInfoMap.get("screen_name");
            String city = (String) userInfoMap.get("city");

            //需要创建新账户 - 将当前账户保存到DB
            UmsMember umsMember = new UmsMember();
            umsMember.setCity(city);
            umsMember.setAccessCode(code);
            umsMember.setAccessToken(access_token);
            umsMember.setBirthday(new Date());
            umsMember.setNickname(screen_name);
            umsMember.setSourceUid(uid);
            umsMember.setSourceType("2");

            //调用userService进行用户的插入 -
            UmsMember member = umsMemberService.insertUmsMember(umsMember);
            //需要从中获取用户id 进行token的设置
            if(member != null){

                //生成token 并且 同步到redis中
                String userId = member.getId();
                String nickname = member.getNickname();
                Map userMap = new HashMap();
                userMap.put("userId",userId);
                userMap.put("nickName",nickname);

                String key = "com-atguigu-gmall";
                String ip = "";
                //获取IP 从请求头中  -  获取ip
                String header = request.getHeader("X-forwarded-for");
                if(StringUtils.isNotBlank(header)){
                    ip = request.getRemoteAddr();
                    if(StringUtils.isBlank(ip)){
                        //如果ip为空 则 为本机地址
                        ip = "127.0.0.1";
                    }
                }
                String token = JwtUtil.encode(key, userMap, ip);

                //生成token 同步到redis中
                umsMemberService.sychronizedTokenToRedis(token,member);

                //发送消息队列到购物车系统 同步缓存中的内容到购物车
                sendMessageToCartSystem(request,response,userId);

                //成功返回到商品首页
                return "redirect:http://search.gmall.com:8084/search.html?newToken="+token;
            }
        }

        //不成功跳到登入页面
        return "index";
    }

    private void sendMessageToCartSystem(HttpServletRequest request,HttpServletResponse response,String userId){
        //发送消息队列到购物车系统 同步缓存中的内容到购物车
        String cartKey = WebConstant.COOKIE_CART_NAME;
        String cookieValue = cookieUtil.getCookieValue(request, response, cartKey, true);
        //将cookie中的购物车内容进行清空
        cookieUtil.setCookie(request,response, cartKey,"",1000*60*60*3,true);
        casService.sendLoginSuccessMessageToCartSystem(cookieValue,userId);
    }

}
