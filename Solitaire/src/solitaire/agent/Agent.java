package solitaire.agent;

import solitaire.game.Game;
import solitaire.game.Position;

public abstract class Agent {
	public boolean responded = true;
	public abstract Position getMove(Game game, long timeDue);
}
