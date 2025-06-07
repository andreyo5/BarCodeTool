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
        frame.setResizable(false);
        frame.setDefaultLookAndFeelDecorated(false);
        return frame;
    } 

    public static void main(String[] args) {
        JFrame frame = createFrame();
        GUI GUIManager = new GUI();
        frame.getContentPane().add(GUI.MainPanel);
        frame.setVisible(true);
        
    }
}
