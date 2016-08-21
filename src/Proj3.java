/*Author: Damon Godwin
  Date: 3/21/2016
  Desc: This program allows a user to play the game Othello against an AI opponent
  which uses iterative deepening minimax with alpha beta pruning to determine an optimum move.*/
import java.util.ArrayList;
import java.util.Scanner;

public class Proj3 {
	public static void main(String[] args) {
		//Variable declarations
		String[][] board = new String[6][6];
		String humanColor = new String();
		String aiColor = new String();
		int moveX = 0;
		int moveY = 0;
		int colorChoice = 0;
		int turn = 1;
		boolean cont = false;
		Board gameBoard = new Board();
		ArrayList<Integer> piecesToFlip = new ArrayList<Integer>();
		Scanner sc = new Scanner(System.in);
		
		//The human decides which color to play as.
		while(!cont) {
			System.out.println("1. Black\n2. White\nSelect which color you would like to play as (black goes first):");
			colorChoice = sc.nextInt();
			if(colorChoice == 1) {
				humanColor = "B";
				aiColor = "W";
				cont = true;
			}
			else if(colorChoice == 2) {
				humanColor = "W";
				aiColor = "B";
				cont = true;
			}
			else {
				System.out.println("Invalid selection. Try again.");
			}
		}
		
		//Initialize the board.
		initBoard(board);
		//Display the board.
		displayBoard(board);
		
		//Play the game until no more moves can be made.
		while(true) {
			int[] bestMove = new int[3];
			piecesToFlip = new ArrayList<Integer>();
			//If it's an odd numbered turn, black takes it's turn.
			if(turn % 2 == 1) {
				//If the human is black, have the human decide which move to make.
				if(humanColor.compareTo("B") == 0) {
					//Loop until the human enters a valid move.
					while(true) {
						System.out.println("It is your turn. Enter a row and column, separated by a space, to place your piece:");
						moveX = sc.nextInt();
						moveY = sc.nextInt();
						//If the move is valid, make the move. Otherwise, print an error message and get new input.
						if(checkMove(board, moveX, moveY, humanColor, aiColor, piecesToFlip)) {
							makeMove(board, moveX, moveY, humanColor, piecesToFlip);
							break;
						}
						else
							System.out.println("The move you have selected is invalid. Please try a different move.");
					}
				}
				//If the ai is black, have it decide it's turn.
				else {
					System.out.println("It is the computer's turn.");
					gameBoard.board = board;
					//Ai decides it's move.
					decideMove(gameBoard, true, aiColor, humanColor, bestMove);
					//Though the move is being checked, this function is only being called to get which pieces to flip.
					checkMove(board, bestMove[0], bestMove[1], aiColor, humanColor, piecesToFlip);
					//Make the move.
					makeMove(board, bestMove[0], bestMove[1], aiColor, piecesToFlip);
				}
			}
			//It is an even turn, so it is white's turn.
			else {
				//If the human is white, have it decide it's turn.
				if(humanColor.compareTo("W") == 0) {
					while(true) {
						System.out.println("It is your turn. Enter a row and column, separated by a space, to place your piece:");
						moveX = sc.nextInt();
						moveY = sc.nextInt();
						if(checkMove(board, moveX, moveY, humanColor, aiColor, piecesToFlip)) {
							makeMove(board, moveX, moveY, humanColor, piecesToFlip);
							break;
						}
						else
							System.out.println("The move you have selected is invalid. Please try a different move.");
					}
				}
				//If the ai is white, have it decide it's turn.
				else {
					System.out.println("It is the computer's turn.");
					gameBoard.board = board;
					decideMove(gameBoard, true, aiColor, humanColor, bestMove);
					checkMove(board, bestMove[0], bestMove[1], aiColor, humanColor, piecesToFlip);
					makeMove(board, bestMove[0], bestMove[1], aiColor, piecesToFlip);
				}
			}
			//Display the board.
			displayBoard(board);
			//Increment the turn.
			turn++;
			//Check to see if there are any moves for the next player.
			//If there aren't, then the game is over.
			if(turn % 2 == 1) {
				if(gameOver(board, "B", "W")) {
					System.out.println("Game Over. White Wins.");
					break;
				}
			}
			else {
				if(gameOver(board, "W", "B")) {
					System.out.println("Game Over. Black Wins.");
					break;
				}
			}
		}
		sc.close();
	}
	
