package com.atguigu.web.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.csource.fastdfs.pool.Connection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

    @Test
    public void contextLoads() throws IOException, MyException {

        String file = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(file);
        TrackerClient trackerClient=new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();

        StorageClient storageClient = new StorageClient(trackerServer);
        String orginalFilename="d://b.jpg";

        // 3 向storageServer上传文件
        String[] jpgs = storageClient.upload_file(orginalFilename, "jpg", null);

        //4 storageServer返回存储地址给客户端
        for (String jpg : jpgs) {
            System.out.println(jpg);
        }
    }

}
