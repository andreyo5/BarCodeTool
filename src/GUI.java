import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUI {
    //метод для составления готовой картинки
    public static JSplitPane MainPanel; // главный контейнер
    private static JSplitPane RightSplitPane; // правая часть подпанель MainPanel
    private static JPanel Barcode_View; // Панель обзора штрихкода подпанель RightSplitPane
    private static JPanel functional_panel; // панель для шифровки/расшифровки кода подпанель RightSplitPane
    private static JPanel BarCode_List_Panel; // панель-обозреватель штрихкодов подпанель MainPanel
    private static String bincode; // бинарный код штрихкода где 1 это черная полоса а 0 белая
    private static String text; // зашифрованный текст
    private static String path; // путь к изображению штрихкода
    final int LeftDivider = 800;
    final int RightDivider = 400;

    public GUI(){
        System.out.println("boot");
        
        MainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true); // Левая часть
        RightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,true); // Правая часть

        BarCode_List_Panel = List_Of_BarCodes();
        Barcode_View = BarCode_Viewer_Panel(path,text);
        functional_panel = Functional_Panel();

        // Добавление контента в панель
        // Левая часть 
        MainPanel.setLeftComponent(BarCode_List_Panel);
        MainPanel.setRightComponent(RightSplitPane);
        MainPanel.setDividerLocation(LeftDivider);

        // Правая часть
        RightSplitPane.setTopComponent(Barcode_View);
        RightSplitPane.setBottomComponent(functional_panel);
        RightSplitPane.setDividerLocation(RightDivider);
    }

    private void Validator(){
        System.out.println("Validating...");
        RightSplitPane.setTopComponent(Barcode_View);
        RightSplitPane.setDividerLocation(RightSplitPane.getDividerLocation());

        MainPanel.setLeftComponent(BarCode_List_Panel);
        MainPanel.setDividerLocation(MainPanel.getDividerLocation());

        Barcode_View.revalidate();
        Barcode_View.repaint();

        BarCode_List_Panel.revalidate();
        BarCode_List_Panel.repaint();
        System.out.println("Revalidated");
    }


    public ImageIcon setImage(String path,int zoom){
        try {
            zoom = (zoom<0)?0:zoom;
            BufferedImage bufferedImage = ImageIO.read(new File(path));
            Image image = bufferedImage.getScaledInstance(bufferedImage.getWidth()+zoom,bufferedImage.getHeight()+zoom,Image.SCALE_DEFAULT);
            ImageIcon icon = new ImageIcon(image);
            return icon;
        } catch (Exception e) {
            return new ImageIcon();
        }
    }
    public static ImageIcon setImage(BufferedImage bimg){
        try {
            Image image = bimg.getScaledInstance(bimg.getWidth(),50,Image.SCALE_DEFAULT);
            ImageIcon icon = new ImageIcon(image);
            return icon;
        } catch (Exception e) {
            return new ImageIcon();
        }
    }
    
    // панель на которой отображаются все штрихкоды где:
    //текст(информация которая зашифрована);?бинарный код?;сам штрихкод(картинка); возможно будет тема где нажат
    private JPanel List_Of_BarCodes(){

        JPanel MainPanel = new JPanel();
        JPanel scrollPanel = new JPanel();
        JPanel buttonsPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(scrollPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JButton refreshButton = new JButton("Обновить данные");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI.BarCode_List_Panel = List_Of_BarCodes();
                Validator();
            }
        });

        try {
            DataBase DB = new DataBase();
            DB.ReadDB();
            scrollPanel.setLayout(new GridLayout(DB.getCount(),1, 5, 5));
            for(int i = 0;i<DB.getCount();i++){
                JPanel row = new JPanel();
                int j = i;
                JButton zoomBtn = new JButton("Увеличить");
                zoomBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        GUI.Barcode_View = BarCode_Viewer_Panel(DB.imBuff[j],DB.codes[j]);
                        Validator();
                    }
                });
                JButton deleteBtn = new JButton("Удалить");
                deleteBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            DB.EraseDB(DB.codes[j]);
                        } catch (SQLException e1) {e1.getLocalizedMessage();}
                        GUI.BarCode_List_Panel = List_Of_BarCodes();
                        Validator();
                    }
                });

                row.setLayout(new GridLayout(1,4, 15, 15));

                row.add(new JLabel(setImage(DB.imBuff[i])));
                row.add(new JLabel(DB.codes[i]));
                row.add(zoomBtn);
                row.add(deleteBtn);

                row.setBorder(BorderFactory.createLineBorder(Color.black));

                scrollPanel.add(row);
            }
        } catch (SQLException|IOException e1) {
            e1.printStackTrace();
        }

        buttonsPanel.add(refreshButton);

        MainPanel.setLayout(new BorderLayout());
        MainPanel.add(scrollPane,BorderLayout.CENTER);
        MainPanel.add(buttonsPanel,BorderLayout.SOUTH);

        return MainPanel;
    }


    // панель на которой отображается: штрих-код(картинка);текст который зашифрован в штрихкоде;
    private JPanel BarCode_Viewer_Panel(String path,String text){
        if(path == null){
            System.out.println("path:"+path);
            return new JPanel();

        }else{
            int zoom = 0;
            System.out.println("path:"+path+"\ntext:"+text);
            JLabel BarCodeIMG_Label = new JLabel(setImage(GUI.path,zoom));
            JLabel Text_Label = new JLabel(GUI.text);
            Text_Label.setFont(new Font("TimesRoman",Font.ITALIC, 22));

            JPanel MainPanel = new JPanel();
            MainPanel.setLayout(new BorderLayout());
            MainPanel.add(BarCodeIMG_Label,BorderLayout.CENTER);
            MainPanel.add(Text_Label,BorderLayout.SOUTH);

            return MainPanel; 
        }   
    }
    private JPanel BarCode_Viewer_Panel(BufferedImage img,String text){

        JLabel BarCodeIMG_Label = new JLabel(setImage(img));
        JLabel Text_Label = new JLabel(text);
        Text_Label.setFont(new Font("TimesRoman",Font.ITALIC, 22));

        JPanel MainPanel = new JPanel();
        MainPanel.setLayout(new BorderLayout());
        MainPanel.add(BarCodeIMG_Label,BorderLayout.CENTER);
        MainPanel.add(Text_Label,BorderLayout.SOUTH);

        return MainPanel; 
        
    }
    
    
    // панель на которой есть выпадающий список состоящий из видов штрихкодов, текстовое поле для ввода информации которую нужно зашифровать, кнопка зашифровать, кнопка расшифровать
    // кнопка расшифровать поменяет наполнение панели на: кнопку выбрать штрих-код(картинку с штрихкодом).
    private static DefaultListModel<String> dlm = new DefaultListModel<String>();
    private static String[] BarCode_Types = { "code128" ,"QR-CODE"  ,"Aztec"};
    private static String selectedBarCode=BarCode_Types[0];
        
    private JPanel Functional_Panel(){
        for (String string : BarCode_Types) {
            dlm.add(0, string);
        }
        
        JList<String> list1 = new JList<String>(dlm);

        JTextField textField = new JTextField("ВВЕДИТЕ ТЕКСТ ДЛЯ ШИФРОВКИ", 30);
        JButton btnIncode = new JButton("Зашифровать");
        JButton btnDecode = new JButton("Расшифровать");
        JScrollPane list_scroller = new JScrollPane(list1);
        JLabel selectedBarCodeLabel = new JLabel("Сейчас выбран: "+selectedBarCode);
        
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
                  selectedBarCode = list1.getSelectedValue().toString();
                  selectedBarCodeLabel.setText("Сейчас выбран: "+selectedBarCode);
                }
            }
        });

        btnIncode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input_text = textField.getText();
                switch (selectedBarCode) {
                    case "code128":
                        Code128Coder.code128(input_text);

                        GUI.path = Code128Coder.last_barcode;
                        GUI.bincode = Code128Coder.bincode;
                        GUI.text = input_text;

                        try {
                            DataBase DB = new DataBase();
                            DB.WriteCodeAndPathToDB(bincode, input_text, path);
                        } catch (SQLException|IOException e1) {
                            e1.printStackTrace();
                        }
                        GUI.BarCode_List_Panel = List_Of_BarCodes();
                        GUI.Barcode_View = BarCode_Viewer_Panel(path,text);
                        Validator();
                        
                        break;
                
                    case "QR-CODE":
                        // QR-code perfrom
                        break;

                    case "Aztec":
                        // Aztec code perfrom
                        break;
                }
            }
            
        });

        // Добавление контента в панель
        JPanel MainPanel = new JPanel();
        MainPanel.setLayout(new BorderLayout());
        MainPanel.add(list_scroller,BorderLayout.WEST);
        MainPanel.add(textField,BorderLayout.CENTER);
        MainPanel.add(selectedBarCodeLabel,BorderLayout.NORTH);

        JPanel ButtonsPanel = new JPanel();
        ButtonsPanel.setLayout(new BorderLayout());
        ButtonsPanel.add(btnIncode,BorderLayout.LINE_START);
        ButtonsPanel.add(btnDecode,BorderLayout.LINE_END);

        MainPanel.add(ButtonsPanel,BorderLayout.SOUTH);

        return MainPanel;
    }
}
