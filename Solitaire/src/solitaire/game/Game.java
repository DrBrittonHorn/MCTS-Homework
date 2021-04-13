package solitaire.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import solitaire.agent.Agent;
import solitaire.agent.Human;

public final class Game {
	private static final Random rand = new Random();
	public List<Position> board;
	public int turn = 1, boardWidth = 7, boardHeight = 20, playsMade = 0, maxPlays = 100, deckFlips = 0, maxDeckFlips = 2;
	public int deckPos = boardWidth*boardHeight, wastePos = deckPos+1, f0Pos=deckPos+2, f1Pos=deckPos+3, f2Pos=deckPos+4, f3Pos=deckPos+5; 
	// unflipped cards
	public List<Card> deck = new ArrayList<Card>(24);
	// flipped cards
	public List<Card> waste = new ArrayList<Card>(24);
	// cards placed at top. Goes from Ace to K for each suit
	public List<Card> foundation2 = new ArrayList<Card>(13);
	public List<Card> foundation0 = new ArrayList<Card>(13);
	public List<Card> foundation3 = new ArrayList<Card>(13);
	public List<Card> foundation1 = new ArrayList<Card>(13);
	// the board cards
//	public List<Card> tab0 = new ArrayList<Card>();
//	public List<Card> tab1 = new ArrayList<Card>();
//	public List<Card> tab2 = new ArrayList<Card>();
//	public List<Card> tab3 = new ArrayList<Card>();
//	public List<Card> tab4 = new ArrayList<Card>();
//	public List<Card> tab5 = new ArrayList<Card>();
//	public List<Card> tab6 = new ArrayList<Card>();
//	private int timesDeckFlipped = 0;
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
		
		//setupGame();
		//setupTestGame();
		//setupTestEasyWinGame();
		setupInterimGame();
		
