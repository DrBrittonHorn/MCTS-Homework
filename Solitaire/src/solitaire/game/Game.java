package solitaire.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import solitaire.agent.Agent;
import solitaire.agent.Human;

public final class Game {
	Random rand = new Random();
	public List<Position> board;
	public int turn = 1, boardWidth = 7, boardHeight = 20;
	public int deckPos = boardWidth*boardHeight, wastePos = deckPos+1, f0Pos=deckPos+2, f1Pos=deckPos+3, f2Pos=deckPos+4, f3Pos=deckPos+5; 
	// unflipped cards
	public List<Card> deck = new ArrayList<Card>();
	// flipped cards
	public List<Card> waste = new ArrayList<Card>();
	// cards placed at top. Goes from Ace to K for each suit
	public List<Card> foundationSpade = new ArrayList<Card>();
	public List<Card> foundationClub = new ArrayList<Card>();
	public List<Card> foundationHeart = new ArrayList<Card>();
	public List<Card> foundationDiamond = new ArrayList<Card>();
	// the board cards
	public List<Card> tab0 = new ArrayList<Card>();
	public List<Card> tab1 = new ArrayList<Card>();
	public List<Card> tab2 = new ArrayList<Card>();
	public List<Card> tab3 = new ArrayList<Card>();
	public List<Card> tab4 = new ArrayList<Card>();
	public List<Card> tab5 = new ArrayList<Card>();
	public List<Card> tab6 = new ArrayList<Card>();
	private boolean gameOver;
	public boolean isSingleFlip = false;
	private int lastFlipCount;
	private GamePiece nullPiece = new GamePiece(false,0, null);
	
