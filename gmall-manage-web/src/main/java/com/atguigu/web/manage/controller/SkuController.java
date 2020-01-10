package com.atguigu.web.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.SkuService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SkuController {

    @Reference
    private SkuService skuService;



}
