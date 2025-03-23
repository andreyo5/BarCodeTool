package Aztec;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// AztecDecoder.java
public class AztecDecoder {
    public String decode(String binaryInput) {
        StringBuilder decoded = new StringBuilder();
        String[] binaryChars = binaryInput.split(" ");
        for (String binary : binaryChars) {
            decoded.append((char) Integer.parseInt(binary, 2));
        }
        return decoded.toString();
    }

    public String decodeFromMatrix(boolean[][] matrix) {
        List<String> binaryList = new ArrayList<>();
        for (int i = 2; i < matrix.length - 2; i++) {
            for (int j = 2; j < matrix[i].length - 2; j++) {
                binaryList.add(matrix[i][j] ? "1" : "0");
            }
        }
        String binaryData = String.join("", binaryList);
        List<String> binaryChars = new ArrayList<>();
        for (int i = 0; i < binaryData.length(); i += 8) {
            if (i + 8 <= binaryData.length()) {
                binaryChars.add(binaryData.substring(i, i + 8));
            }
        }
        return decode(String.join(" ", binaryChars));
    }

    public static void main(String[] args) {
        AztecDecoder encoder = new AztecDecoder();
        AztecDecoder decoder = new AztecDecoder();
        
        String text = "Hello, Aztec!";
        String decodedBinary = encoder.decode(text);
        String decodedText = decoder.decode(decodedBinary);
        
        System.out.println("Encoded Binary: " + decodedBinary);
        System.out.println("Decoded Text: " + decodedText);
    }
}

