package org.hhs.gen.controller;

import org.apache.commons.logging.Log;
import org.hhs.gen.domain.QrCodeInfo;
import org.hhs.gen.service.GeneratorImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Controller
public class QrController {
    private Logger logger = LoggerFactory.getLogger(QrController.class);
    @Autowired
    private GeneratorImageService generatorImage;

    @RequestMapping("work")
    @ResponseBody
    public String getQrcode(@RequestParam("file") MultipartFile mfile, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(!mfile.isEmpty()){
            InputStream inputStream = mfile.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            try {
                mainGen(inputStreamReader);
            } catch (InterruptedException e) {
                logger.error("interrupt", e);
            }
        }
        return "ok!";
    }

    private void generatorImage(List<QrCodeInfo> qrCodeInfoList, int pageSize){
        for(int i = 0; i < qrCodeInfoList.size()/pageSize; i++){
            generatorImage.generatorOne(qrCodeInfoList.subList(i*pageSize, pageSize*(i+1)), "erweima" + i+".jpg", Thread.currentThread().getName());
        }

        int left = qrCodeInfoList.size()%pageSize;
        int mod = qrCodeInfoList.size()/pageSize;
        if(qrCodeInfoList.size()%pageSize != 0){
            generatorImage.generatorOne(qrCodeInfoList.subList(mod*pageSize, pageSize*mod+left), "erweima" + mod+".jpg", Thread.currentThread().getName());
        }
        logger.info("线程"+Thread.currentThread().getName()+"图片生成完毕");
    }

    private List<QrCodeInfo> getqrCodeInfoList(InputStreamReader InputStreamReader){
        long start = System.currentTimeMillis();
        List<QrCodeInfo> qrCodeInfoList = generatorImage.getQrInfoList(new BufferedReader(InputStreamReader));
        long end = System.currentTimeMillis();
        logger.info("文件读取处理时间:"+(end-start)+"ms");
        return qrCodeInfoList;
    }



    public void mainGen(InputStreamReader inputStream) throws InterruptedException {
        Long start = System.currentTimeMillis();
        final List<QrCodeInfo> getqrCodeInfoList = getqrCodeInfoList(inputStream);
        try {
            inputStream.close();
        } catch (IOException e) {
            logger.error("inputStream流关闭失败", e);
        }
        final int coreNums = Runtime.getRuntime().availableProcessors();
        int listSize = getqrCodeInfoList.size();
        final int perSize = listSize/coreNums;
        final int leftSize = listSize%coreNums;
        final CountDownLatch countDownLatch = new CountDownLatch(coreNums);
        ExecutorService executors = Executors.newFixedThreadPool(coreNums);

        for(int i = 0; i < coreNums; i++){
            executors.submit(new workThread(String.valueOf(i), i*perSize, (i+1)*perSize){
                @Override
                public void run() {
                    generatorImage(getqrCodeInfoList.subList(this.start, this.end), 30);
                    countDownLatch.countDown();
                }
            });
        }
        executors.shutdown();
        countDownLatch.await();
        if (leftSize != 0){
            final CountDownLatch countDownLatch1 = new CountDownLatch(1);
            new Thread("pool-2-thread-"+String.valueOf(coreNums)){
                @Override
                public void run() {
                    generatorImage(getqrCodeInfoList.subList(perSize*coreNums, perSize*coreNums+leftSize), 30);
                    countDownLatch1.countDown();
                }
            }.start();
            countDownLatch1.await();
        }
        Long end = System.currentTimeMillis();
        logger.info("总耗时:"+(end-start)/1000+"s");
    }

    private class workThread extends Thread{
        public int start;
        public int end;

        public workThread(String name, int start, int end){
            super(name);
            this.start = start;
            this.end = end;
        }
    }

}
