package com.company;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * The Server class is a logic container of quiz flow
 */
public class Server {
    /**
     * Vector to store active clients
     */
    static Vector<ClientHandler> ar = new Vector<>();
    static boolean gameInProgress=false;
    /**
     * A static integer to store amount of players
     */
    static int i = 1;
    /**
     * Queue is used to store randomized set of questions
     */
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
        /* server is listening on port 1234 */
        ServerSocket ss = new ServerSocket(1234);
        Socket s;
        List<Question> listOfQuestions = new ArrayList<>();

        setQuestions(listOfQuestions, queueOfQuestions);

        /* running infinite loop for getting client request */
        while (true) {
            /* Accept the incoming request */
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
                   /*
                    increment i for new client.
                    i is used for naming only, and can be replaced
                    by any naming scheme
                   */
                   dos.writeUTF("WELCOME!\nYour nick is Player "+i);
                   if (i == 1) {
                       dos.writeUTF("You are an admin");
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
           catch (Exception e) {
               if(e.equals("gameInProgress"))
                   System.out.println("Game in progress. Canceling connection ");

               if(e.equals("noPlaceInLobby"))
                   System.out.println("No place in lobby. Canceling connection ");
           }
        }
    }

     public static void startGame(@NotNull Queue<Question> queueOfQuestions) {
         System.out.println("Game started");

         for(int i = 0; i< queueOfQuestions.size(); ++i) {
             Question question = popQueue(queueOfQuestions);
             String questionText = question.getText(); //zczytwyanie pierwszego pytania z brzegu
             String answer=question.getAnswer();

             for(ClientHandler cli : ar) {
                 try {
                     cli.dos.writeUTF(questionText);
                     //System.out.println(cli.getName());
                 } catch(IOException e){
                     System.out.println("error");
                 }
             }
             for (ClientHandler cli : ar) {
                 try {
                     String cliAnswer = cli.dis.readUTF();
                     //String cliAnswer =cli.dis.readUTF();
                     System.out.println(cli.getName() + ": " + cliAnswer);
                     if (cliAnswer.equals(answer)) {
                         System.out.println("correct");
                         break;
                         //inni nie powwini moc podawac odpowiedzi
                     }
                 } catch (IOException e){
                     System.out.println("error");
                 }
             }
         }

         System.out.println("next_question");
         //wyśletlenie nazwy gracza który udzielił poprawnej opopiwedzi
         //+1 punkt dla tego gracza

         //koniec fora z iloscia pytan
         //wyświeltlenie wyników
    }
}
