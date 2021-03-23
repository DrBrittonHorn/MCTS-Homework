package solitaire.game;

public enum Suit {
	
	SPADE(0) {
		public String toString() {
			return "S";
		}
	},
	DIAMOND(1) {
		public String toString() {
			return "D";
		}
	},
	CLUB(2) {
		public String toString() {
			return "C";
		}
	},
	HEART(3) {
		public String toString() {
			return "H";
		}
	};
	
	private int value;
	
	private Suit(int val)
	{
		this.value = val;
	}
	
	public int getValue()
	{
		return value;
	}
}
