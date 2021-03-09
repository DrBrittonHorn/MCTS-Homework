package solitaire.game;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import solitaire.agent.Agent;
import solitaire.agent.Human;

public final class GameView extends JComponent
{
	private Game game;
	private GameFrame frame;
	private Graphics bufferGraphics;
	private int width = 1000, height = 800, bufferH = 20, bufferW = 20;
	private int pieceWidth = 90, pieceHeight = 150;
	private int foundationOffset = 455, foundationBuffer = 55;
	private int tabularStartY = 200, tabularBuffer = foundationBuffer, cardStackOffset = 20;
	private int chosenCardBack = 5;
	private static final String imageLoc = "res/";
	// club = f0, diamond = f1, spade = f2, heart = f3
	private List<Image> clubs = new ArrayList<>(), spades = new ArrayList<>(), hearts = new ArrayList<>(),
			diamonds = new ArrayList<>(), cardBacks = new ArrayList<>();
	private int highlightX = -1, highlightY = -1;
	private boolean isActiveSelection, isWasteSelected, isFoundationSelected, isPositionSelected;
	private int selectedFoundation = -1, selectedPosition = -1;
	
	// bounding boxes for clicks
	private int deckX, deckY, wasteX, wasteY, f0X, f0Y, f1X, f1Y, f2X, f2Y, f3X, f3Y,
			t0X, t0Y, t1X, t1Y, t2X, t2Y, t3X, t3Y, t4X, t4Y, t5X, t5Y, t6X, t6Y;
	
	// keep track of what the player clicked on first
	private Position moveStart;

	public GameView(Game game)
	{
		this.game = game;
		this.loadImages();
	}

	private void loadImages()
	{
		// clubs
		IntStream.iterate(1, i -> i + 1).limit(13).forEach(i -> loadCard(i, Suit.CLUB));

		// spades
		IntStream.iterate(1, i -> i + 1).limit(13).forEach(i -> loadCard(i, Suit.SPADE));

		// diamonds
		IntStream.iterate(1, i -> i + 1).limit(13).forEach(i -> loadCard(i, Suit.DIAMOND));

		// hearts
		IntStream.iterate(1, i -> i + 1).limit(13).forEach(i -> loadCard(i, Suit.HEART));

		// card back
		loadCardBacks();
	}

	private void loadCard(int rank, Suit suit)
	{
		List<Image> cardList = null;
		if (suit == Suit.CLUB)
		{
			cardList = clubs;
		} else if (suit == Suit.SPADE)
		{
			cardList = spades;
		} else if (suit == Suit.DIAMOND)
		{
			cardList = diamonds;
		} else if (suit == Suit.HEART)
		{
			cardList = hearts;
		} else
		{
			System.out.println("YIKES! Bad suit!!");
			return;
		}

		String stringRank;
		// Get non-numeric value
		switch (rank)
		{
			case 1:
				stringRank = "A";
				break;
			case 11:
				stringRank = "J";
				break;
			case 12:
				stringRank = "Q";
				break;
			case 13:
				stringRank = "K";
				break;
			default:
				stringRank = Integer.toString(rank);
				break;
		}
		//System.out.println(imageLoc + stringRank + suit + ".png");
		cardList.add(Toolkit.getDefaultToolkit().createImage(imageLoc + stringRank + suit + ".png"));
	}
	
	private Image getCardImage(Card card)
	{
		if (card.suit == Suit.CLUB)
		{
			return clubs.get(card.rank-1);
		} else if (card.suit == Suit.SPADE)
		{
			return spades.get(card.rank-1);
		} else if (card.suit == Suit.DIAMOND)
		{
			return diamonds.get(card.rank-1);
		} else if (card.suit == Suit.HEART)
		{
			return hearts.get(card.rank-1);
		} else
		{
			System.out.println("YIKES! Bad suit!!");
			return null;
		}
	}

	private void loadCardBacks()
	{
		// blue, gray, green, purple, red, yellow
		cardBacks.add(Toolkit.getDefaultToolkit().createImage(imageLoc + "blue_back.png"));
		cardBacks.add(Toolkit.getDefaultToolkit().createImage(imageLoc + "gray_back.png"));
		cardBacks.add(Toolkit.getDefaultToolkit().createImage(imageLoc + "green_back.png"));
		cardBacks.add(Toolkit.getDefaultToolkit().createImage(imageLoc + "purple_back.png"));
		cardBacks.add(Toolkit.getDefaultToolkit().createImage(imageLoc + "red_back.png"));
		cardBacks.add(Toolkit.getDefaultToolkit().createImage(imageLoc + "yellow_back.png"));
	}

