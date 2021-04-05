package connectfour.game;

import java.util.ArrayList;
import java.util.List;

import connectfour.game.GamePiece;
import connectfour.game.Position;

public final class Game {
	private List<Position> board;
	private int boardWidth = 7, boardHeight = 6;
	private int turn = 1;
	private boolean gameOver;
	
	protected Game() {
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
	
	// Add piece and position
	public void advanceGame(Position move)
	{
		if (gameOver)
			return;
		int xCell = move.getX();
		// check the row where the piece lands
		for (int y = boardHeight-1; y >= 0; y--)
		{
			if (board.get((xCell * boardHeight) + y).getPiece().getOwner() == 0)
			{
				board.get((xCell * boardHeight) + y).getPiece().setOwner(turn);
				break;
			}
			if (y == 0)
			{
				System.out.println("No space for piece. Ignoring.");
			}
		}
		
		if (isWinningBoard(this.board) != 0)
		{
			gameOver = true;
			return;
		}

		turn *= -1;
		/*for (Position p : this.getValidMoves(this.getBoard(), this.getTurn()))
		{
			System.out.println(p.getPiece().getOwner() + ": " + p.getX() + "," + p.getY());
		}*/
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
		int[][] directions = {{1,0}, {1,-1}, {1,1}, {0,1}};
	    for (int[] d : directions) {
	        int dx = d[0];
	        int dy = d[1];
	        for (int x = 0; x < boardWidth; x++) {
	            for (int y = 0; y < boardHeight; y++) {
	                int lastx = x + 3*dx;
	                int lasty = y + 3*dy;
	                if (0 <= lastx && lastx < boardWidth && 0 <= lasty && lasty < boardHeight) {
	                    int w = tempBoard[x][y];
	                    if (w != 0 && w == tempBoard[x+dx][y+dy] 
	                                 && w == tempBoard[x+2*dx][y+2*dy] 
	                                 && w == tempBoard[lastx][lasty]) {
	                    	System.out.println("winner! " + w);
	                        return w;
	                    }
	                }
	            }
	        }
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
		if (tempBoard.length != boardWidth)
		{
			new Exception("Invalid board width.");
			System.exit(-1);
		}
		for (int i = 0; i < boardWidth; i++)
		{
			if (tempBoard[i].length != boardHeight)
			{
				new Exception("Invalid board height.");
				System.exit(-1);
			}
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
					validMoves.add(new Position(xCell,-1,new GamePiece("", turn)));
					break;
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
		//List<Position> newBoard = new ArrayList<Position>(origBoard);
		List<Position> newBoard = copyBoard(origBoard);
		int xCell = move.getX();
		int turn = move.getPiece().getOwner();
		// check the row where the piece lands
		for (int y = boardHeight-1; y >= 0; y--)
		{
			if (origBoard.get((xCell*boardHeight)+y).getPiece().getOwner() == 0)
			{
				newBoard.get((xCell*boardHeight)+y).getPiece().setOwner(turn);
				break;
			}
			if (y == 0)
			{
				System.out.println("No space for piece. Ignoring.");
				return null;
			}
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
