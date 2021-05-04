package solitaire.agent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import solitaire.game.Game;
import solitaire.game.Move;

public class HiddenMCTSSolution extends Agent {
	private Game game = null;
	private static final Random rand = new Random();
	Node root = null;
	
	@Override
	public Move getMove(Game game, long timeDue) {
		System.out.println("*********** Starting MCTS *************");
		this.game = game;
		if (root == null)
		{
			root = new Node();
			root.state = game;
		}
		root.depth = 0;
		/*for (Move m : root.state.getValidMoves(root.state.board, 1))
		{
			System.out.println(m.toString());
			root.state.hiddenInfoSimulateMove(root.state.board, m);
		}*/
		performMCTS(timeDue);
		
		Node ret = bestRootMove();
		//printTree(root);
		if (ret == null)
		{
			System.out.println("Returning null from MCTS");
			return null;
		}
		System.out.println("*********** Finished MCTS *************");
		root = null;
		return ret.moveToGetHere;
	}
	
	private void performMCTS(long timeLimit)
	{
		DateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS"); 
		long startTime = System.currentTimeMillis();
        Date startDate = new Date(startTime); 
		System.out.println("start time: " + format.format(startDate));
		long timeDue = System.currentTimeMillis() + timeLimit;
        Date timeDueDate = new Date(timeDue); 
		System.out.println("time due: " + format.format(timeDueDate));
		
		int iters = 0, maxIter = 100000;
		while (System.currentTimeMillis() < timeDue && iters++ < maxIter)
		{
			System.out.println("New MCTS iteration: " + iters);
			Node n = selection();
//			System.out.print("Selected Node:");
//			n.printNode();
			n = expansion(n);
//			System.out.print("Expanding node:");
//			n.printNode();
			int score = simulate(n);
			backpropagate(n, score);
			if (n.parent.simulations == n.parent.children.size())
				n.parent.isFullyExpanded = true;
		}
	}
	
	private Node selection()
	{
		Node chosen = root;
		while (chosen.isFullyExpanded)
		{
			chosen = chosen.bestUCT();
		}
		return chosen;
	}
	
	private Node expansion(Node n)
	{
		if (n.children == null)
		{
			n.children = new ArrayList<Node>();
			for (Move mv : n.state.getValidMoves(n.state.board, 1))
			{
				//System.out.println("Making new child: " + mv);
				Node child = new Node();
				child.moveToGetHere = mv;
				child.depth = n.depth+1;
				child.parent = n;
				child.state = n.state.hiddenInfoSimulateMove(n.state.board, mv);
				
				if (child.state.isWinningBoard(child.state.board) != 0 || 
						child.state.getValidMoves(child.state.board, 1).isEmpty() ||
						child.state.maxPlays <= child.state.playsMade)
				{
					child.isFullyExpanded = true;
				}
				
				// add child to parent's list
				n.children.add(child);
			}
		}
		for (Node c : n.children)
		{
			if (c.simulations == 0)
				return c;
		}
		System.out.println("This should be a fully expanded node");
		return null;
	}
	
	private int simulate(Node n)
	{
		List<Move> validMoves = n.state.getValidMoves(n.state.board, 1);
		Game newG = n.state;
		while (newG.isWinningBoard(newG.board) == 0 && !validMoves.isEmpty())
		{
			int randInt = rand.nextInt(validMoves.size());
			try {
				newG = newG.hiddenInfoSimulateMove(newG.board, validMoves.get(randInt));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				newG.printGame();
				System.out.println(validMoves.get(randInt).toString());
				System.exit(1);
			}
			validMoves = newG.getValidMoves(newG.board, 1);
		}
		
		return newG.isWinningBoard(newG.board);
	}
	
	private void backpropagate(Node n, int score)
	{
		while(n != null)
		{
			if (score != -1) // ignoring losses for now
				n.score += score;
			n.simulations++;
			n = n.parent;
		}
	}
	
	private Node bestRootMove()
	{
		if (root.children == null )
			return root;
		double bestAvgScore = -1;
		Node bestChild = null;
		for (Node child : root.children)
		{
			child.printNodeStats();
			double childAvgScore = (child.simulations == 0) ? 0.0 : (child.score / (double) child.simulations);
			if (childAvgScore > bestAvgScore)
			{
				bestAvgScore = childAvgScore;
				bestChild = child;
			}
		}
		
		// return the move with highest win percentage
		return bestChild;
	}
	
	private class Node
	{
		Game state;
		int depth;
		Move moveToGetHere;
		Node parent;
		List<Node> children;
		int simulations;
		int score;
		boolean isFullyExpanded;
		
		public Node bestUCT()
		{
			double bestUCTVal = -1.0f;
			Node best = null;
			for (Node child : children)
			{
				double childUCT = child.uct();
				if (childUCT > bestUCTVal)
				{
					best = child;
					bestUCTVal = childUCT; 
				}
			}
			return best;
		}
		
		public double uct()
		{
			// just return 0 for the root node, it's never used and won't cause null pointer exceptions
			if (this.parent == null)
				return 0.0;
			// if no simulations have been run, return max value
			if (this.simulations == 0)
				return Double.MAX_VALUE;
			// calculate uct
			// uct = (w_i / s_i) + (C * sqrt(log(S)/s_i))
			return (this.score / (double) this.simulations) + 
					(2 * Math.sqrt( // added C_p parameter since score is outside [0,1]
							Math.log(this.parent.simulations) / (double) this.simulations)
							);
		}
		
		public void printNode()
		{
			System.out.println("===== Node Begin =====");
			state.printGame();
			System.out.println("score: " + this.score + ", simulations: " + this.simulations + ", uct: " + this.uct() +
					", moveToGetHere: " + ((this.moveToGetHere != null) ? moveToGetHere.toString() 
					: "null"));
			System.out.println("===== Node End =====");
		}
		
		public void printNodeStats()
		{
			System.out.println("score: " + this.score + ", simulations: " + this.simulations + ", parent sim: " + this.parent.simulations + ", uct: " + this.uct() + ", avg: " + (score/(double)simulations) + 
					", moveToGetHere: " + ((this.moveToGetHere != null) ? moveToGetHere.toString() 
					: "null"));
		}
	
	}

}
