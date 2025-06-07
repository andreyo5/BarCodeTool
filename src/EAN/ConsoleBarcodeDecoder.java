package EAN;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

public class ConsoleBarcodeDecoder {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== EAN-13 Decoder ===");
        System.out.println("Supported formats: PNG, JPG, GIF, BMP");
        System.out.print("Enter image path: ");
        String path = scanner.nextLine().trim();
        
        try {
            System.out.println("Loading image...");
            BufferedImage image = ImageIO.read(new File(path));
            if (image == null) {
                System.err.println("Unsupported image format");
                waitForExit(scanner);
                return;
            }
            
            System.out.println("Processing image...");
            String binaryString = BarcodeImageProcessor.processImage(image);
            
            System.out.println("Decoding barcode...");
            String ean13 = EAN13Decoder.decode(binaryString);
            
            printDecodingResult(ean13, path);
            
        } catch (EAN13Decoder.InvalidBarcodeException e) {
            System.err.println("Barcode decoding error: " + e.getMessage());
            System.err.println("Possible reasons:");
            System.err.println("- No barcode found in image");
            System.err.println("- Poor image quality");
            System.err.println("- Barcode type not supported");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            waitForExit(scanner);
        }
    }
    
    private static void printDecodingResult(String ean13, String path) {
        System.out.println("\n=== Decoding Result ===");
        System.out.println("Image: " + path);
        System.out.println("EAN-13: " + ean13);
        System.out.println("\nStructure Analysis:");
        System.out.println("First digit (Number system): " + ean13.charAt(0));
        System.out.println("Manufacturer code: " + ean13.substring(1, 7));
        System.out.println("Product code: " + ean13.substring(7, 12));
        System.out.println("Check digit: " + ean13.charAt(12));
    }
    
    private static void waitForExit(Scanner scanner) {
        System.out.print("\nPress Enter to exit...");
        scanner.nextLine();
    }
}