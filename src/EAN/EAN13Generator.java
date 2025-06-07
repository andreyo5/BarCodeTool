package EAN;
public class EAN13Generator {
    public static String last_barcode;
    public static String bincode;

    static int width_last_barcode;
    static int height_last_barcode;

    public String generate(String code) {
        if (code == null || !code.matches("\\d{12}")) {
            throw new IllegalArgumentException("Требуется 12 цифр");
        }
        
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = code.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit * 1 : digit * 3;
        }
        
        int checksum = (10 - (sum % 10)) % 10;
        
        String final_code = code + checksum;
        try {
            BarcodeImageCreator barcodeImageCreator = new BarcodeImageCreator();
            barcodeImageCreator.createImage(final_code);
            last_barcode = barcodeImageCreator.last_barcode;
            bincode = barcodeImageCreator.bincode;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return final_code;
    }
}