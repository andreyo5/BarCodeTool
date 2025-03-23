package Aztec;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

// AztecEncoder.java
public class AztecCoder {
    public String encode(String input) {
        StringBuilder encoded = new StringBuilder();
        for (char c : input.toCharArray()) {
            encoded.append(Integer.toBinaryString(c)).append(" ");
        }
        return encoded.toString().trim();
    }

    public boolean[][] encodeToMatrix(String input) {
        int size = (int) Math.ceil(Math.sqrt(input.length() * 8)) + 4; // Определяем размер матрицы
        boolean[][] matrix = new boolean[size][size];
        
        String binaryData = encode(input).replace(" ", "");
        int index = 0;
        for (int i = 2; i < size - 2 && index < binaryData.length(); i++) {
            for (int j = 2; j < size - 2 && index < binaryData.length(); j++) {
                matrix[i][j] = binaryData.charAt(index) == '1';
                index++;
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        AztecCoder encoder = new AztecCoder();
        String text = "Hello, Aztec!";
        boolean[][] matrix = encoder.encodeToMatrix(text);
        
        SwingUtilities.invokeLater(() -> displayMatrix(matrix));
    }

    private static void displayMatrix(boolean[][] matrix) {
        int cellSize = 10;
        int size = matrix.length * cellSize;
        
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLACK);
        
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j]) {
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                }
            }
        }
        g.dispose();
        
        ImageIcon icon = new ImageIcon(image);
        JLabel label = new JLabel(icon);
        JFrame frame = new JFrame("Aztec Code");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(label);
        frame.pack();
        frame.setVisible(true);
    }
}
