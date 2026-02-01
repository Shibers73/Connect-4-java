package com.sca.connect4;

public class Board {
	private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final char EMPTY = '.';
    private static final char PLAYER1 = 'X';
    private static final char PLAYER2 = 'O';
    
    private char[][] board;
    private char curr;	//Current player
    private int gameState; //0: In progress | 1: player1Win | 2: player2:Win | 3: Draw
    
    public Board(){
    	init();
    }
    
    public void init() {
    	gameState = 0;
    	curr = PLAYER1;
    	board = new char[ROWS][COLS];
    	
    	for(int i = 0; i < ROWS; i++) {
    		for(int j = 0; j < COLS; j++) {
    			this.board[i][j] = EMPTY;
    		}
    	}
    	
    	
    }
    
    public boolean isValid(int col) {
        if(col >= 0 && col < COLS && board[0][col] == EMPTY) return true;
        return false;
    }
    
    public int getY(int col) {
        //Find the first empty row, starting from the bottom
        for(int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == EMPTY) {
                return row;
            }
        }
        return -1; //Full
    }
    
    private boolean checkWin(int row, int col) {	//Check all directions, only 4+ in one direction is needed for a win, no need to return the winner since it's going to be the current player, we check it the moment the circle is to be placed. Hypothetically if we were to feed a pre-made board with a winner our code wouldn't be able to get a result until one makes a move which will cause problems if player1 is curr and player2 is the winner but player1 wins with his new move, making both winners
        return checkDirection(row, col, 0, 1) ||	//Horizontal
               checkDirection(row, col, 1, 0) ||	//Vertical
               checkDirection(row, col, 1, 1) ||	//Diagonal \ (up left)
               checkDirection(row, col, 1, -1);		//Diagonal / (up right)
    }
    
    //Scroll all the way down for the direction explanation
    private boolean checkDirection(int row, int col, int dRow, int dCol) {
        int count = 1;
        count += countInDirection(row, col, dRow, dCol);
        count += countInDirection(row, col, -dRow, -dCol);
        return count >= 4;	//It's called connect 4 for a reason
    }
    
    private int countInDirection(int row, int col, int dRow, int dCol) {
        int count = 0;
        int r = row + dRow;	//Row
        int c = col + dCol;	//Column
        
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == curr) {
            count++;
            //Move to the next position in the desired direction
            r += dRow;	//Row
            c += dCol;	//Column
        }
        
        return count;
    }
    
    private boolean isFull() {
        for (int j = 0; j < COLS; j++) {	//Check the top row, if none are empty then it's full due to the nature of how connect 4 works
            if (board[0][j] == EMPTY) {
                return false;
            }
        }
        return true;
    }
    
    private void switchPlayer() {
    	curr = (curr == PLAYER1) ? PLAYER2 : PLAYER1;	//curr: player1 -> curr: player2 and viceversa
    }
    
    public char getCurrentPlayer() {
        return curr;
    }
    
    public int getGameState() {
    	return gameState;
    }
    
    public String getBoardString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n 1 2 3 4 5 6 7\n");
        sb.append("---------------\n");
        for (int i = 0; i < ROWS; i++) {
            sb.append("|");
            for (int j = 0; j < COLS; j++) {
                sb.append(board[i][j]).append("|");
            }
            sb.append("\n");
        }
        sb.append("---------------\n");
        return sb.toString();
    }
    
    public boolean putPiece(int col) {
    	if(!isValid(col)) {
            return false;
        }
        
        int row = getY(col);
        board[row][col] = curr;
    	
    	if (checkWin(row, col)) {
    		gameState = (curr == PLAYER1) ?  1 : 2; //If the winning move is made by player1 then set it to 1, otherwise it's 2
    	} else if (isFull()) {
    		gameState = 3;	//If the board is full and nobody made the winning move then it's a draw
    	} else {
    		switchPlayer(); //No winning move has been made and the game can continue
    	}
    	
    	return true;
    }
}

/*	Direction table
	dRow	dCol	Direction
	-1		-1			↖
	-1		0			↑
	-1		1			↗
	0		-1			←
	0		1			→
	1		-1			↙
	1		0			↓
	1		1			↘
 */
/*	Direction graph
	(-1,-1)  (-1, 0)  (-1, 1)
   		↖       ↑        ↗
   
	( 0,-1)    	      ( 0, 1)
   	   ←              	 →
   
	( 1,-1)  ( 1, 0)  ( 1, 1)
   		↙       ↓        ↘
 */
/*	Board graph  
	(0, 0) (0, 1) (0, 2) (0, 3) (0, 4) (0, 5) (0, 6)
	(1, 0) (1, 1) (1, 2) (1, 3) (1, 4) (1, 5) (1, 6)
	(2, 0) (2, 1) (2, 2) (2, 3) (2, 4) (2, 5) (2, 6)
	(3, 0) (3, 1) (3, 2) (3, 3) (3, 4) (3, 5) (3, 6)
	(4, 0) (4, 1) (4, 2) (4, 3) (4, 4) (4, 5) (4, 6)
	(5, 0) (5, 1) (5, 2) (5, 3) (5, 4) (5, 5) (5, 6)
*/