	public GameView showGame()
	{
		if (frame == null)
			this.frame = new GameFrame(this);
		return this;
	}

	public void paintComponent(Graphics g)
	{
		//System.out.println("painting...");
		bufferGraphics = g;
		Graphics2D g2d = (Graphics2D) g;
		drawBoard(g2d);
		drawPieces(g2d);
		drawDeck(g2d);
		drawWaste(g2d);
		drawSelectedTab(g2d);
	}

	private void drawBoard(Graphics2D g2d)
	{
		// background
		bufferGraphics.setColor(new Color(0, 80, 40));
		bufferGraphics.fillRect(0, 0, width, height);
		// outlines
		bufferGraphics.setColor(Color.WHITE);
		// outline deck and discard
		deckX = bufferW;
		deckY = bufferH;
		bufferGraphics.drawRect(deckX, deckY, pieceWidth, pieceHeight);
		wasteX = bufferW+pieceWidth+foundationBuffer;
		wasteY = bufferH;
		bufferGraphics.drawRect(wasteX, wasteY, pieceWidth, pieceHeight);
		// outline foundations
		IntStream.iterate(0, i -> i + 1).limit(4).forEach(i -> drawFoundation(i, g2d));
		// outline tabulars
		IntStream.iterate(0, i -> i + 1).limit(7).forEach(i -> drawTabular(i, g2d));
	}

	private void drawFoundation(int i, Graphics2D g2d)
	{
		int foundationStart = foundationOffset;
		switch (i) {
			case 0:
				f0X = foundationStart + ((pieceWidth + foundationBuffer) * i);
				f0Y = bufferH;
				break;
			case 1:
				f1X = foundationStart + ((pieceWidth + foundationBuffer) * i);
				f1Y = bufferH;
				break;
			case 2:
				f2X = foundationStart + ((pieceWidth + foundationBuffer) * i);
				f2Y = bufferH;
				break;
			case 3:
				f3X = foundationStart + ((pieceWidth + foundationBuffer) * i);
				f3Y = bufferH;
				break;
			default:
				System.out.println("Incorrect foundation number");
				break;
		}
		if (isFoundationSelected && selectedFoundation == i)
		{
			bufferGraphics.setColor(Color.RED);
		}
		bufferGraphics.drawRect(foundationStart + ((pieceWidth + foundationBuffer) * i), bufferH, pieceWidth,
				pieceHeight);
		if (isFoundationSelected && selectedFoundation == i)
		{
			bufferGraphics.setColor(Color.WHITE);
		}
		drawFoundationPieces(i, g2d);
	}

	private void drawTabular(int i, Graphics2D g2d)
	{
		int tabStart = bufferW;
		switch (i) {
			case 0:
				t0X = tabStart + ((pieceWidth + tabularBuffer) * i);
				t0Y = tabularStartY;
				break;
			case 1:
				t1X = tabStart + ((pieceWidth + tabularBuffer) * i);
				t1Y = tabularStartY;
				break;
			case 2:
				t2X = tabStart + ((pieceWidth + tabularBuffer) * i);
				t2Y = tabularStartY;
				break;
			case 3:
				t3X = tabStart + ((pieceWidth + tabularBuffer) * i);
				t3Y = tabularStartY;
				break;
			case 4:
				t4X = tabStart + ((pieceWidth + tabularBuffer) * i);
				t4Y = tabularStartY;
				break;
			case 5:
				t5X = tabStart + ((pieceWidth + tabularBuffer) * i);
				t5Y = tabularStartY;
				break;
			case 6:
				t6X = tabStart + ((pieceWidth + tabularBuffer) * i);
				t6Y = tabularStartY;
				break;
			default:
				System.out.println("Incorrect tabular number");
				break;
		}
		bufferGraphics.drawRect(tabStart + ((pieceWidth + tabularBuffer) * i), tabularStartY, pieceWidth, pieceHeight);
	}

	private void drawDeck(Graphics2D g2d)
	{
		if (game.deck.size() > 0)
		{
			g2d.drawImage(cardBacks.get(chosenCardBack), bufferW, bufferH, pieceWidth + bufferW, pieceHeight + bufferH,
					0, 0, cardBacks.get(chosenCardBack).getWidth(null), cardBacks.get(chosenCardBack).getHeight(null),
					this);
		}
	}

