package solitaire.game;

public enum Suit {
	SPADE {
		public String toString() {
			return "S";
		}
	},
	CLUB {
		public String toString() {
			return "C";
		}
	},
	DIAMOND {
		public String toString() {
			return "D";
		}
	},
	HEART {
		public String toString() {
			return "H";
		}
	};
}
