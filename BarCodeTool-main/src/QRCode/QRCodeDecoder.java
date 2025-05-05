package QRCode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class QRCodeDecoder {
    public String finishedbincode;


    public String decodeBarcode(String path) throws IOException{
        BufferedImage image = ImageIO.read(new File(path));

        return new String();    
    }

    private String getVersion(BufferedImage image){
        return new String();
    }
}
