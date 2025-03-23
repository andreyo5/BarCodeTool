package QRCode;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class QRCodeBarCodeMaker {
    QRCodeLibrary qr = new QRCodeLibrary();

    public String bincode;
    public String last_barcode;
    
    public void makeBarcode(String code,int version,int QRCODE_masktype){
        try {
            int width = qr.getSize(version); //ширина
            int height = width; //высота
            //if(version>1){width+=7;height+=7;}
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
            String[] pattern = qr.getVersionPattern(version);  // добавление версии
            if(version>=7){
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

            int maskVersion = QRCODE_masktype;
            int x=width-1;
            int y=height-1;
            int collmns = x/2;
            int collumn=0;
            int firstcollumns = (version>=7)?4:3;
            boolean vertical_up=true;
            String[] byte_sequnce = code.split(";");
            int d=0;boolean f=false;
            int sl=0; // synch line
            while (true) {
                if(f){break;}
                String _byte;
                if(d>=byte_sequnce.length){
                    _byte="0";
                }else{
                    _byte = byte_sequnce[d];
                }
                String b = dec_to_bin(_byte);
                while(b.length()!=8)b="0"+b;
                System.out.println(d+" "+b);
                for (int i = 0; i < b.length(); i++) {
                    
                    if (x < 0 || x > width || y < 0 || y > height){ f=true;break;}
                    if(x==6){
                        sl=1;
                        x=5;
                    }

                    if(b.charAt(i)=='1'){
                        int rgb = (qr.setMask(maskVersion, x, y)==0)?0xffffff:0x000000;
                        bufferedImage.setRGB(x, y, rgb);
                    }else{
                        if(b.charAt(i)=='0'){
                            int rgb = (qr.setMask(maskVersion, x, y)==0)?0x000000:0xffffff;
                            bufferedImage.setRGB(x, y, rgb);
                        }
                    }
                    if(vertical_up){
                        x--;
                        if(x<=width-collumn*2-3-sl){ // вверх
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
                        
                    }else{
                        x--;
                        if(x==width-collumn*2-3-sl){ // вниз
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
                d++;
            }

            int[] locationsOfAligmentPatterns = qr.getLocationsOfAligmentPatterns(version);    // добавление узоров выранивания
            if(version>=2){
                int p=-2;
                System.out.println(locationsOfAligmentPatterns.length/2);
                int[] modules = {locationsOfAligmentPatterns[0],locationsOfAligmentPatterns[locationsOfAligmentPatterns.length-1]};
                for(int asd:modules){
                    System.out.println("---------------------------------------------------"+asd+"---------------------------------------------------");
                }
                for (int i = 0; i < locationsOfAligmentPatterns.length; i++) {
                
                    for (int j = 0; j < locationsOfAligmentPatterns.length; j++) {

                        if(version>=7)if((locationsOfAligmentPatterns[i]==modules[0] & locationsOfAligmentPatterns[j]==modules[0]) ||
                        (locationsOfAligmentPatterns[i]==modules[0] & locationsOfAligmentPatterns[j]==modules[1]) ||
                        (locationsOfAligmentPatterns[i]==modules[1] & locationsOfAligmentPatterns[j]==modules[0])
                        ){
                            continue;
                        }   

                        String[][] aligment_pattern = qr.aligment_pattern;
                            for (int k = 0; k < 5; k++) {
                                for (int h = 0; h < 5; h++) {
                                    if(aligment_pattern[k][h]=="1"){
                                        bufferedImage.setRGB(locationsOfAligmentPatterns[i]+k+p,locationsOfAligmentPatterns[j]+h+p,0x000000);
                                    }else{if(aligment_pattern[k][h]=="0"){
                                        bufferedImage.setRGB(locationsOfAligmentPatterns[i]+k+p,locationsOfAligmentPatterns[j]+h+p,0xffffff);
                                    }}
    
                                }
                            }
                    }
                }
            }

            
            String maskpattern=qr.getMask(maskVersion);
            System.out.println(maskpattern);
            
            x=0;y=8;
            boolean horizontal = true;
            for (int i = 0; i < maskpattern.length();i++) {
                if((bufferedImage.getRGB(x, y)&0xffffff)==0x000000){
                    if(horizontal){
                       x++; 
                    }else{
                        y--;
                    }
                    i--;
                }else{
                    if(maskpattern.charAt(i)=='1')bufferedImage.setRGB(x, y, 0x000000);
                    if(horizontal){
                        x++;
                    }else{
                        y--;
                    }
                }
                if(x==8 & y==8){
                    horizontal=false;
                }
            }

            x=8;
            y=height-1;
            horizontal = false;
            for (int i = 0; i < maskpattern.length();i++) {
                if((bufferedImage.getRGB(x, y)&0xffffff)==0x000000){
                    if(horizontal){
                       x++; 
                    }else{
                        y--;
                    }
                    i--;
                }else{
                    if(maskpattern.charAt(i)=='1')bufferedImage.setRGB(x, y, 0x000000);
                    if(horizontal){
                        x++;
                    }else{
                        y--;
                    }
                }
                if(x==8 & y==height-8){
                    bufferedImage.setRGB(x, y, 0x000000);
                    x=width-8;y=8;

                    horizontal=true;
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

    private String dec_to_bin(String dec){
        int num=Integer.parseInt(dec);
        String s = "";
        while (num != 0) {
            int rem = num % 2;
            num /= 2;
            s = rem + s;
        }
        return s;
    }
    
}
