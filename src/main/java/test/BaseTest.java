package test;


import org.hhs.gen.controller.QrController;
import org.hhs.gen.utils.QrCodeProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application.xml"})
public class BaseTest {
    @Autowired
    private QrController qrController;
    @Test
    public void testProperties() throws FileNotFoundException, InterruptedException {
        File file = new File("D:\\ali\\erweima.txt");
        InputStream inputStream = new FileInputStream(file);
//        qrController.mainGen(inputStream);
    }
}
