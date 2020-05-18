package solitaire.agent;

import solitaire.game.Game;
import solitaire.game.GamePiece;
import solitaire.game.Position;

public class Human extends Agent{
	public int xClick,yClick;
	public Human()
	{
		responded = false;
	}

	@Override
	public Position getMove(Game game, long timeDue) {
		Position p = new Position(xClick,yClick,new GamePiece(false,game.getTurn(),null));
		xClick = -1;
		yClick = -1;
		responded = false;
		return p;
	}

}
