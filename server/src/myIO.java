import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class myIO {
    static private int bufSize = 1024;
    static int SERVER_ERROR = -1;
    static int SUCCESS = 0;
    static int Failed = 1;
    static int WRONG_ACCOUNT_PASSWORD_COMBO = 2;
    static int INVALID_EMAIL_ADDRESS = 3;
    static int ACCOUNT_NOT_LINKED = 4;
    static int NOT_STRONG_PASSWORD = 5;
    static int SHORT_PASSWORD = 6;
    static int CONNECTION_FAILED = 7;
    static int SPOT0 = 8;
    static int NIN_NOT_REGISTER = 9;
    static int WRONG_PASSWORD = 10;
    static int MOBILEREG_INSERTION_ERROR = 11;
    static int PAYEE_INFO_NOT_MATCHING = 12;
    static int INVALID_TRANSACTION_AMOUNT = 13;
    static int PROVIDED_ACCOUNT_LINKED = 14;
    static int WRONG_WITHDRAWAL_PASSWORD = 15;
    static int ACCOUNT_ADDITION_NAME_NOT_MATCH = 16;
    static int LINKEDACCOUNTS_INSERTION_ERROR = 17;
    static int EMAIL_TAKEN = 18;
    static int CELL_TAKEN = 19;
    static int SERVER_TIMEOUT = 20;
    static int SPECIFIED_ACCOUNT_NOT_FOUND = 21;

    /**
     * convert int to byte[]
     */
    static byte[] intToBytes(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (i & 0xff);
        bytes[1] = (byte) ((i >> 8) & 0xff);
        bytes[2] = (byte) ((i >> 16) & 0xff);
        bytes[3] = (byte) ((i >> 24) & 0xff);
        return bytes;
    }

    /**
     * byte[] to int
     */
    static int bytesToInt(byte[] bytes) {
        int i;
        i = ((bytes[0] & 0xff) | ((bytes[1] & 0xff) << 8)
                | ((bytes[2] & 0xff) << 16) | ((bytes[3] & 0xff) << 24));
        return i;
    }

    /**
     * receive messages from client and respond
     * @param soc socket accepted from client
     */
    static void toClient(Socket soc){
        byte[] ret;
        byte[] respond;
        try {
            if ((ret = receive(soc))!= null){
                respond = serverService.serRedirect(ret);
            }else{
                respond = myIO.intToBytes(CONNECTION_FAILED);
            }
            send(soc, respond);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * send respond via socket to client
     */
    private static void send(Socket socket, byte[] message){
        try {
            byte[] bFront; //four bytes represents the length of following bytes array message
            int left = message.length;
            OutputStream out = socket.getOutputStream();
            while (left > 0){
                // send bufSize of data once
                if (left <= bufSize){
                    bFront = intToBytes(message.length);
                    // bFront represents the length of message not including itself and
                    // the next byte which indicating whether if there are more upcoming messages.
                    out.write(bFront);
                    out.write(0); // 0 indicating there is no more messages
                    out.write(message);
                    out.flush();
                    left -= message.length;
                }else{
                    bFront = intToBytes(bufSize);
                    out.write(bFront);
                    out.write(1); // 1 indicating there are more messages
                    out.write(Arrays.copyOfRange(message,0, bufSize));
                    message = Arrays.copyOfRange(message, bufSize, message.length); // remove bytes already sent
                    out.flush();
                    left -= bufSize;
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * receive message from client via socket
     */
    private static byte[] receive(Socket socket) throws IOException{

        InputStream is = socket.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int more = 1;
        while (more == 1){
            byte[] ret = read(is);
            more = ret[0];
            outputStream.write(Arrays.copyOfRange(ret, 1, ret.length));
        }
        return outputStream.toByteArray();

    }

    /**
     * read from InputStream in
     */
    private static byte[] read(InputStream in) throws IOException{
        //bLen/len only represents the length of message not including one byte indicating
        //whether if there are more messages upcoming
        byte[] bLen = new byte[4];
        byte[] bReceMsg = new byte[bufSize + 1];
        int read = 0;

        while (read != 4){
            read += in.read(bLen, read, 4);
        }

        int len = bytesToInt(bLen);
        read = 0;
        read += in.read(bReceMsg, 0, 1);

        while (read < len + 1){ //add one since there is one more byte should be read
            read += in.read(bReceMsg, read, len); //only needs to read len bytes
        }

        return Arrays.copyOfRange(bReceMsg, 0, len + 1);
    }

    /**
     * convert a String to bytes array of length byteLen
     * fill the rest with ' ' space if byteLen is larger than length of str
     */
    static byte[] toBytes(String str, int byteLen){
        byte[] buf = new byte[byteLen];
        for (int i = 0; i < byteLen; i++){
            if (i < str.length()){
                buf[i] = (byte) str.charAt(i);
            }else{
                buf[i] = ' ';
            }
        }
        return buf;
    }

    /**
     * convert a bytes array specified from 'from 'index with length of 'len' back to String
     * trim the leading and trailing spaces
     */
    static String bytesToString(byte[] bytes, int from, int len){
        byte[] msg = Arrays.copyOfRange(bytes, from, from + len);
        return new String(msg).trim();
    }

    /**
     * tool used to divide a byte array into pieces with specified length array 'bytesLen'
     * the rest of bytes not in range specified by bytesLen will be added at the end of return arrayList
     */
    static ArrayList<byte[]> bytesArrayDivider(byte[] msg, int... bytesLen){
        ArrayList<byte[]> ret = new ArrayList<>();
        int left = msg.length;
        int i = 0;
        int from = 0;
        while (i < bytesLen.length && left >= bytesLen[i]){
            ret.add(Arrays.copyOfRange(msg, from, from + bytesLen[i]));
            from +=  bytesLen[i];
            left -= bytesLen[i];
            i++;
        }
        ret.add(Arrays.copyOfRange(msg, from, from + left));
        return ret;
    }

    /**
     * convert long to bytes array of length 8
     */
    static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return buffer.array();
    }

    /**
     * convert bytes array to long
     */
    static long bytesToLong(byte[] bytes, int from) {
        byte[] mbytes = Arrays.copyOfRange(bytes, from, from + 8);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(mbytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    /**
     * convert bytes array to float
     */
    static float bytesToFloat(byte[] bytes, int from){
        byte[] mbytes = Arrays.copyOfRange(bytes, from, from + 4);
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(mbytes);
        buffer.flip();//need flip
        return buffer.getFloat();
    }

    /**
     * convert float to bytes array
     */
    static byte[] floatToBytes(float x){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putFloat(x);
        return buffer.array();
    }

    /**
     * padding trans_id with 0s
     */
    static String transIDPadding(int trans_id, int maxLength){
        return String.format("%0" + maxLength + "d", trans_id);

    }

    public static void main(String[] args) {
        String test = "  12323  3123   ";
        System.out.println(myIO.transIDPadding(123, 10));
    }
}
