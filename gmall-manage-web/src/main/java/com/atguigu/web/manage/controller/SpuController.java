package com.atguigu.web.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pojo.PmsBaseSaleAttr;
import com.atguigu.gmall.pojo.PmsProductImage;
import com.atguigu.gmall.pojo.PmsProductInfo;
import com.atguigu.gmall.pojo.PmsProductSaleAttr;
import com.atguigu.gmall.service.SpuService;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.atguigu.gmall.util.*;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    private SpuService spuService;

    @ResponseBody
    @RequestMapping("spuList")
    public List<PmsProductInfo> spuList(String catalog3Id){

        List<PmsProductInfo> pmsProductInfoList = spuService.getAllPmsProductInfo(catalog3Id);

        return pmsProductInfoList;
    }

    @ResponseBody
    @RequestMapping("baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList(){

        List<PmsBaseSaleAttr> allPmsProductSaleAttrList = spuService.getAllPmsProductSaleAttr();

        return allPmsProductSaleAttrList;
    }

    @ResponseBody
    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){

        spuService.savePmsProductInfo(pmsProductInfo);

        return "success";
    }

    @ResponseBody
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){

        try {
            String file = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(file);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getTrackerServer();

            StorageClient storageClient = new StorageClient(trackerServer);
            String orginalFilename = multipartFile.getOriginalFilename();

            int lastIndex = orginalFilename.lastIndexOf(".");
            String file_ext_name = orginalFilename.substring(lastIndex + 1);
            String[] jpgs = storageClient.upload_file(multipartFile.getBytes(), file_ext_name, null);

            String imageUrl = WebConstant.LINUX_ADDRESS_PREFIX;
            for (String jpg : jpgs) {
                imageUrl = imageUrl + "/" + jpg;
            }

            return imageUrl;
        }catch(Exception e){
            e.printStackTrace();
            return "FAILED";
        }
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        List<PmsProductSaleAttr> allPmsProductSaleAttrList = spuService.getAllPmsProductSaleAttr(spuId);

        return allPmsProductSaleAttrList;
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){

        List<PmsProductImage> pmsProductImageList = spuService.getAllPmsProductImage(spuId);

        return pmsProductImageList;
    }

}
