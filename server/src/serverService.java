
import java.util.ArrayList;
import java.util.Arrays;

class serverService{
    /**
     * redirect client message based on client request code
     * @param msg input message including request code(1 byte)
     * @return return message including return code(4 bytes)
     */
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
                retMsg = serverService.retrievePayees(info);
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

    /**
     * client register request.
     * First call registerAuth to validate NIN, account and withdrawal password combination
     * in order to make sure the client has at least one bank account and is willing to open
     * the mobile banking service.
     * If successful, call Server.db.register to register the client in database table mobileReg,
     * then call Server.db.accountAddition to put the bank account in database table linkedAccounts;
     * @param msg bytes stream including National Identity Number(NIN),
     *            first account number to be linked,
     *            withdrawal password for the account,
     *            registered email, user picked nickname,
     *            user sexuality, cellphone, address, password for log in
     *            and password for transaction.
     *
     */
    private static returnMessage register(byte[] msg){
        ArrayList<byte[]> divided = myIO.bytesArrayDivider(msg, 18, 8, 6, 50, 10, 1, 15, 100, 20, 10);
        String nin = new String(divided.get(0)).trim();
        String account = new String(divided.get(1)).trim();
        String withdraw = new String(divided.get(2)).trim();
        String email = new String(divided.get(3)).trim();
        String nick = new String(divided.get(4)).trim();
        int sex = divided.get(5)[0];
        String cell = new String(divided.get(6)).trim();
        String address = new String(divided.get(7)).trim();
        String pwd = new String(divided.get(8)).trim();
        String trans_pwd = new String(divided.get(9)).trim();
        int err;
        // check account owner identity
        returnMessage ret = registerAuth(myIO.bytesArrayDivider(msg, 18 + 8 + 6).get(0));
        err = ret.getRet();
        if (err == 0) {
            // check email, cell, nin uniqueness
            err = Server.db.uniquenessCheck(email, cell, nin);
            if (err == 0) {
                returnMessage retMsg = Server.db.register(email, nick, sex, nin, cell, address, pwd, trans_pwd);
                err = retMsg.getRet();
                if (err == 0) {
                    return Server.db.accountAddition(account, withdraw, nin);
                }
            }
        }
        return new returnMessage(err);
    }

    /**
     * client log in request
     * @param msg including Login option which indicates whether it is an email login or
     *            an account number login, account which can be email or account number and
     *            password.
     * @return If log in successful, return result code 0 and account information including
     *         linked accounts number, account owner first and last name, and account balance
     */
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

    /**
     * retrieve mobile bank service personal info and linked accounts including
     * linked accounts number, account owner first and last name, and account balance.
     * @param id clients's NIN
     */
    private static returnMessage retrieveInfo(String id){
        return Server.db.retrieve(id);
    }

    /**
     * transaction request
     * @param msg including account starts the transaction, payee account,
     *            payee account's first name and last name, transaction amount,
     *            transaction password and transaction memo.
     */
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

    /**
     * retrieve transaction detail
     * @param msg including account to be retrieved, starting time and ending time.
     */
    private static returnMessage retrieveTransDetail(byte[] msg){
        String account = myIO.bytesToString(msg, 0, 8);
        long start = myIO.bytesToLong(msg, 8); // long takes 8 bytes
        long end = myIO.bytesToLong(msg, 16);
        //System.out.println("received date range: " + start + "   " + end);
        return Server.db.transDetailRetrieve(account,start, end);

    }

    /**
     * retrieve client's payees list
     * @param msg including clients login email
     */
    private static returnMessage retrievePayees(byte[] msg){
        String email = myIO.bytesToString(msg, 0, 50);
        return Server.db.retrievePayees(email);
    }

    /**
     * client request to modify payees
     */
    private static byte[] modifyPayees(byte[] msg){
        int ret = Server.db.modifyPayees(msg);
        if (ret == 1){ //success
            return myIO.intToBytes(myIO.SUCCESS);
        }else if (ret == -1){
            return myIO.intToBytes(ret);
        }else{
            return myIO.intToBytes(myIO.FAILED);
        }
    }

    /**
     * client request to link a new account
     * @param msg includes account to be linked, withdrawal password and NIN
     */
    private static returnMessage accountAddition(byte[] msg){
        String account = myIO.bytesToString(msg, 0, 8);
        String withdrawals_password = myIO.bytesToString(msg, 8, 6);
        String nin = myIO.bytesToString(msg, 14, 18);
        //String last_name = myIO.bytesToString(msg, 32, 10);
        return Server.db.accountAddition(account, withdrawals_password, nin);
    }

    /**
     * client request to modify personal info
     * @param msg includes email, cell, address and NIN
     */
    private static byte[] personalProfileModification(byte[] msg){
        String email = myIO.bytesToString(msg, 0, 50);
        String cell = myIO.bytesToString(msg, 50, 15);
        String addr = myIO.bytesToString(msg, 65, 100);
        String nin = myIO.bytesToString(msg, 165, 18);
        return myIO.intToBytes(Server.db.personalProfileModification(email, cell, addr, nin));
    }

    /**
     * client request to modify login password
     * @param msg includes NIN, old password and new password
     */
    private static byte[] passwordReset(byte[] msg){
        ArrayList<byte[]> divided = myIO.bytesArrayDivider(msg, 18,20,20);
        String nin = new String(divided.get(0)).trim();
        String pwd = new String(divided.get(1)).trim();
        String new_pwd =new String(divided.get(2)).trim();
        return myIO.intToBytes(Server.db.passwordReset(nin, pwd, new_pwd));
    }

    /**
     * client request to modify transaction password
     * @param msg includes NIN, old transaction password and new transaction password
     */
    private static byte[] transPasswordReset(byte[] msg){
        ArrayList<byte[]> divided = myIO.bytesArrayDivider(msg, 18,10,10);
        String nin = new String(divided.get(0)).trim();
        String pwd = new String(divided.get(1)).trim();
        String new_pwd =new String(divided.get(2)).trim();
        return myIO.intToBytes(Server.db.transPasswordReset(nin, pwd, new_pwd));
    }

    /**
     * process before register a new mobile banking service or link a new account
     * @param msg includes NIN, account number and withdrawal password
     */
    private static returnMessage registerAuth(byte[] msg){
        ArrayList<byte[]> divided = myIO.bytesArrayDivider(msg, 18,8,6);
        String nin = new String(divided.get(0)).trim();
        String account = new String(divided.get(1)).trim();
        String withdrawal_pwd =new String(divided.get(2)).trim();
        return Server.db.registerAuth(nin, account, withdrawal_pwd);
    }

}
