package EAN;

import java.util.HashMap;
import java.util.Map;

public class EAN13Decoder {
    private static final Map<String, Integer> LEFT_A_PATTERNS = createLeftPatternsA();
    private static final Map<String, Integer> LEFT_B_PATTERNS = createLeftPatternsB();
    private static final Map<String, Integer> RIGHT_PATTERNS = createRightPatterns();
    
    private static Map<String, Integer> createLeftPatternsA() {
        Map<String, Integer> patterns = new HashMap<>();
        patterns.put("0001101", 0);
        patterns.put("0011001", 1);
        patterns.put("0010011", 2);
        patterns.put("0111101", 3);
        patterns.put("0100011", 4);
        patterns.put("0110001", 5);
        patterns.put("0101111", 6);
        patterns.put("0111011", 7);
        patterns.put("0110111", 8);
        patterns.put("0001011", 9);
        return patterns;
    }

    private static Map<String, Integer> createLeftPatternsB() {
        Map<String, Integer> patterns = new HashMap<>();
        patterns.put("0100111", 0);
        patterns.put("0110011", 1);
        patterns.put("0011011", 2);
        patterns.put("0100001", 3);
        patterns.put("0011101", 4);
        patterns.put("0111001", 5);
        patterns.put("0000101", 6);
        patterns.put("0010001", 7);
        patterns.put("0001001", 8);
        patterns.put("0010111", 9);
        return patterns;
    }

    private static Map<String, Integer> createRightPatterns() {
        Map<String, Integer> patterns = new HashMap<>();
        patterns.put("1110010", 0);
        patterns.put("1100110", 1);
        patterns.put("1101100", 2);
        patterns.put("1000010", 3);
        patterns.put("1011100", 4);
        patterns.put("1001110", 5);
        patterns.put("1010000", 6);
        patterns.put("1000100", 7);
        patterns.put("1001000", 8);
        patterns.put("1110100", 9);
        return patterns;
    }

    public static String decode(String barcode) throws InvalidBarcodeException {
        if (barcode == null || barcode.length() < 95) {
            throw new InvalidBarcodeException("Invalid barcode length");
        }

        String cleanBarcode = extractCleanBarcode(barcode);
        DecodingResult result = decodeBarcodeStructure(cleanBarcode);
        
        if (!validateChecksum(result.getEan13())) {
            throw new InvalidBarcodeException("Checksum validation failed");
        }
        
        return result.getEan13();
    }

    private static String extractCleanBarcode(String barcode) throws InvalidBarcodeException {
        int start = findPatternWithTolerance(barcode, "101", 0);
        int middle = findPatternWithTolerance(barcode, "01010", start + 3);
        int end = findPatternWithTolerance(barcode, "101", middle + 5);
        
        if (start == -1 || middle == -1 || end == -1) {
            throw new InvalidBarcodeException("Barcode guard patterns not found");
        }
        
        return barcode.substring(start, end + 3);
    }

    private static int findPatternWithTolerance(String str, String pattern, int fromIndex) {
        int tolerance = 2;
        for (int i = fromIndex; i <= str.length() - pattern.length(); i++) {
            int errors = 0;
            for (int j = 0; j < pattern.length(); j++) {
                if (str.charAt(i + j) != pattern.charAt(j)) {
                    if (++errors > tolerance) break;
                }
            }
            if (errors <= tolerance) return i;
        }
        return -1;
    }

    private static DecodingResult decodeBarcodeStructure(String cleanBarcode) throws InvalidBarcodeException {
        String leftPart = cleanBarcode.substring(3, 45);
        String rightPart = cleanBarcode.substring(50, 92);
        
        StringBuilder ean13 = new StringBuilder();
        int[] leftDigits = new int[6];
        int firstDigit = determineEncodingScheme(leftPart, leftDigits);
        
        ean13.append(firstDigit);
        for (int digit : leftDigits) ean13.append(digit);
        
        for (int i = 0; i < 6; i++) {
            String pattern = rightPart.substring(i * 7, (i + 1) * 7);
            ean13.append(findDigitWithTolerance(pattern, RIGHT_PATTERNS));
        }
        
        return new DecodingResult(ean13.toString(), firstDigit);
    }

    private static int determineEncodingScheme(String leftPart, int[] leftDigits) throws InvalidBarcodeException {
        for (int scheme = 0; scheme < 10; scheme++) {
            try {
                for (int i = 0; i < 6; i++) {
                    String pattern = leftPart.substring(i * 7, (i + 1) * 7);
                    leftDigits[i] = (scheme == 0 || (scheme <= 3 && i == 0)) 
                        ? findDigitWithTolerance(pattern, LEFT_A_PATTERNS)
                        : findDigitWithTolerance(pattern, LEFT_B_PATTERNS);
                }
                return scheme;
            } catch (InvalidBarcodeException e) {
                continue;
            }
        }
        throw new InvalidBarcodeException("Cannot determine encoding scheme");
    }

    private static int findDigitWithTolerance(String pattern, Map<String, Integer> patterns) throws InvalidBarcodeException {
        if (patterns.containsKey(pattern)) {
            return patterns.get(pattern);
        }
        
        for (Map.Entry<String, Integer> entry : patterns.entrySet()) {
            if (hammingDistance(pattern, entry.getKey()) <= 1) {
                return entry.getValue();
            }
        }
        
        throw new InvalidBarcodeException("Unknown pattern: " + pattern);
    }

    private static int hammingDistance(String s1, String s2) {
        int distance = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) != s2.charAt(i)) distance++;
        }
        return distance;
    }

    private static boolean validateChecksum(String ean13) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(ean13.charAt(i));
            sum += (i % 2 == 0) ? digit * 1 : digit * 3;
        }
        return (10 - (sum % 10)) % 10 == Character.getNumericValue(ean13.charAt(12));
    }

    public static class DecodingResult {
        private final String ean13;
        private final int firstDigit;
        
        public DecodingResult(String ean13, int firstDigit) {
            this.ean13 = ean13;
            this.firstDigit = firstDigit;
        }
        
        public String getEan13() { return ean13; }
        public int getFirstDigit() { return firstDigit; }
    }

    public static class InvalidBarcodeException extends Exception {
        public InvalidBarcodeException(String message) {
            super(message);
        }
    }
}