	private void drawWaste(Graphics2D g2d)
	{
		if (game.waste.size() > 0)
		{
			int highlightX;
			if (game.isSingleFlip)
			{
				Card c = game.waste.get(game.waste.size()-1);
				g2d.drawImage(getCardImage(c), wasteX, wasteY,
						wasteX + pieceWidth, pieceHeight + wasteY, 0, 0,
						getCardImage(c).getWidth(null), getCardImage(c).getHeight(null), this);
				highlightX = wasteX;
			}
			else
			{
				if (game.getLastFlipCount() > 2)
				{
					Card c2 = game.waste.get(game.waste.size()-3);
					g2d.drawImage(getCardImage(c2), wasteX, wasteY,
							wasteX + pieceWidth, pieceHeight + wasteY, 0, 0,
							getCardImage(c2).getWidth(null), getCardImage(c2).getHeight(null), this);
					Card c1 = game.waste.get(game.waste.size()-2);
					g2d.drawImage(getCardImage(c1), wasteX + cardStackOffset, wasteY,
							wasteX + pieceWidth + cardStackOffset, pieceHeight + wasteY, 0, 0,
							getCardImage(c1).getWidth(null), getCardImage(c1).getHeight(null), this);
					Card c0 = game.waste.get(game.waste.size()-1);
					g2d.drawImage(getCardImage(c0), wasteX + (cardStackOffset*2), wasteY,
							wasteX + pieceWidth + (cardStackOffset*2), pieceHeight + wasteY, 0, 0,
							getCardImage(c0).getWidth(null), getCardImage(c0).getHeight(null), this);
					highlightX = wasteX + (cardStackOffset*2);
				}
				else if (game.getLastFlipCount() > 1)
				{
					Card c1 = game.waste.get(game.waste.size()-2);
					g2d.drawImage(getCardImage(c1), wasteX, wasteY,
							wasteX + pieceWidth, pieceHeight + wasteY, 0, 0,
							getCardImage(c1).getWidth(null), getCardImage(c1).getHeight(null), this);
					Card c0 = game.waste.get(game.waste.size()-1);
					g2d.drawImage(getCardImage(c0), wasteX + cardStackOffset, wasteY,
							wasteX + pieceWidth + cardStackOffset, pieceHeight + wasteY, 0, 0,
							getCardImage(c0).getWidth(null), getCardImage(c0).getHeight(null), this);
					highlightX = wasteX + cardStackOffset;
				}
				else
				{
					Card c = game.waste.get(game.waste.size()-1);
					g2d.drawImage(getCardImage(c), wasteX, wasteY,
							wasteX + pieceWidth, pieceHeight + wasteY, 0, 0,
							getCardImage(c).getWidth(null), getCardImage(c).getHeight(null), this);
					highlightX = wasteX;
				}
			}
			if (isWasteSelected)
			{
				bufferGraphics.setColor(Color.RED);
				bufferGraphics.drawRect(highlightX, wasteY, pieceWidth, pieceHeight);
				bufferGraphics.setColor(Color.WHITE);
			}
		}
	}

	private void drawFoundationPieces(int i, Graphics2D g2d)
	{
		List<Card> foundationCards = null;
		if (i == 0)
		{
			foundationCards = game.foundation0;
		}
		if (i == 1)
		{
			foundationCards = game.foundation1;
		}
		if (i == 2)
		{
			foundationCards = game.foundation2;
		}
		if (i == 3)
		{
			foundationCards = game.foundation3;
		}
		
		if (foundationCards.size() > 0)
		{
			g2d.drawImage(getCardImage(foundationCards.get(foundationCards.size()-1)), foundationOffset + ((pieceWidth + foundationBuffer) * i), bufferH,
					foundationOffset + ((pieceWidth + foundationBuffer) * i) + pieceWidth, pieceHeight + bufferH, 0, 0,
					getCardImage(foundationCards.get(foundationCards.size()-1)).getWidth(null), getCardImage(foundationCards.get(foundationCards.size()-1)).getHeight(null), this);
		}
	}

