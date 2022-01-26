package com.company;

import java.io.*;
import java.util.*;
import java.net.*;

// Server class
public class Server
{

    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();

    // counter for clients
    static boolean gameInProgress=false;
    static int i = 1;
    public static Queue<Question> queueOfQuestions = new LinkedList<>();
    public static void setQuestions(List<Question> listOfQuestions, Queue<Question> queueOfQuestions) throws FileNotFoundException {
        String line, answer = null;
        String text = "";
        String[] separated;
        File questions = new File("src/com/company/questions.txt");
        Scanner sc = new Scanner(questions);
        Question question = new Question();

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            separated = line.split("\\=");

            if (separated.length == 2) {
                text += separated[0] + '\n';
                answer = separated[1];
            } else if(line == "") {
                question.setText(text);
                question.setAnswer(answer);
                text = "";
                answer = null;
                listOfQuestions.add(question);
                question = new Question();
            } else {
                text += separated[0] + '\n';
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

    public static void main(String[] args) throws IOException
    {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);
        Socket s;
        List<Question> listOfQuestions = new ArrayList<>();


        setQuestions(listOfQuestions, queueOfQuestions);

        // running infinite loop for getting
        // client request
        while (true)
        {
            // Accept the incoming request
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

                   // increment i for new client.
                   // i is used for naming only, and can be replaced
                   // by any naming scheme
                   dos.writeUTF("WELCOME!\nYour nick is Player "+i);
                   if (i == 1) {
                       dos.writeUTF("You are admin");
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
           catch (Exception e)
           {
               if(e.equals("gameInProgress"))
                   System.out.println("Game in progress. Canceling connection ");
               if(e.equals("noPlaceInLobby"))
               System.out.println("No place in lobby. Canceling connection ");
           }
        }
    }

     public static void startGame(Queue<Question> queueOfQuestions) {
         System.out.println("Game started");
         //for (ilość pytań w kolejce)
         Question question = queueOfQuestions.remove();
             for(ClientHandler cli : ar)
             {
                 try {
                     String questionText = question.getText(); //zczytwyanie pierwszego pytania z brzegu
                     String answer=question.getAnswer();
                     cli.dos.writeUTF(questionText);
                     System.out.println(cli.getName());
                     //while(ilość odpowiedzi!=ilości graczy lub cli.dis.readUTF()!=answer)
                     //{
                     //    kod odpowiedzialny za odczyt odpowiedzi wraz z ich nadawcami
                     //}
                     //wyśletlenie nazwy gracza który udzielił poprawnej opopiwedzi
                     //+1 punkt dla tego gracza
         //koniec fora
         //wyświeltlenie wyników
                 }catch(IOException e){
                     System.out.println("error");
                 }
             }
     }
}
