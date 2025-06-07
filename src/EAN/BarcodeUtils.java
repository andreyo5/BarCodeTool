package EAN;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Утилиты для работы с изображениями
 */
public class BarcodeUtils {
    /**
     * Загружает изображение из файла
     */
    public static BufferedImage loadImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        if (image == null) {
            throw new IOException("Не удалось загрузить изображение");
        }
        return image;
    }
    
    /**
     * Конвертирует изображение в оттенки серого
     */
    public static BufferedImage convertToGrayscale(BufferedImage original) {
        BufferedImage gray = new BufferedImage(
            original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        gray.getGraphics().drawImage(original, 0, 0, null);
        return gray;
    }
}