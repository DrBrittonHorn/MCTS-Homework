package solitaire.game;

public class Card {
	public Suit suit;
	// 1 = ace ... 11 = J, 12 = Q, 13 = K
	public int rank;
	
	@SuppressWarnings("unused")
	private Card()
	{
		
	}
	
	public Card(int rank, Suit suit)
	{
		this.rank = rank;
		this.suit = suit;
	}
	
	public String toString()
	{
		String stringRank;
		// Get non-numeric value
		switch(this.rank) {
			case 1:
				stringRank = " A";
				break;
			case 11:
				stringRank = " J";
				break;
			case 12:
				stringRank = " Q";
				break;
			case 13:
				stringRank = " K";
				break;
			default:
				stringRank = String.format ("%2d", this.rank);
				break;
		}
		return stringRank + " " + this.suit;
	}
}
