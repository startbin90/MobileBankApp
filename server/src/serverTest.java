import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class serverTest {
    private static Socket getSocket(ServerSocket serverSocket){
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            return null;
        }
    }
    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            serverSocket.setSoTimeout(5000);
            System.out.println("start listening");
            while (true){
                Socket socket = getSocket(serverSocket);
                if (socket != null){
                    myIO.toClient(socket);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
