package com.company;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;

/**
 * ClientHandler class
 */
class ClientHandler implements Runnable {
    String username;
    String tempAnswer;
    final Integer id;
    final DataInputStream dis;
    final DataOutputStream dos;
    boolean isInGame;
    boolean isBlocked;
    boolean isUsernameSetted;
    boolean answerInLastStream;
    int numberOfPoints;
    int numberOfWrongAnswers;
    static Timer timer;
    Socket s;

    public ClientHandler(Socket s, Integer id, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.id = id;
        this.isInGame = false;
        this.isBlocked = false;
        this.isUsernameSetted = false;
        this.answerInLastStream = false;
        this.numberOfPoints = 0;
        this.numberOfWrongAnswers = 0;
        this.s = s;
        this.username = "Player " + id;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) {
        this.username = username;
        this.isUsernameSetted = true;
    }

    public int getNumberOfPoints() { return numberOfPoints; }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void adminLobbyHandler(String received) throws IOException {
        int numberOfPlayers;

        if (Server.adminSetNumberOfPlayers) {
            if (isInteger(received)) {
                numberOfPlayers = Integer.parseInt(received);
                if (numberOfPlayers >= 1 && numberOfPlayers  <= 4) {
                    Server.numberOfPlayersPerGame = numberOfPlayers;
                    Server.adminSetNumberOfPlayers = false;
                    System.err.println("Admin has set number of players to: " + numberOfPlayers);
                    dos.writeUTF("You have successfully set number of players for this game.\n2/3: Please now enter your username:" );
                } else {
                    System.err.println("Error with setting number of players: Admin has written number out of range: " + received);
                    dos.writeUTF("1/3: You have to provide number between 1 and 4. Your number: " + numberOfPlayers + " is out of the range");
                }
            } else {
                System.err.println("Error with setting number of players: Admin has written: " + received);
                dos.writeUTF("1/3: You have to provide number between 1 and 4. Your command: " + received + " can not be converted to Integer");
            }
        } else if (!this.isUsernameSetted) {
                this.setUsername(received);
                Server.numberOfSettedUsernames += 1;
                dos.writeUTF("You have successfully set your username\n" );

                if (Server.numberOfPlayersPerGame == 1) {
                    dos.writeUTF("It looks like you want to play alone\n3/3: Type \"Start\" to start the gameplay");
                } else if (Server.numberOfPlayers < Server.numberOfPlayersPerGame) {
                    dos.writeUTF("Waiting for other players to join the game..." );
                }
        } else if (Server.numberOfPlayersPerGame == Server.numberOfSettedUsernames) {
            if (received.equals("Start")) {
                Server.gameInProgress = true;
                System.err.println("Game started");

                for (ClientHandler cli : Server.ar) {
                    cli.numberOfWrongAnswers = 0;
                    cli.isBlocked = false;
                    cli.numberOfPoints = 0;
                }

                timer = new Timer();
                timer.schedule(new TimeOutQuestion(),0,5000);
            } else {
                dos.writeUTF("Error: Wrong command: " + received + "\n3/3: Type \"Start\" to start the gameplay" );
            }
        } else {
            if (received.equals("Start")) {
                dos.writeUTF("You have to wait for other players to join and set their usernames" );
            } else {
                dos.writeUTF("Error: Wrong command: " + received );
            }
        }
    }

    public void playerLobbyHandler(String received) throws IOException {
        if (!this.isUsernameSetted) {
            this.setUsername(received);
            Server.numberOfSettedUsernames += 1;
            dos.writeUTF("You have successfully set your username");

            if (Server.numberOfPlayers < Server.numberOfPlayersPerGame) {
                dos.writeUTF("Waiting for other players to join the game..." );
            } else if ((Server.numberOfPlayers == Server.numberOfPlayersPerGame) && (Server.numberOfSettedUsernames != Server.numberOfPlayersPerGame )) {
                dos.writeUTF("Waiting for other players to set their usernames..." );
            } else {
                dos.writeUTF("Waiting for admin to start the gameplay..." );
                Server.ar.get(0).dos.writeUTF("It looks like everyone has joined the game and set their usernames\n3/3: Type \"Start\" to start the gameplay");
            }
        } else if (!Server.gameInProgress) {
            dos.writeUTF("Error: Wrong command: " + received );
        } else {
            tempAnswer = received;
            answerInLastStream = true;
        }
    }

    public void gameplayHandler() throws IOException {
        String received = "";

        if (answerInLastStream) {
            received = tempAnswer;
            answerInLastStream = false;
        } else {
            received = dis.readUTF();
        }

        if (!isBlocked) {
            System.err.println("game functionality\n correctAnswer = " + Server.correctAnswer);
            System.err.println(" receivedAnswer by " + this.getUsername() + " is: " + received);
            if (received.equals(Server.correctAnswer)) {
                this.numberOfPoints += 1;
                System.err.println(" correct answer by " + this.getUsername() + " number of points = " + this.numberOfPoints);
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimeOutQuestion(),0,5000);
                this.isBlocked = true;
            } else if (received.equals("a") || received.equals("b") || received.equals("c")) {
                System.err.println(" wrong answer by " + this.getUsername());
                this.numberOfWrongAnswers += 1;
                System.err.println(this.getUsername() + " gave  " + this.numberOfWrongAnswers + " wrong answers ");
                this.isBlocked = true;
            } else dos.writeUTF("Error: Wrong command: " + received + '\n');
        } else dos.writeUTF("Wait. You are blocked.");
    }

    @Override
    public void run() {
        String received = "";

        while (!Thread.interrupted()) {
           try {
                   if (!Server.gameInProgress) {
                       received = dis.readUTF();
                       System.err.println("Player with id: " + this.id + " has joined the lobby");
                       if (this.id == 1 ) {
                           adminLobbyHandler(received);
                       } else {
                           playerLobbyHandler(received);
                       }
                   } else {
                        gameplayHandler();
                   }
           } catch (IOException e) {
               System.err.println(this.username + " disconnected");
               Server.numberOfPlayers -= 1;
               Server.ar.remove(this);
               break;
           }
        }

        try {
            this.dis.close();
            this.dos.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
