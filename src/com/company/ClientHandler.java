package com.company;


import java.io.*;
import java.util.*;
import java.net.*;

/**
 * ClientHandler class
 */
class ClientHandler implements Runnable {
    final String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;
    boolean isInGame;

    /**
     * constructor
     */
    public ClientHandler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos) {
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
                //System.out.println(cli.getName());
            } catch (IOException e) {
                System.out.println("missing user");
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
                if(Server.gameInProgress)
                {
                    System.out.println("game func");
                        //wysyłanie pytania
                        Question question = Server.popQueue(Server.queueOfQuestions);
                        String questionText = question.getText();
                        sendQuestion(questionText);
                        String correctAnswer = question.getAnswer();
                        System.out.println("correctAnswer= "+correctAnswer);

                        //odbieranie odpowiedzi
                        received = dis.readUTF();
                        System.out.println("receivedAnswer by "+this.getName()+" is: "+received);

                        if (received.equals(correctAnswer)) {
                            System.out.println("correct answer by " + this.getName());

                        }
                }
                else
                {
                        System.out.println("else func");

                        //problem pojawia się w poniższej linijce bo gdy admin przechodzi do received
                        // wyżej pozostali gracze tkwią na tym recived
                        //
                        //moje propozycje rozwiązania to albo dodanie jakiegoś czegoś co zapewni nam czekanie na input do dis
                        //albo dodanie restartu wątków w starcie gry żeby wszyscey gracze przeskoczyli do wyższego recived

                        received = dis.readUTF();
                        System.out.println(this.name+": "+received);

                        if(received.equals("logout")) {
                            this.isloggedin=false;
                            this.s.close();
                            break;
                        }
                        if(received.equals("Start") & this.name.equals("Player 1")) {
                            Server.gameInProgress=true;
                            System.out.println("Game started");
                            //tutaj jakiś restart wątków ale według internetu nie da się tego zrobić
                        }
                }

            } catch (IOException e) {
                System.out.println(this.name+" disconnected");
                Server.i-=1;
                break;
                //trzeba dodać zmniejszenie vectora ar o gracza który się wylogował żeby nie wyrzucało błędu "missing user" w funkcji
                //sendQuestions
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
