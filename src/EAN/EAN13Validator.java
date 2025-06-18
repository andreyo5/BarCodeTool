package EAN;
public class EAN13Validator {
    public static boolean validate(String ean13) {
        if (ean13 == null || !ean13.matches("\\d{13}")) {
            return false;
        }
        
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = ean13.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit * 1 : digit * 3;
        }
        
        int checksum = (10 - (sum % 10)) % 10;
        return checksum == (ean13.charAt(12) - '0');
    }
}
