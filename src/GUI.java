import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Aztec.AztecCoder;
import Code128.Code128Coder;
import Code128.Code128Decoder;
import QRCode.QRCodeBarCodeMaker;
import QRCode.QRCodeCoder;
//import QRCode.QRCodeDecoder;
// import Aztec.AztecCoder;
// import Aztec.AztecDecoder;
import QRCode.QRCodeDecoder;

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

    //qr
    private static int QRCODE_masktype; //qr mask
    private static JTextField QRCODE_masktype_textfield; // qr mask field


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
            Image image = bimg.getScaledInstance(bimg.getWidth()+150,bimg.getHeight()+150,Image.SCALE_DEFAULT);
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
                        GUI.Barcode_View = BarCode_Viewer_Panel(DB.types[j],DB.imBuff[j],DB.codes[j]);
                        Validator();
                    }
                });
                JButton deleteBtn = new JButton("Удалить");
                deleteBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            DB.EraseDB(String.valueOf(DB.types[j]),DB.codes[j]);
                        } catch (SQLException e1) {e1.getLocalizedMessage();}
                        GUI.BarCode_List_Panel = List_Of_BarCodes();
                        Validator();
                    }
                });

                row.setLayout(new GridLayout(1,5, 15, 15));

                String a = "";
                switch (DB.types[i]) {
                    case 1:
                        a = "Code128";
                        break;
                    case 2:
                        a="QR-Code";
                        break;
                    case 3:
                        a="Aztec";
                        break;
                    default:
                        a="Undefined";
                        break;
                }
                row.add(new JLabel(a));
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
            JLabel BarCodeIMG_Label = new JLabel(setImage(path,zoom));
            JLabel Text_Label = new JLabel(text);
            Text_Label.setFont(new Font("TimesRoman",Font.ITALIC, 22));

            JPanel MainPanel = new JPanel();
            MainPanel.setLayout(new BorderLayout());
            MainPanel.add(BarCodeIMG_Label,BorderLayout.CENTER);
            MainPanel.add(Text_Label,BorderLayout.SOUTH);

            return MainPanel; 
        }   
    }
    private JPanel BarCode_Viewer_Panel(int type,BufferedImage img,String text){

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
        JPanel selectedBarCodePanel = new JPanel();
        JLabel selectedBarCodeLabel = new JLabel("Сейчас выбран: "+selectedBarCode);
        QRCODE_masktype_textfield = new JTextField("Введите маску");
        
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
                  selectedBarCode = list1.getSelectedValue().toString();
                  selectedBarCodeLabel.setText("Сейчас выбран: "+selectedBarCode);
                }
                if(selectedBarCode=="QR-CODE"){
                    selectedBarCodePanel.removeAll();
                    selectedBarCodePanel.add(selectedBarCodeLabel);
                    selectedBarCodePanel.add(QRCODE_masktype_textfield);
                }else{
                    selectedBarCodePanel.removeAll();
                    selectedBarCodePanel.add(selectedBarCodeLabel);
                }
                

            }
        });

        btnIncode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input_text = textField.getText();
                int type=0;
                switch (selectedBarCode) {
                    case "code128":
                        Code128Coder.code128(input_text);

                        GUI.path = Code128Coder.last_barcode;
                        GUI.bincode = Code128Coder.bincode;
                        GUI.text = input_text;
                        type=1;

                        try {
                            DataBase DB = new DataBase();
                            DB.WriteCodeAndPathToDB(type,bincode, input_text, path);
                        } catch (SQLException|IOException e1) {
                            e1.printStackTrace();
                        }
                        GUI.BarCode_List_Panel = List_Of_BarCodes();
                        GUI.Barcode_View = BarCode_Viewer_Panel(path,text);
                        Validator();
                        
                        break;
                
                    case "QR-CODE":
                        QRCodeCoder qr = new QRCodeCoder();
                        try {
                            QRCODE_masktype = Integer.parseInt(QRCODE_masktype_textfield.getText());
                        } catch (Exception e1) {
                            QRCODE_masktype = 2;
                            System.err.println(e1.getLocalizedMessage());
                        }
                        GUI.bincode = qr.IncodeToQR(input_text,QRCODE_masktype);
                        GUI.path = qr.last_barcode;
                        GUI.text = input_text;
                        type=2;

                        try {
                            DataBase DB = new DataBase();
                            DB.WriteCodeAndPathToDB(type,bincode, input_text, path);
                        } catch (SQLException|IOException e1) {
                            e1.printStackTrace();
                        }
                        
                        System.out.println(bincode);

                        GUI.BarCode_List_Panel = List_Of_BarCodes();
                        GUI.Barcode_View = BarCode_Viewer_Panel(path,text);
                        Validator();

                        break;

                    case "Aztec":
                        AztecCoder Aztec = new AztecCoder();
                        GUI.bincode = Aztec.encode(input_text);
                        GUI.path = "0";
                        GUI.text = input_text;
                        type=3;

                        try {
                            DataBase DB = new DataBase();
                            DB.WriteCodeAndPathToDB(type,bincode, input_text, path);
                        } catch (SQLException|IOException e1) {
                            e1.printStackTrace();
                        }
                        
                        System.out.println(bincode);

                        GUI.BarCode_List_Panel = List_Of_BarCodes();
                        GUI.Barcode_View = BarCode_Viewer_Panel(path,text);
                        Validator();
                        break;
                }
            }
            
        });

        btnDecode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePathField="";
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(new JFrame());
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField = selectedFile.getAbsolutePath();
                }

                if (filePathField.isEmpty()) {
                    JOptionPane.showMessageDialog(new JFrame(), "Пожалуйста, выберите файл.");
                    return;
                }
                try {
                    DataBase db = new DataBase();
                    String result="";
                    String result_bincode="";
                    QRCodeDecoder qr = new QRCodeDecoder();
                    switch (selectedBarCode) {
                        case "Aztec":
                            
                            break;

                        case "code128":
                            result = Code128Decoder.decodeBarcode(filePathField);
                            result_bincode = Code128Decoder.finishedbincode;
                        break;

                        case "QR-CODE":
                            result = qr.decodeBarcode(filePathField); // text
                            result_bincode = qr.finishedbincode; // bincode
                        break;
                    
                        default:
                            break;
                    }
                    

                    db.WriteCodeAndPathToDB(1,result_bincode,result,filePathField);

                    GUI.Barcode_View = BarCode_Viewer_Panel(filePathField,result);
                    GUI.BarCode_List_Panel = List_Of_BarCodes();
                    Validator();

                } catch (IOException | SQLException ex) {
                    ex.getLocalizedMessage();
                    JOptionPane.showMessageDialog(new JFrame(), "Ошибка при чтении файла.");
                }
            }
            
        });

        // Добавление контента в панель
        JPanel IncodePanel = new JPanel();

        IncodePanel.setLayout(new BorderLayout());
        IncodePanel.add(list_scroller,BorderLayout.WEST);
        IncodePanel.add(textField,BorderLayout.CENTER);
        IncodePanel.add(selectedBarCodePanel,BorderLayout.NORTH);

        JPanel ButtonsPanel = new JPanel();
        ButtonsPanel.setLayout(new BorderLayout());
        ButtonsPanel.add(btnIncode,BorderLayout.LINE_START);
        ButtonsPanel.add(btnDecode,BorderLayout.LINE_END);

        IncodePanel.add(ButtonsPanel,BorderLayout.SOUTH);

        return IncodePanel;
    }
}
