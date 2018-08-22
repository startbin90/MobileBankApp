import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Server class creates the Server object, connects to database
 * and push to ExecutorService to be executed.
 * run method tries to accept connections from clients and
 * starts a new ServerSocketWorker Runnable and push it to
 * ExecutorService to be executed
 *
 * Server initializes a newCachedThreadPool to execute itself
 * and all ServerSocketWorker Runnable
 */
public class Server implements Runnable{
    private int port;
    private boolean isListen = true;
    //public ArrayList<ServerSocketThread> SST = new ArrayList<>();
    static dbServer db = new dbServer();
    private ExecutorService exec;

    private Server(int port){
        this.port = port;
        db.connectDB("jdbc:postgresql://localhost:5432/banking", "davychen", null);
        exec = Executors.newCachedThreadPool();
        exec.execute(this);
    }
    private Socket getSocket(ServerSocket serverSocket){
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
    }
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            //serverSocket.setSoTimeout(5000);
            System.out.println("Listening at: " + "---" + Thread.currentThread().getName() + "---");
            while (isListen){
                Socket socket = getSocket(serverSocket);
                if (socket != null){
                    exec.execute(new ServerSocketWorker(socket));
                }
            }
            serverSocket.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * worker Runnable which processes request from client
     * It is executed in the thread managed by executorService
     */
    public class ServerSocketWorker implements Runnable{
        Socket socket;
        private String ip;

        ServerSocketWorker(Socket socket){
            this.socket = socket;
            ip = socket.getInetAddress().toString();
        }

        @Override
        public void run(){
            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            Date dateobj = new Date();
            System.out.println("---" + Thread.currentThread().getName() + "---"
                    + "New connection from IP: " + ip +" at " + df.format(dateobj));
            myIO.toClient(socket);

        }
    }

    public static void main(String[] args) {
        new Server(1234);
    }
}
