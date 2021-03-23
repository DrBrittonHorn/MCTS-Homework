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
	@Override
	public boolean equals(Object o)
	{
		if (o == this) { 
            return true; 
        } 
  
        if (!(o instanceof GamePiece)) { 
            return false; 
        } 
          
        GamePiece p = (GamePiece) o; 
          
        return p.flipped == this.flipped &&
        		p.owner == this.owner &&
        		p.card.equals(this.card);
	}
	
	@Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((flipped) ? 1 : 0);
        result = 31 * result + owner;
        result = 31 * result + (card == null ? 0 : card.hashCode());
        return result;
    }
}