	private void drawPieces(Graphics2D g2d)
	{
		List<Position> board = game.getBoard();
		for (int x = 0; x < game.getBoardWidth(); x++)
		{
			for (int y = 0; y < game.getBoardHeight(); y++)
			{
				GamePiece piece = board.get((x * game.getBoardHeight()) + y).getPiece();
				if (piece.getCard() != null)
				{
					if (piece.isFlipped())
					{
						g2d.drawImage(getCardImage(piece.getCard()), bufferW + ((pieceWidth + tabularBuffer) * x), tabularStartY + (y*cardStackOffset),
								bufferW + ((pieceWidth + tabularBuffer) * x) + pieceWidth, pieceHeight + tabularStartY + (y*cardStackOffset), 0, 0,
								getCardImage(piece.getCard()).getWidth(null), getCardImage(piece.getCard()).getHeight(null), this);
					} else
					{
						g2d.drawImage(cardBacks.get(chosenCardBack), bufferW + ((pieceWidth + tabularBuffer) * x), tabularStartY + (y*cardStackOffset),
								bufferW + ((pieceWidth + tabularBuffer) * x) + pieceWidth, pieceHeight + tabularStartY + (y*cardStackOffset), 0, 0,
								cardBacks.get(chosenCardBack).getWidth(null), cardBacks.get(chosenCardBack).getHeight(null), this);
					}
					
				}
			}
		}
	}
	
	private void drawSelectedTab(Graphics2D g2d)
	{
		//System.out.println(isActiveSelection + " " + isPositionSelected);
		if (isActiveSelection && isPositionSelected)
		{
			//System.out.println("Drawing selected tab");
			// board.get((x * game.getBoardHeight()) + y)
			int x = selectedPosition / game.getBoardHeight();
			int y = selectedPosition % game.getBoardHeight();
			bufferGraphics.setColor(Color.RED);
			bufferGraphics.drawRect(bufferW + ((pieceWidth + tabularBuffer) * x), tabularStartY + (y*cardStackOffset), pieceWidth, pieceHeight);
			bufferGraphics.setColor(Color.WHITE);
		}
	}
	
