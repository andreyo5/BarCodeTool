package QRCode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class QRCodeDecoder {
    public String finishedbincode;
    public int version;
    public int maskVersion;
    QRCodeLibrary qr = new QRCodeLibrary();


    private int getVersion(int height){
        return (height-17)/4;
    }
    private int getMask(int[][] minimazedqrcode,int height){
        int x=0; int y=8;
        int width = height;
        int mask_len=15;
        String mask_left_head="",mask_left_bottom_to_right_head="";
    
        for(int i =0;i<mask_len+3;i++){ // левый верхний угол
            if(i<8){
                mask_left_head+=String.valueOf(minimazedqrcode[y][x]);
                x++;
            }
            if(i>8){
                mask_left_head+=String.valueOf(minimazedqrcode[y][x]);
                y--;
            }
        }
        System.out.println("mask_left_head:"+mask_left_head);
        x=8;y=height-1;
        for(int i =0;i<mask_len+1;i++){ // левый верхний угол
            if(i<8){
                mask_left_bottom_to_right_head+=String.valueOf(minimazedqrcode[y][x]);
                y--;
            }
            if(i==7){
                x=width-8;y=8;
            }
            if(i>7){
                mask_left_bottom_to_right_head+=String.valueOf(minimazedqrcode[y][x]);
                x++;
            }
        }
        System.out.println("mask_left_bottom_to_right_head:"+mask_left_bottom_to_right_head);

        StringBuilder sb1 = new StringBuilder(mask_left_head);
        sb1.deleteCharAt(6);
        sb1.deleteCharAt(9);
        mask_left_head = sb1.toString();

        StringBuilder sb2 = new StringBuilder(mask_left_bottom_to_right_head);
        sb2.deleteCharAt(7);
        mask_left_bottom_to_right_head=sb2.toString();

        if(!mask_left_head.equals(mask_left_bottom_to_right_head)){
            System.err.println("!!! МАСКИ РАЗЛИЧАЮТСЯ! ТРЕБУЕТСЯ ПРОВЕРКА! БУДЕТ ИСПОЛЬЗОВАНА ЛЕВАЯ ВЕРХНЯЯ МАСКА! !!!");
        }

        int mask=0;
        for(int i = 0;i<8;i++){
            if(mask_left_head.equals(qr.mask_M[i][1])){
                mask=Integer.parseInt(qr.mask_M[i][0]);
            }
        }
        // System.out.println("mask_left_head:"+mask_left_head);
        // System.out.println("mask_left_bottom_to_right_head:"+mask_left_bottom_to_right_head);
        System.out.println("mask:"+mask);

        return mask;

    }

    private int[][] getMinimizedMatrix(BufferedImage bufferedImage){
        int x=0;
        int rgb=0;
        //(rgb&0xffffff) == 0 ?"1":"0";
        while((rgb&0xffffff)==0){
            rgb=bufferedImage.getRGB(x, 0);
            x++;
        }
        int module_len =x/7;
        double value = (double)bufferedImage.getWidth()/(double)module_len;System.out.println(value);
        double result = Math.ceil(value);
        int module_amount_in_row = (int)result;
        int[][] matrix = new int[module_amount_in_row][module_amount_in_row];

        System.out.println("module_len:"+module_len);
        System.out.println("module_amount_in_row:"+module_amount_in_row);
        System.out.println("bufferedImage.getWidth():"+bufferedImage.getWidth());

        for(int i =0;i<module_amount_in_row;i++){
            for (int j = 0; j < module_amount_in_row; j++) {
                try {
                    matrix[i][j]=(bufferedImage.getRGB(j*module_len, i*module_len)&0xffffff)==0?1:0;
                } catch (Exception e) {
                    int h,k;
                    h=j*module_len-(j*module_len-bufferedImage.getWidth());
                    k=i*module_len-(i*module_len-bufferedImage.getWidth());
                    matrix[i][j]=(bufferedImage.getRGB(h,k)&0xffffff)==0?1:0;
                }
            }
        }

        for(int i =0;i<module_amount_in_row;i++){
            for (int j = 0; j < module_amount_in_row; j++) {
                System.out.print(matrix[i][j]+" ");
            }
            System.out.println();
        }


        return matrix;
    }

    private int getByte_sequence_lenght(int version,int height){
        int a = (height*2) - 1 + (8*8*2) + 7*7;
        if(version >=7) a+=6*3*2;
        return (21*21 - a)/8;
    }

    private boolean CheckIncodetype(String a){
        String b = "";
        for(int i = 0;i<4;i++)b+=a.charAt(i);

        return b.equals("0100");
    }

    private String getBitSequence(String[] byte_sequence){ // получаем чисто последовательность битов чистой информации без служебной(версия и размер)

        int info_amount_bytes = qr.maxInfoAmount_M[version]/8; // общее кол-во информации

        int amount_blocks_of_info = qr.blocksInfoAmount_M[version]; // количество блоков
        int amount_of_info_in_block = info_amount_bytes/amount_blocks_of_info; // количество информации в блоке
        int amount_of_additional_blocks = info_amount_bytes%amount_blocks_of_info; // количество дополняемых блоков

        int[] max_amount_of_info_in_blocks=new int[amount_blocks_of_info]; // максимум информации в блоке


        for(int i = max_amount_of_info_in_blocks.length-1;i>-1;i--){
            int a = amount_of_info_in_block+amount_of_additional_blocks;
            if(amount_of_additional_blocks>0)amount_of_additional_blocks--;
            max_amount_of_info_in_blocks[i]=a;
        }

        String[][] byte_blocks = new String[amount_blocks_of_info][];

        for(int i = 0; i < amount_blocks_of_info; i++){
            byte_blocks[i]=new String[max_amount_of_info_in_blocks[i]];
        }

        for (int i = 0; i < byte_blocks.length; i++) {
            int k = i;
            for (int j = 0; j < max_amount_of_info_in_blocks[i]; j++) {
                byte_blocks[i][j]=byte_sequence[k];
                k+=amount_blocks_of_info;
            }
        }

        System.out.println("amount_blocks_of_info:"+amount_blocks_of_info);
        System.out.println("amount_of_additional_blocks:"+amount_of_additional_blocks);
        for(int a:max_amount_of_info_in_blocks){
            System.out.println("max_amount_of_info_in_blocks:"+a);
        }
        for (int i = 0; i < byte_blocks.length; i++) {
            for (int j = 0; j < byte_blocks[i].length; j++) {
                System.out.println("byte_blocks:"+byte_blocks[i][j]);
            }
        }

        String bitsequence = "";
        for(String[] a:byte_blocks){
            for(String b:a){
                bitsequence+=b;
            }
        }

        String info_amount ="";
        if(version >=1 & version <=9){
            info_amount = bitsequence.substring(4,12);
            int a = Integer.parseInt(bin_to_dec(info_amount));
            bitsequence = bitsequence.substring(12,a*8+12);
        }else{
            if(version >=10 & version <=40){
                info_amount = bitsequence.substring(4,20);
                int a = Integer.parseInt(bin_to_dec(info_amount));
                bitsequence = bitsequence.substring(20,a*8+20);
            }
        }
        System.out.println(info_amount.length());
        System.out.println(info_amount);
        

        System.out.println(bitsequence.length());
        System.out.println(bitsequence);
        return bitsequence;
    }

    private String DecodeBitSequence(String bitSequence){

        String outputString="";
        for (int i = 0; i < bitSequence.length(); i+=8) {
            String strByte="0";
            try {
                strByte = bitSequence.substring(i, i+8);

                int decimalValue = Integer.parseInt(strByte, 2);
            
                byte b = (byte)decimalValue;

                byte[] bytes = new byte[]{b};
                
                String str = new String(bytes, "UTF-8");
                char character = str.charAt(0);
            
                outputString+=character;    

            } catch (Exception e) {
            }
        }

        return outputString;
    }

    public String decodeBarcode(String path) throws IOException{
        BufferedImage bufferedImage = ImageIO.read(new File(path));

        // изображение qrcode переводится в минимальный матричный вид для удобной работы с ним
        int[][] minimazedqrcode = getMinimizedMatrix(bufferedImage);

        int width = minimazedqrcode.length; //ширина
        int height = width; //высота

        version = getVersion(width);
        maskVersion = getMask(minimazedqrcode,height);

        int x=width-1;
        int y=height-1;
        int collmns = x/2;
        int collumn=0;
        int firstcollumns = (version>=7)?4:3;
        boolean vertical_up=true;

        int[] locationsOfAligmentPatterns = qr.getLocationsOfAligmentPatterns(version);
        

        int byte_sequnce_lenght = getByte_sequence_lenght(version,height);
        String[] byte_sequence = new String[byte_sequnce_lenght];

        int d=0;boolean f=false;
        int sl=0; // synch line

        while (true) {
            if(f){break;}
            String _byte="";
            for (int i = 0; i < 8; i++) {
                
                if (x < 0 || x > width || y < 0 || y > height){ f=true;break;}
                if(x==6){
                    sl=1;
                    x=5;
                }
                if(vertical_up){// вверх
                    if(version>1){
                        for(int r = 0;r<locationsOfAligmentPatterns.length;r++){
                            int x1,x4;
                            int y1,y4;

                            x1 = locationsOfAligmentPatterns[r]-2;//1
                            y1 = locationsOfAligmentPatterns[r]-2;

                                            //  1---2
                                            //  | o |
                                            //  3---4

                            x4 = locationsOfAligmentPatterns[r]+2;//4
                            y4 = locationsOfAligmentPatterns[r]+2;

                            while(
                                ((x>=x1)&&(x<=x4))
                                &&
                                ((y>=y1)&&(y<=y4))
                            ){
                                x--;
                                if(x<=width-collumn*2-3-sl){
                                    y--;
                                    x=width-collumn*2-1-sl;
                                }
                            }
                        }
                    }
                    // _byte+=minimazedqrcode[y][x];

                    if(minimazedqrcode[y][x]==0){
                        _byte+=qr.setMask(maskVersion, x, y)==0?String.valueOf("1"):"0";
                    }else{
                        if(minimazedqrcode[y][x]==1){
                            _byte+=qr.setMask(maskVersion, x, y)==0?"0":"1";
                        }
                    }

                    x--;
                    if(x<=width-collumn*2-3-sl){ 
                        y--;
                        if(y==6)y--;
                        if((y==8 & collumn<=firstcollumns)||(y==-1)||(y==8 & collumn>=collmns-4)){
                            vertical_up=!vertical_up;
                            collumn++;
                            x=width-collumn*2-1-sl;
                            y=(collumn<=firstcollumns || collumn>=collmns-4)?9:0;
                        }
                        x=width-collumn*2-1-sl;
                    }
                    
                }else{// вниз
                    if(version>1){
                        for(int r = 0;r<locationsOfAligmentPatterns.length;r++){
                            int x1,x4;
                            int y1,y4;

                            x1 = locationsOfAligmentPatterns[r]-2;//1
                            y1 = locationsOfAligmentPatterns[r]-2;

                                            //  1---2
                                            //  | o |
                                            //  3---4

                            x4 = locationsOfAligmentPatterns[r]+2;//4
                            y4 = locationsOfAligmentPatterns[r]+2;

                            while(
                                ((x>=x1)&&(x<=x4))
                                &&
                                ((y>=y1)&&(y<=y4))
                            ){
                                x--;
                                if(x==width-collumn*2-3-sl){
                                    y++;
                                    x=width-collumn*2-1-sl;
                                }
                                    
                            }

                        }
                    }
                    //_byte+=minimazedqrcode[y][x];

                    if(minimazedqrcode[y][x]==0){
                        _byte+=qr.setMask(maskVersion, x, y)==0?String.valueOf("1"):"0";
                    }else{
                        if(minimazedqrcode[y][x]==1){
                            _byte+=qr.setMask(maskVersion, x, y)==0?"0":"1";
                        }
                    }
                    x--;
                    if(x==width-collumn*2-3-sl){ 
                        y++;
                        if(y==6)y++;
                        if((y==height)||(y==height-8 & collumn>=collmns-4)){
                            vertical_up=!vertical_up;
                            collumn++;
                            x=width-collumn*2-1-sl;
                            y=(collumn>=collmns-4)?height-9:height-1;
                        }
                        x=width-collumn*2-1-sl;
                    }
                }
                
            }

            byte_sequence[d] = _byte;
            d++;
        }

        if(!CheckIncodetype(byte_sequence[0])){
            JOptionPane.showMessageDialog(new JFrame(), "Неверный тип штрихкода");
            return new String();  
        };

        String bit_sequence = getBitSequence(byte_sequence);
        finishedbincode = bit_sequence;

        String outputText = DecodeBitSequence(bit_sequence);
        System.out.println(outputText);

        return outputText;
        
          
    }
    private String bin_to_dec(String s){
        int ans = 0, i, p = 0;
 
        // length of String
        int len = s.length();
 
        // Traversing the String
        for (i = len - 1; i >= 0; i--) {
 
            if (s.charAt(i) == '1') {
                // Calculating Decimal Number
                ans += Math.pow(2, p);
            }
            // incrementing value of p
            p++;
        }

        return Integer.toString(ans);
    }
}
