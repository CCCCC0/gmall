package com.atguigu.gmall.util;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
/**
 * @param
 * @return
 */
@Component
public class CookieUtil {
    /***
     * 获得cookie中的值，默认为主ip：www.gmall.com
     * @param request
     * @param response
     * @param cookieName
     * @param isDecoder
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName, boolean isDecoder) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookieName == null){
            return null;
        }
        String retValue = null;
        try {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(cookieName)) {
                    if (isDecoder) {//如果涉及中文
                        retValue = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                    } else {
                        retValue = cookies[i].getValue();
                    }
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }
    /***
     * 设置cookie的值
     * @param request        request.getCookies 接收浏览器传入的cookie
     * @param response       response.addCookie 添加cookie
     * @param cookieName
     * @param cookieValue
     * @param cookieMaxage   如果设置cookie的过期时间 则关闭浏览器之后 cookie会消失
     * @param isEncode       cookie中的内容编码格式 设置为utf-8
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxage, boolean isEncode) {
        try {
            if (cookieValue == null) {
                cookieValue = "";
            } else if (isEncode) {
                cookieValue = URLEncoder.encode(cookieValue, "utf-8");
            }
            Cookie cookie = new Cookie(cookieName, cookieValue);
            if (cookieMaxage >= 0)
                cookie.setMaxAge(cookieMaxage);
            if (null != request)// 设置域名的cookie
                cookie.setDomain(getDomainName(request));
            // 在域名的根路径下保存
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /***
     * 获得cookie的主域名，本系统为gmall.com，保存时使用
     * @param request   设置Cookie的domain -- 相当于域   设置在gmall.com
     *                              则每个微服务都能够获取系统中其他微服务的cookie
     * @return
     */
    private static final String getDomainName(HttpServletRequest request) {
        String domainName = null;
        String serverName = request.getRequestURL().toString();
        if (serverName == null || serverName.equals("")) {
            domainName = "";
        } else {
            serverName = serverName.toLowerCase();
            serverName = serverName.substring(7);
            final int end = serverName.indexOf("/");
            serverName = serverName.substring(0, end);
            final String[] domains = serverName.split("\\.");
            int len = domains.length;
            if (len > 3) {
                // www.xxx.com.cn
                domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
            } else if (len <= 3 && len > 1) {
                // xxx.com or xxx.cn
                domainName = domains[len - 2] + "." + domains[len - 1];
            } else {
                domainName = serverName;
            }
        }
        if (domainName != null && domainName.indexOf(":") > 0) {
            String[] ary = domainName.split("\\:");
            domainName = ary[0];
        }
        System.out.println("domainName = " + domainName);
        return domainName;
    }
    /***
     * 将cookie中的内容按照key删除
     * @param request
     * @param response
     * @param cookieName
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        setCookie(request, response, cookieName, null, 0, false);
    }
}