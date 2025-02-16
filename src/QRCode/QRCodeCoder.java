package QRCode;
public class QRCodeCoder {
    private QRCodeLibrary qr = new QRCodeLibrary();

    public String IncodeToQR(String input_text){
        String result="";

        // Определение типа
        String type="";
        try {//A - цифровой тип(только цифры);
            int input = Integer.parseInt(input_text);
            type = "A";

        } catch (Exception e) {// B-Буквенно-Цифровой(цифры и буквы);
            type = "B";
        }
        
        //result = (type=="A")?NumericCoding(input_text):LetterNumericCoding(input_text);

        return result;
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

    // private String LetterNumericCoding(String input){
    //     // В этом случае на 2 символа требуется 11 бит информации. Входной поток символов разделяется на группы по 2, 
    //     // в группе каждый символ кодируется согласно таблице QRCodeLibrary.java,
    //     // значение первого символа в группе умножается на 45 и прибавляется к значение второго символа.
    //     // Полученное число переводится в 11-битное двоичное число и добавляется к последовательности бит.
    //     // Если в последней группе 1 символ, то его значение сразу кодируется 6-битным числом и добавляется к последовательности бит.

    //     String bincode="";
    //     int j =0;
    //     String couple="";
        
    //     for(int i =0;i<input.length();i++){
    //         int last_i;
    //         if(j==2){
    //             if(couple.length()==2){
    //                 String a1=String.valueOf(couple.charAt(0));
    //                 String a2=String.valueOf(couple.charAt(1));
                    
    //                 try {
    //                     a1=a1.toUpperCase();
    //                 } catch (Exception e) {}
    //                 try {
    //                     a2=a2.toUpperCase();
    //                 } catch (Exception e) {}
    //                 bincode += dec_to_bin(String.valueOf(
    //                         (qr.getIndex(a1)*45)
    //                         +qr.getIndex(a2)
    //                     )); 
    //             }
    //             else{
    //                 String a1=String.valueOf(couple.charAt(0));
                    
    //                 try {
    //                     a1=a1.toUpperCase();
    //                 } catch (Exception e) {}
    //                bincode += dec_to_bin(String.valueOf(
    //                         (qr.getIndex(a1))
    //                     )); 
    //             }
    //             couple="";
    //             j=0;
    //         }
    //         }
    //         couple+=input.charAt(i);
    //         j++;
    //     }
    //     return bincode;
    // }
    
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
