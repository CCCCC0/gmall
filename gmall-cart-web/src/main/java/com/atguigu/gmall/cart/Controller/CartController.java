package com.atguigu.gmall.cart.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pojo.OmsCartItem;
import com.atguigu.gmall.pojo.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.WebConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
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

  @RequestMapping("addToCart") //当不确定页面传入的数据为何种类型时  采用BigDecimal
  public String addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, BigDecimal quantity){

     //检查用户是否登入
     //测试数据  userId
      String userId = "";   //设置为空  将购物车中的内容添加到浏览器cookie中

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
              //存入DB
          } else {
              //存入cookie  两条分支 存入过购物车的话 相同的商品 进行数量添加  不相同商品 直接添加
              //没存入过购物车的话  直接添加
              if (StringUtils.isNotBlank(cookieValue)) {
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
      return "redirect:/success.html";
  }

  @RequestMapping("cartList")
    public String cartList(){


      return "cartList";
  }

}
