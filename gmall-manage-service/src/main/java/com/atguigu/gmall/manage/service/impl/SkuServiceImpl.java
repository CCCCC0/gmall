package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.pojo.PmsSkuAttrValue;
import com.atguigu.gmall.pojo.PmsSkuImage;
import com.atguigu.gmall.pojo.PmsSkuInfo;
import com.atguigu.gmall.pojo.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.atguigu.gmall.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void savePmsSkuInfo(PmsSkuInfo pmsSkuInfo) {

        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);

        //获取自增主键Id
        String skuId = pmsSkuInfo.getId();

        //进行图片集合的插入
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        //进行Sku属性的插入
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
             pmsSkuAttrValue.setSkuId(skuId);
             pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        //进行Sku销售属性的插入
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
    }

    @Override
    public List<PmsSkuInfo> getPmsSkuInfoListBySpuId(String spuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setProductId(spuId);
        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.select(pmsSkuInfo);

        for (PmsSkuInfo skuInfo : pmsSkuInfoList) {
            PmsSkuSaleAttrValue saleAttrValue = new PmsSkuSaleAttrValue();
            saleAttrValue.setSkuId(skuInfo.getId());
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuSaleAttrValueMapper.select(saleAttrValue);
            skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        }

        return pmsSkuInfoList;
    }


    public PmsSkuInfo getSkuInfoByIdCopy(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImageList = pmsSkuImageMapper.select(pmsSkuImage);
        pmsSkuInfo1.setSkuImageList(pmsSkuImageList);

        return pmsSkuInfo1;
    }

    @Override
    public PmsSkuInfo getSkuInfoById(String skuId) {

        Jedis jedis = redisUtil.getJedis();

        String key = "PmsSkuInfo" + ":" + skuId + ":" + "info";

        String json = jedis.get(key);

        try {
            if (StringUtils.isNotBlank(json)) {

                PmsSkuInfo pmsSkuInfo = JSON.parseObject(json, PmsSkuInfo.class);

                return pmsSkuInfo;
            } else {
                PmsSkuInfo pmsSkuInfo = getSkuInfoByIdCopy(skuId);
                if(pmsSkuInfo != null) {
                    //同步数据到redis
                    String skuJson = JSON.toJSONString(pmsSkuInfo);
                    jedis.set(key, skuJson);

                    return pmsSkuInfo;
                }
            }
        }catch(Exception e){
            //导引错误信息到系统日志
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return null;
    }

    /*
    为了解决redis被大量并发量攻击导致的 缓存穿透：被大量的并发 访问不存在的一个key 导致的 大量并发直接攻击到DB上
    解决问题：当redis被缓存穿透后 在另一个redis上添加分布式锁
              给大量的请求添加编号 使得访问DB操作 正常
     */
    public PmsSkuInfo getSkuInfoByIdCopy1(String skuId) {

        Jedis jedis = redisUtil.getJedis();

        String key = "PmsSkuInfo" + ":" + skuId + ":" + "info";

        String json = jedis.get(key);

        UUID uuids = UUID.randomUUID();
        String uuid = UUID.randomUUID().toString();

        try {
            if (StringUtils.isNotBlank(json)) {

                PmsSkuInfo pmsSkuInfo = JSON.parseObject(json, PmsSkuInfo.class);
                System.out.println(uuids + "同学成功访问缓存");
                return pmsSkuInfo;
            } else {

                //先进行redis分布式锁的添加
                //nx 表示当前Key添加过值后  不能进行覆盖操作 - 只有被删除以后才能
                String lock = "sku:" + skuId + ":lock";
                String result = jedis.set(lock, uuid, "nx", "ex", 10);

                if (StringUtils.isNotBlank(result) && "OK".equals(result)){
                    System.out.println(uuids + "同学成功开启了分布式锁并访问了数据库");
                    //访问DB操作
                    PmsSkuInfo pmsSkuInfo = getSkuInfoByIdCopy(skuId);
                    if(pmsSkuInfo != null) {
                        //同步数据到redis
                        String skuJson = JSON.toJSONString(pmsSkuInfo);
                        jedis.set(key, skuJson);

                        //释放锁 - 将当前sku:skuId:lock删除掉 --有Bug 可能把别人的锁进行删除
                        //存在问题：锁的查询删除操作 一来一回 会有网络延迟
                        //解决方案：将redis中需要进行的操作 写一个脚本运行 则不会存在上面一来一回的网络延迟
                        // 删除自己定义的锁
                        //调用lura脚本
                        String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                        jedis.eval(script, Collections.singletonList(lock),Collections.singletonList(uuid));
         //             jedis.del("lock");

                        return pmsSkuInfo;
                    }else{
                        return null;
                    }
                }else{
                    System.out.println(uuids + "同学正在等待锁--回旋中");
                    Thread.sleep(3000);
                    return getSkuInfoByIdCopy1(skuId);
                }
            }
        }catch(Exception e){
            //导引错误信息到系统日志
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return null;
    }

    @Override
    public List<PmsSkuInfo> getAllPmsSkuInfo() {

        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> list = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(list);
        }

        return  pmsSkuInfoList;
    }

}
