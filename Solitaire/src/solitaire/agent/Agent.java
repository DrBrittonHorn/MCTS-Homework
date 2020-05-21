package solitaire.agent;

import solitaire.game.Game;
import solitaire.game.Move;
import solitaire.game.Position;

public abstract class Agent {
	public volatile boolean responded = true;
	public abstract Move getMove(Game game, long timeDue);
}
