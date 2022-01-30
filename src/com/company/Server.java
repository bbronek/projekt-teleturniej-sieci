package com.company;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * The Server class is a logic container of quiz flow
 */
public class Server {
    static Vector<ClientHandler> ar = new Vector<>();
    static boolean gameInProgress=false;
    static int i = 1;
    public static Queue<Question> queueOfQuestions = new LinkedList<>();

    public static void setQuestions(List<Question> listOfQuestions, Queue<Question> queueOfQuestions) throws FileNotFoundException {
        String line, answer = null;
        StringBuilder text = new StringBuilder();
        String[] separated;
        File questions = new File("src/com/company/questions.txt");
        Scanner sc = new Scanner(questions);
        Question question = new Question();

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            separated = line.split("=");

            if (separated.length == 2) {
                text.append(separated[0]).append('\n');
                answer = separated[1];
            } else if(line.equals("")) {
                question.setText(text.toString());
                question.setAnswer(answer);
                text = new StringBuilder();
                answer = null;
                listOfQuestions.add(question);
                question = new Question();
            } else {
                text.append(separated[0]).append('\n');
            }
        }
        randomizingQuestions(listOfQuestions, queueOfQuestions);
    }

    public static void randomizingQuestions(List<Question> listOfQuestions, Queue<Question> queueOfQuestions) {
        Collections.shuffle(listOfQuestions);

        for (int i = 0; i< 10; ++i) {
            queueOfQuestions.add(listOfQuestions.get(i));
        }
    }

    public static Question popQueue(Queue<Question> queueOfQuestions) {
        return queueOfQuestions.remove();
    }

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(1234);
        Socket s;
        List<Question> listOfQuestions = new ArrayList<>();
        setQuestions(listOfQuestions, queueOfQuestions);

        while (true) {
           try {
               s = ss.accept();
               System.err.println("New client request received : " + s);

               DataInputStream dis = new DataInputStream(s.getInputStream());
               DataOutputStream dos = new DataOutputStream(s.getOutputStream());

               if (i < 5 & !gameInProgress) {
                   System.err.println("Creating a new handler for player " + i);
                   ClientHandler mtch = new ClientHandler(s, "Player " + i, dis, dos);
                   Thread t = new Thread(mtch);
                   System.err.println("Adding player " + i  + " to active client list");
                   ar.add(mtch);
                   // start the thread.
                   t.start();
                   /*
                    increment i for new client.
                    i is used for naming only, and can be replaced
                    by any naming scheme
                   */
                   dos.writeUTF("WELCOME!\nYour nick is Player "+i);
                   if (i == 1) {
                       dos.writeUTF("You are admin");
                       dos.writeUTF("Type 'Start' to start game.");
                   }
                   i++;
               } else if(gameInProgress) {
                   dos.writeUTF("Game in progress. Try again later.");
                   throw new Exception("gameInProgress");
               } else {
                   dos.writeUTF("No place in lobby. Try again later.");
                   throw new Exception("noPlaceInLobby");
                   //dis.close();
                   //dos.close();
               }
           } catch (Exception e) {
               if(e.equals("gameInProgress"))
                   System.out.println("Game in progress. Canceling connection ");
               if(e.equals("noPlaceInLobby"))
                   System.out.println("No place in lobby. Canceling connection ");
           }
        }
    }
}

