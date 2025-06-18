package EAN;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class BarcodeImageCreator {
    public static String last_barcode;
    public String bincode;

    static int width_last_barcode;
    static int height_last_barcode;
    
    private static final String[][] LEFT_PATTERNS = {
        {"0001101", "0100111"}, // 0
        {"0011001", "0110011"}, // 1
        {"0010011", "0011011"}, // 2
        {"0111101", "0100001"}, // 3
        {"0100011", "0011101"}, // 4
        {"0110001", "0111001"}, // 5
        {"0101111", "0000101"}, // 6
        {"0111011", "0010001"}, // 7
        {"0110111", "0001001"}, // 8
        {"0001011", "0010111"}  // 9
    };
    
    private static final String[] RIGHT_PATTERNS = {
        "1110010", "1100110", "1101100", "1000010", "1011100",
        "1001110", "1010000", "1000100", "1001000", "1110100"
    };

    private static final String[] PREFIX_PATTERNS = {
        "AAAAAA", "AABABB", "AABBAB", "AABBBA", "ABAABB",
        "ABBAAB", "ABBBAA", "ABABAB", "ABABBA", "ABBABA"
    };

    static void saveBarCode(BufferedImage bufferedImage){
        try {
            int countFiles = new File("img").listFiles().length;
            countFiles++;
            for(int i=0;i<countFiles;i++){
                String path="img//barcode"+i+".png";
                File file = new File(path);
                if(file.exists()==true){
                    System.out.println(path+": exists");
                    continue;
                }
                else{
                    last_barcode=path;
                    ImageIO.write(bufferedImage,"png",file);
                    file.createNewFile();
                    break;
                }
            }
        } catch (Exception e) {
            
        }
        
    }

    public void createImage(String ean13) throws Exception {
        if (!EAN13Validator.validate(ean13)) {
            while(ean13.length()!=12){
                ean13=ean13+"0";
            }
        }

        // Параметры изображения
        int width = 600;
        int height = 300;
        int quietZone = 20;

        bincode = ean13;
        width_last_barcode = width;
        height_last_barcode = height;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Фон
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Генерация паттерна
        String pattern = generateFullPattern(ean13);
        float barWidth = 6;
        
        // Рисуем штрихи
        g.setColor(Color.BLACK);
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '1') {
                int x = quietZone + (int)(i * barWidth);
                int barHeight = isGuardPosition(i, pattern.length()) ? height : height - 40;
                //g.fillRect(x, 0, (int)Math.ceil(barWidth), barHeight);
                for(int j =0;j<barHeight;j++){
                    for(int k =0;k<6;k++)
                        image.setRGB(x+k, j, 0x000000);
                }
            }
        }

        //Текст
        g.setFont(new Font("Arial", Font.PLAIN, 40));
        g.drawString(String.valueOf(ean13.charAt(0)),0,height-5);
        g.drawString(ean13.substring(1, 7),100,height-5);
        g.drawString(ean13.substring(7),370,height-5);

        
        saveBarCode(image);
        g.dispose();
    }

    private static boolean isGuardPosition(int index, int length) {
        return index < 3 || (index >= 45 && index < 50) || index >= length - 3;
    }

    private static String generateFullPattern(String ean13) {
        StringBuilder pattern = new StringBuilder();
        int firstDigit = ean13.charAt(0) - '0';
        String encoding = PREFIX_PATTERNS[firstDigit];
        
        pattern.append("101"); // Start guard
        
        for (int i = 1; i <= 6; i++) {
            int digit = ean13.charAt(i) - '0';
            pattern.append(LEFT_PATTERNS[digit][encoding.charAt(i-1) == 'A' ? 0 : 1]);
        }
        
        pattern.append("01010"); // Center guard
        
        for (int i = 7; i <= 12; i++) {
            int digit = ean13.charAt(i) - '0';
            pattern.append(RIGHT_PATTERNS[digit]);
        }
        
        pattern.append("101"); // End guard
        return pattern.toString();
    }
}