package com.company;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    static final int SERVERPORT = 1234;

    public static void main(String[] args) throws IOException {
        InetAddress ip = InetAddress.getByName("localhost");
        Socket s = new Socket(ip, SERVERPORT);
        final Scanner scn = new Scanner(System.in);
        final DataInputStream dis = new DataInputStream(s.getInputStream());
        final DataOutputStream dos = new DataOutputStream(s.getOutputStream());

        Thread sendMessage = new Thread(() -> {
            while (true) {
                String msg = scn.nextLine();

                try {
                    dos.writeUTF(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread readMessage = new Thread(() -> {
            while (true) {
                try {
                    String msg = dis.readUTF();
                    System.out.println(msg);
                } catch (IOException e) {
                    System.out.println("Connection with server lost");
                    break;
                }
            }
        });

        sendMessage.start();
        readMessage.start();
    }
}
