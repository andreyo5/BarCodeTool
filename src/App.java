import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class App{
    static int WIDTH = 1280; // ширина x
    static int HEIGHT = 720; // высота y
    static JFrame frame;

    @SuppressWarnings("static-access")
    public static JFrame createFrame(){
        frame = new JFrame("Штрих-код Мастер");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(WIDTH,HEIGHT);
        frame.setResizable(true);
        frame.setDefaultLookAndFeelDecorated(false);
        return frame;
    } 

    private static void SomePreparations(){
        String curdir = System.getProperty("user.dir");
        Path path = Paths.get(curdir+"\\img");

        if (!Files.exists(path)) {
            
            new File(curdir+"\\img").mkdir();
        }
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:BarCodeDB.db");
            try {
                    PreparedStatement pstmt = conn.prepareStatement("select * from BarCodes");
                    pstmt.executeQuery();
                } catch (Exception e) {
                    String SQL = "CREATE TABLE BarCodes (bincode TEXT NOT NULL UNIQUE,type INTEGER NOT NULL,text TEXT NOT NULL, blobcode BLOB NOT NULL, PRIMARY KEY(bincode))";
                    PreparedStatement pstmt = conn.prepareStatement(SQL);  
                    pstmt.executeUpdate();

                    pstmt.close();
                }
            conn.close();
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
    }

    public static void main(String[] args) {
        SomePreparations();
        JFrame frame = createFrame();
        GUI GUIManager = new GUI();
        frame.getContentPane().add(GUI.MainPanel);
        frame.setVisible(true);

    }
}
