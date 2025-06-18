package EAN;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class BarcodeDecoder {
    public String finishedbincode;

    // Таблицы кодировок
    private static final String[] L_TABLE = {
        "0001101", "0011001", "0010011", "0111101", "0100011",
        "0110001", "0101111", "0111011", "0110111", "0001011"
    };
    
    private static final String[] G_TABLE = {
        "0100111", "0110011", "0011011", "0100001", "0011101",
        "0111001", "0000101", "0010001", "0001001", "0010111"
    };
    
    private static final String[] R_TABLE = {
        "1110010", "1100110", "1101100", "1000010", "1011100",
        "1001110", "1010000", "1000100", "1001000", "1110100"
    };
    
    private static final Map<String, String> FIRST_DIGIT_PATTERNS = new HashMap<>();
    static {
        FIRST_DIGIT_PATTERNS.put("LLLLLL", "0");
        FIRST_DIGIT_PATTERNS.put("LLGLGG", "1");
        FIRST_DIGIT_PATTERNS.put("LLGGLG", "2");
        FIRST_DIGIT_PATTERNS.put("LLGGGL", "3");
        FIRST_DIGIT_PATTERNS.put("LGLLGG", "4");
        FIRST_DIGIT_PATTERNS.put("LGGLLG", "5");
        FIRST_DIGIT_PATTERNS.put("LGGGLL", "6");
        FIRST_DIGIT_PATTERNS.put("LGLGLG", "7");
        FIRST_DIGIT_PATTERNS.put("LGLGGL", "8");
        FIRST_DIGIT_PATTERNS.put("LGGLGL", "9");
    }

    public static String decode(String barcode) {
        // Проверка длины
        if (barcode.length() != 95) {
            throw new IllegalArgumentException("Invalid barcode length. Expected 95, got " + barcode.length());
        }
        
        // Проверка защитных полос
        if (!barcode.startsWith("101") || !barcode.endsWith("101")) {
            throw new IllegalArgumentException("Invalid guard patterns");
        }
        if (!barcode.substring(45, 50).equals("01010")) {
            throw new IllegalArgumentException("Invalid center guard pattern");
        }

        // Извлечение данных
        String leftPart = barcode.substring(3, 45);
        String rightPart = barcode.substring(50, 92);

        // Декодирование левой части
        StringBuilder encodingPattern = new StringBuilder();
        StringBuilder digits = new StringBuilder();
        
        for (int i = 0; i < 6; i++) {
            String segment = leftPart.substring(i * 7, (i + 1) * 7);
            String digit = decodeLeftSegment(segment);
            
            if (digit == null) {
                throw new IllegalArgumentException("Unknown left pattern: " + segment);
            }
            
            char encodingType = digit.charAt(0);
            digits.append(digit.charAt(1));
            encodingPattern.append(encodingType);
        }

        // Определение первой цифры
        String firstDigit = FIRST_DIGIT_PATTERNS.get(encodingPattern.toString());
        if (firstDigit == null) {
            throw new IllegalArgumentException("Unknown encoding pattern: " + encodingPattern);
        }

        // Декодирование правой части
        for (int i = 0; i < 6; i++) {
            String segment = rightPart.substring(i * 7, (i + 1) * 7);
            String digit = decodeRightSegment(segment);
            
            if (digit == null) {
                throw new IllegalArgumentException("Unknown right pattern: " + segment);
            }
            
            digits.append(digit);
        }

        String result = firstDigit + digits.toString();
        
        // Проверка контрольной суммы
        if (!validateChecksum(result)) {
            throw new IllegalArgumentException("Checksum validation failed");
        }

        return result;
    }

    private static String decodeLeftSegment(String segment) {
        for (int i = 0; i < 10; i++) {
            if (segment.equals(L_TABLE[i])) {
                return "L" + i;  // L-кодировка
            }
            if (segment.equals(G_TABLE[i])) {
                return "G" + i;  // G-кодировка
            }
        }
        return null;
    }

    private static String decodeRightSegment(String segment) {
        for (int i = 0; i < 10; i++) {
            if (segment.equals(R_TABLE[i])) {
                return String.valueOf(i);
            }
        }
        return null;
    }

    private static boolean validateChecksum(String ean13) {
        if (ean13.length() != 13) return false;
        
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(ean13.charAt(i));
            sum += (i % 2 == 0) ? digit * 1 : digit * 3;
        }
        
        int checksum = (10 - (sum % 10)) % 10;
        int lastDigit = Character.getNumericValue(ean13.charAt(12));
        
        return checksum == lastDigit;
    }

    public String Startdecode(BufferedImage image) {

        int image_width = image.getWidth();
        String code = "";
        for(int i =0;i<image_width;i++){
            int rgb = image.getRGB(i,image.getHeight()/2) & 0xffffff;
            String num = rgb == 0xffffff ? "0" : "1";
            code+=num;
            
        }
        System.out.println(code);

        String patternblack="";
        int a=0;

        for(int i = 0;i<code.length();i++){
            if(code.charAt(i)=='1')a++;
            else{
                if(a==0) continue;
                if(a!=6) System.out.println(code.charAt(i)+"!=6 = "+a);
                patternblack+=String.valueOf(a)+";";
                a=0;
            }
        }
        code="";
        String[] pattern = patternblack.split(";");
        System.out.println(pattern.length);
        System.out.println("patternblack:"+patternblack);
        int minimalbar=Integer.parseInt(pattern[0]);
        System.out.println("minimalbar:"+minimalbar);

        for(int i =0;i<image_width;i+=minimalbar){
            if(i>=image_width)break;
            int rgb = image.getRGB(i,image.getHeight()/2) & 0xffffff;
            String num;
            num = rgb == 0xffffff ? "0" : "1";
            code+=num;
        }

        System.out.println("code:"+code);
        int d=0,c=code.length()-1;
        while(code.charAt(d)!='1'){
            d++;
        }
        while(code.charAt(c)!='1'){
            c--;
        }
        finishedbincode=code.substring(d, c+1);
        System.out.println(finishedbincode);
            
        
        try {
            return decode(finishedbincode);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }
}