		/*
		// for testing purposes only
		for (int addCard = 7; addCard < 11; addCard++)
		{
			board.get((6 * boardHeight) + addCard).setPiece(new GamePiece(true,1,deck.get(0)));
			deck.remove(0);
		}
		*/
	}
	
	private void setupGame() {
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
	}
	
	private void setupTestGame() 
	{
		List<Card> allCards = new ArrayList<Card>();
		// Generate all cards
		// spade, diamond, club, heart. Aces at 0 S, 13 D, 26 C, 39 H
		for (Suit s : Suit.values())
		{
			for (int i = 1; i <= 13; i++)
			{
				Card c = new Card(i, s);
				allCards.add(c);
			}
		}
		//Add deck: AC,JC,8C,10H,7D,4S,2S,7C,8S,6C,KS,AS,AH,10C,7H,KD,5C,6S,JD,9H,4D,9C,8D,9D
		deck.add(allCards.get(26));
		deck.add(allCards.get(2*13+10));
		deck.add(allCards.get(2*13+7));
		deck.add(allCards.get(3*13+9));
		deck.add(allCards.get(1*13+6));
		deck.add(allCards.get(3));
		deck.add(allCards.get(1));
		deck.add(allCards.get(2*13+6));
		deck.add(allCards.get(7));
		deck.add(allCards.get(2*13+5));
		deck.add(allCards.get(12));
		deck.add(allCards.get(0));
		deck.add(allCards.get(39));
		deck.add(allCards.get(2*13+9));
		deck.add(allCards.get(3*13+6));
		deck.add(allCards.get(13+12));
		deck.add(allCards.get(2*13+4));
		deck.add(allCards.get(5));
		deck.add(allCards.get(1*13+10));
		deck.add(allCards.get(3*13+8));
		deck.add(allCards.get(1*13+3));
		deck.add(allCards.get(2*13+8));
		deck.add(allCards.get(1*13+7));
		deck.add(allCards.get(1*13+8));
		
		// add tab pieces. Aces at 0 S, 13 D, 26 C, 39 H
		board.get(0).setPiece(new GamePiece(true,1,allCards.get(14)));	// 2 D
		board.get((1 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(9)));	// 10 S
		board.get((1 * boardHeight) + 1).setPiece(new GamePiece(true,1,allCards.get(2)));	// 3 S
		board.get((2 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(13+9)));	// 10 D
		board.get((2 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(13+5)));	// 6 D
		board.get((2 * boardHeight) + 2).setPiece(new GamePiece(true,1,allCards.get(13)));	// A D
		board.get((3 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(39+11)));	// Q H
		board.get((3 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(39+12)));	// K H
		board.get((3 * boardHeight) + 2).setPiece(new GamePiece(false,1,allCards.get(39+1)));	// 2 H
		board.get((3 * boardHeight) + 3).setPiece(new GamePiece(true,1,allCards.get(26+11)));	// Q C
		board.get((4 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(26+2)));	// 3 C
		board.get((4 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(8)));	// 9 S
		board.get((4 * boardHeight) + 2).setPiece(new GamePiece(false,1,allCards.get(13+11)));	// Q D
		board.get((4 * boardHeight) + 3).setPiece(new GamePiece(false,1,allCards.get(39+3)));	// 4 H
		board.get((4 * boardHeight) + 4).setPiece(new GamePiece(true,1,allCards.get(26+12)));	// K C
		board.get((5 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(26+3)));	// 4 C
		board.get((5 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(39+4)));	// 5 H
		board.get((5 * boardHeight) + 2).setPiece(new GamePiece(false,1,allCards.get(39+10)));	// J H
		board.get((5 * boardHeight) + 3).setPiece(new GamePiece(false,1,allCards.get(39+2)));	// 3 H
		board.get((5 * boardHeight) + 4).setPiece(new GamePiece(false,1,allCards.get(26+1)));	// 2 C
		board.get((5 * boardHeight) + 5).setPiece(new GamePiece(true,1,allCards.get(39+5)));	// 6 H
		board.get((6 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(11)));	// Q S
		board.get((6 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(6)));	// 7 S
		board.get((6 * boardHeight) + 2).setPiece(new GamePiece(false,1,allCards.get(13+2)));	// 3 D
		board.get((6 * boardHeight) + 3).setPiece(new GamePiece(false,1,allCards.get(13+4)));	// 5 D
		board.get((6 * boardHeight) + 4).setPiece(new GamePiece(false,1,allCards.get(39+7)));	// 8 H
		board.get((6 * boardHeight) + 5).setPiece(new GamePiece(false,1,allCards.get(10)));	// J S
		board.get((6 * boardHeight) + 6).setPiece(new GamePiece(true,1,allCards.get(4)));	// 5 S
	}
	
	private void setupTestEasyWinGame() 
	{
		List<Card> allCards = new ArrayList<Card>();
		// Generate all cards
		// spade, diamond, club, heart. Aces at 0 S, 13 D, 26 C, 39 H
		for (Suit s : Suit.values())
		{
			for (int i = 1; i <= 13; i++)
			{
				Card c = new Card(i, s);
				allCards.add(c);
			}
		}
		//Add deck
		deck.add(allCards.get(51));
		deck.add(allCards.get(38));
		deck.add(allCards.get(12));
		deck.add(allCards.get(37));
		deck.add(allCards.get(11));
		deck.add(allCards.get(25));
		deck.add(allCards.get(10));
		deck.add(allCards.get(24));
		deck.add(allCards.get(50));
		deck.add(allCards.get(23));
		deck.add(allCards.get(49));
		deck.add(allCards.get(36));
		deck.add(allCards.get(48));
		deck.add(allCards.get(35));
		deck.add(allCards.get(9));
		deck.add(allCards.get(34));
		deck.add(allCards.get(8));
		deck.add(allCards.get(22));
		deck.add(allCards.get(7));
		deck.add(allCards.get(21));
		deck.add(allCards.get(47));
		deck.add(allCards.get(20));
		deck.add(allCards.get(46));
		deck.add(allCards.get(33));
		
		// add tab pieces. Aces at 0 S, 13 D, 26 C, 39 H
		board.get(0).setPiece(new GamePiece(true,1,allCards.get(13)));
		
		board.get((1 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(1)));
		board.get((1 * boardHeight) + 1).setPiece(new GamePiece(true,1,allCards.get(39)));
		
		board.get((2 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(42)));
		board.get((2 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(15)));
		board.get((2 * boardHeight) + 2).setPiece(new GamePiece(true,1,allCards.get(26)));
		
		board.get((3 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(30)));
		board.get((3 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(29)));
		board.get((3 * boardHeight) + 2).setPiece(new GamePiece(false,1,allCards.get(41)));
		board.get((3 * boardHeight) + 3).setPiece(new GamePiece(true,1,allCards.get(0)));
		
		board.get((4 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(31)));
		board.get((4 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(4)));
		board.get((4 * boardHeight) + 2).setPiece(new GamePiece(false,1,allCards.get(3)));
		board.get((4 * boardHeight) + 3).setPiece(new GamePiece(false,1,allCards.get(28)));
		board.get((4 * boardHeight) + 4).setPiece(new GamePiece(true,1,allCards.get(14)));
		
		board.get((5 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(45)));
		board.get((5 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(5)));
		board.get((5 * boardHeight) + 2).setPiece(new GamePiece(false,1,allCards.get(18)));
		board.get((5 * boardHeight) + 3).setPiece(new GamePiece(false,1,allCards.get(17)));
		board.get((5 * boardHeight) + 4).setPiece(new GamePiece(false,1,allCards.get(2)));
		board.get((5 * boardHeight) + 5).setPiece(new GamePiece(true,1,allCards.get(40)));
		
		board.get((6 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(6)));
		board.get((6 * boardHeight) + 1).setPiece(new GamePiece(false,1,allCards.get(32)));
		board.get((6 * boardHeight) + 2).setPiece(new GamePiece(false,1,allCards.get(19)));
		board.get((6 * boardHeight) + 3).setPiece(new GamePiece(false,1,allCards.get(44)));
		board.get((6 * boardHeight) + 4).setPiece(new GamePiece(false,1,allCards.get(43)));
		board.get((6 * boardHeight) + 5).setPiece(new GamePiece(false,1,allCards.get(16)));
		board.get((6 * boardHeight) + 6).setPiece(new GamePiece(true,1,allCards.get(27)));
	}
	
	private void setupInterimGame()
	{
		List<Card> allCards = new ArrayList<Card>();
		// Generate all cards
		// spade, diamond, club, heart. Aces at 0 S, 13 D, 26 C, 39 H
		for (Suit s : Suit.values())
		{
			for (int i = 1; i <= 13; i++)
			{
				Card c = new Card(i, s);
				allCards.add(c);
			}
		}
		
		//Add deck
		deck.add(allCards.get(51));
		deck.add(allCards.get(38));
		deck.add(allCards.get(12));
//		deck.add(allCards.get(37));
//		deck.add(allCards.get(11));
		deck.add(allCards.get(25));
//		deck.add(allCards.get(10));
//		deck.add(allCards.get(24));
//		deck.add(allCards.get(50));
//		deck.add(allCards.get(23));
//		deck.add(allCards.get(49));
//		deck.add(allCards.get(36));
//		deck.add(allCards.get(48));
//		deck.add(allCards.get(35));
//		deck.add(allCards.get(9));
//		deck.add(allCards.get(34));
//		deck.add(allCards.get(8));
//		deck.add(allCards.get(22));
//		deck.add(allCards.get(7));
//		deck.add(allCards.get(21));
//		deck.add(allCards.get(47));
//		deck.add(allCards.get(20));
//		deck.add(allCards.get(46));
//		deck.add(allCards.get(33));

		foundation0.add(allCards.get(0));
		foundation0.add(allCards.get(1));
		foundation0.add(allCards.get(2));
		foundation0.add(allCards.get(3));
		foundation0.add(allCards.get(4));
		foundation0.add(allCards.get(5));
		foundation0.add(allCards.get(6));
		foundation0.add(allCards.get(7));
		foundation0.add(allCards.get(8));
		foundation0.add(allCards.get(9));
		foundation0.add(allCards.get(10));
		foundation0.add(allCards.get(11));
//		board.get((6 * boardHeight) + 0).setPiece(new GamePiece(false,1,allCards.get(6)));
		
		foundation1.add(allCards.get(13));
		foundation1.add(allCards.get(14));
		foundation1.add(allCards.get(15));
		foundation1.add(allCards.get(16));
		foundation1.add(allCards.get(17));
		foundation1.add(allCards.get(18));
		foundation1.add(allCards.get(19));
		foundation1.add(allCards.get(20));
		foundation1.add(allCards.get(21));
		foundation1.add(allCards.get(22));
		foundation1.add(allCards.get(23));
		foundation1.add(allCards.get(24));
		
		foundation2.add(allCards.get(26));
		foundation2.add(allCards.get(27));
		foundation2.add(allCards.get(28));
		foundation2.add(allCards.get(29));
		foundation2.add(allCards.get(30));
		foundation2.add(allCards.get(31));
		foundation2.add(allCards.get(32));
		foundation2.add(allCards.get(33));
		foundation2.add(allCards.get(34));
		foundation2.add(allCards.get(35));
		foundation2.add(allCards.get(36));
		foundation2.add(allCards.get(37));
//		board.get((3 * boardHeight) + 0).setPiece(new GamePiece(true,1,allCards.get(30)));
//		board.get((4 * boardHeight) + 0).setPiece(new GamePiece(true,1,allCards.get(31)));
//		board.get((6 * boardHeight) + 1).setPiece(new GamePiece(true,1,allCards.get(32)));
		
		foundation3.add(allCards.get(39));
		foundation3.add(allCards.get(40));
		foundation3.add(allCards.get(41));
		foundation3.add(allCards.get(42));
		foundation3.add(allCards.get(43));
		foundation3.add(allCards.get(44));
		foundation3.add(allCards.get(45));
		foundation3.add(allCards.get(46));
		foundation3.add(allCards.get(47));
		foundation3.add(allCards.get(48));
		foundation3.add(allCards.get(49));
		foundation3.add(allCards.get(50));
//		board.get((5 * boardHeight) + 0).setPiece(new GamePiece(true,1,allCards.get(45)));
		
		
		
		playsMade = 50;

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
//		System.out.println("Advancing Game");
		if (move == null)
			return;
		if (gameOver)
			return;
		if (++playsMade >= maxPlays)
		{
			gameOver = true;
			//return;
		}
		if (!isValidMove(move))
		{
			System.out.println("Invalid move attempted: " + move.getFromPosition() + ":::" + move.getToPosition());
			System.out.println("deckFlips: " + deckFlips + ", max: " + maxDeckFlips + ", deckSize: " + deck.size());
			this.printDeck(deck);
			this.printBoardText(board);
			return;
		}
		// single click moves are always in toPosition
		if (move.getFromPosition() == null)
		{
			if (move.getToPosition().isDeck())
			{
				if (deckFlips >= maxDeckFlips && deck.size() == 0)
				{
					System.out.println("Max deck flips reached");
					return;
				}
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
			
			// move to foundation
			if (move.getToPosition().isFoundation())
			{
				if(!oldPiece.getCard().equals(getLastFlippedCardInTab(fromPosition.getX()).getPiece().getCard()))
				{
					System.out.println("Attempted to move an unclear card.");
					return;
				}
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
				board.get((fromPosition.getX() * boardHeight) + fromPosition.getY()).setPiece(new GamePiece(false,0,null));
				return;
			}
			else if (move.getToPosition().getX() >= 0)
			{
				board.get((fromPosition.getX() * boardHeight) + fromPosition.getY()).setPiece(new GamePiece(false,0,null));
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
		if(isWinningBoard(board) != 0) {
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
//		System.out.println("resetting deck.");
		++deckFlips;
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
				foundation2.size() == 13 && foundation3.size() == 13) 
			return 1;
		if (maxPlays <= playsMade)
			return -1;
		//else if(deckFlips >= 3)
		//	return -1;
		return 0;
	}
	
	public float getBoardScore(List<Position> origBoard)
	{
		return 5*(foundation0.size() + foundation1.size() + foundation2.size() + foundation3.size()) + ((maxPlays-playsMade) >> 4);
	}
	
	public boolean gameOver()
	{
		if (getValidMoves(board, turn).size() == 0)
		{
			System.out.println("No moves left.");
			return true;
		}
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
		List<Move> validMoves = new ArrayList<Move>();
		Set<Position> toCards = new HashSet<Position>();
		Set<Position> fromCards = new HashSet<Position>();
		//Add tabular cards
		for(int i = 0; i < 7; i++) {
			Position lastFlipped = getLastFlippedCardInTab(i);
			Position lastUnflipped = getLastUnflippedCardInTab(i);
			if(lastFlipped!= null) {
				toCards.add(lastFlipped);
				fromCards.add(lastFlipped);
				int j = lastFlipped.getY()-1;
				Position prev = lastFlipped;
				while(j>=0 && board.get(i*boardHeight +j).getPiece().isFlipped()) {
					int prevRank = prev.getPiece().getCard().rank;
					Suit prevSuit = prev.getPiece().getCard().suit;
					int curRank = board.get(i*boardHeight + j).getPiece().getCard().rank;
					Suit curSuit = board.get(i*boardHeight + j).getPiece().getCard().suit;
					if(curRank - prevRank == 1) {
						if((curSuit == Suit.CLUB || curSuit == Suit.SPADE) &&
								(prevSuit == Suit.DIAMOND ||prevSuit == Suit.HEART)) {
							fromCards.add(board.get(i*boardHeight + j));
						} else if((curSuit == Suit.DIAMOND || curSuit == Suit.HEART) &&
								(prevSuit == Suit.CLUB ||prevSuit == Suit.SPADE)) {
							fromCards.add(board.get(i*boardHeight + j));
						} else {
							j = -1;
						}
					} else {
						j = -1;
					}
					prev = board.get(i*boardHeight + j);
					j = j-1;
				}
			} else if(lastUnflipped != null) {
				validMoves.add(new Move(null, lastUnflipped));
			}
		}
		//Add foundation cards
		if(foundation0.size()>= 1) {
			Position found0 = new Position(-1, -1, new GamePiece(true, 1, foundation0.get(foundation0.size()-1)), false, false, true, 0);			
			//toCards.add(found0);
			fromCards.add(found0);
		}
		if (foundation0.size() < 13)
			toCards.add(board.get(f0Pos));
		if(foundation1.size()>=1) {
			Position found1 = new Position(-1, -1, new GamePiece(true, 1, foundation1.get(foundation1.size()-1)), false, false, true, 1);
			//toCards.add(found1);
			fromCards.add(found1);
		}
		if (foundation1.size() < 13)
			toCards.add(board.get(f1Pos));
		if(foundation2.size()>=1) {
			Position found2 = new Position(-1, -1, new GamePiece(true, 1, foundation2.get(foundation2.size()-1)), false, false, true, 2);
			//toCards.add(found2);
			fromCards.add(found2);
		}
		if (foundation2.size() < 13)
			toCards.add(board.get(f2Pos));
		if(foundation3.size()>=1) {
			Position found3 = new Position(-1, -1, new GamePiece(true, 1, foundation3.get(foundation3.size()-1)), false, false, true, 3);	
			//toCards.add(found3);
			fromCards.add(found3);
		}
		if (foundation3.size() < 13)
			toCards.add(board.get(f3Pos));
		// add waste cards
		if(waste.size()>=1) {
			Position lastWaste = new Position(-1, -1, new GamePiece(true, 1, waste.get(waste.size()-1)),false, true, false, -1);
			fromCards.add(lastWaste);
		}
		// add top deck card
//		System.out.println("DECK: " + board.get(deckPos));
//		if (deck.size() == 0)
//			System.out.println("getValidMoves() == deckFlips: " + deckFlips + ", maxDeckFlips: " + maxDeckFlips + ", deckSize: " + deck.size());
		if (!(deck.size() == 0 && deckFlips >= maxDeckFlips))
		{
			//System.out.println("getValidMoves() == deckFlips: " + deckFlips + ", maxDeckFlips: " + maxDeckFlips + ", deckSize: " + deck.size());
			validMoves.add(new Move(null,board.get(deckPos)));
		}
		
		for(Position toPos : toCards) {
			//System.out.println("to POS: " + toPos);
			for(Position fromPos : fromCards) {
				if (fromPos.equals(toPos)) continue;
				//System.out.println("from POS: " + fromPos);
				int fromRank = fromPos.getPiece().getCard().rank;
				Suit fromSuit = fromPos.getPiece().getCard().suit;
				if(toPos.isFoundation()) { // placing a card in foundation
					if(fromRank == 1 && !fromPos.isFoundation()) { // check if foundation is empty for ace moves
						if(foundation0.size() == 0 && toPos.getFoundationNum() == 0) {
							validMoves.add(new Move(fromPos, board.get(f0Pos)));
						} else if(foundation1.size() == 0 && toPos.getFoundationNum() == 1) {
							validMoves.add(new Move(fromPos, board.get(f1Pos)));
						} else if(foundation2.size() == 0 && toPos.getFoundationNum() == 2) {
							validMoves.add(new Move(fromPos, board.get(f2Pos)));
						} else if(foundation3.size() == 0 && toPos.getFoundationNum() == 3) {
							validMoves.add(new Move(fromPos, board.get(f3Pos)));
						} 
						continue;
					}
					Card toCard = getTopFoundationCard(toPos.getFoundationNum());
					if (toCard == null)
					{
						//System.out.println("Null to foundation card");
						continue;
					}
					int toRank = toCard.rank;
					Suit toSuit = toCard.suit;
					if(fromSuit == toSuit && fromRank - toRank == 1 && 
							(
									(fromPos.getX() >= 0 && fromPos.equals(getLastFlippedCardInTab(fromPos.getX()))) ||
									(fromPos.isWaste())
							)) {
						validMoves.add(new Move(fromPos, toPos));
					}
					continue;
				}
				int toRank = toPos.getPiece().getCard().rank;
				Suit toSuit = toPos.getPiece().getCard().suit;

				if(toRank - fromRank == 1) {
					if((fromSuit == Suit.HEART || fromSuit == Suit.DIAMOND) &&
							(toSuit == Suit.SPADE || toSuit == Suit.CLUB)) {
						if(fromPos.getX() != toPos.getX()) {
							validMoves.add(new Move(fromPos, toPos));
						}
					} else if ((toSuit == Suit.HEART || toSuit == Suit.DIAMOND) &&
							(fromSuit == Suit.SPADE || fromSuit == Suit.CLUB)) {
						if(fromPos.getX() != toPos.getX()) {
							validMoves.add(new Move(fromPos, toPos));
						}
					}
				} else if(fromRank == 13 ) {
					for(int tab=0; tab<7; tab++) {
						if(board.get(tab*boardHeight).getPiece().getCard() == null) {
							validMoves.add(new Move(fromPos, board.get(tab*boardHeight)));
						}
					}
				}
			}
		}
		return validMoves;
	}
	
	public boolean isValidMove(Move move)
	{
		
		Position from = move.getFromPosition();
		Position to = move.getToPosition();
		if (from == null && to.isDeck() && (deckFlips >= maxDeckFlips && deck.size() == 0))
			return false;
		Card fromCard = null;
		if (from != null && from.getPiece() != null)
		{
			if (from.isWaste())
			{
				//System.out.println("isValidMove:if: " + waste.get(waste.size()-1));
				fromCard = waste.get(waste.size()-1);
			}
			else if (from.isFoundation())
			{
				if (to.isFoundation()) return false;
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
				//System.out.println("isValidMove:foundation else: " + fromCard.toString());
			} else {	
				//System.out.println("isValidMove:else: " + from.getPiece().getCard().toString());
				fromCard = from.getPiece().getCard();
				//System.out.println("2isValidMove:else: " + fromCard.toString());
			}
		}
//		else
//			System.out.println("isValidMove:from is null");
		if (to.isWaste()) return false;
		// check from card is a K and to is empty tab
		if (from != null && from.getPiece() != null
				&& fromCard.rank != 13
				&& to != null && to.getX() >= 0 
				&& board.get(to.getX() * boardHeight).getPiece().getCard() == null)
		{
			return false;
		}
		if(from != null && to != null && fromCard!= null) {
			if(to.getPiece().getCard()!=null && !to.isFoundation()) {
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
				// check if from card is free
				if (from.getX() >= 0 && !getLastFlippedCardInTab(from.getX()).equals(from))
					return false;
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
	
	public Card getTopFoundationCard(int foundationNum)
	{
		Card ret = null;
		switch(foundationNum) {
			case(0):
				if (!foundation0.isEmpty())
					ret = foundation0.get(foundation0.size()-1);
				break;
			case(1):
				if (!foundation1.isEmpty())
					ret = foundation1.get(foundation1.size()-1);
				break;
			case(2):
				if (!foundation2.isEmpty())
					ret = foundation2.get(foundation2.size()-1);
				break;
			case(3):
				if (!foundation3.isEmpty())
					ret = foundation3.get(foundation3.size()-1);
				break;
			default:
				System.out.println("Invalid foundation number in getTopFoundationCard!");
				return null;
		}
		return ret;
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
						System.out.print(p.getCard().toString() + "  | ");
					else
						System.out.print("*" + p.getCard().toString() + " | ");
				}
				else
					System.out.print("      | ");
				i++;
			}
			System.out.println();
		}
		System.out.print("F0: ");
		for (Card c : foundation0)
		{
			System.out.print(" |" + c + "| ");
		}
		System.out.println();
		System.out.print("F1: ");
		for (Card c : foundation1)
		{
			System.out.print(" |" + c + "| ");
		}
		System.out.println();
		System.out.print("F2: ");
		for (Card c : foundation2)
		{
			System.out.print(" |" + c + "| ");
		}
		System.out.println();
		System.out.print("F3: ");
		for (Card c : foundation3)
		{
			System.out.print(" |" + c + "| ");
		}
		System.out.println();
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
	
	public void printWaste(List<Card> waste)
	{
		System.out.println(" ===== BEGIN WASTE ======");
		for (Card c : waste)
		{
			System.out.println(c.toString());
		}
		System.out.println(" ===== END WASTE ======");
	}
	
	public void printTopWaste()
	{
		if (waste.size() > 0)
			System.out.println("Waste top: " + waste.get(waste.size()-1));
		else
			System.out.println("Waste top: null");
	}
	
	public void printGame()
	{
		printBoardText(board);
		printDeck(deck);
		printTopWaste();
	}
	
	public List<Position> copyBoard(List<Position> origBoard)
	{
		ArrayList<Position> newBoard = new ArrayList<Position>();
		
		for (Position origP : origBoard)
		{
			newBoard.add(new Position(origP.getX(), origP.getY(), new GamePiece(origP.getPiece().isFlipped(), origP.getPiece().getOwner(), origP.getPiece().getCard()),
					origP.isDeck(), origP.isWaste(), origP.isFoundation(), origP.getFoundationNum()
					));
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
		g.deckFlips = this.deckFlips;
		g.playsMade = this.playsMade;
		
		return g;
	}
}
