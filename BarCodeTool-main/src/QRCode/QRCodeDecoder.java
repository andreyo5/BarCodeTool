package QRCode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class QRCodeDecoder {
    public String finishedbincode;
    QRCodeLibrary qr = new QRCodeLibrary();


    private int getVersion(int height){
        return (height/4 ) - 17;
    }
    private int getMask(BufferedImage bufferedImage,int height){
        int x=0; int y=8;
        int width = height;
        int mask_len=15;
        String mask_left_head="",mask_left_bottom_to_right_head="";
    
        for(int i =0;i<mask_len+3;i++){ // левый верхний угол
            if(i<8){
                int rgb = bufferedImage.getRGB(x, y);
                String a= (rgb&0xffffff) == 0 ?"1":"0";
                mask_left_head+=a;
                x++;
            }
            if(i>8){
                int rgb = bufferedImage.getRGB(x, y);
                String a= (rgb&0xffffff) == 0 ?"1":"0";
                mask_left_head+=a;
                y--;
            }
        }
        System.out.println("mask_left_head:"+mask_left_head);
        x=8;y=height-1;
        for(int i =0;i<mask_len+1;i++){ // левый верхний угол
            if(i<8){
                int rgb = bufferedImage.getRGB(x, y);
                String a= (rgb&0xffffff) == 0 ?"1":"0";
                mask_left_bottom_to_right_head+=a;
                y--;
            }
            if(i==7){
                x=width-8;y=8;
            }
            if(i>7){
                int rgb = bufferedImage.getRGB(x, y);
                String a= (rgb&0xffffff) == 0 ?"1":"0";
                mask_left_bottom_to_right_head+=a;
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
        System.out.println("mask:"+mask);

        return mask;

    }

    public String decodeBarcode(String path) throws IOException{
        BufferedImage bufferedImage = ImageIO.read(new File(path));

        int width = bufferedImage.getWidth(); //ширина
        int height = width; //высота

        int version = getVersion(width);
        int maskVersion = getMask(bufferedImage,height);

        // int x=width-1;
        // int y=height-1;
        // int collmns = x/2;
        // int collumn=0;
        // int firstcollumns = (version>=7)?4:3;
        // boolean vertical_up=true;
        // String[] byte_sequnce = code.split(";");
        // int d=0;boolean f=false;
        // int sl=0; // synch line
        // while (true) {
        //     if(f){break;}
        //     String _byte;
        //     if(d>=byte_sequnce.length){
        //         _byte="0";
        //     }else{
        //         _byte = byte_sequnce[d];
        //     }
        //     String b = dec_to_bin(_byte);
        //     while(b.length()!=8)b="0"+b;
        //     System.out.println(d+" "+b);
        //     for (int i = 0; i < b.length(); i++) {
                
        //         if (x < 0 || x > width || y < 0 || y > height){ f=true;break;}
        //         if(x==6){
        //             sl=1;
        //             x=5;
        //         }
        //         if(vertical_up){// вверх
        //             if(version>1){
        //                 System.out.println("x"+x);
        //                 System.out.println("y"+y);
        //                 System.out.println("locationsOfAligmentPatterns"+locationsOfAligmentPatterns[0]);
        //                 for(int r = 0;r<locationsOfAligmentPatterns.length;r++){
        //                     int x1,x4;
        //                     int y1,y4;

        //                     x1 = locationsOfAligmentPatterns[r]-2;//1
        //                     y1 = locationsOfAligmentPatterns[r]-2;

        //                                     //  1---2
        //                                     //  | o |
        //                                     //  3---4

        //                     x4 = locationsOfAligmentPatterns[r]+2;//4
        //                     y4 = locationsOfAligmentPatterns[r]+2;

        //                     System.out.println("---0---");
        //                     while(
        //                         ((x>=x1)&&(x<=x4))
        //                         &&
        //                         ((y>=y1)&&(y<=y4))
        //                     ){
        //                         System.out.println("Processing..");
        //                         x--;
        //                         if(x<=width-collumn*2-3-sl){
        //                             y--;
        //                             x=width-collumn*2-1-sl;
        //                         }
        //                     }
        //                     System.out.println("---1---");
        //                 }
        //             }
        //             if(b.charAt(i)=='1'){
        //                 int rgb = (qr.setMask(maskVersion, x, y)==0)?0xffffff:0x000000;
        //                 bufferedImage.setRGB(x, y, rgb);
        //             }else{
        //                 if(b.charAt(i)=='0'){
        //                     int rgb = (qr.setMask(maskVersion, x, y)==0)?0x000000:0xffffff;
        //                     bufferedImage.setRGB(x, y, rgb);
        //                 }
        //             }
        //             x--;
        //             if(x<=width-collumn*2-3-sl){ 
        //                 y--;
        //                 if(y==6)y--;
        //                 if((y==8 & collumn<=firstcollumns)||(y==-1)||(y==8 & collumn>=collmns-4)){
        //                     vertical_up=!vertical_up;
        //                     collumn++;
        //                     x=width-collumn*2-1-sl;
        //                     y=(collumn<=firstcollumns || collumn>=collmns-4)?9:0;
        //                 }
        //                 x=width-collumn*2-1-sl;
        //             }
                    
        //         }else{// вниз
        //             if(version>1){
        //                 for(int r = 0;r<locationsOfAligmentPatterns.length;r++){
        //                     int x1,x4;
        //                     int y1,y4;

        //                     x1 = locationsOfAligmentPatterns[r]-2;//1
        //                     y1 = locationsOfAligmentPatterns[r]-2;

        //                                     //  1---2
        //                                     //  | o |
        //                                     //  3---4

        //                     x4 = locationsOfAligmentPatterns[r]+2;//4
        //                     y4 = locationsOfAligmentPatterns[r]+2;

        //                     while(
        //                         ((x>=x1)&&(x<=x4))
        //                         &&
        //                         ((y>=y1)&&(y<=y4))
        //                     ){
        //                         x--;
        //                         if(x==width-collumn*2-3-sl){
        //                             y++;
        //                             x=width-collumn*2-1-sl;
        //                         }
                                    
        //                     }

        //                 }
        //             }
        //             if(b.charAt(i)=='1'){
        //                 int rgb = (qr.setMask(maskVersion, x, y)==0)?0xffffff:0x000000;
        //                 bufferedImage.setRGB(x, y, rgb);
        //             }else{
        //                 if(b.charAt(i)=='0'){
        //                     int rgb = (qr.setMask(maskVersion, x, y)==0)?0x000000:0xffffff;
        //                     bufferedImage.setRGB(x, y, rgb);
        //                 }
        //             }
        //             x--;
        //             if(x==width-collumn*2-3-sl){ 
        //                 y++;
        //                 if(y==6)y++;
        //                 if((y==height)||(y==height-8 & collumn>=collmns-4)){
        //                     vertical_up=!vertical_up;
        //                     collumn++;
        //                     x=width-collumn*2-1-sl;
        //                     y=(collumn>=collmns-4)?height-9:height-1;
        //                 }
        //                 x=width-collumn*2-1-sl;
        //             }
        //         }
                
        //     }
        //     d++;
        // }
        return new String();    
    }

    private String getVersion(BufferedImage image){
        return new String();
    }
}
