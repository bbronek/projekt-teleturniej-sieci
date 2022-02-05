package com.company;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class TimeOutQuestion extends TimerTask {

    public void run() {
        //oblokowywanie graczy pod warunkiem że nie udzielili 3 błędnych odpowiedzi
        try {
            for (ClientHandler cli : Server.ar) {
                if (cli.numberOfWrongAnswers != 3) {
                    cli.isBlocked = false;
                } else {
                    cli.numberOfWrongAnswers = 0;
                    cli.dos.writeUTF("!!!!!\nYou are blocked for to many wrong answers. Wait for next question.\n!!!!!");
                }
            }
        }catch (IOException e){

        }
        //wysyłanie pytań do graczy
        if (!Server.queueOfQuestions.isEmpty()) {
            Question question = Server.queueOfQuestions.remove();
            Server.questionText = question.getText();
            Server.correctAnswer = question.getAnswer();
            for (ClientHandler cli : Server.ar) {
                try {
                    cli.dos.writeUTF(Server.questionText);
                } catch (IOException e) {
                    System.err.println("missing user");
                }
            }
        } else {
            //zakończenie gry i wyświetlanie rezultatów
            try {
                throw new Exception("end of questions");
            } catch (Exception e) {
                System.out.println("end of questions");
                this.cancel();
                Server.correctAnswer=null;
                Server.gameInProgress=false;
                Server.printResults();
                Server.randomizingQuestions(Server.listOfQuestions, Server.queueOfQuestions);
            }
        }
    }
}