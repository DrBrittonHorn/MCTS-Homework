package solitaire.game;

public class Position {
	private int x;
	private int y;
	private int foundationNum;
	private boolean foundation;
	private boolean waste;
	private boolean deck;
	private GamePiece p;
	
	public Position(int x, int y, GamePiece p)
	{
		this(x, y, p, false, false, false, -1);
	}
	
	public Position(int x, int y, GamePiece p, boolean isDeck, boolean isWaste, boolean isFoundation, int foundationNum)
	{
		this.x = x;
		this.y = y;
		this.p = p;
		this.deck = isDeck;
		this.waste = isWaste;
		this.foundation = isFoundation;
		this.foundationNum = foundationNum;
	}
	
	@SuppressWarnings("unused")
	private Position() {}
	
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public GamePiece getPiece()
	{
		return p;
	}
	
	protected void setX(int x)
	{
		this.x = x;
	}
	protected void setY(int y)
	{
		this.y = y;
	}
	protected void setPiece(GamePiece p)
	{
		this.p = p;
	}
	public boolean isFoundation()
	{
		return foundation;
	}

	public void setFoundation(boolean foundation)
	{
		this.foundation = foundation;
	}
	
	public int getFoundationNum()
	{
		return foundationNum;
	}
	
	public void setFoundationNum(int foundationNum)
	{
		this.foundationNum = foundationNum;
	}

	public boolean isWaste()
	{
		return waste;
	}

	public void setWaste(boolean waste)
	{
		this.waste = waste;
	}

	public boolean isDeck()
	{
		return deck;
	}

	public void setDeck(boolean deck)
	{
		this.deck = deck;
	}
	
	public Position copy()
	{
		Position copy = new Position(this.x, this.y, this.p, this.deck, this.waste, this.foundation, this.foundationNum);
		return copy;
	}
}

