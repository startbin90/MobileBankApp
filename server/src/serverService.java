
import java.util.ArrayList;
import java.util.Arrays;

class serverService{

     static byte[] serRedirect(byte[] msg) {
        //String ms = new String(msg);
        //System.out.println("received message: " + ms);
        int req;
        byte[] info;
        req = msg[0] & 0xFF;
        System.out.println("Request Code: " + req);
        info = Arrays.copyOfRange(msg, 1, msg.length);
        byte[] ret;
        int err;
        returnMessage retMsg;
        switch (req){
            case 1:
                retMsg = serverService.register(info);
                if (retMsg.getRet() == 0){
                    System.out.println("account registered");
                }
                return retMsg.toBytesArray();
            case 2:
                retMsg = serverService.logIn(info);
                if (retMsg.getRet() == 0){
                    System.out.println("account logined ");
                }
                return retMsg.toBytesArray();
            case 3:
                ret = serverService.passwordReset(info);
                if (myIO.bytesToInt(Arrays.copyOfRange(ret, 0,4)) == 0){
                    System.out.println("password reset");
                }
                return ret;
            case 4:
                ret = serverService.transPasswordReset(info);
                if (myIO.bytesToInt(Arrays.copyOfRange(ret, 0,4)) == 0){
                    System.out.println("transPassword reset");
                }
                return ret;
            case 5:
                ret = serverService.personalProfileModification(info);
                err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0,4));
                if (err == 0){
                    System.out.println("personal profile modification success");
                }
                return ret;
            case 6:
                retMsg = serverService.retrieveTransDetail(info);
                if (retMsg.getRet() == 0 || retMsg.getRet() == 1){
                    System.out.println("account detail retrieved");
                }
                return retMsg.toBytesArray();
            case 7:
                ret = serverService.transaction(info);
                err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0,4));
                if (err == 0){
                    System.out.println("transfer success");
                }
                return ret;
            case 8:
                retMsg = serverService.accountAddition(info);
                err = retMsg.getRet();
                if (err == 0){
                    System.out.println("account bind success");
                }
                return retMsg.toBytesArray();
            case 9:
                retMsg = serverService.getRecipients(info);
                if (retMsg.getRet() == 0){
                    System.out.println("payees list retrieved");
                }
                return retMsg.toBytesArray();
            case 10:
                String nin = myIO.bytesToString(info, 0, 18);
                retMsg = serverService.retrieveInfo(nin);
                if (retMsg.getRet() == 0){
                    System.out.println("account list refreshed");
                }
                return retMsg.toBytesArray();
            case 11:
                ret = serverService.modifyPayees(info);
                if (myIO.bytesToInt(Arrays.copyOfRange(ret, 0,4)) == 0){
                    System.out.println("payees modified successfully");
                }
                return ret;
            case 12:
                retMsg = serverService.registerAuth(info);
                if (retMsg.getRet() == 0){
                    System.out.println("Authentication before registration success");
                }
                return retMsg.toBytesArray();
            default:
                return myIO.intToBytes(100); // not supported request
        }
    }

    private static returnMessage register(byte[] msg){
        ArrayList<byte[]> divided = myIO.bytesArrayDivider(msg, 18, 8, 6, 50, 10, 1, 15, 100, 20, 10);
        String nin = new String(divided.get(0)).trim();
        String account = new String(divided.get(1)).trim();
        String withdraw = new String(divided.get(2)).trim();
        String email = new String(divided.get(3)).trim();
        String nick = new String(divided.get(4)).trim();
        int sex = divided.get(5)[0];
        String cell = new String(divided.get(6)).trim();
        String addr = new String(divided.get(7)).trim();
        String pwd = new String(divided.get(8)).trim();
        String transpwd = new String(divided.get(9)).trim();
        returnMessage ret = registerAuth(myIO.bytesArrayDivider(msg, 18 + 8 + 6).get(0));
        if (ret.getRet() == 0){
            returnMessage retMsg = Server.db.register(email, nick, sex, nin, cell, addr, pwd, transpwd);
            if (retMsg.getRet() == 0){
                return Server.db.accountAddition(account, withdraw, nin);
            }else{
                return retMsg;
            }
        }else{
            return ret;
        }
    }

    private static returnMessage logIn(byte[] msg){
        int loginOpt = msg[0];
        String account = myIO.bytesToString(msg, 0, 50);
        String pwd = myIO.bytesToString(msg, 50, 70);
        returnMessage ret = Server.db.logIn(loginOpt, account, pwd);
        if (ret.getRet() == 0){

            //18 length of String id
            return retrieveInfo(myIO.bytesToString(ret.getMessage(), 0, 18));
        }else {
            ret.abandonMsgPart();
            return ret;
        }
    }

    private static returnMessage retrieveInfo(String id){
        return Server.db.retreive(id);
    }

    private static byte[] transaction(byte[] msg) {
        String from = myIO.bytesToString(msg, 0, 8);
        String payee = myIO.bytesToString(msg, 8, 8);
        String first_name = myIO.bytesToString(msg, 16, 10);
        String last_name = myIO.bytesToString(msg, 26, 10);
        float value = myIO.bytesToFloat(msg, 36);
        String trans_pwd = myIO.bytesToString(msg, 40, 10);
        String memo = myIO.bytesToString(msg, 50, 100);
        return myIO.intToBytes(Server.db.transaction(from, payee, first_name, last_name, value, trans_pwd, memo));
    }

    private static returnMessage retrieveTransDetail(byte[] msg){
        String account = myIO.bytesToString(msg, 0, 8);
        long start = myIO.bytesToLong(msg, 8); // long takes 8 bytes
        long end = myIO.bytesToLong(msg, 16);
        System.out.println("received date range: " + start + "   " + end);
        return Server.db.transDetailRetrive(account,start, end);

    }

    private static returnMessage getRecipients(byte[] msg){
        String email = myIO.bytesToString(msg, 0, 50);
        return Server.db.getRecipients(email);
    }

    private static byte[] modifyPayees(byte[] msg){
        int ret = Server.db.modifyPayees(msg);
        if (ret == 1){ //success
            return myIO.intToBytes(0);
        }else if (ret == -1){
            return myIO.intToBytes(ret);
        }else{
            return myIO.intToBytes(1);
        }
    }

    private static returnMessage accountAddition(byte[] msg){
        String account = myIO.bytesToString(msg, 0, 8);
        String withdrawals_password = myIO.bytesToString(msg, 8, 6);
        String nin = myIO.bytesToString(msg, 14, 18);
        //String last_name = myIO.bytesToString(msg, 32, 10);
        return Server.db.accountAddition(account, withdrawals_password, nin);
    }

    private static byte[] personalProfileModification(byte[] msg){
        String email = myIO.bytesToString(msg, 0, 50);
        String cell = myIO.bytesToString(msg, 50, 15);
        String addr = myIO.bytesToString(msg, 65, 100);
        String nin = myIO.bytesToString(msg, 165, 18);
        return myIO.intToBytes(Server.db.personalProfileModification(email, cell, addr, nin));
    }

    private static byte[] passwordReset(byte[] msg){
        ArrayList<byte[]> divided = myIO.bytesArrayDivider(msg, 18,20,20);
        String nin = new String(divided.get(0)).trim();
        String pwd = new String(divided.get(1)).trim();
        String new_pwd =new String(divided.get(2)).trim();
        return myIO.intToBytes(Server.db.passwordReset(nin, pwd, new_pwd));
    }

    private static byte[] transPasswordReset(byte[] msg){
        ArrayList<byte[]> divided = myIO.bytesArrayDivider(msg, 18,10,10);
        String nin = new String(divided.get(0)).trim();
        String pwd = new String(divided.get(1)).trim();
        String new_pwd =new String(divided.get(2)).trim();
        return myIO.intToBytes(Server.db.transPasswordReset(nin, pwd, new_pwd));
    }

    private static returnMessage registerAuth(byte[] msg){
        ArrayList<byte[]> divided = myIO.bytesArrayDivider(msg, 18,8,6);
        String nin = new String(divided.get(0)).trim();
        String account = new String(divided.get(1)).trim();
        String withdrawal_pwd =new String(divided.get(2)).trim();
        return Server.db.registerAuth(nin, account, withdrawal_pwd);
    }

}
