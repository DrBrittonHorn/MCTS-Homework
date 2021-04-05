package tictactoe.game;

import java.util.ArrayList;
import java.util.List;

import tictactoe.game.GamePiece;
import tictactoe.game.Position;

public final class Game {
	private List<Position> board;
	private int turn = 1, boardWidth = 3, boardHeight = 3;
	private boolean gameOver;
	
	public Game() {
		board = new ArrayList<Position>();
		for (int x = 0; x < boardWidth; x++)
		{
			for (int y = 0; y < boardHeight; y++)
			{
				board.add(new Position(x, y, new GamePiece("",0)));
			}
		}
	}
	
	public List<Position> getBoard()
	{
		return this.board;
	}
	
	public int getBoardWidth()
	{
		return this.boardWidth;
	}
	
	public int getBoardHeight()
	{
		return this.boardHeight;
	}
	
	public int getTurn()
	{
		return this.turn;
	}
	
	public void advanceGame(Position move)
	{
		if (gameOver)
			return;
		if (board.get((move.getX() * boardHeight) + move.getY()).getPiece().getOwner() != 0)
		{
			System.out.println("Piece exists. Ignoring.");
			return;
		}
		board.get((move.getX() * boardHeight) + move.getY()).getPiece().setOwner(turn);
		
		if (isWinningBoard(this.board) != 0)
		{
			gameOver = true;
			return;
		}

		turn *= -1;
		return;
	}
	
	public int isWinningBoard(List<Position> origBoard)
	{
		int[][] tempBoard = new int[boardWidth][boardHeight];
		for (int x = 0; x < boardWidth; x++)
		{
			for (int y = 0; y < boardHeight; y++)
			{
				tempBoard[x][y] = origBoard.get((x * boardHeight) + y).getPiece().getOwner();
			}
		}
		checkValidBoard(tempBoard);
		// vertical win conditions
		for (int i = 0; i < 3; i++)
		{
			if (tempBoard[i][0] + tempBoard[i][1] + tempBoard[i][2] == 3)
			{
				System.out.println("vertical win for: 1");
				return 1;
			}
			if (tempBoard[i][0] + tempBoard[i][1] + tempBoard[i][2] == -3)
			{
				System.out.println("vertical win for: -1");
				return -1;
			}
		}
		// horizontal win conditions
		for (int i = 0; i < 3; i++)
		{
			if (tempBoard[0][i] + tempBoard[1][i] + tempBoard[2][i] == 3)
			{
				System.out.println("horizontal win for: 1");
				return 1;
			}
			if (tempBoard[0][i] + tempBoard[1][i] + tempBoard[2][i] == -3)
			{
				System.out.println("horizontal win for: -1");
				return -1;
			}
		}
		// diagonal win conditions
		if ((tempBoard[0][0] + tempBoard[1][1] + tempBoard[2][2] == 3)
			|| (tempBoard[2][0] + tempBoard[1][1] + tempBoard[0][2] == 3))
		{
			System.out.println("diagonal win for: 1");
			return 1;
		}
		if ((tempBoard[0][0] + tempBoard[1][1] + tempBoard[2][2] == -3)
				|| (tempBoard[2][0] + tempBoard[1][1] + tempBoard[0][2] == -3))
		{
				System.out.println("diagonal win for: -1");
				return -1;
		}
		
		// no win condition found
		return 0;
	}
	
	public boolean gameOver()
	{
		return this.gameOver;
	}
	
	private void checkValidBoard(int[][] tempBoard)
	{
		if (tempBoard.length != 3)
		{
			new Exception("Invalid board width.");
			System.exit(-1);
		}
		if (tempBoard[0].length != 3 ||
				tempBoard[1].length != 3 ||
				tempBoard[2].length != 3)
		{
			new Exception("Invalid board height.");
			System.exit(-1);
		}
	}
	
	public List<Position> getValidMoves(List<Position> origBoard, int turn)
	{
		ArrayList<Position> validMoves = new ArrayList<Position>();
		if (isWinningBoard(origBoard) != 0)
			return validMoves;
		
		for(int xCell = 0; xCell < boardWidth; xCell++)
		{
			for (int yCell = 0; yCell < boardHeight; yCell++)
			{
				if(origBoard.get((xCell*boardHeight+yCell)).getPiece().getOwner() == 0)
				{
					validMoves.add(new Position(xCell,yCell,new GamePiece("", turn)));
				}
			}
		}
		
		return validMoves;
	}
	
	public List<Position> simulateMove(List<Position> origBoard, Position move)
	{
		if (isWinningBoard(origBoard) != 0)
		{
			System.out.println("Game already won by: " + isWinningBoard(origBoard));
			return null;
		}
		int xCell = move.getX();
		int yCell = move.getY();
		int turn = move.getPiece().getOwner();
		//List<Position> newBoard = new ArrayList<Position>(origBoard);
		List<Position> newBoard = copyBoard(origBoard);
		if (origBoard.get((xCell*boardHeight)+yCell).getPiece().getOwner() == 0)
		{
			newBoard.get((xCell*boardHeight)+yCell).getPiece().setOwner(turn);
		}
		
		return newBoard;
	}
	
	public void printBoardText(List<Position> board)
	{
		int i = 0;
		for (int y = boardHeight - 1; y >= 0; y--)
		{
			System.out.print("| ");
			for (int x = 0; x < boardWidth; x++)
			{
				System.out.print(board.get(x*boardHeight + y).getPiece().getOwner() + " | ");
				i++;
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public List<Position> copyBoard(List<Position> origBoard)
	{
		ArrayList<Position> newBoard = new ArrayList<Position>();
		
		for (Position origP : origBoard)
		{
			newBoard.add(new Position(origP.getX(), origP.getY(), new GamePiece(origP.getPiece().getPieceName(), origP.getPiece().getOwner())));
		}
		
		return newBoard;
	}
}
