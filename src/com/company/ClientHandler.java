package com.company;


import java.io.*;
import java.net.*;

/**
 * ClientHandler class
 */
class ClientHandler implements Runnable {
    final String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    boolean isloggedin;
    boolean isInGame;
    Socket s;

    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin = true;
        this.isInGame = false;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        String received = "";
        String tempAnswer = "";
        boolean openStream = false;

        while (true) {
           try {
               if (!Server.gameInProgress) {
                   System.err.println(this.name + " joined the lobby");
                   received = dis.readUTF();
                   if (this.name.equals("Player 1") && received.equals("Start")) {
                       Server.gameInProgress = true;
                       System.err.println("Game started");
                       openStream = true;
                   } else {
                       tempAnswer = received;
                   }
               }

               if(Server.sendQuestions) {
                   Server.sendQuestion();
                   Server.sendQuestions = false;
               }

               if (openStream) {
                   received = dis.readUTF();
               } else {
                   received = tempAnswer;
                   openStream = true;
               }

               System.err.println("game functionality\n correctAnswer = " + Server.correctAnswer);
               System.err.println(" receivedAnswer by " + this.getName() + " is: " + received);

               if (received.equals(Server.correctAnswer)) {
                   System.err.println(" correct answer by " + this.getName());
                   Server.sendQuestions = true;
               } else {
                   System.err.println(" wrong answer by " + this.getName());
               }

               if (received.equals("logout")) {
                   this.isloggedin = false;
                   this.s.close();
                   break;
               }

           } catch (IOException e) {
               System.err.println(this.name + " disconnected");
               Server.numberOfPlayers -= 1;
               Server.ar.remove(this);
               break;
           }
        }

        try {
            /* closing resources */
            this.dis.close();
            this.dos.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
