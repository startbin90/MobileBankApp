package com.example.davychen.helloworld;

import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class myIO {
    static private int bufSize = 1024;
    static String TAG = "myIO";
    /**
     * convert int to byte[]
     */
    public static byte[] intToBytes(int i) {
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
    public static int bytesToInt(byte[] bytes) {
        int i;
        i = ((bytes[0] & 0xff) | ((bytes[1] & 0xff) << 8)
                | ((bytes[2] & 0xff) << 16) | ((bytes[3] & 0xff) << 24));
        return i;
    }

    public static byte[] toServer(int req, byte[] mes){
        Socket soc;
        if ((soc = send(req, mes)) != null){
            try {
                soc.setSoTimeout(5000);
                return receive(soc);
            } catch (SocketException e) {
                Log.e(TAG, "setSoTimeout failed");
                return myIO.intToBytes(-2);
            } catch (SocketTimeoutException e) {
                return myIO.intToBytes(20); // server time out
            } catch (IOException e) {
                Log.e(TAG, "receive method error");
                return myIO.intToBytes(-2);
            }

        }else{
            return myIO.intToBytes(7); // connection error
        }
    }

    /**
     * combine req with mes and send request to Server
     */
    private static Socket send(int req, byte[] mes){
        Socket socket = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(req);
            outputStream.write(mes);
            byte message[] = outputStream.toByteArray();

            byte[] bFront; //four bytes represents the length of following bytes array message
            int left = message.length;

            // Server address and port
            socket = new Socket(MainActivity.ip, 1234);
            OutputStream out = socket.getOutputStream();
            while (left > 0) {
                // send bufSize of data once
                if (left <= bufSize) {
                    bFront = intToBytes(message.length);
                    // bFront represents the length of message not including itself and
                    // the next byte which indicating whether if there are more upcoming messages.
                    out.write(bFront);
                    out.write(0); // 0 indicating there is no more messages
                    out.write(message);
                    out.flush();
                    left -= message.length;
                } else {
                    bFront = intToBytes(bufSize);
                    out.write(bFront);
                    out.write(1); // 1 indicating there are more messages
                    out.write(Arrays.copyOfRange(message, 0, bufSize));
                    message = Arrays.copyOfRange(message, bufSize, message.length); // remove bytes already sent
                    out.flush();
                    left -= bufSize;
                }
            }
            return socket;
        }catch (ConnectException e ){
            Log.e(TAG, "socket connect error");

        } catch (IOException e1) {
            Log.e(TAG, "occurred in myIO.send()");
        }
        return socket;
    }

    private static byte[] receive(Socket socket) throws IOException{

        InputStream is = socket.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        int more = 1;
        while (more == 1){
            byte[] ret = read(is);
            more = ret[0];
            outputStream.write(Arrays.copyOfRange(ret, 1, ret.length));
        }
        return outputStream.toByteArray();

    }

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

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            return !ip.endsWith(".");
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static int pwdChecker(String password) {
        boolean hasLetter = false;
        boolean hasDigit = false;
        if (password.length() >= 8) {
            for (int i = 0; i < password.length(); i++) {
                char x = password.charAt(i);
                if (Character.isLetter(x)) {

                    hasLetter = true;
                } else if (Character.isDigit(x)) {

                    hasDigit = true;
                }

                // no need to check further, break the loop
                if (hasLetter && hasDigit) {

                    break;
                }

            }
            if (hasLetter && hasDigit) {
                return 0; //("STRONG");
            } else {
                return 1;//("NOT STRONG");
            }
        } else {
            return 2;//("HAVE AT LEAST 8 CHARACTERS");
        }
    }
    public static boolean isAccountNumber(String num){
        if (num == null || "".equals(num) || num.length() != 8) {
            return false;
        }
        for (int i = 0; i < num.length(); i++){
            if (!Character.isDigit(num.charAt(i))) {
                return false;
            }
        }
        return true;

    }
    public static boolean isSimpleIDNumber(String IDNumber){
        if (IDNumber == null || "".equals(IDNumber) || IDNumber.length() != 18) {
            return false;
        }
        for (int i = 0; i < IDNumber.length(); i++){
            if (!Character.isDigit(IDNumber.charAt(i))) {
                return false;
            }
        }
        return true;

    }
    public static boolean isIDNumber(String IDNumber) {
        if (IDNumber == null || "".equals(IDNumber)) {
            return false;
        }
        // 定义判别用户身份证号的正则表达式（15位或者18位，最后一位可以为字母）
        String regularExpression = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|" +
                "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";
        //假设18位身份证号码:41000119910101123X  410001 19910101 123X
        //^开头
        //[1-9] 第一位1-9中的一个      4
        //\\d{5} 五位数字           10001（前六位省市县地区）
        //(18|19|20)                19（现阶段可能取值范围18xx-20xx年）
        //\\d{2}                    91（年份）
        //((0[1-9])|(10|11|12))     01（月份）
        //(([0-2][1-9])|10|20|30|31)01（日期）
        //\\d{3} 三位数字            123（第十七位奇数代表男，偶数代表女）
        //[0-9Xx] 0123456789Xx其中的一个 X（第十八位为校验值）
        //$结尾

        //假设15位身份证号码:410001910101123  410001 910101 123
        //^开头
        //[1-9] 第一位1-9中的一个      4
        //\\d{5} 五位数字           10001（前六位省市县地区）
        //\\d{2}                    91（年份）
        //((0[1-9])|(10|11|12))     01（月份）
        //(([0-2][1-9])|10|20|30|31)01（日期）
        //\\d{3} 三位数字            123（第十五位奇数代表男，偶数代表女），15位身份证不含X
        //$结尾


        boolean matches = IDNumber.matches(regularExpression);

        //判断第18位校验值
        if (matches) {

            if (IDNumber.length() == 18) {
                try {
                    char[] charArray = IDNumber.toCharArray();
                    //前十七位加权因子
                    int[] idCardWi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                    //这是除以11后，可能产生的11位余数对应的验证码
                    String[] idCardY = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
                    int sum = 0;
                    for (int i = 0; i < idCardWi.length; i++) {
                        int current = Integer.parseInt(String.valueOf(charArray[i]));
                        int count = current * idCardWi[i];
                        sum += count;
                    }
                    char idCardLast = charArray[17];
                    int idCardMod = sum % 11;
                    if (idCardY[idCardMod].toUpperCase().equals(String.valueOf(idCardLast).toUpperCase())) {
                        return true;
                    } else {
                        System.out.println("身份证最后一位:" + String.valueOf(idCardLast).toUpperCase() +
                                "错误,正确的应该是:" + idCardY[idCardMod].toUpperCase());
                        return false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("异常:" + IDNumber);
                    return false;
                }
            }

        }
        return matches;
    }

    public static byte[] toBytes(String str, int byteLen){
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
    public static String bytesToString(byte[] bytes, int from, int len){
        byte[] msg = Arrays.copyOfRange(bytes, from, from + len);
        return new String(msg).trim();
    }

    /**
     * tool used to divide a byte array into pieces with specified length array 'bytesLen'
     * the rest of bytes not in range specified by bytesLen will be added at the end of return arrayList
     */
    public static ArrayList<byte[]> bytesArrayDivider(byte[] msg, int... bytesLen){
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
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return buffer.array();
    }

    /**
     * convert bytes array to long
     */
    public static long bytesToLong(byte[] bytes, int from, int len) {
        byte[] mbytes = Arrays.copyOfRange(bytes, from, from + len);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(mbytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    /**
     * convert bytes array to float
     */
    public static float bytesToFloat(byte[] bytes, int from, int len){
        byte[] mbytes = Arrays.copyOfRange(bytes, from, from + len);
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(mbytes);
        buffer.flip();//need flip
        return buffer.getFloat();
    }

    /**
     * convert float to bytes array
     */
    public static byte[] floatToBytes(float x){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putFloat(x);
        return buffer.array();
    }


}