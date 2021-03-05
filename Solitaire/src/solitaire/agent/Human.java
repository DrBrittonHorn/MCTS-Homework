package solitaire.agent;

import solitaire.game.Game;
import solitaire.game.GamePiece;
import solitaire.game.Move;
import solitaire.game.Position;

public class Human extends Agent{
	private Move moveToMake;
	public Human()
	{
		responded = false;
	}

	@Override
	public Move getMove(Game game, long timeDue) {
		responded = false;
		if (moveToMake == null)
			return null;
		Move returnMove = moveToMake.shallowCopy();
		moveToMake = null;
		return returnMove;
	}
	
	public boolean setMove(Game game, Move move)
	{
		System.out.println("Setting move");
		moveToMake = move;
		//responded = true;
		return game.isValidMove(move);
	}

}
