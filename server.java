import java.util.Random;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;


public class server {

    /**
     * Runs the application. Pairs up clients that connect.
     */
    public static void main(String[] args) throws Exception {
		
			ServerSocket listener = new ServerSocket(3000);
			Socket socket = listener.accept();
			BufferedReader inFromCleint = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
			
			System.out.println("Tic Tac Toe Server is Running");
			//zmiena przyjmuje input od clienta
			//x=input
			//x=2
			//1[a,b,c]
			//2[d,e,f]
			System.out.println(inFromCleint.readLine());
			Socket socket1 = listener.accept();
			BufferedReader inFromCleint1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
			DataOutputStream outToClient1 = new DataOutputStream(socket1.getOutputStream());
			System.out.println(inFromCleint1.readLine());
				while(true){
					
			}

	}
}