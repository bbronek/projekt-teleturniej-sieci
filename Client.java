import java.util.Random;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;


public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
		    Socket clientSocket = new Socket("localhost",3000);
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		    outToServer.writeBytes("LOGIN 464953"+'\n');
        // while(true){
        
        // }
	}
}
