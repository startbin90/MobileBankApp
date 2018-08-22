import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * returnMessage class is used by dbServer class and serverService class
 * to pass result more efficiently
 *
 */
public class returnMessage {
    /**
     * ret represents the result code of the operation
     * message represents the extra data the operation generates
     */
    private int ret;
    private byte[] message;

    /**
     * constructor with one param, set message to null
     * @param ret result code
     */
    returnMessage(int ret) {
        this.ret = ret;
        this.message = null;
    }

    /**
     * constructor with one param
     * @param ret result code
     * @param message extra data
     */
    returnMessage(int ret, byte[] message) {
        this.ret = ret;
        this.message = message;
    }

    /**
     * getter and setter for two attributes
     */
    int getRet() {
        return ret;
    }

    void setMessage(byte[] message) {
        this.message = message;
    }

    byte[] getMessage() {
        return message;
    }

    /**
     * convert two attributes to bytes and concatenate them inside one array and returned
     */
    byte[] toBytesArray(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(myIO.intToBytes(this.ret));
            if (this.message != null) {
                out.write(this.message);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return myIO.intToBytes(myIO.SERVER_ERROR);
        }
    }

    /**
     * abandon message by setting message attribute to null
     */
    void abandonMsgPart(){
        this.message = null;
    }

    public static void main(String[] args) {
        String test = ".";
        System.out.println(Float.valueOf(test));
    }


}
