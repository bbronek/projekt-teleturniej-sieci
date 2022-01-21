import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  public static void main(String[] args) {
     try {
        System.out.println("Waiting for players...");
        ServerSocket ss = new ServerSocket(2137);
        Socket soc = ss.accept();
        System.out.println("Connection established");

     } catch(Exception e) {
       e.printStackTrace();
    }
  }
 
    
}