	/**
	 * Handles clicks from the user
	 * @param x
	 * @param y
	 * @param human
	 */
	protected void handleMouseClick(int x, int y, Agent human)
	{
		if (human == null) return;
		//System.out.println(x + "::" + y);
		Human h = (Human) human;
		Position posClick = null;
		// check click on deck
		if (y >= deckY && y <= deckY + pieceHeight
				&& x >= deckX && x <= deckX + pieceWidth)
		{
			if (!isActiveSelection)
			{
				System.out.println("Clicked on deck");
				posClick = game.getBoard().get(game.deckPos);
				h.setMove(game, new Move(null, posClick));
				moveStart = null;
				h.responded = true;
				return;
			}
		}
		
		// check click on waste
		if (y >= wasteY && y <= wasteY + pieceHeight
				&& x >= wasteX && x <= wasteX + pieceWidth + (game.getLastFlipCount() * cardStackOffset))
		{
			if (!isActiveSelection && game.waste.size() > 0)
			{
				System.out.println("Clicked on waste w/o active selection");
				isActiveSelection = true;
				isWasteSelected = true;
				moveStart = game.getBoard().get(game.wastePos);
			}
			else if (isWasteSelected)
			{
				System.out.println("Clicked on waste w/ existing selection");
				isActiveSelection = false;
				isWasteSelected = false;
			}
			else
				System.out.println("Not clickable right now.");
			//posClick = game.getBoard().get(game.wastePos);
		}
		
		// check click on foundations
		if (y >= f0Y && y <= f0Y + pieceHeight
				&& x >= f0X && x <= f0X + pieceWidth)
		{
			System.out.println(isActiveSelection);
			if (!isActiveSelection && game.foundation0.size() > 0)
			{
				System.out.println("Clicked on F0 w/o active selection");
				isActiveSelection = true;
				isFoundationSelected = true;
				selectedFoundation = 0;
				moveStart = game.getBoard().get(game.f0Pos);
			}
			else if (isFoundationSelected && selectedFoundation == 0)
			{
				System.out.println("Clicked on F0 w/ existing selection");
				isActiveSelection = false;
				isFoundationSelected = false;
				selectedFoundation = -1;
			}
			else if (isActiveSelection)
			{
				// clicked on foundation second, attempting to move card to foundation
				if (moveStart == null)
					System.out.println("move start is null!");
				boolean valid = h.setMove(game, new Move(moveStart, game.getBoard().get(game.f0Pos)));
				if (valid)
				{
					isActiveSelection = false;
					isFoundationSelected = false;
					isPositionSelected = false;
					isWasteSelected = false;
					selectedFoundation = -1;
					selectedPosition = -1;
					h.responded = true;
				}
				return;
			}
			//posClick = game.getBoard().get(game.f0Pos);
		}
		else if (y >= f1Y && y <= f1Y + pieceHeight
				&& x >= f1X && x <= f1X + pieceWidth)
		{
			if (!isActiveSelection && game.foundation1.size() > 0)
			{
				System.out.println("Clicked on F1 w/o active selection");
				isActiveSelection = true;
				isFoundationSelected = true;
				selectedFoundation = 1;
				moveStart = game.getBoard().get(game.f1Pos);
			}
			else if (isFoundationSelected && selectedFoundation == 1)
			{
				System.out.println("Clicked on F1 w/ existing selection");
				isActiveSelection = false;
				isFoundationSelected = false;
				selectedFoundation = -1;
			}
			else if (isActiveSelection)
			{
				// clicked on foundation second, attempting to move card to foundation
				if (moveStart == null)
					System.out.println("move start is null!");
				boolean valid = h.setMove(game, new Move(moveStart, game.getBoard().get(game.f1Pos)));
				if (valid)
				{
					isActiveSelection = false;
					isFoundationSelected = false;
					isPositionSelected = false;
					isWasteSelected = false;
					selectedFoundation = -1;
					selectedPosition = -1;
					h.responded = true;
				}
				return;
			}
		}
		else if (y >= f2Y && y <= f2Y + pieceHeight
				&& x >= f2X && x <= f2X + pieceWidth)
		{
			if (!isActiveSelection && game.foundation2.size() > 0)
			{
				System.out.println("Clicked on F2 w/o active selection");
				isActiveSelection = true;
				isFoundationSelected = true;
				selectedFoundation = 2;
				moveStart = game.getBoard().get(game.f2Pos);
			}
			else if (isFoundationSelected && selectedFoundation == 2)
			{
				System.out.println("Clicked on F2 w/ existing selection");
				isActiveSelection = false;
				isFoundationSelected = false;
				selectedFoundation = -1;
			}
			else if (isActiveSelection)
			{
				// clicked on foundation second, attempting to move card to foundation
				if (moveStart == null)
					System.out.println("move start is null!");
				boolean valid = h.setMove(game, new Move(moveStart, game.getBoard().get(game.f2Pos)));
				if (valid)
				{
					isActiveSelection = false;
					isFoundationSelected = false;
					isPositionSelected = false;
					isWasteSelected = false;
					selectedFoundation = -1;
					selectedPosition = -1;
					h.responded = true;
				}
				return;
			}
		}
		else if (y >= f3Y && y <= f3Y + pieceHeight
				&& x >= f3X && x <= f3X + pieceWidth)
		{
			if (!isActiveSelection && game.foundation3.size() > 0)
			{
				System.out.println("Clicked on F3 w/o active selection");
				isActiveSelection = true;
				isFoundationSelected = true;
				selectedFoundation = 3;
				moveStart = game.getBoard().get(game.f3Pos);
			}
			else if (isFoundationSelected && selectedFoundation == 3)
			{
				System.out.println("Clicked on F3 w/ existing selection");
				isActiveSelection = false;
				isFoundationSelected = false;
				selectedFoundation = -1;
			}
			else if (isActiveSelection)
			{
				// clicked on foundation second, attempting to move card to foundation
				if (moveStart == null)
					System.out.println("move start is null!");
				boolean valid = h.setMove(game, new Move(moveStart, game.getBoard().get(game.f3Pos)));
				if (valid)
				{
					isActiveSelection = false;
					isFoundationSelected = false;
					isPositionSelected = false;
					isWasteSelected = false;
					selectedFoundation = -1;
					selectedPosition = -1;
					h.responded = true;
				}
				return;
			}
		}
		
		// check click on tabular (later check for individual card)
		if (y >= t0Y
				&& x >= t0X && x <= t0X + pieceWidth)
		{
			handleTabClick(0, y, h);
		}
		else if (y >= t1Y
				&& x >= t1X && x <= t1X + pieceWidth)
		{
			handleTabClick(1, y, h);			
		}
		else if (y >= t2Y
				&& x >= t2X && x <= t2X + pieceWidth)
		{
			handleTabClick(2, y, h);			
		}
		else if (y >= t3Y
				&& x >= t3X && x <= t3X + pieceWidth)
		{
			handleTabClick(3, y, h);			
		}
		else if (y >= t4Y
				&& x >= t4X && x <= t4X + pieceWidth)
		{
			handleTabClick(4, y, h);			
		}
		if (y >= t5Y
				&& x >= t5X && x <= t5X + pieceWidth)
		{
			handleTabClick(5, y, h);			
		}
		else if (y >= t6Y
				&& x >= t6X && x <= t6X + pieceWidth)
		{
			handleTabClick(6, y, h);			
		}
		human.responded = true;
	}
	