	public Game() {
		// Initialize all positions to be blank
		board = new ArrayList<Position>();
		for (int x = 0; x < boardWidth; x++)
		{
			for (int y = 0; y < boardHeight; y++)
			{
				board.add(new Position(x, y, nullPiece));
			}
		}
		// add deck position
		board.add(new Position(-1, -1, new GamePiece(false,0, null), true, false, false, -1));
		// add waste position
		board.add(new Position(-1, -1, new GamePiece(false,0, null), false, true, false, -1));
		// add four foundation positions
		board.add(new Position(-1, -1, new GamePiece(false,0, null), false, false, true, 0));
		board.add(new Position(-1, -1, new GamePiece(false,0, null), false, false, true, 1));
		board.add(new Position(-1, -1, new GamePiece(false,0, null), false, false, true, 2));
		board.add(new Position(-1, -1, new GamePiece(false,0, null), false, false, true, 3));
		
		// Generate all cards
		for (Suit s : Suit.values())
		{
			for (int i = 1; i <= 13; i++)
			{
				Card c = new Card(i, s);
				deck.add(c);
			}
		}
		// Shuffle
		Collections.shuffle(deck);
		// Place cards
		for (int tab = 0; tab < boardWidth; tab++)
		{
			for (int stackHeight = tab; stackHeight < 7; stackHeight++)
			{
				board.get((stackHeight * boardHeight) + tab).setPiece(new GamePiece(tab == stackHeight ? true : false,1,deck.get(0)));
				deck.remove(0);
			}
		}
		
		/*
		// for testing purposes only
		for (int addCard = 7; addCard < 11; addCard++)
		{
			board.get((6 * boardHeight) + addCard).setPiece(new GamePiece(true,1,deck.get(0)));
			deck.remove(0);
		}
		*/
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
	
	public int getBoardIndex(Position p)
	{
		if (p.isDeck())
			return deckPos;
		if (p.isFoundation())
		{
			switch (p.getFoundationNum())
			{
				case 0:
					return f0Pos;
				case 1:
					return f1Pos;
				case 2:
					return f2Pos;
				case 3:
					return f3Pos;
				default:
					return -1;
			}
		}
		if (p.isWaste())
			return wastePos;
		return (p.getX() * boardHeight) + p.getY();
	}
	
	protected void advanceGame(Move move)
	{
		if (move == null)
			return;
		if (gameOver)
			return;
		if (!isValidMove(move))
		{
			System.out.println("Invalid move attempted: " + move.toString());
			return;
		}
		// single click moves are always in toPosition
		if (move.getFromPosition() == null)
		{
			if (move.getToPosition().isDeck())
			{
				if (deck.size() > 0)
					flipDeck();
				else
					resetDeck();
				return;
			}
			else // clicked on unflipped tab
			{
				GamePiece oldpiece = board.get((move.getToPosition().getX() * boardHeight)+move.getToPosition().getY()).getPiece();
				oldpiece.setFlipped(true);
				return;
			}
		}
		if (move.getFromPosition().isFoundation())
		{
			if (move.getToPosition().isFoundation()) return;
			Card oldCard;
			int foundationNum = move.getFromPosition().getFoundationNum();
			switch (foundationNum) {
				case 0:
					if (foundationClub.size() <= 0) return;
					oldCard = foundationClub.get(foundationClub.size()-1);
					foundationClub.remove(foundationClub.size()-1);
					break;
				case 1:
					if (foundationDiamond.size() <= 0) return;
					oldCard = foundationDiamond.get(foundationDiamond.size()-1);
					foundationDiamond.remove(foundationDiamond.size()-1);
					break;
				case 2:
					if (foundationSpade.size() <= 0) return;
					oldCard = foundationSpade.get(foundationSpade.size()-1);
					foundationSpade.remove(foundationSpade.size()-1);
					break;
				case 3:
					if (foundationHeart.size() <= 0) return;
					oldCard = foundationHeart.get(foundationHeart.size()-1);
					foundationHeart.remove(foundationHeart.size()-1);
					break;
				default:
					System.out.println("Bad foundation number!");
					return;
			}
			if (move.getToPosition().getX() >= 0)
			{
				// move to tab
				Position lastCard = getLastFlippedCardInTab(move.getToPosition().getX());
				board.get((lastCard.getX() * boardHeight) + lastCard.getY() + 1).setPiece(new GamePiece(true, 1, oldCard));
			}
		}
		else if (move.getFromPosition().isWaste())
		{
			if (waste.size() <= 0)
			{
				System.out.println("waste is empty!");
				return;
			}
			Card oldCard = waste.get(waste.size()-1);
			if (move.getToPosition().isFoundation())
			{
				int foundationNum = move.getToPosition().getFoundationNum();
				switch (foundationNum) {
					case 0:
						foundationClub.add(oldCard);
						waste.remove(waste.size()-1);
						lastFlipCount--;
						break;
					case 1:
						foundationDiamond.add(oldCard);
						waste.remove(waste.size()-1);
						lastFlipCount--;
						break;
					case 2:
						foundationSpade.add(oldCard);
						waste.remove(waste.size()-1);
						lastFlipCount--;
						break;
					case 3:
						foundationHeart.add(oldCard);
						waste.remove(waste.size()-1);
						lastFlipCount--;
						break;
					default:
						System.out.println("Bad foundation number! " + foundationNum);
						return;
				}
			}
			// if not foundation, must be tabular
			else
			{
				Position lastCard = getLastFlippedCardInTab(move.getToPosition().getX());
				if (lastCard != null)
					board.get((lastCard.getX() * boardHeight) + lastCard.getY() + 1).setPiece(new GamePiece(true, 1, oldCard));
				else // empty tab
					board.get((move.getToPosition().getX() * boardHeight)).setPiece(new GamePiece(true, 1, oldCard));
				waste.remove(waste.size()-1);
				lastFlipCount--;
				return;
			}
		}
		else
		{
			// must be a move from one of the tabular positions
			Position fromPosition = move.getFromPosition();
			if (!fromPosition.getPiece().isFlipped())
			{
				System.out.println("Attempted to move a face down card");
				return;
			}
			if (fromPosition.getX() == move.getToPosition().getX())
			{
				System.out.println("Attempted to move to same tab. Returning.");
				return;
			}
			GamePiece oldPiece = board.get((fromPosition.getX() * boardHeight) + fromPosition.getY()).getPiece();
			board.get((fromPosition.getX() * boardHeight) + fromPosition.getY()).setPiece(new GamePiece(false,0,null));
			// move to foundation
			if (move.getToPosition().isFoundation())
			{
				// need to add check that card is free
				int foundationNum = move.getToPosition().getFoundationNum();
				switch (foundationNum) {
					case 0:
						foundationClub.add(oldPiece.getCard());
						break;
					case 1:
						foundationDiamond.add(oldPiece.getCard());
						break;
					case 2:
						foundationSpade.add(oldPiece.getCard());
						break;
					case 3:
						foundationHeart.add(oldPiece.getCard());
						break;
					default:
						System.out.println("Bad foundation number!");
						return;
				}
			}
			else if (move.getToPosition().getX() >= 0)
			{
				int tmpItr = 0;
				// move to another tab
				Position lastCard = getLastFlippedCardInTab(move.getToPosition().getX());
				if (lastCard == null) // empty tab
				{
					board.get(move.getToPosition().getX() * boardHeight).setPiece(new GamePiece(true, 0, oldPiece.getCard()));
				}
				else
				{
					board.get((lastCard.getX() * boardHeight) + lastCard.getY() + 1).setPiece(new GamePiece(true, 1, oldPiece.getCard()));
					tmpItr = 1;
				}
				// add rest of stack
				for (; tmpItr <= boardHeight; tmpItr++)
				{
					GamePiece nextPiece = board.get((fromPosition.getX() * boardHeight) + fromPosition.getY() + tmpItr).getPiece();
					if (nextPiece.getCard() != null)
					{
						board.get((fromPosition.getX() * boardHeight) + fromPosition.getY() + tmpItr).setPiece(new GamePiece(false,0,null));
						board.get((lastCard.getX() * boardHeight) + lastCard.getY() + 1 + tmpItr).setPiece(new GamePiece(true, 1, nextPiece.getCard()));
					}
					else
						break;
				}
			}
		}
		return;
	}
	
	private List<Position> getStack(int x, int y)
	{
		List<Position> stack = new ArrayList<>();
		
		return stack;
	}
	
	private void resetDeck()
	{
		System.out.println("resetting deck.");
		for (Card wasteCard : waste)
		{
			deck.add(0,wasteCard);
		}
		waste = new ArrayList<Card>();
		lastFlipCount = 0;
	}
	
	private void flipDeck()
	{
		int flipCount = 0;
		int iterations = (isSingleFlip) ? 1 : 3;
		for (int i = 0; i < iterations; i++)
		{
			if (deck.size() <= 0) break;
			flipCount++;
			Card deckTop = deck.remove(deck.size()-1);
			waste.add(deckTop);
		}
		lastFlipCount = flipCount;
	}
	
	public Position getLastFlippedCardInTab(int tab)
	{
		for (int y = getBoardHeight() - 1; y>= 0; y--)
		{
			Position tmpPos = getBoard().get((tab*getBoardHeight()) + y);
			GamePiece piece = tmpPos.getPiece();
			if (piece.getCard() != null)
			{
				if (piece.isFlipped())
					return tmpPos;
				else
					return null;
			}
		}
		return null;
	}
	
	public Position getLastUnflippedCardInTab(int tab)
	{
		for (int y = getBoardHeight() - 1; y>= 0; y--)
		{
			Position tmpPos = getBoard().get((tab*getBoardHeight()) + y);
			GamePiece piece = tmpPos.getPiece();
			if (piece.getCard() != null)
			{
				if (piece.isFlipped())
					return null;
				else
					return tmpPos;
			}
		}
		return null;
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
				//System.out.println("vertical win for: 1");
				return 1;
			}
			if (tempBoard[i][0] + tempBoard[i][1] + tempBoard[i][2] == -3)
			{
				//System.out.println("vertical win for: -1");
				return -1;
			}
		}
		// horizontal win conditions
		for (int i = 0; i < 3; i++)
		{
			if (tempBoard[0][i] + tempBoard[1][i] + tempBoard[2][i] == 3)
			{
				//System.out.println("horizontal win for: 1");
				return 1;
			}
			if (tempBoard[0][i] + tempBoard[1][i] + tempBoard[2][i] == -3)
			{
				//System.out.println("horizontal win for: -1");
				return -1;
			}
		}
		// diagonal win conditions
		if ((tempBoard[0][0] + tempBoard[1][1] + tempBoard[2][2] == 3)
			|| (tempBoard[2][0] + tempBoard[1][1] + tempBoard[0][2] == 3))
		{
			//System.out.println("diagonal win for: 1");
			return 1;
		}
		if ((tempBoard[0][0] + tempBoard[1][1] + tempBoard[2][2] == -3)
				|| (tempBoard[2][0] + tempBoard[1][1] + tempBoard[0][2] == -3))
		{
				//System.out.println("diagonal win for: -1");
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
	
	public List<Move> getValidMoves(List<Position> origBoard, int turn)
	{
		ArrayList<Move> validMoves = new ArrayList<Move>();
		
		return validMoves;
	}
	
	public boolean isValidMove(Move move)
	{
		Position from = move.getFromPosition();
		Card fromCard = null;
		if (from != null && from.getPiece() != null)
		{
			if (from.isWaste())
			{
				System.out.println(waste.get(waste.size()-1));
				fromCard = waste.get(waste.size()-1);
			}
			else
			{	
				System.out.println(from.getPiece().getCard().toString());
				fromCard = from.getPiece().getCard();
			}
		}
		Position to = move.getToPosition();
		// check from card is a K and to is empty tab
		if (from != null && from.getPiece() != null
				&& fromCard.rank != 13
				&& to != null && to.getX() >= 0 
				&& board.get(to.getX() * boardHeight).getPiece().getCard() == null)
		{
			return false;
		}
		return true;
	}
	
	public List<Position> simulateMove(List<Position> origBoard, Move move)
	{
		if (isWinningBoard(origBoard) != 0)
		{
//			System.out.println("Game already won by: " + isWinningBoard(origBoard));
			return null;
		}
		//List<Position> newBoard = new ArrayList<Position>(origBoard);
		List<Position> newBoard = copyBoard(origBoard);
		
		return newBoard;
	}
	
	public void printBoardText(List<Position> board)
	{
		int i = 0;
		for (int y = 0; y <= boardHeight - 1; y++)
		{
			System.out.print("| ");
			for (int x = 0; x < boardWidth; x++)
			{
				GamePiece p = board.get(x*boardHeight + y).getPiece();
				if (p.getCard() != null)
				{
					if (p.isFlipped())
						System.out.print(p.getCard().toString() + " | ");
					else
						System.out.print("**** | ");
				}
				else
					System.out.print("     | ");
				i++;
			}
			System.out.println();
		}
	}
	
	public void printDeck(List<Card> deck)
	{
		System.out.println(" ===== BEGIN DECK ======");
		for (Card c : deck)
		{
			System.out.println(c.toString());
		}
		System.out.println(" ===== END DECK ======");
	}
	
	public List<Position> copyBoard(List<Position> origBoard)
	{
		ArrayList<Position> newBoard = new ArrayList<Position>();
		
		for (Position origP : origBoard)
		{
			newBoard.add(new Position(origP.getX(), origP.getY(), new GamePiece(origP.getPiece().isFlipped(), origP.getPiece().getOwner(), origP.getPiece().getCard())));
		}
		
		return newBoard;
	}

	public int getLastFlipCount()
	{
		return lastFlipCount;
	}
}
