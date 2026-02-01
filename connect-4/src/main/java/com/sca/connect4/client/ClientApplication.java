package com.sca.connect4.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

class ClientApplication {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int MAX_ATTEMPTS = 10; // Massimo 10 tentativi
    private static final int MAX_WAIT = 64; // Massimo 64 secondi di attesa
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    private Scanner scanner = new Scanner(System.in);
    
    public ClientApplication() {
        int attempts = 0;
        
        while (attempts < MAX_ATTEMPTS) {	//Backoff
            try {
                System.out.println("Attempting to connect to server... (attempt " + (attempts + 1) + "/" + MAX_ATTEMPTS + ")");
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                System.out.println("Connected to server!");
                return; //Successful
                
            } catch (IOException e) {
                attempts++;
                
                if (attempts >= MAX_ATTEMPTS) {
                    System.err.println("Could not connect to server after " + MAX_ATTEMPTS + " attempts.");
                    System.err.println("Please make sure the server is running on " + SERVER_HOST + ":" + SERVER_PORT);
                    System.exit(1);
                }
                
                //2^attempts seconds (max 64)
                int waitTime = Math.min((int)Math.pow(2, attempts), MAX_WAIT);
                System.out.println("Connection failed: " + e.getMessage());
                System.out.println("Retrying in " + waitTime + " seconds...\n");
                
                try {
                    Thread.sleep(waitTime * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("Connection attempts interrupted");
                    System.exit(1);
                }
            }
        }
    }
    
    public void play() {
        
        ServerMessageReceiver receiver = new ServerMessageReceiver(in);
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();
        
        //Main input loop
		while (!receiver.isGameOver()) {
		    System.out.print("Enter column (1-7) or 'q' to quit: ");
		    String input = scanner.nextLine().trim();
		    
		    if (input.equalsIgnoreCase("q")) {
		        break;
		    }
		    
		    //Check if it's the player's turn
		    if (!receiver.isMyTurn()) {
		        System.out.println("❌ Wait for your turn!");
		        continue;
		    }
		    
		    try {
		        int col = Integer.parseInt(input);
		        
		        if (col >= 1 && col <= 7) {
		            //MOVE:column
		            out.println("MOVE:" + (col - 1));
		            receiver.setMyTurn(false); //Block input until server confirms
		        } else {
		            System.out.println("Invalid column! Enter 1-7.");
		        }
		    } catch (NumberFormatException e) {
		        System.out.println("Invalid input! Enter a number 1-7.");
		    }
		}
		
		closeClient();
    }
    
    public void closeClient() {
    	try {
			socket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
        scanner.close();
    }
    
    public static void main(String[] args) {
        ClientApplication client = new ClientApplication();
        client.play();
    }
}