package solitaire.agent;

import java.util.List;
import java.util.Random;

import solitaire.game.Game;
import solitaire.game.Move;
import solitaire.game.Position;

public class RandomAgent extends Agent {


	@Override
	public Move getMove(Game game, long timeDue) {
		List<Move> validMoves = game.getValidMoves(game.getBoard(), game.getTurn());
		
		for (Move move : validMoves)
		{
			List<Position> newBoard = game.simulateMove(game.getBoard(), move);
			if (game.isWinningBoard(newBoard) == game.getTurn())
			{
				System.out.println("Winning move for agent found!");
				return move;
			}
		}

		Random r = new Random();
		int chosen = r.nextInt(validMoves.size());
		return validMoves.get(chosen);
	}

}
