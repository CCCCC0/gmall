package com.atguigu.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginIsRequiredIntercept;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    //进行拦截的方法 true表示放行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取请求中方法中的 注解 和 注解值
        HandlerMethod annotation = (HandlerMethod) handler;
        LoginIsRequiredIntercept methodAnnotation = annotation.getMethodAnnotation(LoginIsRequiredIntercept.class);
        boolean flag = methodAnnotation.isRequired();

        //无注解 直接放行
        if (annotation == null) {
            return true;
        }

        //有注解 - 获取token令牌进行校验
        String token = "";
        String newToken = request.getParameter("newToken");
        String oldToken = CookieUtil.getCookieValue(request, response, "oldToken", true);

        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        //对token的值进行判断
        if (StringUtils.isNotBlank(token)) {
            //两种校验方法  - 1 ：cas中心进行校验    - 2 : 去中心化 使用jwt工具进行校验

            //token存在  去cas进行校验
            String url = "Http://passport.gmall.com:8087/verify?token=" + token;
            String result = HttpclientUtil.doGet(url);
            if (StringUtils.isNotBlank(result)) {
                //校验初始成功
                Map map = JSON.parseObject(result, Map.class);
                String userId = (String) map.get("userId");
                String nickName = (String) map.get("nickName");
                if (StringUtils.isNotBlank(userId)) {
                    //校验成功 - 重新设置cookie
                    CookieUtil.setCookie(request, response, "oldToken", token, 1000 * 60 * 60 * 24, true);
                    request.setAttribute("userId", userId);
                    request.setAttribute("nickName", nickName);
                    return true;
                }
            }
        }

        //校验不成功 判断是否为不需要登入也能正常访问的请求
        if (flag) {
            //需要登入服务 - 进行登入
            String remoteUser = request.getRemoteAddr();
            //获取原请求地址
            StringBuffer requestURL = request.getRequestURL();
            response.sendRedirect("http://passport.gmall.com:8087/index?originUrl=" + requestURL + "&ip=" + remoteUser);

            return false;
        }

        return true;
    }
}
