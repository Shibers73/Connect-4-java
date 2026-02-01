package com.sca.connect4.server;
import java.io.*;
import java.net.*;
import com.sca.connect4.Board;

class GameServer {
    private static final int PORT = 8888;
    private Board board;
    private ClientHandler[] clients;
    private boolean gameEnded = false;
    private boolean gameStarted = false;

    public GameServer() {
        board = new Board();
        clients = new ClientHandler[2];
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for 2 players...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                //Find the first available slot (in case a player disconnected before game started)
                int slotIndex = -1;
                for (int i = 0; i < 2; i++) {
                    if (clients[i] == null || !clients[i].isConnected()) {
                        slotIndex = i;
                        break;
                    }
                }
                
                if (slotIndex == -1) {
                    //No slot available, reject connection
                    clientSocket.close();
                    continue;
                }
                
                int playerId = slotIndex + 1;
                ClientHandler handler = new ClientHandler(clientSocket, playerId, this);
                clients[slotIndex] = handler;
                new Thread(handler).start();
                System.out.println("Player " + playerId + " connected");
                
                //Check if both players are now connected
                int connectedCount = 0;
                for (int i = 0; i < 2; i++) {
                    if (clients[i] != null && clients[i].isConnected()) {
                        connectedCount++;
                    }
                }
                
                //Start game only when both players are connected
                if (connectedCount == 2) {
                    gameStarted = true;
                    System.out.println("Game started!");

                    broadcast("GAME_START");
                    broadcast(board.getBoardString());
                    broadcast("TURN:" + board.getCurrentPlayer());
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public synchronized void processMove(int playerId, int column) {
        if (gameEnded) {
            return;
        }

        char expectedPlayer = (playerId == 1) ? 'X' : 'O';

        //Check if it's the right turn
        if (board.getCurrentPlayer() != expectedPlayer) {
            sendToPlayer(playerId, "INVALID:Not your turn");
            return;
        }

        //Validate and make the move
        if (board.putPiece(column)) {
            //Move accepted
            broadcast("MOVE:" + (column + 1) + ":" + expectedPlayer);
            broadcast(board.getBoardString());

            //Check game state
            int state = board.getGameState();
            if (state == 1) {
                gameEnded = true;
                broadcast("WIN:1");
                printGameResult(1, "Player 1 (X)");
            } else if (state == 2) {
                gameEnded = true;
                broadcast("WIN:2");
                printGameResult(2, "Player 2 (O)");
            } else if (state == 3) {
                gameEnded = true;
                broadcast("DRAW");
                printGameResult(0, "DRAW");
            } else {
                //Game continues, send next turn
                broadcast("TURN:" + board.getCurrentPlayer());
            }

        } else {
            //Move rejected (column full or invalid)
            sendToPlayer(playerId, "INVALID:Column full or invalid");
            //Resend turn to the same player so they can try again
            sendToPlayer(playerId, "TURN:" + expectedPlayer);
        }
    }

    public synchronized void handlePlayerDisconnection(int disconnectedPlayerId) {
        if (!gameStarted) {
            System.out.println("Player " + disconnectedPlayerId + " disconnected before game started");
            System.out.println("Waiting for replacement player...\n");
            return;
        }

        if (gameEnded) {
            System.out.println("Player " + disconnectedPlayerId + " disconnected after game ended");
            return;
        }

        gameEnded = true;

        int winnerId = (disconnectedPlayerId == 1) ? 2 : 1;
        String winnerName = (winnerId == 1) ? "Player 1 (X)" : "Player 2 (O)";	//You should get the gist of it by now, come on

        System.out.println("\n⚠️  Player " + disconnectedPlayerId + " disconnected!");
        System.out.println("🎉 " + winnerName + " wins by forfeit!");

        //Notify the other player of the win by forfeit
        broadcast("WIN:" + winnerId);
    }

    private void printGameResult(int winnerId, String winnerName) {
        if (winnerId == 0) {
            System.out.println("\nGAME ENDED: DRAW");
        } else {
            System.out.println("GAME ENDED: " + winnerName + " WINS!");
        }
    }

    private void sendToPlayer(int playerId, String message) {
        for (int i = 0; i < 2; i++) {
            if (clients[i] != null && clients[i].getID() == playerId && clients[i].isConnected()) {
                clients[i].sendMessage(message);
                break;
            }
        }
    }

    private void broadcast(String message) {
        for (int i = 0; i < 2; i++) {
            if (clients[i] != null && clients[i].isConnected()) {
                clients[i].sendMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}