	//Function initializes the board.
	public static void initBoard(String[][] board) {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				if((i == 2 && j == 2) || (i == 3 && j == 3))
					board[i][j] = "W";
				else if((i == 2 && j == 3) || (i == 3 && j == 2))
					board[i][j] = "B";
				else
					board[i][j] = "*";
			}
		}
	}
	
	//Function displays the board.
	public static void displayBoard(String[][] board) {
		System.out.println(" 012345");
		for(int i = 0; i < 6; i++) {
			System.out.print(i);
			for(int j = 0; j < 6; j++) {
				System.out.print(board[i][j]);
			}
			System.out.println();
		}
	}
	
	//Function creates a copy of the board.
	public static void copyBoard(String[][] board, String[][] newBoard) {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				newBoard[i][j] = board[i][j];
			}
		}
	}
	
	//Computer determines its move.
	public static void decideMove(Board gameBoard, boolean isMax, String plyColor, String oppColor, int[] bestMove) {
		/*An array is used to hold the coordinates of the best move as well as a bit that indicates whether or not to terminate the iterative deepening search.
		  bestMove[0] holds the row number of the best move,
		  bestMove[1] holds the column number of the best move,
		  bestMove[2] holds the a 0 or a 1 to indicate whether or not to terminate the search.*/
		bestMove[2] = 0;
		int bestScore = 0;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		//Continue calling the search algorithm until bestMove[2] is 1.
		for(int d = 0;; d++) {
			bestScore = AB_minimax(d, gameBoard, isMax, plyColor, oppColor, bestMove, alpha, beta);
			if(bestMove[2] == 1)
				break;
		}
		System.out.println("Minimax score: " + bestScore);
	}
	
	//Function implements the minimax algorithm with alpha beta pruning.
	public static int AB_minimax(int d, Board currBoard, boolean isMax, String plyColor, String oppColor, int[] bestMove, int alpha, int beta) {
		//Variable declarations.
		String swap;
		int bestScore = 0;
		ArrayList<Board> children = new ArrayList<Board>();
		ArrayList<Integer> scores = new ArrayList<Integer>();
		
		//If the depth is 0, return the heuristic score.
		if(d == 0) {
			return evalScore(currBoard, oppColor);
		}
		//Otherwise if a terminal node is reached, or if depth 3 is reached, set bestMove[2] = 1 and return the heuristic score.
		else if(gameOver(currBoard.board, plyColor, oppColor) || d == 3) {
			bestMove[2] = 1;
			return evalScore(currBoard, oppColor);
		}
		
		//Expand the current board to get all valid moves.
		expand(currBoard, plyColor, oppColor, children);
		
		//If the current player is the maximizing player.
		if(isMax) {
			//Switch which player is playing, for the next call to minimax.
			swap = plyColor;
			plyColor = oppColor;
			oppColor = swap;
			bestScore = Integer.MIN_VALUE;
			//Loop through all children, calling minimax until it's time to terminate.
			for(int i = 0; i < children.size(); i++) {
				//Compare the previous best score with the score that is returned from the minimax call.
				//The max of these is added to an ArrayList of scores.
				scores.add(Math.max(bestScore, AB_minimax(d - 1, children.get(i), false, plyColor, oppColor, bestMove, alpha, beta)));
				//Alpha is found by taking the max of the best score so far, the previously found alpha value.
				alpha = Math.max(alpha, scores.get(i));
				//The search is cutoff if the beta value is no better than the alpha value.
				if(beta <= alpha)
					break;
			}
			//Loop through all the best scores stored and based on the which value is highest, get the corresponding move.
			for(int i = 0; i < scores.size(); i++) {
				if(scores.get(i) > bestScore) {
					bestScore = scores.get(i);
					bestMove[0] = children.get(i).moveX;
					bestMove[1] = children.get(i).moveY;
				}
			}
			return bestScore;
		}
		//If the current player is the minimizing player.
		else {
			//Switch which player is playing, for the next call to minimax.
			swap = plyColor;
			plyColor = oppColor;
			oppColor = swap;
			bestScore = Integer.MAX_VALUE;
			//Loop through all children, calling minimax until it's time to terminate.
			for(int i = 0; i < children.size(); i++) {
				//Compare the previous best score with the score that is returned from the minimax call.
				//The minimum of these is added to an ArrayList of scores.
				scores.add(Math.min(bestScore, AB_minimax(d - 1, children.get(i), true, plyColor, oppColor, bestMove, alpha, beta)));
				//Beta is found by taking the minimum of the best score so far, the previously found beta value.
				beta = Math.min(beta, scores.get(i));
				//The search is cutoff if the beta value is no better than the alpha value.
				if(beta <= alpha)
					break;
			}
			//Loop through all the best scores stored and based on the which value is highest, get the corresponding move.
			for(int i = 0; i < scores.size(); i++) {
				if(scores.get(i) < bestScore) {
					bestScore = scores.get(i);
					bestMove[0] = children.get(i).moveX;
					bestMove[1] = children.get(i).moveY;
				}
			}
			return bestScore;
		}
	}
	
	//Function expands a board state.
	public static void expand(Board currBoard, String plyColor, String oppColor, ArrayList<Board> children) {
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				Board newBoard = new Board();
				ArrayList<Integer> piecesToFlip = new ArrayList<Integer>();
				String[][] child = new String[6][6];
				copyBoard(currBoard.board, child);
				if(checkMove(child, i, j, plyColor, oppColor, piecesToFlip)) {
					makeMove(child, i, j, plyColor, piecesToFlip);
					newBoard.board = child;
					newBoard.moveX = i;
					newBoard.moveY = j;
					children.add(newBoard);
				}
			}
		}
	} 
	
	//Function returns a heuristic score to be used in minimax.
	//The heuristic score is calculated by counting the number of pieces on the board for the current player.
	public static int evalScore(Board currBoard, String oppColor) {
		int score = 0;
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				if(currBoard.board[i][j].compareTo(oppColor) == 0)
					score++;
			}
		}
		return score;
	}
	
	//Function checks if a move is valid.
	public static boolean checkMove(String[][] board, int moveX, int moveY, String plyColor, String oppColor, ArrayList<Integer> piecesToFlip) {
		//Variable declarations
		int i = 0;
		int j = 0;
		int l = 0;
		boolean upLft = false;
		boolean up = false;
		boolean upRgt = false;
		boolean lft = false;
		boolean rgt = false;
		boolean dwnLft = false;
		boolean dwn = false;
		boolean dwnRgt = false;
		int numOfOpp = 0;
					
		//Make a copy of the board to check the move before actually making it.
		String[][] tmpBoard = new String[6][6];
		copyBoard(board, tmpBoard);
					
		//Check if the move will be outside of the board's bounds.
		if(moveX < 0 || moveX > 6)
			return false;
		if(moveY < 0 || moveY > 6)
			return false;
					
		//Check if the space is already occupied.
		if(tmpBoard[moveX][moveY].compareTo("*") != 0)
			return false;
					
		//If the space is on the board and not occupied, try the move to see if it's valid.
		tmpBoard[moveX][moveY] = plyColor;
				
		//Loop up and left if possible
		if(moveX - 1 >= 1 && moveY - 1 >= 1) {
			//If the adjacent space is occupied by the opposing player's piece, keep looking.
			if(tmpBoard[moveX - 1][moveY - 1].compareTo(oppColor) == 0) {
				i = moveX - 1;
				j = moveY - 1;
				while(i >= 0 && j >= 0) {
					//If a space is occupied by the opposing player's piece, increment a counter.
					if(tmpBoard[i][j].compareTo(oppColor) == 0) {
						numOfOpp++;
						piecesToFlip.add(i);
						piecesToFlip.add(j);
					}
					//If the space is empty, break the loop. 
					if(tmpBoard[i][j].compareTo("*") == 0)
						break;
					/*If the space is occupied by the player's piece and there was at least one opposing piece,
					  then the move is valid and the loop is broken.*/
					if(tmpBoard[i][j].compareTo(plyColor) == 0 && numOfOpp > 0) {
						upLft = true;
						break;
					}
					i--;
					j--;
				}
			}
		}
		//If the search was false, remove the pieces to flip that were previously found.
		if(!upLft) {
			l = piecesToFlip.size() - 1;
			for(int n = 0; n < numOfOpp; n++) {
				piecesToFlip.remove(l);
				piecesToFlip.remove(l - 1);
				l = l - 2;
			}
		}
		numOfOpp = 0;
		//Look up if possible
		if(moveX - 1 >= 1) {
			if(tmpBoard[moveX - 1][moveY].compareTo(oppColor) == 0) {
				for(int k = moveX - 1; k >= 0; k--) {
					if(tmpBoard[k][moveY].compareTo(oppColor) == 0) {
						numOfOpp++;
						piecesToFlip.add(k);
						piecesToFlip.add(moveY);
					}
					if(tmpBoard[k][moveY].compareTo("*") == 0)
						break;
					if(tmpBoard[k][moveY].compareTo(plyColor) == 0 && numOfOpp > 0) {
						up = true;
						break;
					}
				}
			}
		}
		if(!up) {
			l = piecesToFlip.size() - 1;
			for(int n = 0; n < numOfOpp; n++) {
				piecesToFlip.remove(l);
				piecesToFlip.remove(l - 1);
				l = l - 2;
			}
		}
		numOfOpp = 0;	
		//Loop up and right if possible
		if(moveX - 1 >= 1 && moveY + 1 <= 4) {
			if(tmpBoard[moveX - 1][moveY + 1].compareTo(oppColor) == 0) {
				i = moveX - 1;
				j = moveY + 1;
				while(i >= 0 && j < 6) {
					if(tmpBoard[i][j].compareTo(oppColor) == 0) {
						numOfOpp++;
						piecesToFlip.add(i);
						piecesToFlip.add(j);
					}
					if(tmpBoard[i][j].compareTo("*") == 0)
						break;
					if(tmpBoard[i][j].compareTo(plyColor) == 0 && numOfOpp > 0) {
						upRgt = true;
						break;
					}
					i--;
					j++;
				}
			}
		}
		if(!upRgt) {
			l = piecesToFlip.size() - 1;
			for(int n = 0; n < numOfOpp; n++) {
				piecesToFlip.remove(l);
				piecesToFlip.remove(l - 1);
				l = l - 2;
			}
		}
		numOfOpp = 0;
		//Look left if possible
		if(moveY - 1 >= 1) {
			if(tmpBoard[moveX][moveY - 1].compareTo(oppColor) == 0) {
				for(int k = moveY - 1; k >= 0; k--) {
					if(tmpBoard[moveX][k].compareTo(oppColor) == 0) {
						numOfOpp++;
						piecesToFlip.add(moveX);
						piecesToFlip.add(k);
					}
					if(tmpBoard[moveX][k].compareTo("*") == 0)
						break;
					if(tmpBoard[moveX][k].compareTo(plyColor) == 0 && numOfOpp > 0) {
						lft = true;
						break;
					}
				}
			}
		}
		if(!lft) {
			l = piecesToFlip.size() - 1;
			for(int n = 0; n < numOfOpp; n++) {
				piecesToFlip.remove(l);
				piecesToFlip.remove(l - 1);
				l = l - 2;
			}
		}
		numOfOpp = 0;
		//Look right if possible
		if(moveY + 1 <= 4) {
			if(tmpBoard[moveX][moveY + 1].compareTo(oppColor) == 0) {
				for(int k = moveY + 1; k < 6; k++) {
					if(tmpBoard[moveX][k].compareTo(oppColor) == 0) {
						numOfOpp++;
						piecesToFlip.add(moveX);
						piecesToFlip.add(k);
					}
					if(tmpBoard[moveX][k].compareTo("*") == 0)
						break;
					if(tmpBoard[moveX][k].compareTo(plyColor) == 0 && numOfOpp > 0) {
						rgt = true;
						break;
					}
				}
			}
		}
		if(!rgt) {
			l = piecesToFlip.size() - 1;
			for(int n = 0; n < numOfOpp; n++) {
				piecesToFlip.remove(l);
				piecesToFlip.remove(l - 1);
				l = l - 2;
			}
		}
		numOfOpp = 0;
		//Look down and left if possible
		if(moveX + 1 <= 4 && moveY - 1 >= 1) {
			if(tmpBoard[moveX + 1][moveY - 1].compareTo(oppColor) == 0) {
				i = moveX + 1;
				j = moveY - 1;
				while(i < 6 && j >= 0) {
					if(tmpBoard[i][j].compareTo(oppColor) == 0) {
						numOfOpp++;
						piecesToFlip.add(i);
						piecesToFlip.add(j);
					}
					if(tmpBoard[i][j].compareTo("*") == 0)
						break;
					if(tmpBoard[i][j].compareTo(plyColor) == 0 && numOfOpp > 0) {
						dwnLft = true;
						break;
					}
					i++;
					j--;
				}
			}
		}
		if(!dwnLft) {
			l = piecesToFlip.size() - 1;
			for(int n = 0; n < numOfOpp; n++) {
				piecesToFlip.remove(l);
				piecesToFlip.remove(l - 1);
				l = l - 2;
			}
		}
		numOfOpp = 0;
		//Look down if possible
		if(moveX + 1 <= 4) {
			if(tmpBoard[moveX + 1][moveY].compareTo(oppColor) == 0) {
				for(int k = moveX + 1; k < 6; k++) {
					if(tmpBoard[k][moveY].compareTo(oppColor) == 0) {
						numOfOpp++;
						piecesToFlip.add(k);
						piecesToFlip.add(moveY);
					}
					if(tmpBoard[k][moveY].compareTo("*") == 0)
						break;
					if(tmpBoard[k][moveY].compareTo(plyColor) == 0 && numOfOpp > 0) {
						dwn = true;
						break;
					}
				}
			}
		}
		if(!dwn) {
			l = piecesToFlip.size() - 1;
			for(int n = 0; n < numOfOpp; n++) {
				piecesToFlip.remove(l);
				piecesToFlip.remove(l - 1);
				l = l - 2;
			}
		}
		numOfOpp = 0;
		//Look down and right if possible
		if(moveX + 1 <= 4 && moveY + 1 <= 4) {
			if(tmpBoard[moveX + 1][moveY + 1].compareTo(oppColor) == 0) {
				i = moveX + 1;
				j = moveY + 1;
				while(i < 6 && j < 6) {
					if(tmpBoard[i][j].compareTo(oppColor) == 0) {
						numOfOpp++;
						piecesToFlip.add(i);
						piecesToFlip.add(j);
					}
					if(tmpBoard[i][j].compareTo("*") == 0)
						break;
					if(tmpBoard[i][j].compareTo(plyColor) == 0 && numOfOpp > 0) {
						dwnRgt = true;
						break;
					}
					i++;
					j++;
				}
			}
		}
		if(!dwnRgt) {
			l = piecesToFlip.size() - 1;
			for(int n = 0; n < numOfOpp; n++) {
				piecesToFlip.remove(l);
				piecesToFlip.remove(l - 1);
				l = l - 2;
			}
		}
		//If there were valid moves return true.
		if(upLft || up || upRgt || lft || rgt || dwnLft || dwn || dwnRgt)
			return true;
		else
			return false;
	}
	
	//Function makes a move.
	public static void makeMove(String[][] board, int moveX, int moveY, String plyColor, ArrayList<Integer> piecesToFlip) {
		int x = 0;
		int y = 0;
		board[moveX][moveY] = plyColor;
		for(int i = piecesToFlip.size() - 1; i > 0; i-=2) {
			y = piecesToFlip.get(i);
			x = piecesToFlip.get(i - 1);
			board[x][y] = plyColor;
		}
	}
			
	//Function determines if the game is over.
	public static boolean gameOver(String[][] board, String plyColor, String oppColor) {
		ArrayList<Integer> noFlip = new ArrayList<Integer>();
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				if(checkMove(board, i, j, plyColor, oppColor, noFlip)) {
					return false;
				}
			}
		}
		return true;
	}
}
