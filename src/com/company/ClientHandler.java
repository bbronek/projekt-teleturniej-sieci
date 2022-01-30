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
        this.isloggedin=true;
        this.isInGame=false;
    }

    public String getName() {
        return name;
    }

    public static void sendQuestion(String questionText) {
        for (ClientHandler cli : Server.ar) {
            try {
                cli.dos.writeUTF(questionText);
            } catch (IOException e) {
                System.err.println("missing user");
            }
        }
    }

    @Override
    public void run() {
        String received = "";
        String firstIterationAnswer = "";
        boolean openStream = false;


        while (true) {
           try {
               if (!Server.gameInProgress) {
                   System.err.println("admin lobby");
                   received = dis.readUTF();
                   if (this.name.equals("Player 1") && received.equals("Start")) {
                       Server.gameInProgress = true;
                       System.err.println("Game started");
                       openStream = true;
                   } else {
                       firstIterationAnswer = received;
                   }
               }
               Question question = Server.popQueue(Server.queueOfQuestions);
               String questionText = question.getText();
               String correctAnswer = question.getAnswer();
               sendQuestion(questionText);

               if (openStream) {
                   received = dis.readUTF();
               } else {
                   received = firstIterationAnswer;
                   openStream = true;
               }

               System.err.println("game functionality\n correctAnswer= " + correctAnswer);
               System.err.println("receivedAnswer by " + this.getName() + " is: " + received);

               if (received.equals(correctAnswer)) {
                   System.err.println("correct answer by " + this.getName());
               } else if (received.equals("logout")) {
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
