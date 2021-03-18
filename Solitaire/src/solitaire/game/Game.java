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
	public List<Card> foundation2 = new ArrayList<Card>();
	public List<Card> foundation0 = new ArrayList<Card>();
	public List<Card> foundation3 = new ArrayList<Card>();
	public List<Card> foundation1 = new ArrayList<Card>();
	// the board cards
//	public List<Card> tab0 = new ArrayList<Card>();
//	public List<Card> tab1 = new ArrayList<Card>();
//	public List<Card> tab2 = new ArrayList<Card>();
//	public List<Card> tab3 = new ArrayList<Card>();
//	public List<Card> tab4 = new ArrayList<Card>();
//	public List<Card> tab5 = new ArrayList<Card>();
//	public List<Card> tab6 = new ArrayList<Card>();
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
	
	public Game(boolean basicSetup)
	{
		
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
		System.out.println("Advancing Game");
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
					if (foundation0.size() <= 0) return;
					oldCard = foundation0.get(foundation0.size()-1);
					foundation0.remove(foundation0.size()-1);
					break;
				case 1:
					if (foundation1.size() <= 0) return;
					oldCard = foundation1.get(foundation1.size()-1);
					foundation1.remove(foundation1.size()-1);
					break;
				case 2:
					if (foundation2.size() <= 0) return;
					oldCard = foundation2.get(foundation2.size()-1);
					foundation2.remove(foundation2.size()-1);
					break;
				case 3:
					if (foundation3.size() <= 0) return;
					oldCard = foundation3.get(foundation3.size()-1);
					foundation3.remove(foundation3.size()-1);
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
						foundation0.add(oldCard);
						waste.remove(waste.size()-1);
						lastFlipCount--;
						break;
					case 1:
						foundation1.add(oldCard);
						waste.remove(waste.size()-1);
						lastFlipCount--;
						break;
					case 2:
						foundation2.add(oldCard);
						waste.remove(waste.size()-1);
						lastFlipCount--;
						break;
					case 3:
						foundation3.add(oldCard);
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
						foundation0.add(oldPiece.getCard());
						break;
					case 1:
						foundation1.add(oldPiece.getCard());
						break;
					case 2:
						foundation2.add(oldPiece.getCard());
						break;
					case 3:
						foundation3.add(oldPiece.getCard());
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
					board.get(move.getToPosition().getX() * boardHeight).setPiece(new GamePiece(true, 1, oldPiece.getCard()));
					tmpItr = 1;
				}
				else
				{
					board.get((lastCard.getX() * boardHeight) + lastCard.getY() + 1).setPiece(new GamePiece(true, 1, oldPiece.getCard()));
					tmpItr = 1;
				}
				// add rest of stack
				for (; tmpItr <= boardHeight; tmpItr++)
				{
					/*System.out.println("itr: " + tmpItr);
					if (board.get((fromPosition.getX() * boardHeight) + fromPosition.getY() + tmpItr).getPiece() != null)
						System.out.println("iterating: " + board.get((fromPosition.getX() * boardHeight) + fromPosition.getY() + tmpItr).getPiece().getCard());
						*/
					GamePiece nextPiece = board.get((fromPosition.getX() * boardHeight) + fromPosition.getY() + tmpItr).getPiece();
					if (nextPiece.getCard() != null)
					{
						board.get((fromPosition.getX() * boardHeight) + fromPosition.getY() + tmpItr).setPiece(new GamePiece(false,0,null));
						if (lastCard != null)
							board.get((lastCard.getX() * boardHeight) + lastCard.getY() + 1 + tmpItr).setPiece(new GamePiece(true, 1, nextPiece.getCard()));
						else
							board.get(move.getToPosition().getX() * boardHeight + tmpItr).setPiece(new GamePiece(true, 1, nextPiece.getCard()));
					}
					else
						break;
				}
			}
		}
		if(isWinningBoard(board) == 1) {
			gameOver = true;
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
		if(foundation0.size() == 13  && foundation1.size()== 13 &&
				foundation2.size() == 13 && foundation3.size() == 13) {
			return 1;
		} else return 0;
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
		//get all from cards < the ones that you CAN move from. this means going back through each tab and finding the last card
		// in the pattern. it also means the foundation cards that exist and the waste card if that is there
		// nnext get all to cards < the ones that you can move TO. this is the first of all foundation cards and the last card 
		// in every tab. it also means empty tabs
		
		// for each from card check all of the to cards and store in validMoves if it works
		// for tab: alternating color and decreasing by one
		// for foundation: same suit and increasing by one OR ace
		// for empty tab: is king
		// also the deck is a possible turn, as is flipping an unflipped card
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
				System.out.println("isValidMove:if: " + waste.get(waste.size()-1));
				fromCard = waste.get(waste.size()-1);
			}
			else if (from.isFoundation())
			{
				switch (from.getFoundationNum()) {
				case 0:
					fromCard = foundation0.get(foundation0.size()-1);
					break;
				case 1:
					fromCard = foundation1.get(foundation1.size()-1);
					break;
				case 2:
					fromCard = foundation2.get(foundation2.size()-1);
					break;
				case 3:
					fromCard = foundation3.get(foundation3.size()-1);
					break;
				default:
					System.out.println("Bad foundation number!");
					return false;
				}
				System.out.println("isValidMove:foundation else: " + fromCard.toString());
			} else {	
				System.out.println("isValidMove:else: " + from.getPiece().getCard().toString());
				fromCard = from.getPiece().getCard();
				//System.out.println("2isValidMove:else: " + fromCard.toString());
			}
		}
		else
			System.out.println("isValidMove:from is null");
		Position to = move.getToPosition();
		// check from card is a K and to is empty tab
		if (from != null && from.getPiece() != null
				&& fromCard.rank != 13
				&& to != null && to.getX() >= 0 
				&& board.get(to.getX() * boardHeight).getPiece().getCard() == null)
		{
			return false;
		}
		if(from != null && to != null && fromCard!= null) {
			if(to.getPiece().getCard()!=null) {
				Suit fromSuit = fromCard.suit;
				int fromRank = fromCard.rank;
				Suit toSuit = to.getPiece().getCard().suit;
				int toRank = to.getPiece().getCard().rank;
				if(toRank- fromRank != 1) {
					return false;
				} else {
					if((fromSuit==Suit.CLUB || fromSuit == Suit.SPADE) &&
							!(toSuit==Suit.DIAMOND || toSuit == Suit.HEART)) {
						return false;
					} else if((fromSuit==Suit.DIAMOND || fromSuit == Suit.HEART) &&
							!(toSuit==Suit.CLUB || toSuit == Suit.SPADE)) {
						return false;
					}
				}
			} else if(to.isFoundation()) {
				Card toCard = null;
				switch (to.getFoundationNum()) {
				case 0:
					if(foundation0.isEmpty()) {
						if(fromCard.rank!=1)
							return false;
						else return true;
					} else {
						toCard = foundation0.get(foundation0.size()-1);
						if(fromCard.suit != toCard.suit || (fromCard.rank-toCard.rank)!=1) return false;
					}
					break;
				case 1:
					if(foundation1.isEmpty()) {
						if(fromCard.rank!=1)
							return false;
						else return true;
					} else {
						toCard = foundation1.get(foundation1.size()-1);
						if(fromCard.suit != toCard.suit || (fromCard.rank-toCard.rank)!=1) return false;
					}
					break;
				case 2:
					if(foundation2.isEmpty()) {
						if(fromCard.rank!=1)
							return false;
						else return true;
					} else {
						toCard = foundation2.get(foundation2.size()-1);
						if(fromCard.suit != toCard.suit || (fromCard.rank-toCard.rank)!=1) return false;
					}
					break;
				case 3:
					if(foundation3.isEmpty()) {
						if(fromCard.rank!=1)
							return false;
						else return true;
					} else {
						toCard = foundation3.get(foundation3.size()-1);
						if(fromCard.suit != toCard.suit || (fromCard.rank-toCard.rank)!=1) return false;
					}
					break;
				default:
					System.out.println("Bad foundation number!");
					return false;
				}
			}
		}
		return true;
	}
	
	public Game simulateMove(List<Position> origBoard, Move move)
	{
		Game g = this.clone();
		if (isWinningBoard(origBoard) != 0)
		{
			System.out.println("Game already won by: " + isWinningBoard(origBoard));
			return null;
		}
		//List<Position> newBoard = new ArrayList<Position>(origBoard);
		g.advanceGame(move);
		
		return g;
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
	
	@Override
	public Game clone()
	{
		Game g = new Game(true);
		//copy board
		g.board = copyBoard(this.board);
		//iterate foundations
		for(Card c: foundation0)
		{
			g.foundation0.add(c);
		}
		for(Card c: foundation1)
		{
			g.foundation1.add(c);
		}
		for(Card c: foundation2)
		{
			g.foundation2.add(c);
		}
		for(Card c: foundation3)
		{
			g.foundation3.add(c);
		}
		//iterate deck
		for(Card c: deck)
		{
			g.deck.add(c);
		}
		//iterate waste
		for(Card c: waste)
		{
			g.waste.add(c);
		}
		
		g.turn = this.turn;
		g.lastFlipCount = this.lastFlipCount;
		g.gameOver = this.gameOver;
		
		return g;
	}
}
