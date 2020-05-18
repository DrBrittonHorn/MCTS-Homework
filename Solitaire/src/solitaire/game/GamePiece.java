package solitaire.game;

public class GamePiece {
	private boolean flipped;
	private int owner;
	private Card card;
	
	public GamePiece(boolean flipped, int owner, Card card)
	{
		this.flipped = flipped;
		this.owner = owner;
		this.card = card;
	}
	
	@SuppressWarnings("unused")
	private GamePiece()
	{
		
	}
	
	public boolean isFlipped()
	{
		return this.flipped;
	}
	
	public int getOwner()
	{
		return this.owner;
	}
	
	protected void setOwner(int owner)
	{
		this.owner = owner;
	}
	
	protected void setFlipped(boolean flipped)
	{
		this.flipped = flipped;
	}
	
	public Card getCard()
	{
		return this.card;
	}
	
	protected void setCard(Card card)
	{
		this.card = card;
	}
}
