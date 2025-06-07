package EAN;

import java.awt.*;
import java.awt.image.*;
import java.util.Arrays;

public class BarcodeImageProcessor {
    public static String processImage(BufferedImage image) throws EAN13Decoder.InvalidBarcodeException {
        BufferedImage processedImage = preprocessImage(image);
        BarcodeLocation location = locateBarcode(processedImage);
        if (location == null) {
            throw new EAN13Decoder.InvalidBarcodeException("Barcode not found in image");
        }
        return extractBinaryString(processedImage, location);
    }

    private static BufferedImage preprocessImage(BufferedImage original) {
        BufferedImage grayImage = new BufferedImage(
            original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        
        BufferedImage binaryImage = new BufferedImage(
            grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        
        int[] histogram = new int[256];
        for (int y = 0; y < grayImage.getHeight(); y++) {
            for (int x = 0; x < grayImage.getWidth(); x++) {
                int gray = new Color(grayImage.getRGB(x, y)).getRed();
                histogram[gray]++;
            }
        }
        
        int threshold = calculateOtsuThreshold(histogram);
        
        for (int y = 0; y < grayImage.getHeight(); y++) {
            for (int x = 0; x < grayImage.getWidth(); x++) {
                int gray = new Color(grayImage.getRGB(x, y)).getRed();
                binaryImage.setRGB(x, y, gray < threshold ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        
        return binaryImage;
    }

    private static int calculateOtsuThreshold(int[] histogram) {
        int total = Arrays.stream(histogram).sum();
        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * histogram[i];
        
        float sumB = 0;
        int wB = 0;
        float varMax = 0;
        int threshold = 0;
        
        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            
            int wF = total - wB;
            if (wF == 0) break;
            
            sumB += i * histogram[i];
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
            float varBetween = wB * wF * (mB - mF) * (mB - mF);
            
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }
        return threshold;
    }

    private static BarcodeLocation locateBarcode(BufferedImage image) {
        int[] transitionCounts = new int[image.getHeight()];
        
        for (int y = 0; y < image.getHeight(); y++) {
            boolean prevBlack = isBlack(image, 0, y);
            for (int x = 1; x < image.getWidth(); x++) {
                boolean currentBlack = isBlack(image, x, y);
                if (currentBlack != prevBlack) {
                    transitionCounts[y]++;
                    prevBlack = currentBlack;
                }
            }
        }
        
        int bestLine = 0;
        int maxTransitions = 0;
        for (int y = 0; y < transitionCounts.length; y++) {
            if (transitionCounts[y] > maxTransitions) {
                maxTransitions = transitionCounts[y];
                bestLine = y;
            }
        }
        
        if (maxTransitions < 20) return null;
        
        int startX = findEdge(image, bestLine, true);
        int endX = findEdge(image, bestLine, false);
        
        if (endX - startX < 50) return null;
        
        return new BarcodeLocation(startX, endX, bestLine);
    }

    private static int findEdge(BufferedImage image, int y, boolean fromLeft) {
        int width = image.getWidth();
        int step = fromLeft ? 1 : -1;
        int start = fromLeft ? 0 : width - 1;
        int end = fromLeft ? width : -1;
        
        boolean foundBlack = false;
        for (int x = start; x != end; x += step) {
            if (isBlack(image, x, y)) {
                foundBlack = true;
            } else if (foundBlack) {
                return x;
            }
        }
        return fromLeft ? width - 1 : 0;
    }

    private static String extractBinaryString(BufferedImage image, BarcodeLocation location) {
        StringBuilder binary = new StringBuilder();
        int y = location.getScanLine();
        
        for (int x = location.getStartX(); x <= location.getEndX(); x++) {
            binary.append(isBlack(image, x, y) ? "1" : "0");
        }
        
        return binary.toString();
    }

    private static boolean isBlack(BufferedImage image, int x, int y) {
        if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) {
            return false;
        }
        return image.getRGB(x, y) == Color.BLACK.getRGB();
    }

    private static class BarcodeLocation {
        private final int startX;
        private final int endX;
        private final int scanLine;
        
        public BarcodeLocation(int startX, int endX, int scanLine) {
            this.startX = startX;
            this.endX = endX;
            this.scanLine = scanLine;
        }
        
        public int getStartX() { return startX; }
        public int getEndX() { return endX; }
        public int getScanLine() { return scanLine; }
    }
}