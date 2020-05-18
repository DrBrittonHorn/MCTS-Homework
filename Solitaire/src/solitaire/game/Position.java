package solitaire.game;

public class Position {
	private int x;
	private int y;
	private GamePiece p;
	
	public Position(int x, int y, GamePiece p)
	{
		this.x = x;
		this.y = y;
		this.p = p;
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
}

