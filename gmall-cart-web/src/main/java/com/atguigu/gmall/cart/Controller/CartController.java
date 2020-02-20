package com.atguigu.gmall.cart.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginIsRequiredIntercept;
import com.atguigu.gmall.pojo.OmsCartItem;
import com.atguigu.gmall.pojo.PmsSkuInfo;
import com.atguigu.gmall.service.OmsCartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.WebConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

   @Reference
   private SkuService skuService;

   @Reference
   private OmsCartService omsCartService;

   @LoginIsRequiredIntercept(isRequired = false)
  @RequestMapping("addToCart") //当不确定页面传入的数据为何种类型时  采用BigDecimal
  public String addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, BigDecimal quantity){

      //检查用户是否登入
      //测试数据  userId
      String userId = (String)request.getAttribute("userId");
      String nickName = (String) request.getAttribute("nickName");

      String cookieName = WebConstant.COOKIE_CART_NAME;

      String cookieValue = CookieUtil.getCookieValue(request, response, cookieName, true);

      //创造购物车集合
      List<OmsCartItem> omsCartItems = JSON.parseArray(cookieValue, OmsCartItem.class);
      if(omsCartItems == null){
          List<OmsCartItem> cartItems = new ArrayList<>();
          omsCartItems = cartItems;
      }

      if(StringUtils.isNotBlank(skuId)) {
          //将要添加的商品从数据库中查询出来
          PmsSkuInfo sku = skuService.getSkuInfoById(skuId);
          OmsCartItem omsCartItem = new OmsCartItem();

          omsCartItem.setProductId(sku.getProductId());
          omsCartItem.setProductSkuId(sku.getId());
          omsCartItem.setCreateDate(new Date());
          omsCartItem.setIsChecked("1");
          omsCartItem.setProductName(sku.getSkuName());
          omsCartItem.setQuantity(quantity);
          omsCartItem.setProductPic(sku.getSkuDefaultImg());
          omsCartItem.setProductCategoryId(sku.getCatalog3Id());
          omsCartItem.setPrice(sku.getPrice());

          //主逻辑线  登入时 数据存入DB  没登入时 数据存在 cookie中
          if (StringUtils.isNotBlank(userId)) {
              //将数据存入DB  -  新车还是老车  新车直接添加  老车则添加数量
              omsCartItem.setMemberId(userId);

              //与cookie中的逻辑方式不一样的方法 - 通过userId和skuId确定购物车是否存在
              OmsCartItem cart = omsCartService.selectOmsCartItemByUserIdAndSkuId(userId, skuId);

              if (cart != null){
                 //不为空 则将改变数量
                 omsCartItem.setQuantity(omsCartItem.getQuantity().add(quantity));
                 omsCartService.updateOmsCart(omsCartItem);
              }else{
                  //为空  DB则直接添加
                  omsCartService.addOmsCart(omsCartItem);
              }
          } else {
              //存入cookie  两条分支 存入过购物车的话 相同的商品 进行数量添加  不相同商品 直接添加
              //没存入过购物车的话  直接添加
              if (StringUtils.isNotBlank(cookieValue) && !"[]".equals(cookieValue)) {
                  //存在内容的话 - 判断cookie中是否存在相同的购物车信息
                  if(omsCartItem != null && omsCartItems.size() > 0){
                      boolean flag = false;
                      for (OmsCartItem cartItem : omsCartItems) {
                          //存在相同的商品  改变数量
                          if (skuId.equals(cartItem.getProductSkuId())){
                              cartItem.setQuantity(cartItem.getQuantity().add(quantity));
                              //存在则立马跳出循环
                              flag = true;
                              break;
                          }
                      }
                      if (!flag){
                          omsCartItems.add(omsCartItem);
                      }
                  }
              } else {
                  //cookie中没有内容时 将内容加入购物车
                  omsCartItems.add(omsCartItem);
              }
          }
          String cartListString = JSON.toJSONString(omsCartItems);
          CookieUtil.setCookie(request,response,cookieName,cartListString,1000*60*60*3,true);
      }

      return "redirect:/success.html";  //需要传递静态数据到success页面
  }

  @LoginIsRequiredIntercept(isRequired = false)
  @RequestMapping("cartList")
    public String cartList(HttpServletRequest request,HttpServletResponse response, ModelMap modelMap){
        //页面显示   内容
        //两条分支   没登入  从cookie中获取内容
        //登入       从redis中获取内容
      String userId = (String)request.getAttribute("userId");
      String nickName = (String) request.getAttribute("nickName");

      List<OmsCartItem> cartList = new ArrayList<>();

      if(StringUtils.isNotBlank(userId)){
          //登入 - Redis中拿
          cartList = omsCartService.selectOmsCartItemsByRedis(userId);
      }else{
          //未登入 - Cookie中拿
          String cookieValue = CookieUtil.getCookieValue(request, response, WebConstant.COOKIE_CART_NAME, true);
          if(StringUtils.isNotBlank(cookieValue)) {
              cartList = JSON.parseArray(cookieValue, OmsCartItem.class);
          }
      }

      //总金额的设置 放入modelMap
      if (cartList != null && cartList.size() > 0){

          BigDecimal totalMoney = getTotalMoney(cartList);
          modelMap.put("totalMoney",totalMoney);
      }

      modelMap.put("cartList",cartList);

      return "cartList";
  }

    private BigDecimal getTotalMoney(List<OmsCartItem> cartList) {
        BigDecimal totalMoney = new BigDecimal("0");

        for (OmsCartItem omsCartItem : cartList) {
            if(omsCartItem != null && "1".equals(omsCartItem.getIsChecked())){
                omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
                totalMoney = totalMoney.add(omsCartItem.getTotalPrice());
            }
        }

        return totalMoney;
    }

    @LoginIsRequiredIntercept(isRequired = false)
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request,HttpServletResponse response,String isChecked,String skuId,ModelMap modelMap){

        String userId = (String)request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        //传递skuId 和 是否勾选的状态   将DB或redis中的内容改变  或者改变cookie中的内容

        String cookieValue = CookieUtil.getCookieValue(request, response, WebConstant.COOKIE_CART_NAME, true);
        List<OmsCartItem> cartItems = new ArrayList<>();

        if(StringUtils.isNotBlank(userId)){
            //修改DB中的 然后同步到redis中 - 将redis中的内容全部进行同步 不止一条
            cartItems = omsCartService.selectOmsCartItemsByRedis(userId);
            omsCartService.updateIsCheckedStatus(userId,skuId,isChecked);
        }else{
            //修改cookie中的内容
            if (StringUtils.isNotBlank(cookieValue)){
                List<OmsCartItem> omsCartItems = JSON.parseArray(cookieValue,OmsCartItem.class);
                cartItems = omsCartItems;
                if(cartItems != null && cartItems.size() > 0){
                    for (OmsCartItem omsCartItem : cartItems) {
                        if(omsCartItem != null && skuId != null && skuId.equals(omsCartItem.getProductSkuId())){
                            if(StringUtils.isNotBlank(isChecked)) {
                                omsCartItem.setIsChecked(isChecked);
                            }
                        }
                    }
                }
            }
        }

     //将改变后的数据全部重新放入到页面中
     modelMap.put("cartList",cartItems);

     //算出总金额
     if(cartItems != null && cartItems.size() > 0){
         BigDecimal totalMoney = getTotalMoney(cartItems);
         if(totalMoney != null){
             modelMap.put("totalMoney",totalMoney);
         }
     }

      return "cartInnerHtml";
    }

}
