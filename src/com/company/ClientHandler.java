package com.company;


import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ClientHandler class
 */
class ClientHandler implements Runnable {
    final String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    boolean isloggedin;
    boolean isInGame;
    boolean isBlocked;
    int numberOfPoints;
    int numberOfWrongAnswers;
    Socket s;
    static Timer timer;

    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.isloggedin = true;
        this.isInGame = false;
        this.numberOfPoints = 0;
        this.numberOfWrongAnswers=0;
        this.isBlocked=false;
        this.s = s;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfPoints() {
        return numberOfPoints;
    }

    @Override
    public void run() {
        String received = "";
        String tempAnswer = "";
        boolean openStream = false;

        while (!Thread.interrupted()) {
           try {
                   if (!Server.gameInProgress) {
                   System.err.println(this.name + " has joined the lobby");
                   received = dis.readUTF();
                   if (this.name.equals("Player 1") && received.equals("Start")) {
                       Server.gameInProgress = true;
                       System.err.println("Game started");
                       openStream = true;
                       //ustawianie statystyk gracza
                       for (ClientHandler cli : Server.ar) {
                           cli.numberOfWrongAnswers=0;
                           cli.isBlocked=false;
                           cli.numberOfPoints=0;
                       }
                       //interwałowe wysyłanie pytań
                       timer = new Timer();
                       timer.schedule(new TimeOutQuestion(),0,5000);

                   } else {
                       tempAnswer = received;
                   }
               }
                   else
                   {
                       if (openStream) {
                           received = dis.readUTF();
                       } else {
                           received = tempAnswer;
                           openStream = true;
                       }
                       if(!isBlocked){
                           System.err.println("game functionality\n correctAnswer = " + Server.correctAnswer);
                           System.err.println(" receivedAnswer by " + this.getName() + " is: " + received);

                           if (received.equals(Server.correctAnswer)) {
                               this.numberOfPoints += 1;
                               System.err.println(" correct answer by " + this.getName() + " number of points = " + this.numberOfPoints);
                               timer.cancel();
                               timer = new Timer();
                               timer.schedule(new TimeOutQuestion(),0,5000);
                               this.isBlocked=true;
                           } else {
                               System.err.println(" wrong answer by " + this.getName());
                               this.numberOfWrongAnswers+=1;
                               System.err.println(this.getName()+" gave  " + this.numberOfWrongAnswers + " wrong answers ");
                               this.isBlocked=true;
                           }
                       }else{
                           dos.writeUTF("Wait. You are blocked.");
                           received="";
                       }


                       if (received.equals("logout")) {
                           this.isloggedin = false;
                           this.s.close();
                           break;
                       }
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
