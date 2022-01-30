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
        String received;
        while (true) {
           try {
                /* receive the string */
               //tutaj jakieś oczekiwanie na input na dis i dopiero przejście do ifów
                if (Server.gameInProgress) {
                    Question question = Server.popQueue(Server.queueOfQuestions);
                    String questionText = question.getText();
                    String correctAnswer = question.getAnswer();
                    sendQuestion(questionText);
                    received = dis.readUTF();

                    System.err.println("correctAnswer= " + correctAnswer + "\ngame functionality");
                    System.err.println("receivedAnswer by " + this.getName() + " is: " + received);

                    if (received.equals(correctAnswer)) {
                        System.err.println("correct answer by " + this.getName());
                    }
                } else {
                    System.err.println("else func");
                    received = dis.readUTF();
                    if(Server.gameInProgress || this.name.equals("Player 1")) {

                       System.err.println(this.name + ": " + received);
                       if (this.name.equals("Player 1") && received.equals("Start")) {
                           Server.gameInProgress = true;
                           System.err.println("Game started");
                       } else if (received.equals("logout")) {
                           this.isloggedin = false;
                           this.s.close();
                           break;
                       }
                   }
                }

            } catch (IOException e) {
                System.err.println(this.name + " disconnected");
                Server.i -= 1;
                Server.ar.remove(this);
                break;
            }
        } try {
            /* closing resources */
            this.dis.close();
            this.dos.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
