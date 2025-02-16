package QRCode;

public class QRCodeLibrary {
    public String[] symbols;

    public QRCodeLibrary(){
        symbols= new String[]{"0","1","2","3","4","5","6","7","8","9",
        "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"," ",
        "$","%","*","+","-",".","/",":"};
    }

    public int getIndex(String symbol){
        int index =0;
        for(int i = 0;i<symbols.length;i++){
            if(symbol.equals(symbols[i])){
                index=i;
                break;
            }
        }
        return index;
    }
}
