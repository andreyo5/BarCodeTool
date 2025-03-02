package QRCode;

import java.awt.image.*;
import java.io.File;

import javax.imageio.ImageIO;

public class QRCodeCoder {

//  ------------------------------------   
// 
//  https://habr.com/ru/articles/172525/
//  https://habr.com/ru/articles/172525/
//  https://habr.com/ru/articles/172525/
// 
//  ------------------------------------

    private QRCodeLibrary qr = new QRCodeLibrary();

    public String last_barcode;
    public String bincode;

    static int width_last_barcode;
    static int height_last_barcode;
    

    public String IncodeToQR(String input_text){
        String result = "";
        String bit_sequence = "";// последовательность из бит введённой информации
        String amount_of_data_field = "";//длина поля количества данных тип+длина
        int length=0;// длина введённой информации
    

        // Определение типа
        String type="";
        try {//0001 - цифровой тип(только цифры);
            int input = Integer.parseInt(input_text);
            type = "0001";


        } catch (Exception e) {// 0010-Буквенно-Цифровой(цифры и буквы);
            type = "0010";
        }

        //                      Версия 1–9	    Версия 10–26	Версия 27–40
        // Цифровое	            10 бит	        12 бит	        14 бит
        // Буквенно-цифровое	9 бит	        11 бит	        13 бит
        // Побайтовое	        8 бит	        16 бит	        16 бит

        if(type=="0001"){ // цифра
            length = input_text.length();
            bit_sequence = NumericCoding(input_text);

            if(qr.getVersion(length)>=1 & qr.getVersion(length)<=9){
                String field=dec_to_bin(String.valueOf(length));
                while(field.length()!=10){
                    field="0"+field;
                }
                amount_of_data_field=type+field;

            }else{
                if(qr.getVersion(length)>=10 & qr.getVersion(length)<=26){

                    String field=dec_to_bin(String.valueOf(length));
                    while(field.length()!=12){
                        field="0"+field;
                    }
                    amount_of_data_field=type+field;

                }else{
                    if(qr.getVersion(length)>=27 & qr.getVersion(length)<=40){
                        
                        String field=dec_to_bin(String.valueOf(length));
                        while(field.length()!=14){
                            field="0"+field;
                        }
                        amount_of_data_field=type+field;

                    }
    
                }
            }

        }else{
            if(type=="0010"){ // буква
                length = input_text.length();
                bit_sequence = LetterNumericCoding(input_text);
    
                if(qr.getVersion(length)>=1 & qr.getVersion(length)<=9){
                    String field=dec_to_bin(String.valueOf(length));
                    while(field.length()!=9){
                        field="0"+field;
                    }
                    amount_of_data_field=type+field;
    
                }else{
                    if(qr.getVersion(length)>=10 & qr.getVersion(length)<=26){
    
                        String field=dec_to_bin(String.valueOf(length));
                        while(field.length()!=11){
                            field="0"+field;
                        }
                        amount_of_data_field=type+field;
    
                    }else{
                        if(qr.getVersion(length)>=27 & qr.getVersion(length)<=40){
                            
                            String field=dec_to_bin(String.valueOf(length));
                            while(field.length()!=13){
                                field="0"+field;
                            }
                            amount_of_data_field=type+field;
    
                        }
        
                    }
                }
    
            }
        }
        
        bit_sequence=amount_of_data_field+bit_sequence;
        System.out.println("lenght:"+length);
        System.out.println("version:"+qr.version);
        System.out.println("amount_of_data_field:"+amount_of_data_field);
        System.out.println("before 0++ bit_sequence:"+bit_sequence+" "+bit_sequence.length());

        while(bit_sequence.length()%8!=0){ // подстановка нулей под размер
            bit_sequence+="0";
        }

        System.out.println("after 0++ bit_sequence:"+bit_sequence+" "+bit_sequence.length());
        System.out.println("before a_temp++ bit_sequence:"+bit_sequence+" "+bit_sequence.length());

        int a_temp=1;
        do{
            if(a_temp==1)bit_sequence+="11101100";else{ // поочердное добавление "пустышек?"
                bit_sequence+="00010001";
                a_temp=0;
            }
            a_temp++;
        }while(bit_sequence.length()<qr.maxInfoAmount_M[qr.version]);
        a_temp=0;

        System.out.println("after a_temp++ bit_sequence:"+bit_sequence+" "+bit_sequence.length());

        int[] amount_of_info_in_blocks = new int[qr.blocksInfoAmount_M[qr.version]]; // длина блоков (кол-во байтов в одном блоке)
        int byte_amount = bit_sequence.length()/8; //количество байтов
        int amount_of_additional_blocks = byte_amount%qr.blocksInfoAmount_M[qr.version];  // кол-во доп блоков
        System.out.println("byte_amount:"+byte_amount);
        for(int i =qr.blocksInfoAmount_M[qr.version];i>0;i--){//сборка размеров блоков учитывай дополнительные блоки
            int a=byte_amount/qr.blocksInfoAmount_M[qr.version];
            if(amount_of_additional_blocks!=0){a++;amount_of_additional_blocks--;};
                
            amount_of_info_in_blocks[i-1]=a;
            System.out.println(i-1+"-block: "+a);
            a=0;
        }

        // String[][] byteBlocks = new String[line][colmn]

        int LINE = qr.blocksInfoAmount_M[qr.version]; // кол-во линий для массива с блоками байтов 
        int COLMN = amount_of_info_in_blocks[amount_of_info_in_blocks.length-1]; // кол-во стоблцов

        int[][] byteBlocks = new int[LINE][COLMN]; // массив с блоками  байтов

        String[] bit_blocks = new String[bit_sequence.length()/8]; // разбиение последовательности битов на отдельные комбинации битов(по 8)

        int current_bit;
        current_bit=0;

        for(int i =0;i<bit_blocks.length;i++){
            String bitline=bit_sequence.substring(current_bit,current_bit+8);
            bit_blocks[i]=bitline;
            current_bit+=8;
        }

        current_bit=0;

        for(int i =0;i<LINE;i++){      //заполнение блоков байтами
            for (int j = 0; j < COLMN; j++) {
                byteBlocks[i][j] = Integer.parseInt(bit_to_byte(bit_blocks[j+current_bit]));

            }
            current_bit+=COLMN-1;
            
        }
        
        for(int i =0;i<LINE;i++){
            System.out.println("block:"+i);
            for (int j = 0; j < COLMN; j++) {
                System.out.print(byteBlocks[i][j]+";");
            }
            System.out.println("");
            
        } // вывод


        int amount_of_bytes_of_correction = qr.bytesOfCorrection_M[qr.version]; // кол-во байтов корекции
        //int[] generating_polynomials = qr.getPolynomials_M(amount_of_bytes_of_correction); // генерирующий многочлен

        int COLMNcorrection=COLMN+amount_of_bytes_of_correction;//столбцы для блоков коррекции
        int[][] blocks_of_correction_bytes = new int[LINE][COLMNcorrection];

        for (int i = 0; i < LINE; i++) {
            blocks_of_correction_bytes[i] = qr.generateCorrectionBytes(byteBlocks[i],amount_of_bytes_of_correction);
        }
        
        for (int i = 0; i < blocks_of_correction_bytes.length; i++) {
            System.out.println("blocks_of_correction_bytes: "+i);
            for (int j : blocks_of_correction_bytes[i]) {
                System.out.print(j+";");
            }
            System.out.println("");
        }

        String byte_sequence = "";
        for (int i = 0; i < COLMN; i++) {
            for (int j = 0; j < LINE; j++) {
                byte_sequence+=byteBlocks[j][i];
                byte_sequence+=";";
            }
        }
        System.out.println("byte_sequence:"+byte_sequence);
        for (int i = 0; i < COLMNcorrection; i++) {
            for (int j = 0; j < LINE; j++) {
                try {
                    byte_sequence+=blocks_of_correction_bytes[j][i];
                    byte_sequence+=";";
                } catch (Exception e) {
                    continue;
                }
            }
        }

        System.out.println("byte_sequence"+byte_sequence);
        makeBarcode(byte_sequence);
        return byte_sequence;
    }

