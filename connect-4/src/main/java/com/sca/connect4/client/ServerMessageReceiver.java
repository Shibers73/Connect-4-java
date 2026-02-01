package com.sca.connect4.client;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerMessageReceiver implements Runnable {
	
	private int playerId;
    private char playerSymbol;
    private BufferedReader in;
    
    private boolean isMyTurn = false;
    private boolean gameOver = false;
	
    public ServerMessageReceiver(BufferedReader in) {
    	this.in = in;
    }
    
    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                handleServerMessage(line);
            }
        } catch (IOException e) {
            System.err.println("Disconnected from server");
            gameOver = true;
        }
    }
    
    public boolean isGameOver() {
    	return gameOver;
    }
    
    public boolean isMyTurn() {
    	return isMyTurn;
    }
    
    public void setMyTurn(boolean isMyTurn) {
    	this.isMyTurn = isMyTurn;
    }
    
    private void handleServerMessage(String message) {
        if (message.startsWith("PLAYER:")) {
            //PLAYER:Player:Symbol
            String[] parts = message.split(":");
            playerId = Integer.parseInt(parts[1]);
            playerSymbol = parts[2].charAt(0);
            System.out.println("\n>>> You are Player " + playerId + " (" + playerSymbol + ")");
            
        } else if (message.equals("GAME_START")) {
            System.out.println("\n>>> Game started! Player 1 (X) goes first.");
            
        } else if (message.startsWith("MOVE:")) {
            //MOVE:Column:Symbol
            String[] parts = message.split(":");
            System.out.println("\n>>> Player " + parts[2] + " played column " + parts[1]);
            
        } else if (message.startsWith("TURN:")) {
            //TURN:Symbol
            char turn = message.split(":")[1].charAt(0);
            if (turn == playerSymbol) {
                System.out.println("\n>>> YOUR TURN!");
                isMyTurn = true;
            } else {
                System.out.println("\n>>> Waiting for opponent...");
                isMyTurn = false;
            }
            
        } else if (message.startsWith("WIN:")) {
            //WIN:Player
            int winner = Integer.parseInt(message.split(":")[1]);
            if (winner == playerId) {
                System.out.println("\n>>> 🎉 YOU WIN! 🎉");
            } else {
                System.out.println("\n>>> You lost. Player " + winner + " wins!");
            }
            gameOver = true;
            isMyTurn = false;
            
        } else if (message.equals("DRAW")) {
            System.out.println("\n>>> Game ended in a DRAW!");
            gameOver = true;
            isMyTurn = false;
            
        } else if (message.startsWith("INVALID:")) {
        	//INVALID:Message
            System.out.println("\n>>> ❌ " + message.substring(8));
            if (message.contains("Not your turn")) {
                isMyTurn = false;	//Fix the turn
            } else {
                isMyTurn = true; //Try again
            }
        } else {
            //Board display or misc text
            System.out.println(message);
        }
    }
}