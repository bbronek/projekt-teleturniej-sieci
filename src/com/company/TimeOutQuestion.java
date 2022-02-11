package com.company;

import java.io.IOException;
import java.util.TimerTask;

public class TimeOutQuestion extends TimerTask {
    public void run() {
        for (ClientHandler cli : Server.ar) {
            if (cli.numberOfWrongAnswers != 3) {
                cli.isBlocked = false;
            } else {
                cli.numberOfWrongAnswers = 0;
                try {
                    cli.dos.writeUTF("!!!!!\nYou are blocked for to many wrong answers. Wait for next question.\n!!!!!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

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
                System.err.println("end of questions");
                this.cancel();
                Server.correctAnswer = null;
                Server.gameInProgress = false;
                Server.printResults();
                Server.randomizingQuestions();
        }
    }
}
