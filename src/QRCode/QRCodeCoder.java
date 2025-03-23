package QRCode;

import java.nio.charset.StandardCharsets;

public class QRCodeCoder {

    QRCodeBarCodeMaker qr_maker = new QRCodeBarCodeMaker();
    QRCodeLibrary qr_Library = new QRCodeLibrary();
    private int version;
    public String last_barcode;

    public String IncodeToQR(String input_text,int QRCODE_masktype){
        String bit_sequence = ByteIncode(input_text);
        bit_sequence = AddServiceInfo(bit_sequence,input_text);

        String[] array_bit_sequence = FillingUp(bit_sequence);
        int[][] byte_blocks = GenerateBlocks(array_bit_sequence);


        qr_maker.makeBarcode(CombiningBlocks(byte_blocks),version,QRCODE_masktype);

        last_barcode=qr_maker.last_barcode;

        return bit_sequence;
        
    }

    private String ByteIncode(String input_text){
        String bit_sequence = "";
        byte[] bytes = input_text.getBytes(StandardCharsets.UTF_8);
        for (byte b:bytes) {
            String a = dec_to_bin(Byte.toString(b));
            while(a.length()!=8){
                a="0"+a;
            }
            bit_sequence+=a;
            
        }

        version = qr_Library.getVersion(bit_sequence.length());
        version = (version >= 10)?qr_Library.getVersion(bit_sequence.length()+20):qr_Library.getVersion(bit_sequence.length()+12);

        return bit_sequence;
    }

    private String AddServiceInfo(String bit_sequence,String input_text){
        String field="";
        if(version >=1 & version <=9){
            field = dec_to_bin(String.valueOf(input_text.length()));
            while(field.length()!=8){
                field="0"+field;
            }
            field = "0100"+field;
        }else{
            if(version >=10 & version <=40){
                field = dec_to_bin(String.valueOf(input_text.length()));
                while(field.length()!=16){
                    field="0"+field;
                }
                field = "0100"+field;
            }
        }
        return field+bit_sequence;
    }

    private String[] FillingUp(String bit_sequence){
        while (bit_sequence.length()%8!=0) {
            bit_sequence+="0";
        }
        int d=0;
        while(bit_sequence.length()<qr_Library.maxInfoAmount_M[version]){
            if(d==0){
                bit_sequence+="11101100";
                d++;
            }else{
                bit_sequence+="00010001";
                d=0;
            }
        }

        System.out.println("Version:"+version);
        System.out.println("Final bit sequence:"+bit_sequence+" "+bit_sequence.length());

        String[] a = new String[bit_sequence.length()/8];
        for (int i = 0; i < a.length; i++) {
            a[i] = bit_sequence.substring(i*8, (i+1)*8);
        }
        return a;
    }

    private int[][] GenerateBlocks(String[] bit_sequence){
        int whole_amount_of_blocks = qr_Library.blocksInfoAmount_M[version];
        int amount_of_blocks = bit_sequence.length/whole_amount_of_blocks;
        int amount_of_additional_blocks = bit_sequence.length%whole_amount_of_blocks;
        int[] maximum_info_in_blocks=new int[whole_amount_of_blocks];

        for(int i = maximum_info_in_blocks.length-1;i>-1;i--){
            int a = amount_of_blocks+amount_of_additional_blocks;
            if(amount_of_additional_blocks>0)amount_of_additional_blocks--;
            maximum_info_in_blocks[i]=a;
        }

        for (int k: maximum_info_in_blocks) {
            System.out.println("maximum_info_in_blocks:"+k);
        }

        int[][] byte_blocks = new int[whole_amount_of_blocks][maximum_info_in_blocks[maximum_info_in_blocks.length-1]];
        int a=0;
        for(int i=0;i<whole_amount_of_blocks;i++){
            for(int j=0;j<maximum_info_in_blocks[i];j++){
                byte_blocks[i][j] = Integer.parseInt(bin_to_dec(bit_sequence[j+a]));
            }
            a+=maximum_info_in_blocks[i];
        }

        for(int i = 0;i<byte_blocks.length;i++){
            System.out.print("byte_block "+i+":");
            for(int j=0;j<byte_blocks[i].length;j++){
                System.out.print(" "+byte_blocks[i][j]);
            }
            System.out.println();
        }

        return byte_blocks;
    }

    private String CombiningBlocks(int[][] byte_block){
        String byte_code="";
        int BYTE_BLOCKS = byte_block.length;
        int BYTE_NUMBERS = byte_block[BYTE_BLOCKS-1].length;

        int[][] blocks_of_correction = new int[BYTE_BLOCKS][qr_Library.bytesOfCorrection_M[version]];

        for (int i = 0; i < BYTE_NUMBERS; i++) {
            for(int j = 0;j<BYTE_BLOCKS;j++){
                byte_code+=byte_block[j][i];
                byte_code+=";";
            }
        }

        
        // for (int i = 0; i < blocks_of_correction.length; i++) {
            
        //     blocks_of_correction[i]=qr_Library.ReedSolomon(byte_block[i], qr_Library.bytesOfCorrection_M[version]);
        // }
        for (int i = 0; i < byte_block.length; i++) {
            
            blocks_of_correction[i]=qr_Library.ReedSolomon(byte_block[i], qr_Library.bytesOfCorrection_M[version]);
        }

        for(int i = 0;i<blocks_of_correction.length;i++){
            System.out.print("blocks_of_correction "+i+":");
            for(int j=0;j<blocks_of_correction[i].length;j++){
                System.out.print(" "+blocks_of_correction[i][j]);
            }
            System.out.println();
        }

        int BYTE_OF_CORRECTION_BLOCKS = blocks_of_correction.length;
        int BYTE_OF_CORRECTION_NUMBERS = blocks_of_correction[BYTE_OF_CORRECTION_BLOCKS-1].length;

        for (int i = 0; i < BYTE_OF_CORRECTION_NUMBERS; i++) {
            for(int j = 0;j<BYTE_OF_CORRECTION_BLOCKS;j++){
                byte_code+=blocks_of_correction[j][i];
                byte_code+=";";
            }
        }



        System.out.println(byte_code);

        return byte_code;
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
