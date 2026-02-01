package com.sca.connect4.server;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private GameServer server;
    private int playerId;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = true;

    public ClientHandler(Socket socket, int playerId, GameServer server) {
        this.server = server;
        this.playerId = playerId;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating handler: " + e.getMessage());
            isConnected = false;
        }
    }

    public void sendMessage(String message) {
        try {
            if (isConnected && out != null) {
                out.println(message);
                out.flush();
                if (out.checkError()) {
                    isConnected = false;
                }
            }
        } catch (Exception e) {
            isConnected = false;
        }
    }

    public int getID() {
        return this.playerId;
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void run() {
        try {
            out.println("PLAYER:" + playerId + ":" + (playerId == 1 ? "X" : "O"));
            out.flush();

            //Receive loop
            String line;
            while ((line = in.readLine()) != null) {
                //MOVE:column
                if (line.startsWith("MOVE:")) {
                    try {
                        int column = Integer.parseInt(line.substring(5));
                        server.processMove(playerId, column);
                    } catch (NumberFormatException e) {
                        out.println("INVALID:Bad format");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Player " + playerId + " disconnected");
        } finally {	//If anything happens go here
            isConnected = false;
            //Notify the server that this player has disconnected
            server.handlePlayerDisconnection(playerId);
        }
    }
}