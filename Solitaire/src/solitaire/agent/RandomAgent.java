package solitaire.agent;

import java.util.List;
import java.util.Random;

import solitaire.game.Game;
import solitaire.game.Move;
import solitaire.game.Position;

public class RandomAgent extends Agent {


	@Override
	public Move getMove(Game game, long timeDue) {
		System.out.println("**************************** GET MOVE ******************************");
		List<Move> validMoves = game.getValidMoves(game.getBoard(), game.getTurn());
		
		for (Move move : validMoves)
		{
			if(move.getFromPosition() == null)
				System.out.println("null -> " + move.getToPosition());
			else
			{
				System.out.println(move.getFromPosition() + " -> " + move.getToPosition());
			}
			Game newG = game.simulateMove(game.getBoard(), move);
			if (newG.isWinningBoard(newG.board) == game.getTurn())
			{
				System.out.println("Winning move for agent found!");
				return move;
			}
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Random r = new Random();
		int chosen = r.nextInt(validMoves.size());
		return validMoves.get(chosen);
	}

}
