package com.company;

import java.io.*;
import java.util.*;
import java.net.*;

// ClientHandler class
class ClientHandler implements Runnable
{
    Scanner scn = new Scanner(System.in);
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;

    // constructor
    public ClientHandler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin=true;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {

        String received;
        while (true)
        {
            try
            {

                // receive the string
                received = dis.readUTF();

                System.out.println(this.name+": "+received);

                if(received.equals("Start") & this.name.equals("Player 1")){
                    Server.gameInProgress=true;
                    Server.startGame();
                }

                if(received.equals("logout")){
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }


            } catch (IOException e) {

                e.printStackTrace();
            }

        }
        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
