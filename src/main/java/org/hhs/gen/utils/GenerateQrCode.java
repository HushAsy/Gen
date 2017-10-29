package org.hhs.gen.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.hhs.gen.controller.QrController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 产生二维码
 */
public class GenerateQrCode {
    private static final int QRCOLOR = 0xFF000000;   //默认是黑色
    private static final int BGWHITE = 0xFFFFFFFF;   //背景颜色
    private Logger logger = LoggerFactory.getLogger(GenerateQrCode.class);

    private static int count = 0;
    private static int dirCount = 0;
    private static boolean start = true;

    private static File fileDirRoot = new File("image");
    private static final String catalinaHome = System.getProperty("catalina.home")+File.pathSeparator+"temp";
    static {
        if (!fileDirRoot.exists()){
            fileDirRoot.mkdir();
        }
    }
    private static File fileDir = null;



    public void getQrCode(String content, String number, String fileName, String filePath){
        addContentToImage(content, BarcodeFormat.QR_CODE, getDecodeHintType(),number, fileName);
        logger.info(new Date().toString()+"已经完成:"+fileName);
    }

    /**
     * 生成二维码bufferedImage图片
     *
     * @param content
     *            编码内容
     * @param barcodeFormat
     *            编码类型
     * @param hints
     *            设置参数
     * @return
     */
    public BufferedImage getQR_CODEBufferedImage(String content, BarcodeFormat barcodeFormat, Map<EncodeHintType, ?> hints) {
        MultiFormatWriter multiFormatWriter = null;
        BitMatrix bm = null;
        BufferedImage image = null;
        try {
            multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            bm = multiFormatWriter.encode(content, barcodeFormat, QrCodeProperties.getQcWidth(), QrCodeProperties.getQcHigh(), hints);
            int w = bm.getWidth();
            int h = bm.getHeight();
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < w; x++)
            {
                for (int y = 0; y < h; y++)
                {
                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }
        }
        catch (WriterException e) {
            logger.error("生成二维码失败", e);
        }
        return image;
    }

    public void addContentToImage(String content,
                                  BarcodeFormat barcodeFormat,
                                  Map<EncodeHintType, ?> hints,
                                  String productName,
                                  String fileName){
        try{
            /**
             * 读取二维码图片，并构建绘图对象
             */
            BufferedImage image = getQR_CODEBufferedImage(content, barcodeFormat, hints);
            Graphics2D g = image.createGraphics();
            g.dispose();

            //把编号添加上去，这里最多支持两行。太长就会自动截取啦
            if (productName != null && !productName.equals("")) {
                //新的图片，把带logo的二维码下面加上文字
                BufferedImage outImage = new BufferedImage(QrCodeProperties.getQcWidth(), QrCodeProperties.getQcHigh()+25, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D outg = outImage.createGraphics();
                //画二维码到新的面板
                outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
                //画文字到新的面板
                outg.setColor(Color.BLACK);
                Font font = new Font("宋体",Font.BOLD,28);
                outg.setFont(font); //字体、字型、字号
                int strWidth = outg.getFontMetrics().stringWidth(productName);
                if (strWidth > (QrCodeProperties.getQcHigh()-1)) {
//                  //长度过长就截取前面部分
                    String productName1 = productName.substring(0, productName.length()/2);
                    String productName2 = productName.substring(productName.length()/2, productName.length());
                    int strWidth1 = outg.getFontMetrics().stringWidth(productName1);
                    int strWidth2 = outg.getFontMetrics().stringWidth(productName2);
                    outg.drawString(productName1, QrCodeProperties.getQcHigh()/2  - strWidth1/2, image.getHeight() + (outImage.getHeight() - image.getHeight())/2 + 12 );
                    BufferedImage outImage2 = new BufferedImage(200, 250, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D outg2 = outImage2.createGraphics();
                    outg2.drawImage(outImage, 0, 0, outImage.getWidth(), outImage.getHeight(), null);
                    outg2.setColor(Color.BLACK);
                    outg2.setFont(new Font("宋体",Font.BOLD,28)); //字体、字型、字号
                    outg2.drawString(productName2, QrCodeProperties.getQcHigh()/2  - strWidth2/2, outImage.getHeight() + (outImage2.getHeight() - outImage.getHeight())/2 + 5 );
                    outg2.dispose();
                    outImage2.flush();
                    outImage = outImage2;
                }else {
                    outg.drawString(productName, QrCodeProperties.getQcHigh()/2  - strWidth/2 , image.getHeight() + (outImage.getHeight() - image.getHeight())/2+4); //画文字
                }
                outg.dispose();
                outImage.flush();
                image = outImage;
            }
            image.flush();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.flush();
            ImageIO.write(image, "png", baos);

            //二维码生成的路径，但是实际项目中，我们是把这生成的二维码显示到界面上的，因此下面的折行代码可以注释掉

            if(count > 1000){
                fileDir = new File(fileDirRoot,"erweima"+(dirCount++));
                if(!fileDir.exists()){
                    fileDir.mkdir();
                }
                count = 0;
            }else if(start){
                fileDir = new File(fileDirRoot,"erweima"+(dirCount++));
                if(!fileDir.exists()){
                    fileDir.mkdir();
                }
                start = false;
            }
            File file = new File(fileDir, fileName);
            ImageIO.write(image, "png", file); //TODO
            count++;
            baos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置二维码的格式参数
     *
     * @return
     */
    public Map<EncodeHintType, Object> getDecodeHintType()
    {
        // 用于设置QR二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        // 设置QR二维码的纠错级别（H为最高级别）具体级别信息
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 设置编码方式
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.MAX_SIZE, 350);
        hints.put(EncodeHintType.MIN_SIZE, 100);
        return hints;
    }

//    public static void main(String...args) throws FileNotFoundException, InterruptedException {
//        QrController qrController = new QrController();
//        File file = new File("D:\\2017\\erweima1.txt");
//        qrController.mainGen(new InputStreamReader(new FileInputStream(file)));
//    }
}