    private String dec_to_bin(String dec){
        int num=Integer.parseInt(dec);
        String s = "";
        while (num != 0) {
            int   rem = num % 2;
            num /= 2;
            s = rem + s;
        }
        return s;
    }

    private String bit_to_byte(String bit){
        String _byte ="";
        int a=0;
        var strbild = new StringBuilder(bit);
        bit = strbild.reverse().toString();
        for(int i = bit.length()-1;i>-1;i--){
            int b = Integer.valueOf(String.valueOf(bit.charAt(i)));
            a+= (int)Integer.valueOf(String.valueOf(bit.charAt(i)))*Math.pow(b*2, i);
        }
        _byte=String.valueOf(a);
        return _byte;
    }

    private void makeBarcode(String code){
        try {
            int width = qr.getSize(qr.version); //ширина
            int height = width; //высота
            BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

            for(int x = 0;x<width;x++){
                for(int y = 0;y<height;y++){
                    bufferedImage.setRGB(x,y,0xFFFFFF);  // заполнение холста белыми квадратами
                }
            }

            String[][] search_pattern = qr.search_pattern;
            int limiter =7;
            int next_x=0;
            int next_y=0;
            

            for (int i = 0; i < 3; i++) {
                if(i==1){
                    next_x=width-7;
                    next_y=0;
                }else{
                    if(i==2){
                        next_x=0;
                        next_y=height-7;
                    }
                }
                                                                         // добавление поисковых узоров
                for(int x = 0+next_x;x<limiter+next_x;x++){
                    for(int y = 0+next_y;y<limiter+next_y;y++){
                        if(search_pattern[x-next_x][y-next_y]=="1"){  
                            bufferedImage.setRGB(x,y,0x000000);

                        }
                    }
                }
            }

            for(int x = 8;x<width-8;x++){
                int y = 6;
                if(x%2==0)bufferedImage.setRGB(x,y,0x000000);        // добавление полос синхроницзации
                
            }
            for(int y = 8;y<width-8;y++){
                int x = 6;
                if(y%2==0)bufferedImage.setRGB(x,y,0x000000);
                
            }
            String[] pattern = qr.getVersionPattern(qr.version);  // добавление версии
            if(qr.version>=7){
                int y = height-10;
                int x = 0;
                for (int i = 0; i < pattern.length; i++) {
                    for (int j = 0; j < pattern[i].length(); j++) {
                        if(pattern[i].charAt(j)=='1'){
                            bufferedImage.setRGB(x, y, 0x000000);
                        }
                        x++;
                    }
                    x=0;
                    y++;
                }
                x=0;
                y=height-10;
                for (int i = 0; i < pattern.length; i++) {
                    for (int j = 0; j < pattern[i].length(); j++) {
                        if(pattern[i].charAt(j)=='1'){
                            bufferedImage.setRGB(y, x, 0x000000);
                        }
                        x++;
                    }
                    x=0;
                    y++;
                }
            }

            int x=width-1;
            int y=height-1;
            int collmns = x/2;
            int collumn=0;
            int firstcollumns = (qr.version>=7)?5:4;
            boolean vertical_end=false;
            String[] byte_sequnce = code.split(";");
            for (String _byte:byte_sequnce) {
                String b = dec_to_bin(_byte);
                while(b.length()!=8)b="0"+b;
                for (int i = 0; i < b.length(); i++) {
                    if (x < 0 || x >= width || y < 0 || y >= height) break;
                    if(vertical_end==false){
                        if(b.charAt(i)=='1'){bufferedImage.setRGB(x, y, 0x000000);}
                        x--;
                        if(x<=width-collumn*2-3){
                            y--;
                            if(y==6)y--;
                            if((y==8 & collumn<=firstcollumns)||(y==0)||(y==8 & collumn>=collmns-4)){
                                vertical_end=true;
                                collumn++;
                                x=width-collumn*2-1;
                                y=(collumn<=firstcollumns || collumn>=collmns-4)?9:0;
                            }
                            x=width-collumn*2-1;
                        }
                        
                    }else{
                        if(b.charAt(i)=='1'){bufferedImage.setRGB(x, y, 0x000000);}
                        x--;
                        if(x==width-collumn*2-3){
                            y++;
                            if(y==6)y++;
                            if((y==height-1)||(y==height-8 & collumn>=collmns-4)){
                                vertical_end=false;
                                collumn++;
                                x=width-collumn*2-1;
                                y=(collumn>=collmns-4)?height-9:height-1;
                            }
                            x=width-collumn*2-1;
                        }
                    }
                    //System.out.println("y:"+y+"x:"+x+"vertical_end:"+vertical_end+"collumn:"+collumn);
                }
            }

            int[] locationsOfAligmentPatterns = qr.getLocationsOfAligmentPatterns(qr.version);    // добавление узоров выранивания
            if(qr.version>1){
                for (int i = 0; i < locationsOfAligmentPatterns.length; i++) {
                
                    for (int j = 0; j < locationsOfAligmentPatterns.length; j++) {
                        if((locationsOfAligmentPatterns[i]==6 & locationsOfAligmentPatterns[j]==6)
                        ||(locationsOfAligmentPatterns[i]==6 & locationsOfAligmentPatterns[j]==width-7)
                        ||(locationsOfAligmentPatterns[i]==width-7 & locationsOfAligmentPatterns[j]==6)){
                            
                            continue;
                        }else{
                            String[][] aligment_pattern = qr.aligment_pattern;
                            for (int k = 0; k < 5; k++) {
                                for (int h = 0; h < 5; h++) {
                                    if(aligment_pattern[k][h]=="1"){
                                        bufferedImage.setRGB(locationsOfAligmentPatterns[i]+k-2,locationsOfAligmentPatterns[j]+h-2,0x000000);
                                    }else{if(aligment_pattern[k][h]=="0"){
                                        bufferedImage.setRGB(locationsOfAligmentPatterns[i]+k-2,locationsOfAligmentPatterns[j]+h-2,0xffffff);
                                    }}
    
                                }
                                
                                
                            }
                        }
                    }
                }
            }

            String maskpattern=qr.getMask(0);
            System.out.println(maskpattern);
            
            x=0;y=8;
            for(int i =0;i<maskpattern.length();i++){
                if(maskpattern.charAt(i)=='1' & i<=maskpattern.length()/2)bufferedImage.setRGB(x+i, y, 0x000000);
                else{
                    bufferedImage.setRGB(8, y+(y-i)*-1, 0x000000);
                }
            }

            bincode = code;
            saveBarCode(bufferedImage);
           
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
    private void saveBarCode(BufferedImage bufferedImage){
        try {
            
            int countFiles = new File("img").listFiles().length;
            countFiles++;
            for(int i=0;i<countFiles;i++){
                String path="img//barcode"+i+".png";
                File file = new File(path);
                if(file.exists()==true){
                    System.out.println(path+": exists");
                    continue;
                }
                else{
                    last_barcode=path;
                    ImageIO.write(bufferedImage,"png",file);
                    file.createNewFile();
                    break;
                }
            }
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
        
    }

    private String LetterNumericCoding(String input){
        // В этом случае на 2 символа требуется 11 бит информации. Входной поток символов разделяется на группы по 2, 
        // в группе каждый символ кодируется согласно таблице QRCodeLibrary.java,
        // значение первого символа в группе умножается на 45 и прибавляется к значение второго символа.
        // Полученное число переводится в 11-битное двоичное число и добавляется к последовательности бит.
        // Если в последней группе 1 символ, то его значение сразу кодируется 6-битным числом и добавляется к последовательности бит.

        String bincode="";
        String couple="";
        
        for(int i =0;i<input.length();i+=2){
            String a1;String a2;String b="";
            if(input.length()<=i+1){
                couple+=input.charAt(i);
                a1=String.valueOf(couple.charAt(0));
                try {
                    a1=a1.toUpperCase();
                } catch (Exception e) {}
                b += dec_to_bin(String.valueOf(
                    (qr.getIndex(a1))
                ));
                while(b.length()!=6){
                    b="0"+b;
                }
                bincode+=b;
                b="";
                break;
            }
            couple+=input.charAt(i);
            couple+=input.charAt(i+1);
            a1=String.valueOf(couple.charAt(0));
            a2=String.valueOf(couple.charAt(1));
            try {
                a1=a1.toUpperCase();
            } catch (Exception e) {}
            try {
                a2=a2.toUpperCase();
            } catch (Exception e) {}
            b += dec_to_bin(String.valueOf(
                (qr.getIndex(a1)*45)+
                (qr.getIndex(a2))
            ));
            while(b.length()!=11){
                b="0"+b;
            }
            bincode+=b;
            b="";
            couple="";
            
        }
        return bincode;
    }
    
    private String NumericCoding(String input){
        // Этот тип кодирования требует 10 бит на 3 символа. 
        //
        // Вся последовательность символов разбивается на группы по 3 цифры, и каждая группа (трёхзначное число) переводится в 10-битное двоичное число 
        // и добавляется к последовательности бит. Если общее количество символов не кратно 3, то если в конце остаётся 2 символа, 
        // полученное двузначное число кодируется 7 битами, а если 1 символ, то 4 битами.
        //
        // Например, есть строка «12345678», которую надо закодировать. 
        // Мы разбиваем её на числа: 123, 456 и 78, затем переводим каждое из них в двоичный вид: 0001111011, 0111001000 и 1001110, и объединяем это в один поток: 
        // 000111101101110010001001110.

        String row="";
        String bincode="";
        int j=0;

        for(int i =0;i<input.length();i++){ // разбиение на 3-х значные числа
            if(j==3){
                row+=";";j=0;
            }
            row+=input.charAt(i);
            j++;
        }

        row+=";";
        String dec="";

        for(int i =0;i<row.length();i++){
            if(row.charAt(i)==';'){
                String a;
                if(dec.length()==2){ //если осталось 2 цифры
                    a = dec_to_bin(dec);
                    while(a.length()!=7){
                        a="0"+a;
                    }
                }else{
                    if(dec.length()==1){ //если осталось 1 цифра
                        a = dec_to_bin(dec);
                        while(a.length()!=4){
                            a="0"+a;
                        }
                    }else{
                        a = dec_to_bin(dec); // для 3 цифр
                        while(a.length()!=10){
                            a="0"+a;
                        }
                    }
                }
                
                dec="";
                
                bincode+=a;

            }else{
                dec+=row.charAt(i);
            }
        }
        return bincode;
    }
}

