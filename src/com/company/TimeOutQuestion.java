package com.company;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class TimeOutQuestion extends TimerTask {

    public void run() {
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
            try {
                throw new Exception("end of questions");
            } catch (Exception e) {
                System.out.println("end of questions");
                this.cancel();
                Server.correctAnswer=null;
                Server.gameInProgress=false;
                Server.printResults();
                //Server.randomizingQuestions()
            }
        }
    }
}