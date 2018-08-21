import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class returnMessage {
    private int ret;
    private byte[] message;

    public returnMessage(int ret) {
        this.ret = ret;
        this.message = null;
    }

    public returnMessage(int ret, byte[] message) {
        this.ret = ret;
        this.message = message;
    }

    public int getRet() {
        return ret;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public byte[] getMessage() {
        return message;
    }

    public byte[] toBytesArray(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(myIO.intToBytes(this.ret));
            if (this.message != null) {
                out.write(this.message);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return myIO.intToBytes(-1);// -1 stands for server error
        }
    }

    public void abandonMsgPart(){
        this.message = null;
    }

    public static void main(String[] args) {
        String test = ".";
        System.out.println(Float.valueOf(test));
    }


}
