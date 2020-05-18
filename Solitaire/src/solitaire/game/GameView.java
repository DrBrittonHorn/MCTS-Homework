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
	private List<Image> clubs = new ArrayList<>(), spades = new ArrayList<>(), hearts = new ArrayList<>(),
			diamonds = new ArrayList<>(), cardBacks = new ArrayList<>();
	
	// bounding boxes for clicks
	private int deckX, deckY, wasteX, wasteY, f0X, f0Y, f1X, f1Y, f2X, f2Y, f3X, f3Y,
			t0X, t0Y, t1X, t1Y, t2X, t2Y, t3X, t3Y, t4X, t4Y, t5X, t5Y, t6X, t6Y;

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
		System.out.println(imageLoc + stringRank + suit + ".png");
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
		bufferGraphics = g;
		Graphics2D g2d = (Graphics2D) g;
		drawBoard(g2d);
		drawPieces(g2d);
		drawDeck(g2d);
		drawDiscard(g2d);
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
		bufferGraphics.drawRect(foundationStart + ((pieceWidth + foundationBuffer) * i), bufferH, pieceWidth,
				pieceHeight);
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

	private void drawDiscard(Graphics2D g2d)
	{
		if (game.waste.size() > 0)
		{
			Card c = game.deck.get(0);
			g2d.drawImage(getCardImage(c), bufferW + pieceWidth + foundationBuffer, bufferH,
					bufferW + (2 * pieceWidth) + foundationBuffer, pieceHeight + bufferH, 0, 0,
					getCardImage(c).getWidth(null), getCardImage(c).getHeight(null), this);
		}
	}

	private void drawFoundationPieces(int i, Graphics2D g2d)
	{
		List<Card> foundationCards = null;
		if (i == 0)
		{
			foundationCards = game.foundationClub;
		}
		if (i == 1)
		{
			foundationCards = game.foundationDiamond;
		}
		if (i == 2)
		{
			foundationCards = game.foundationHeart;
		}
		if (i == 3)
		{
			foundationCards = game.foundationSpade;
		}
		
		if (game.foundationClub.size() > 0)
		{
			g2d.drawImage(getCardImage(foundationCards.get(0)), foundationOffset + ((pieceWidth + foundationBuffer) * i), bufferH,
					foundationOffset + ((pieceWidth + foundationBuffer) * i) + pieceWidth, pieceHeight + bufferH, 0, 0,
					getCardImage(foundationCards.get(0)).getWidth(null), getCardImage(foundationCards.get(0)).getHeight(null), this);
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
	
	/**
	 * Handles clicks from the user
	 * @param x
	 * @param y
	 * @param human
	 */
	protected void handleMouseClick(int x, int y, Agent human)
	{
		if (human == null) return;
		System.out.println(x + "::" + y);
		Human h = (Human) human;
		
		// check click on deck
		if (y >= deckY && y <= deckY + pieceHeight
				&& x >= deckX && x <= deckX + pieceWidth)
		{
			System.out.println("Clicked on deck");
		}
		// check click on waste
		if (y >= wasteY && y <= wasteY + pieceHeight
				&& x >= wasteX && x <= wasteX + pieceWidth)
		{
			System.out.println("Clicked on waste");
		}
		// check click on foundations
		if (y >= f0Y && y <= f0Y + pieceHeight
				&& x >= f0X && x <= f0X + pieceWidth)
		{
			System.out.println("Clicked on foundation 0");
		}
		if (y >= f1Y && y <= f1Y + pieceHeight
				&& x >= f1X && x <= f1X + pieceWidth)
		{
			System.out.println("Clicked on foundation 1");
		}
		if (y >= f2Y && y <= f2Y + pieceHeight
				&& x >= f2X && x <= f2X + pieceWidth)
		{
			System.out.println("Clicked on foundation 2");
		}
		if (y >= f3Y && y <= f3Y + pieceHeight
				&& x >= f3X && x <= f3X + pieceWidth)
		{
			System.out.println("Clicked on foundation 3");
		}
		// check click on tabular (later check for individual card)
		if (y >= t0Y && y <= t0Y + pieceHeight
				&& x >= t0X && x <= t0X + pieceWidth)
		{
			System.out.println("Clicked on tabular 0");
		}
		if (y >= t1Y && y <= t1Y + pieceHeight
				&& x >= t1X && x <= t1X + pieceWidth)
		{
			System.out.println("Clicked on tabular 1");
		}
		if (y >= t2Y && y <= t2Y + pieceHeight
				&& x >= t2X && x <= t2X + pieceWidth)
		{
			System.out.println("Clicked on tabular 2");
		}
		if (y >= t3Y && y <= t3Y + pieceHeight
				&& x >= t3X && x <= t3X + pieceWidth)
		{
			System.out.println("Clicked on tabular 3");
		}
		if (y >= t4Y && y <= t4Y + pieceHeight
				&& x >= t4X && x <= t4X + pieceWidth)
		{
			System.out.println("Clicked on tabular 4");
		}
		if (y >= t5Y && y <= t5Y + pieceHeight
				&& x >= t5X && x <= t5X + pieceWidth)
		{
			System.out.println("Clicked on tabular 5");
		}
		if (y >= t6Y && y <= t6Y + pieceHeight
				&& x >= t6X && x <= t6X + pieceWidth)
		{
			System.out.println("Clicked on tabular 6");
		}
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
