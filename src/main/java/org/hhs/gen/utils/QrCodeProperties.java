package org.hhs.gen.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class QrCodeProperties {
    private static Logger logger = LoggerFactory.getLogger(QrCodeProperties.class);
    private static Properties p = null;
    static {
        if(p == null){
            synchronized (p){
                if(p == null){
                    loadProperties();
                }
            }
        }
    }

    /**
     * 加载配置类信息
     */
    private static void loadProperties(){
        p = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = QrCodeProperties.class.getResourceAsStream("/qrcode.properties");
            logger.info("从当前类中加载配置信息");
            p.load(inputStream);
        } catch (IOException e) {
            inputStream = QrCodeProperties.class.getClassLoader().getResourceAsStream("qrcode.properties");
            try {
                logger.info("从当前类的加载器中加载配置信息");
                p.load(inputStream);
            } catch (IOException e1) {
                logger.error("加载失败配置信息失败", e);
            }
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("配置类流关闭失败", e);
                }
            }
        }
    }

    public static int getQcHigh(){
        String high = p.getProperty("high");
        if("".equals(high)||high == null){
            return 200;
        }else {
            return Integer.valueOf(high);
        }
    }

    public static int getQcWidth(){
        String width = p.getProperty("width");
        if("".equals(width)||width == null){
            return 200;
        }else {
            return Integer.valueOf(width);
        }
    }

    public static String getFileRoot(){
        String sysDir = System.getProperty("catalina.home");
        return sysDir;
    }
}
