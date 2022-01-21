import java.net.ServerSocket;
import java.net.Socket;

public class Client {
  public static void main(String[] args){
     try {
      System.out.println("Client started");
      Socket soc = new Socket("localhost", 2137);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
 
}
