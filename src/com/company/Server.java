package com.company;

import java.io.*;
import java.util.*;
import java.net.*;

// Server class
public class Server
{

    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();

    // counter for clients
    static boolean gameInProgress=false;
    static int i = 1;

    public static void main(String[] args) throws IOException
    {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);

        Socket s;

        // running infinite loop for getting
        // client request
        while (true)
        {
            // Accept the incoming request
           try {
               s = ss.accept();

               System.out.println("New client request received : " + s);

               // obtain input and output streams
               DataInputStream dis = new DataInputStream(s.getInputStream());
               DataOutputStream dos = new DataOutputStream(s.getOutputStream());
               if (i < 5 & !gameInProgress) {
                   System.out.println("Creating a new handler for player " + i);

                   // Create a new handler object for handling this request.
                   ClientHandler mtch = new ClientHandler(s, "Player " + i, dis, dos);

                   // Create a new Thread with this object.
                   Thread t = new Thread(mtch);

                   System.out.println("Adding player " + i  + " to active client list");

                   // add this client to active clients list
                   ar.add(mtch);

                   // start the thread.
                   t.start();

                   // increment i for new client.
                   // i is used for naming only, and can be replaced
                   // by any naming scheme
                   dos.writeUTF("WELCOME!\nYour nick is Player "+i);
                   if (i == 1) {
                       dos.writeUTF("You are admin");
                       dos.writeUTF("Type 'Start' to start game.");
                   }
                   i++;
               }else if(gameInProgress)
               {
                   dos.writeUTF("Game in progress. Try again later.");
                   throw new Exception("gameInProgress");
               }
               else {
                   dos.writeUTF("No place in lobby. Try again later.");
                   throw new Exception("noPlaceInLobby");
                   //dis.close();
                   //dos.close();
               }
           }
           catch (Exception e)
           {
               if(e.equals("gameInProgress"))
                   System.out.println("Game in progress. Canceling connection ");
               if(e.equals("noPlaceInLobby"))
               System.out.println("No place in lobby. Canceling connection ");
           }
        }
    }

     public static void startGame() {
         System.out.println("Game started");
         //
         // miejsce na kod z przebiegiem gry
         //
     }
}
