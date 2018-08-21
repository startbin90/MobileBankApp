import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class dbServer {

    private Connection connection;
    private int TRANS_DETAIL_AMOUNT = 10;
    dbServer() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    boolean connectDB(String url, String username, String password) {
        //write your code here.
        try {
            connection = DriverManager.getConnection(
                    url,
                    username, password);
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
                return new returnMessage(0);
            }else{
                return new returnMessage(11); //mobilereg table insertion error
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return new returnMessage(-1);
        }

    }

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
                        return new returnMessage(0, myIO.toBytes(id, 18));// login successful
                    }else {
                        return  new returnMessage(2); // wrong password but account exist
                    }
                }else{
                    return new returnMessage(2);// account not exist
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
                            return new returnMessage(0, myIO.toBytes(id, 18));// login successful
                        }else {
                            return  new returnMessage(2); // wrong password but account exist
                        }

                    }else {
                        //id in linkedaccounts but not in mobilereg, which can't happen since linkedaccounts.id
                        //references mobilereg.id. Every id has to be in mobilereg table before added to linkedaccounts.
                        return new returnMessage(-1);
                    }

                }else{
                    return new returnMessage(4);// account not bind
                }

            }

        }catch (SQLException e) {
            e.printStackTrace();
            return new returnMessage(-1); // wrong password but account exist// error
        }
    }

    returnMessage retreive(String id){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
            PreparedStatement retrive = connection.prepareStatement(
                    "select * from mobilereg where id = ?;");

            retrive.setString(1, id);
            ResultSet ret = retrive.executeQuery();

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

                return new returnMessage(0, out.toByteArray());

            }else {
                return new returnMessage(-1); //mobile bank user not exists
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return new returnMessage(-1);// error
        }
    }

    returnMessage transDetailRetrive(String account_num, long start, long end){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
            PreparedStatement retrive = connection.prepareStatement(
                    "select * from transaction where trans_from = ? and trans_date >= ? and trans_date <= ? order by trans_date DESC;");

            retrive.setString(1, account_num);
            Timestamp start_Ts = new java.sql.Timestamp(start);
            Timestamp end_Ts = new java.sql.Timestamp(end);
            System.out.println("start: " + start_Ts.toString() + ", end: " + end_Ts.toString());
            retrive.setTimestamp(2, start_Ts);
            retrive.setTimestamp(3, end_Ts);
            ResultSet ret = retrive.executeQuery();

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
            return new returnMessage(-1); // error
        }
    }

    returnMessage getRecipients(String email){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try{
            PreparedStatement retrive = connection.prepareStatement(
                    "select * from recipients where email = ? order by lastname;");

            retrive.setString(1,email);
            ResultSet ret = retrive.executeQuery();

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
            returnMessage retMsg = new returnMessage(0);//success
            out.write(myIO.intToBytes(count));//total counts of recipients
            for (byte[] bytes: arr){
                out.write(bytes);
            }
            retMsg.setMessage(out.toByteArray());
            return retMsg;
        } catch (SQLException | IOException e) {
            return new returnMessage(-1);// error
        }
    }

    int modifyPayees(byte[] msg){
        try{

            String email = myIO.bytesToString(msg, 0, 50);
            char operationType = (char)msg[50];
            if (operationType == 1){//add
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
            }else if (operationType == 2){ //delete
                String old_account = myIO.bytesToString(msg, 51, 8);
                PreparedStatement update = connection.prepareStatement(
                        "delete from recipients where email = ? and account = ?;");

                update.setString(1,email);
                update.setString(2,old_account);

                return update.executeUpdate();
            }else { // modify
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


    int transaction(String from, String payee, String first_name, String last_name,float value, String trans_pwd, String memo){
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

    private int transactionDetailGenerator(String from, String payee, String last_name, float value, String memo){
        try {
            PreparedStatement path = connection.prepareStatement("set search_path to accountschema;");
            path.execute();
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
                    return 11; //account not existing
                }

            insert_from.setString(7, memo);
            int frmo_ret = insert_from.executeUpdate();

            PreparedStatement insert_payee = connection.prepareStatement(
                    "insert into transaction values (default, ?,?,?,?,'+',?,?,'1',?); ");

            insert_payee.setTimestamp(1, ts);
            insert_payee.setString(2, payee);
            insert_payee.setString(3, from);
                PreparedStatement find_from_last_name = connection.prepareStatement(
                        "select person.lastname from accounts join person on accounts.id = person.id where account = ?");
                find_from_last_name.setString(1, from);
                ResultSet find_from_last_name_ret = find_from_last_name.executeQuery();
                if (find_from_last_name_ret.next()){
                    String from_last_name = find_from_last_name_ret.getString(1);
                    insert_payee.setString(4, from_last_name);
                }else{
                    return 11; //account not existing
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
                return 11; //account not existing
            }

            insert_payee.setString(7, memo);
            int payee_ret = insert_payee.executeUpdate();
            if (frmo_ret == 1 && payee_ret == 1){
                return 0;//success
            }else{
                return 14; //transaction detail insertion error
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int amountChecker(String from, float value){
        try {
            PreparedStatement find_from = connection.prepareStatement(
                    "select balance from accounts where account = ?");

            find_from.setString(1, from);
            ResultSet from_ret = find_from.executeQuery();

            if (from_ret.next()){
                float balance = from_ret.getFloat(1);
                if (value > balance || value <= 0){
                    return 13; //not enough balance or invalid transaction amount
                }else{
                    return 0;
                }
            }else{
                return 11; //either or both account not found account not existing
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int executeTransaction(String from, String payee,float value){
        try {
            PreparedStatement path = connection.prepareStatement("set search_path to accountschema;");
            path.execute();
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
                return 0; // success
            }else{
                return 13; //execute Transaction error
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

    }

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
                    return 12; // payee info not matching
                }else{
                    return 0;
                }
            }else{
                return 11; //account not found account not existing
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int transPwdChecker(String from, String trans_pwd){
        try {
            PreparedStatement path = connection.prepareStatement("set search_path to accountschema;");
            path.execute();
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
                        return 0;
                    }else{
                        return 10;//wrong transaction pwd
                    }

                }else{
                    return 9; //nin has not register online bank which is impossible
                }
            }else{
                return 8; //account not linked yet
            }

        }catch (SQLException e) {
            return -1;
        }

    }

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
                    return new returnMessage(14);
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
                            return new returnMessage(0);//success
                        }else{
                            return new returnMessage(17); //linked account insertion error
                        }
                    }
                    return retMsg;
                }
            }else{
                //nin is not registered online bank
                return new returnMessage(9);
            }

        }catch (SQLException e) {
            return new returnMessage(-1);
        }
    }

    int personalProfileModification(String email, String cell, String addr, String nin){
        try {
            int err = uniquenessCheck(email, cell, nin);
            if (err == 0) {
                PreparedStatement path = connection.prepareStatement("set search_path to accountschema;");
                path.execute();
                PreparedStatement update = connection.prepareStatement(
                        "update mobilereg set email = ?, cell = ?, address = ? where id = ?;");

                update.setString(1, email);
                update.setString(2, cell);
                update.setString(3, addr);
                update.setString(4, nin);
                int ret = update.executeUpdate();
                if (ret == 1) {
                    return 0; //success
                } else {
                    return 1; // failed
                }
            }else if (err == 1){
                return 18; // email been taken
            }else{
                return 19; //err = 2 cell been taken
            }

        }catch (SQLException e) {
            return -1;
        }
    }

    private int uniquenessCheck(String email, String cell, String nin){
        try {
            PreparedStatement path = connection.prepareStatement("set search_path to accountschema;");
            path.execute();
            PreparedStatement check_email = connection.prepareStatement(
                    "select * from mobilereg where id != ? and email = ?;");

            check_email.setString(1, nin);
            check_email.setString(2, email);
            ResultSet ret_email = check_email.executeQuery();
            if (ret_email.next()) {
               return 1; // email been taken
            }
            PreparedStatement check_cell = connection.prepareStatement(
                    "select * from mobilereg where id != ? and cell = ?;");

            check_cell.setString(1, nin);
            check_cell.setString(2, cell);
            ResultSet ret_cell = check_cell.executeQuery();
            if (ret_cell.next()){
                return 2; //cell been taken
            }
            return 0; //email and cell ok;

        }catch (SQLException e) {
            return -1;
        }
    }

    int passwordReset(String nin, String pwd, String new_pwd){
        try {
            PreparedStatement path = connection.prepareStatement("set search_path to accountschema;");
            path.execute();
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
                        return 0; //success
                    }else{
                        return 1; //update failed
                    }
                }else{
                    return ret.getRet();
                }
            }else{
                return 1;
            }

        }catch (SQLException e) {
            return -1;
        }
    }

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
                        return 0; //success
                    }else{
                        return 1;//update failed
                    }

                }else{
                    return 10;//wrong transaction pwd
                }

            }else{
                return 9; //nin has not register online bank which is impossible
            }
        }catch (SQLException e) {
            return -1;
        }
    }

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
                    return new returnMessage(14); // account has been linked
                }
                if (!Objects.equals(withdrawal, ret1.getString(1))){
                    return new returnMessage(15); // wrong withdrawal password
                }else{
                    return new returnMessage(0);  // success
                }

            }else{
                return new returnMessage(21);  //registerAuthentication does not find the account specified by nin and account number.
            }
        }catch (SQLException e) {
            return new returnMessage(-1);
        }
    }



    public static void main(String[] args) throws Exception {
        dbServer newServer = new dbServer();
        newServer.connectDB("jdbc:postgresql://localhost:5432/banking", "davychen", null);
        System.out.println(newServer.personalProfileModification("12@1.3", "qw", "haha", "123"));

    }

}




