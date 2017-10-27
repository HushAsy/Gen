package org.hhs.gen.controller;

import org.hhs.gen.domain.QrCodeInfo;
import org.hhs.gen.service.GeneratorImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Service
public class QrController {
    private Logger logger = LoggerFactory.getLogger(QrController.class);
//    @Autowired
    private GeneratorImageService generatorImage = new GeneratorImageService();
    @RequestMapping("work")
    public void getQrcode(@RequestParam("file") MultipartFile mfile, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(!mfile.isEmpty()){
            InputStream inputStream = mfile.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            try {
                mainGen(inputStreamReader);
            } catch (InterruptedException e) {
                logger.error("interrupt", e);
            }

        }
    }

    private void generatorImage(List<QrCodeInfo> qrCodeInfoList){
        for(int i = 0; i < qrCodeInfoList.size()/10; i++){
            generatorImage.generatorOne(qrCodeInfoList.subList(i*10, 10*(i+1)), "erweima" + i+".jpg", Thread.currentThread().getName());
        }

        int left = qrCodeInfoList.size()%10;
        int mod = qrCodeInfoList.size()/10;
        if(qrCodeInfoList.size()%10 != 0){
            generatorImage.generatorOne(qrCodeInfoList.subList(mod*10, 10*mod+left), "erweima" + mod+".jpg", Thread.currentThread().getName());
        }
        logger.info("线程"+Thread.currentThread().getName()+"图片生成完毕");
    }

    private List<QrCodeInfo> getqrCodeInfoList(InputStreamReader InputStreamReader){
        List<QrCodeInfo> qrCodeInfoList = generatorImage.getQrInfoList(new BufferedReader(InputStreamReader));
        return qrCodeInfoList;
    }



    public void mainGen(InputStreamReader inputStream) throws InterruptedException {
        Long start = System.currentTimeMillis();
        final List<QrCodeInfo> getqrCodeInfoList = getqrCodeInfoList(inputStream);
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
                    generatorImage(getqrCodeInfoList.subList(this.start, this.end));
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        final CountDownLatch countDownLatch1 = new CountDownLatch(1);
        if (leftSize != 0){
            new Thread(String.valueOf(coreNums)){
                @Override
                public void run() {
                    generatorImage(getqrCodeInfoList.subList(perSize*coreNums, perSize*coreNums+leftSize));
                    countDownLatch1.countDown();
                }
            }.start();
        }
        countDownLatch1.await();
        Long end = System.currentTimeMillis();
        logger.info("总耗时:"+(end-start));
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