	private void handleTabClick(int tab, int y, Human h)
	{
		Position posClick = getPositionFromClick(tab,y);
		if (posClick == null)
		{
			System.out.println("Attempted to click on empty tab " + tab);
			posClick = game.board.get(tab * game.getBoardHeight());
		}
		else if (posClick.getPiece().isFlipped())
		{
			System.out.println("Clicked on tabular "+tab+", card " + posClick.getY());
		}
		else
		{
			posClick = game.getLastFlippedCardInTab(tab);
			if (posClick == null) // only unflipped cards
			{
				System.out.println("Unflipping card on tabular "+tab);
				posClick = game.getLastUnflippedCardInTab(tab);
				boolean valid = h.setMove(game, new Move(null, posClick));
				if (valid)
				{
					isActiveSelection = false;
					isFoundationSelected = false;
					isPositionSelected = false;
					isWasteSelected = false;
					selectedFoundation = -1;
					selectedPosition = -1;
					h.responded = true;
				}
				return;
			}
			System.out.println("Augmented. Clicked on tabular "+tab+", card " + posClick.getY());
		}
		if (!isActiveSelection)
		{
			System.out.println("Clicked on tab "+tab+" " + posClick.getY() +  "w/o active selection");
			isActiveSelection = true;
			isPositionSelected = true;
			selectedPosition = (posClick.getX() * game.getBoardHeight()) + posClick.getY();
			moveStart = posClick;
		}
		else if (isPositionSelected && selectedPosition == (posClick.getX() * game.getBoardHeight()) + posClick.getY())
		{
			isActiveSelection = false;
			isPositionSelected = false;
			selectedPosition = -1;
		}
		else
		{
			// clicked on tab second, attempting to move card here
			if (moveStart == null)
				System.out.println("move start is null!");
			boolean valid = h.setMove(game, new Move(moveStart, posClick));
			if (valid)
			{
				isActiveSelection = false;
				isFoundationSelected = false;
				isPositionSelected = false;
				isWasteSelected = false;
				selectedFoundation = -1;
				selectedPosition = -1;
				h.responded = true;
			}
			return;
		}
	}
	
	private Position getPositionFromClick(int tab, int yVal)
	{
		int numCards = 0;
		for (int y = 0; y < game.getBoardHeight(); y++)
		{
			GamePiece piece = game.board.get((tab * game.getBoardHeight()) + y).getPiece();
			if (piece.getCard() != null)
			{
				numCards++;
				//System.out.println("Card: " + piece.getCard().toString());
			}
			else
				break;
		}
		if (numCards == 0)
		{
			// empty tab
			return null;
		}
		int chosenCard = -1;
		if (yVal >= tabularStartY + ((numCards-1)*cardStackOffset)
				&& yVal <= pieceHeight + tabularStartY + ((numCards-1)*cardStackOffset))
		{
			chosenCard = numCards - 1;
			//System.out.println("Clicked on bottom card");
		}
		else if (yVal < tabularStartY + ((numCards-1)*cardStackOffset))
		{
			int card = (yVal - tabularStartY) / cardStackOffset;
			chosenCard = card;
			System.out.println("cardChosen: " + card);
		}
		else return null;
		return game.board.get((tab * game.getBoardHeight()) + chosenCard);
	}

	/**
	 * The Class GameFrame.
	 */
	@SuppressWarnings("serial")
	public class GameFrame extends JFrame
	{
		/**
		 * Instantiates a new game frame.
		 *
		 * @param comp the comp
		 */
		public GameFrame(JComponent comp)
		{
			getContentPane().add(BorderLayout.CENTER, comp);
			getContentPane().setPreferredSize(new Dimension(width, height));
			pack();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation((int) (screen.getWidth() * 3 / 8), (int) (screen.getHeight() * 3 / 8));
			this.setVisible(true);
			this.setResizable(true);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			repaint();
		}
	}
}
