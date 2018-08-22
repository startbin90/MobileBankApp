import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * dbServer is responsible for any interaction with background database
 * including connecting to the database, making insertion and modification operations
 *
 * class returnMessage in used as return type in some methods below. These methods
 * usually returns result code as well as message to be sent to client. In some cases
 * which only result code is returned, int is used as return type.
 */
public class dbServer {

    private Connection connection;
    /**
     * the amount of transaction detail data to be send to client once
     */
    private int TRANS_DETAIL_AMOUNT = 10;

    dbServer() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * connect to database and set search path
     */
    boolean connectDB(String url, String username, String password) {
        try {
            connection = DriverManager.getConnection(
                    url,
                    username, password);
            // set search_path
            PreparedStatement path = connection.prepareStatement("set search_path to accountschema;");
            path.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }


    public boolean disconnectDB() {
        //write your code here.
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * register the given info into mobileReg table
     */
    returnMessage register(String email, String nickname, int sex, String nin, String cell, String addr,
            String pwd, String transpwd){
        try {
            PreparedStatement add = connection.prepareStatement(
                    "insert into mobilereg values (?, ?, CAST(? AS sex_type), ?, ?, ?, ?, ?, ? ,CAST(? AS boolean));");

            add.setString(1, email);
            add.setString(2, nickname);
            if (sex == 0){
                add.setString(3, "Male");
            }else{
                add.setString(3, "Female");
            }
            add.setString(4, nin);
            add.setString(5, cell);
            add.setString(6, addr);
            add.setString(7, pwd);
            add.setString(8, transpwd);

            Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
            add.setTimestamp(9, ts);
            add.setString(10, "true");
            if (add.executeUpdate() == 1){
                return new returnMessage(myIO.SUCCESS);
            }else{
                return new returnMessage(myIO.INSERTION_ERROR_MOBILEREG); //mobilereg table insertion error
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return new returnMessage(myIO.SERVER_ERROR);
        }

    }

    /**
     * client login
     * check password
     */
    returnMessage logIn(int loginOption, String loginAccount, String pwd){
        try {

            if (loginOption == 0){
                //email login option
                PreparedStatement exist = connection.prepareStatement(
                        "select pwd, id from mobilereg where email = ?;");

                exist.setString(1, loginAccount);
                ResultSet ret = exist.executeQuery();
                if (ret.next()){
                    String actualPwd = ret.getString(1);
                    String id = ret.getString(2);
                    if (Objects.equals(actualPwd, pwd)){
                        // login successful, take NIN back since retrieveInfo needs it
                        return new returnMessage(myIO.SUCCESS, myIO.toBytes(id, 18));
                    }else {
                        return  new returnMessage(myIO.WRONG_ACCOUNT_PASSWORD_COMBO); // wrong password but account exist
                    }
                }else{
                    return new returnMessage(myIO.WRONG_ACCOUNT_PASSWORD_COMBO);// account not exist
                }
            }else {
                // account number login option
                PreparedStatement exist = connection.prepareStatement(
                        "select id from linkedaccounts where account = ?;");

                exist.setString(1, loginAccount);
                ResultSet ret = exist.executeQuery();
                if (ret.next()){
                    String id = ret.getString(1);
                    PreparedStatement statement = connection.prepareStatement(
                            "select pwd from mobilereg where id = ?;");

                    statement.setString(1, id);
                    ResultSet ret1 = statement.executeQuery();
                    if (ret1.next()){
                        if (Objects.equals(ret1.getString(1), pwd)){
                            return new returnMessage(myIO.SUCCESS, myIO.toBytes(id, 18));// login successful
                        }else {
                            return new returnMessage(myIO.WRONG_ACCOUNT_PASSWORD_COMBO); // wrong password but account exist
                        }

                    }else {
                        // id in linkedaccounts but not in mobilereg, which can't happen since by database definition
                        // linkedaccounts.id references mobilereg.id.
                        // Every id has to be in mobilereg table before added to linkedaccounts.
                        return new returnMessage(myIO.SERVER_ERROR);
                    }

                }else{
                    return new returnMessage(myIO.ACCOUNT_NOT_LINKED);// account not bind
                }

            }

        }catch (SQLException e) {
            e.printStackTrace();
            return new returnMessage(myIO.SERVER_ERROR); // wrong password but account exist// error
        }
    }

    /**
     * retrieve client personal info and accounts list
     */
    returnMessage retrieve(String id){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
            PreparedStatement retrieve = connection.prepareStatement(
                    "select * from mobilereg where id = ?;");

            retrieve.setString(1, id);
            ResultSet ret = retrieve.executeQuery();

            PreparedStatement accounts = connection.prepareStatement(
                         "select accounts.account, accounts.balance, person.firstname, person.lastname"
                            +" from accounts join linkedaccounts on linkedaccounts.account = accounts.account"
                            + " join person on accounts.id = person.id"
                            +" where linkedaccounts.id = ? order by linkedaccounts.account;");

            accounts.setString(1, id);
            ResultSet ret1 = accounts.executeQuery();
            ArrayList<byte[]> arr = new ArrayList<>();
            int count = 0;
            while (ret1.next()){
                arr.add(myIO.toBytes(ret1.getString(1), 8));
                Float balance = ret1.getFloat(2);
                ByteBuffer buffer = ByteBuffer.allocate(4);
                buffer.putFloat(balance);
                arr.add(buffer.array());
                arr.add(myIO.toBytes(ret1.getString(3), 10));
                arr.add(myIO.toBytes(ret1.getString(4), 10));
                count++;
            }

            if (ret.next()){
                out.write(myIO.toBytes(ret.getString(1), 50));
                out.write(myIO.toBytes(ret.getString(2), 10));
                if (Objects.equals(ret.getString(3), "Male")){
                    out.write(0);
                }else {
                    out.write(1);
                }
                out.write(myIO.toBytes(ret.getString(4), 18));
                out.write(myIO.toBytes(ret.getString(5), 15));
                out.write(myIO.toBytes(ret.getString(6), 100));
                out.write(myIO.intToBytes(count));
                for (byte[] bytes: arr){
                    out.write(bytes);
                }

                return new returnMessage(myIO.SUCCESS, out.toByteArray());

            }else {
                return new returnMessage(myIO.NIN_NOT_REGISTER); //mobile bank user not exists
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return new returnMessage(myIO.SERVER_ERROR);// error
        }
    }

    /**
     * retrieve transaction details from start to end of account_num account
     */
    returnMessage transDetailRetrieve(String account_num, long start, long end){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
            PreparedStatement retrieve = connection.prepareStatement(
                    "select * from transaction where trans_from = ? and trans_date >= ? and trans_date <= ? order by trans_date DESC;");

            retrieve.setString(1, account_num);
            Timestamp start_Ts = new java.sql.Timestamp(start);
            Timestamp end_Ts = new java.sql.Timestamp(end);
            //System.out.println("start: " + start_Ts.toString() + ", end: " + end_Ts.toString());
            retrieve.setTimestamp(2, start_Ts);
            retrieve.setTimestamp(3, end_Ts);
            ResultSet ret = retrieve.executeQuery();

            ArrayList<byte[]> arr = new ArrayList<>();
            int count = 0;
            while (ret.next() && count < this.TRANS_DETAIL_AMOUNT){
                //trans_id size 10
                arr.add(myIO.transIDPadding(ret.getInt(1), 10).getBytes());
                //trans_date size 8
                arr.add(myIO.longToBytes(ret.getTimestamp(2).getTime()));
                //trans_from size 8
                arr.add(myIO.toBytes(ret.getString(3), 8));
                //trans_to size 8
                arr.add(myIO.toBytes(ret.getString(4), 8));
                //trans_to_last_name size 10
                arr.add(myIO.toBytes(ret.getString(5), 10));
                //trans_dir size 1
                arr.add(myIO.toBytes(ret.getString(6), 1));
                //trans_value size 4
                arr.add(myIO.floatToBytes(ret.getFloat(7)));
                //trans_post_balance size 4
                arr.add(myIO.floatToBytes(ret.getFloat(8)));
                //trans_channel size 1
                arr.add(myIO.toBytes(ret.getString(9), 1));
                //trans_meme size 100
                arr.add(myIO.toBytes(ret.getString(10), 100));
                count++;
            }
            returnMessage retMsg;
            if (ret.next()){
                retMsg = new returnMessage(1); //more to be retrieved
            }else{
                retMsg = new returnMessage(0);//no more record
            }
            //total counts of trans details.
            //should be less or equal to TRANS_DETAIL_AMOUNT
            out.write(myIO.intToBytes(count));
            for (byte[] bytes: arr){
                out.write(bytes);
            }
            retMsg.setMessage(out.toByteArray());
            return retMsg;

        } catch (SQLException | IOException e) {
            return new returnMessage(myIO.SERVER_ERROR); // error
        }
    }

    /**
     * retrieve payees of client specified by email
     */
    returnMessage retrievePayees(String email){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
            PreparedStatement retrieve = connection.prepareStatement(
                    "select * from recipients where email = ? order by lastname;");

            retrieve.setString(1,email);
            ResultSet ret = retrieve.executeQuery();

            ArrayList<byte[]> arr = new ArrayList<>();
            int count = 0;
            while (ret.next()){
                //account size 8
                arr.add(myIO.toBytes(ret.getString(2), 8));
                //first name size 10
                arr.add(myIO.toBytes(ret.getString(3), 10));
                //last name size 10
                arr.add(myIO.toBytes(ret.getString(4), 10));
                count++;
            }
            returnMessage retMsg = new returnMessage(myIO.SUCCESS);//success
            out.write(myIO.intToBytes(count));//total counts of recipients
            for (byte[] bytes: arr){
                out.write(bytes);
            }
            retMsg.setMessage(out.toByteArray());
            return retMsg;
        } catch (SQLException | IOException e) {
            return new returnMessage(myIO.SERVER_ERROR);// error
        }
    }

    /**
     * modify payee
     */
    int modifyPayees(byte[] msg){
        try{
            String email = myIO.bytesToString(msg, 0, 50);
            char operationType = (char)msg[50];
            if (operationType == 1){// add a new payee
                String new_account = myIO.bytesToString(msg, 51, 8);
                String first_name = myIO.bytesToString(msg, 59, 10);
                String last_name = myIO.bytesToString(msg, 69, 10);
                PreparedStatement update = connection.prepareStatement(
                        "insert into recipients values(?, ?, ?, ?);");

                update.setString(1,email);
                update.setString(2,new_account);
                update.setString(3,first_name);
                update.setString(4,last_name);

                return update.executeUpdate();
            }else if (operationType == 2){ //delete an existing payee
                String old_account = myIO.bytesToString(msg, 51, 8);
                PreparedStatement update = connection.prepareStatement(
                        "delete from recipients where email = ? and account = ?;");

                update.setString(1,email);
                update.setString(2,old_account);

                return update.executeUpdate();
            }else { // modify an existing payee
                String old_account = myIO.bytesToString(msg, 51, 8);
                String new_account = myIO.bytesToString(msg, 59, 8);
                String first_name = myIO.bytesToString(msg, 67, 10);
                String last_name = myIO.bytesToString(msg, 77, 10);
                PreparedStatement update = connection.prepareStatement(
                        "update recipients set account = ?, firstname = ?, lastname = ? where email = ? and account = ?;");

                update.setString(1,new_account);
                update.setString(2,first_name);
                update.setString(3,last_name);
                update.setString(4,email);
                update.setString(5, old_account);

                return update.executeUpdate();
            }
        } catch (SQLException e) {
            return -1;// error
        }
    }

    /**
     * transaction service
     * check transaction password of from account,
     * if success, check payee info matching or not
     * if success, check transaction amount
     * if success, execute transaction, modify balances of two sides
     * if success, generate transaction detail
     */
    int transaction(String from, String payee, String first_name, String last_name,
                    float value, String trans_pwd, String memo){
        int ret = transPwdChecker(from, trans_pwd);
        if ( ret == 0){

            int payeeCheck = payeeChecker(payee, first_name, last_name);
            if (payeeCheck == 0){

                int amountCheck = amountChecker(from, value);
                if (amountCheck == 0){
                    int execute = executeTransaction(from, payee, value);
                    if (execute == 0){
                        return transactionDetailGenerator(from, payee, last_name, value, memo);
                    }else{
                        return execute;
                    }
                }else{
                    return amountCheck;
                }

            }else{
                return payeeCheck;
            }

        }else{
            return ret;
        }
    }

    /**
     * One transaction generates two transaction details.
     * one transaction detail has its transaction direction as Borrow '+'
     * another transaction detail has its transaction direction as Loan '-'
     * flip from account and payee account
     */
    private int transactionDetailGenerator(String from, String payee, String last_name, float value, String memo){
        try {
            PreparedStatement insert_from = connection.prepareStatement(
                    "insert into transaction values (default, ?,?,?,?,'-',?,?,'1',?); ");

            Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
            insert_from.setTimestamp(1, ts);
            insert_from.setString(2, from);
            insert_from.setString(3, payee);
            insert_from.setString(4, last_name);
            insert_from.setFloat(5, value);

                PreparedStatement find_from_post_balance = connection.prepareStatement(
                        "select balance from accounts where account = ?");
                find_from_post_balance.setString(1, from);
                ResultSet ret = find_from_post_balance.executeQuery();
                if (ret.next()){
                    float post_balance = ret.getFloat(1);
                    insert_from.setFloat(6,post_balance);
                }else{
                    return myIO.ACCOUNT_NOT_FOUND; //account not existing
                }

            insert_from.setString(7, memo);
            int from_ret = insert_from.executeUpdate();

            PreparedStatement insert_payee = connection.prepareStatement(
                    "insert into transaction values (default, ?,?,?,?,'+',?,?,'1',?); ");

            insert_payee.setTimestamp(1, ts);
            insert_payee.setString(2, payee);
            insert_payee.setString(3, from);
                // get last name for from
                PreparedStatement find_from_last_name = connection.prepareStatement(
                        "select person.lastname from accounts join person on accounts.id = person.id where account = ?");
                find_from_last_name.setString(1, from);
                ResultSet find_from_last_name_ret = find_from_last_name.executeQuery();
                if (find_from_last_name_ret.next()){
                    String from_last_name = find_from_last_name_ret.getString(1);
                    insert_payee.setString(4, from_last_name);
                }else{
                    return myIO.ACCOUNT_NOT_FOUND; //account not existing
                }
            insert_payee.setFloat(5, value);

            PreparedStatement find_payee_post_balance = connection.prepareStatement(
                    "select balance from accounts where account = ?");
            find_payee_post_balance.setString(1, payee);
            ResultSet find_payee_post_balance_ret = find_payee_post_balance.executeQuery();
            if (find_payee_post_balance_ret.next()){
                float post_balance = find_payee_post_balance_ret.getFloat(1);
                insert_payee.setFloat(6,post_balance);
            }else{
                return myIO.ACCOUNT_NOT_FOUND; //account not existing
            }

            insert_payee.setString(7, memo);
            int payee_ret = insert_payee.executeUpdate();
            if (from_ret == 1 && payee_ret == 1){
                return myIO.SUCCESS;//success
            }else{
                return myIO.TRANSACTION_DETAIL_INSERTION_ERROR; //transaction detail insertion error
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return myIO.SERVER_ERROR;
        }
    }

    /**
     * check if transaction amount value is valid
     * valid transaction amount is equal or smaller than balance of the account
     * which starts the transaction and is a number greater than 0.
     */
    private int amountChecker(String from, float value){
        try {
            PreparedStatement find_from = connection.prepareStatement(
                    "select balance from accounts where account = ?");

            find_from.setString(1, from);
            ResultSet from_ret = find_from.executeQuery();

            if (from_ret.next()){
                float balance = from_ret.getFloat(1);
                if (value > balance || value <= 0){
                    return myIO.INVALID_TRANSACTION_AMOUNT; //not enough balance or invalid transaction amount
                }else{
                    return myIO.SUCCESS;
                }
            }else{
                return myIO.ACCOUNT_NOT_FOUND; //either or both account not found account not existing
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return myIO.SERVER_ERROR;
        }
    }

    /**
     * execute transaction
     * subtract or add transaction amount on respective side of transaction
     * TODO: 1. can be improved by setting the related row to be modified by one user
     * TODO: 2. onFailedRollBack
     */
    private int executeTransaction(String from, String payee,float value){
        try {
            PreparedStatement exe_from = connection.prepareStatement(
                    "update accounts set balance = balance - ? where account = ?");

            exe_from.setFloat(1, value);
            exe_from.setString(2, from);
            int from_ret = exe_from.executeUpdate();

            PreparedStatement exe_payee = connection.prepareStatement(
                    "update accounts set balance = balance + ? where account = ?");

            exe_payee.setFloat(1, value);
            exe_payee.setString(2, payee);
            int payee_ret = exe_payee.executeUpdate();

            if (from_ret == 1 && payee_ret == 1){
                return myIO.SUCCESS; // success
            }else{
                return myIO.TRANSACTION_FAILED; //execute Transaction error
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return myIO.SERVER_ERROR;
        }

    }

    /**
     * check payee account first and last name
     */
    private int payeeChecker(String payee, String first_name, String last_name){
        try {
            PreparedStatement find_account = connection.prepareStatement(
                    "select accounts.account, person.firstname, person.lastname" +
                            " from accounts join person on accounts.id = person.id where account = ?");

            find_account.setString(1, payee);
            ResultSet ret = find_account.executeQuery();
            if (ret.next()){
                String first = ret.getString(2);
                String last = ret.getString(3);
                if (!Objects.equals(first, first_name) || !Objects.equals(last, last_name)){
                    return myIO.PAYEE_INFO_NOT_MATCHING; // payee info not matching
                }else{
                    return myIO.SUCCESS;
                }
            }else{
                return myIO.ACCOUNT_NOT_FOUND; //account not found account not existing
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return myIO.SERVER_ERROR;
        }
    }

    /**
     * validate transaction account transaction password
     */
    private int transPwdChecker(String from, String trans_pwd){
        try {
            PreparedStatement find_nin = connection.prepareStatement(
                    "select * from linkedaccounts where account = ?");

            find_nin.setString(1, from);
            ResultSet ret = find_nin.executeQuery();
            if (ret.next()){
                String nin = ret.getString(2);
                PreparedStatement find_transpwd = connection.prepareStatement(
                        "select transpwd from mobilereg where id = ?");
                find_transpwd.setString(1, nin);
                ResultSet ret1 = find_transpwd.executeQuery();
                if (ret1.next()){
                    if (Objects.equals(trans_pwd, ret1.getString(1))){
                        return myIO.SUCCESS;
                    }else{
                        return myIO.WRONG_TRANSACTION_PASSWORD;//wrong transaction pwd
                    }

                }else{
                    return myIO.NIN_NOT_REGISTER; //nin has not register online bank which is impossible
                }
            }else{
                return myIO.ACCOUNT_NOT_LINKED; //account not linked yet
            }

        }catch (SQLException e) {
            return myIO.SERVER_ERROR;
        }

    }

    /**
     * validate account withdrawal password and client NIN
     */
    returnMessage accountAddition(String account, String withdrawals_password, String nin){
        try {
            PreparedStatement find_nin = connection.prepareStatement(
                    "select * from mobilereg where id = ?");

            find_nin.setString(1, nin);
            ResultSet ret = find_nin.executeQuery();
            // mobile bank registered
            if (ret.next()){
                PreparedStatement find_linked = connection.prepareStatement(
                        "select * from linkedaccounts where account = ?");
                find_linked.setString(1, account);
                ResultSet ret1 = find_linked.executeQuery();
                ArrayList<String> linked = new ArrayList<>();
                while (ret1.next()){
                    linked.add(ret1.getString(1));
                }
                if (!linked.isEmpty()){
                    // account already linked to some nin registration
                    return new returnMessage(myIO.ACCOUNT_LINKED);
                }else{
                    returnMessage retMsg = registerAuth(nin, account, withdrawals_password);
                    int retCode = retMsg.getRet();
                    if (retCode == 0){
                        PreparedStatement add_account = connection.prepareStatement(
                                "insert into linkedaccounts values (?, ?);");
                        add_account.setString(1, account);
                        add_account.setString(2, nin);
                        int insert_ret = add_account.executeUpdate();
                        if (insert_ret == 1){
                            return new returnMessage(myIO.SUCCESS);//success
                        }else{
                            return new returnMessage(myIO.INSERTION_ERROR_LINKEDACCOUNTS); //linked account insertion error
                        }
                    }
                    return retMsg;
                }
            }else{
                //nin is not registered online bank
                return new returnMessage(myIO.NIN_NOT_REGISTER);
            }

        }catch (SQLException e) {
            return new returnMessage(myIO.SERVER_ERROR);
        }
    }

    /**
     * modify client personal info
     */
    int personalProfileModification(String email, String cell, String addr, String nin){
        try {
            int err = uniquenessCheck(email, cell, nin);
            if (err == 0) {
                PreparedStatement update = connection.prepareStatement(
                        "update mobilereg set email = ?, cell = ?, address = ? where id = ?;");

                update.setString(1, email);
                update.setString(2, cell);
                update.setString(3, addr);
                update.setString(4, nin);
                int ret = update.executeUpdate();
                if (ret == 1) {
                    return myIO.SUCCESS; //success
                } else {
                    return myIO.FAILED; // failed
                }
            }else if (err == 1){
                return myIO.EMAIL_TAKEN; // email been taken
            }else{
                return myIO.CELL_TAKEN; //err = 2 cell been taken
            }

        }catch (SQLException e) {
            return myIO.SERVER_ERROR;
        }
    }

    /**
     * helper method for personalProfileModification
     * check the uniqueness of email and cell the user wants to be updated
     */
    int uniquenessCheck(String email, String cell, String nin){
        try {
            PreparedStatement check_email = connection.prepareStatement(
                    "select * from mobilereg where id != ? and email = ?;");

            check_email.setString(1, nin);
            check_email.setString(2, email);
            ResultSet ret_email = check_email.executeQuery();
            if (ret_email.next()) {
               return myIO.EMAIL_TAKEN; // email been taken
            }
            PreparedStatement check_cell = connection.prepareStatement(
                    "select * from mobilereg where id != ? and cell = ?;");

            check_cell.setString(1, nin);
            check_cell.setString(2, cell);
            ResultSet ret_cell = check_cell.executeQuery();
            if (ret_cell.next()){
                return myIO.CELL_TAKEN; //cell been taken
            }
            return myIO.SUCCESS; //email and cell ok;

        }catch (SQLException e) {
            return myIO.SERVER_ERROR;
        }
    }

    /**
     * reset the login password of the client specified by NIN
     * CALL login before resetting in order to validate identity
     */
    int passwordReset(String nin, String pwd, String new_pwd){
        try {
            PreparedStatement find_email = connection.prepareStatement(
                    "select email from mobilereg where id = ?;");

            find_email.setString(1, nin);
            ResultSet ret_email = find_email.executeQuery();
            if (ret_email.next()) {
                String email = ret_email.getString(1);
                returnMessage ret = logIn(0, email, pwd);
                if (ret.getRet() == 0){
                    PreparedStatement update = connection.prepareStatement(
                            "update mobilereg set pwd = ? where id = ?;");

                    update.setString(1, new_pwd);
                    update.setString(2, nin);
                    int result = update.executeUpdate();
                    if (result == 1){
                        return myIO.SUCCESS; //success
                    }else{
                        return myIO.FAILED; //update failed
                    }
                }else{
                    return ret.getRet();
                }
            }else{
                return myIO.FAILED;
            }

        }catch (SQLException e) {
            return myIO.SERVER_ERROR;
        }
    }

    /**
     * transaction password reset
     * validate old transaction password before resetting
     */
    int transPasswordReset(String nin, String pwd, String new_pwd){
        try {
            PreparedStatement find_transpwd = connection.prepareStatement(
                    "select transpwd from mobilereg where id = ?");
            find_transpwd.setString(1, nin);
            ResultSet ret1 = find_transpwd.executeQuery();
            if (ret1.next()){
                //pwd checked
                if (Objects.equals(pwd, ret1.getString(1))){
                    PreparedStatement update_transpwd = connection.prepareStatement(
                            "update mobilereg set transpwd = ? where id = ?");
                    update_transpwd.setString(1, new_pwd);
                    update_transpwd.setString(2, nin);
                    int ret = update_transpwd.executeUpdate();
                    if (ret == 1){
                        return myIO.SUCCESS; //success
                    }else{
                        return myIO.FAILED;//update failed
                    }

                }else{
                    return myIO.WRONG_TRANSACTION_PASSWORD;//wrong transaction pwd
                }

            }else{
                return myIO.NIN_NOT_REGISTER; //nin has not register online bank which is impossible
            }
        }catch (SQLException e) {
            return myIO.SERVER_ERROR;
        }
    }

    /**
     * validate account
     */
    returnMessage registerAuth(String nin, String account, String withdrawal){
        try {
            PreparedStatement find_nin = connection.prepareStatement(
                    "select withdrawpwd from accounts where id = ? and account = ?");
            find_nin.setString(1, nin);
            find_nin.setString(2, account);
            ResultSet ret1 = find_nin.executeQuery();
            if (ret1.next()){
                PreparedStatement linked = connection.prepareStatement(
                        "select * from linkedaccounts where id = ? and account = ?");
                linked.setString(1, nin);
                linked.setString(2, account);
                ResultSet ret2 = linked.executeQuery();
                if (ret2.next()){
                    return new returnMessage(myIO.ACCOUNT_LINKED); // account has been linked
                }
                if (!Objects.equals(withdrawal, ret1.getString(1))){
                    return new returnMessage(myIO.WRONG_WITHDRAWAL_PASSWORD); // wrong withdrawal password
                }else{
                    return new returnMessage(myIO.SUCCESS);  // success
                }

            }else{
                //registerAuthentication does not find the account specified by nin and account number.
                // wrong nin and account number combination
                return new returnMessage(myIO.SPECIFIED_ACCOUNT_NOT_FOUND);
            }
        }catch (SQLException e) {
            e.printStackTrace();
            return new returnMessage(myIO.SERVER_ERROR);
        }
    }



    public static void main(String[] args) throws Exception {
        dbServer newServer = new dbServer();
        newServer.connectDB("jdbc:postgresql://localhost:5432/banking", "davychen", null);
        System.out.println(newServer.personalProfileModification("12@1.3", "qw", "haha", "123"));

    }

}




