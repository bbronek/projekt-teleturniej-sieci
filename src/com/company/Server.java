package com.company;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * The Server class is a logic container of quiz flow
 */
public class Server {
    public static Queue<Question> queueOfQuestions = new LinkedList<>();
    public static List<Question> listOfQuestions = new ArrayList<>();
    public static List<ClientHandler> listOfLeaders = new ArrayList<>();
    static Vector<ClientHandler> ar = new Vector<>();
    static Vector<Thread> threads = new Vector<>();
    static int numberOfPlayers = 0;
    static int numberOfPlayersPerGame = 1;
    static int numberOfSettedUsernames = 0;
    static String questionText;
    static String correctAnswer;
    static boolean gameInProgress = false;
    static boolean sendQuestions = true;
    static boolean adminSetNumberOfPlayers = true;

    public static void setQuestions() throws FileNotFoundException {
        String line, answer = null;
        StringBuilder text = new StringBuilder();
        String[] separated;
        File questions = new File("src/com/company/questions.txt");
        try (Scanner sc = new Scanner(questions)) {
            Question question = new Question();

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                separated = line.split("=");

                if (separated.length == 2) {
                    text.append(separated[0]).append('\n');
                    answer = separated[1];
                } else if (line.equals("")) {
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
        }
        randomizingQuestions();
    }

    public static void randomizingQuestions() {
        Collections.shuffle(listOfQuestions);

        for (int i = 0; i < 10; ++i) {
            queueOfQuestions.add(listOfQuestions.get(i));
        }
    }

    public static void printResults() {
        try {
            for (ClientHandler cli : ar) {
                cli.dos.writeUTF("======Results======");
                for (ClientHandler cli2 : ar) {
                    String s = String.valueOf(cli2.getUsername() + ": " + cli2.getNumberOfPoints());
                    cli.dos.writeUTF(s);
                }
                setLeaders();
                if (listOfLeaders.size() > 1) {
                    if (listOfLeaders.get(0).getNumberOfPoints() == 0) {
                        cli.dos.writeUTF("There are no winners everyone is a loser with 0 points");
                    } else {
                        cli.dos.writeUTF("There are " + listOfLeaders.size() + " winners!");
                        for (ClientHandler leader: listOfLeaders) {
                            cli.dos.writeUTF(leader.getUsername() + " is the winner!");
                        }
                    }
                } else {
                    ClientHandler leader = listOfLeaders.get(0);
                    if (leader.getNumberOfPoints() == 0) {
                        cli.dos.writeUTF("There is no winner you are a loser with 0 points");
                    } else if (numberOfPlayers == 1) {
                        cli.dos.writeUTF("You are a winner!");
                    } else {
                        cli.dos.writeUTF(leader.getUsername() + " is a winner!");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("missing user");
        }
    }

    public static void setLeaders() {
        ClientHandler leader = Collections.max(ar, Comparator.comparing(clientHandler -> clientHandler.getNumberOfPoints()));

        for (ClientHandler cli: Server.ar) {
            if (cli.getNumberOfPoints() == leader.getNumberOfPoints()) {
                listOfLeaders.add(cli);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(1234);
        Socket s;
        setQuestions();

        while (true) {
           try {
               s = ss.accept();
               numberOfPlayers++;
               System.err.println("New client request received : " + s);

               DataInputStream dis = new DataInputStream(s.getInputStream());
               DataOutputStream dos = new DataOutputStream(s.getOutputStream());

               if (numberOfPlayers <= numberOfPlayersPerGame && !gameInProgress) {
                   System.err.println("Creating a new handler for player with id: " + numberOfPlayers);
                   ClientHandler player = new ClientHandler(s, numberOfPlayers, dis, dos);
                   System.err.println("Adding player with id: " + numberOfPlayers + " to active client list");
                   ar.add(player);
                   Thread t = new Thread(player);
                   threads.add(t);
                   t.start();
                   dos.writeUTF("WELCOME!\n ");

                   if (numberOfPlayers == 1) {
                       dos.writeUTF("You are admin\nConfigure your game in three steps:");
                       dos.writeUTF("1/3: Please provide a number of players (between 1 and 4) for this gameplay:");
                   } else {
                       dos.writeUTF("Please enter your username:");
                   }
               } else if (gameInProgress) {
                   dos.writeUTF("Game in progress. Try again later.");
                   throw new Exception("gameInProgress");
               } else if (adminSetNumberOfPlayers){
                   dos.writeUTF("Admin is now configuring the game. Try again later.");
                   throw new Exception("adminIsConfiguring");
               } else {
                   dos.writeUTF("No place in lobby. Try again later.");
                   throw new Exception("noPlaceInLobby");
               }
           } catch (Exception e) {
               if(e.equals("gameInProgress")) {
                   System.out.println("Game in progress. Canceling connection ");
               } else if(e.equals("noPlaceInLobby")) {
                   System.out.println("No place in lobby. Canceling connection ");
               } else if(e.equals("adminIsConfiguring")) {
                   System.out.println("Admin is configuring the game. Canceling connection ");
               }
           }
        }
    }
}

