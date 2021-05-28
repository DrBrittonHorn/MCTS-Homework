package solitaire.agent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import solitaire.game.Game;
import solitaire.game.Move;

public class HiddenMCTSSolution extends Agent {
	private Game game = null;
	private static final Random rand = new Random();
	Node root = null;
	private static int nextID = 0;
	private Game prevState;
	
	@Override
	public Move getMove(Game game, long timeDue) {
		System.out.println("*********** Starting MCTS *************");
		this.game = game;
		if (root == null)
		{
			nextID = 0;
			root = new Node();
			root.state = game;
		}
		root.depth = 0;
		for (Move m : root.state.getValidMoves(root.state.board, 1))
		{
			System.out.println(m.toString());
			//root.state.hiddenInfoSimulateMove(root.state.board, m);
		}
		performMCTS(timeDue);
		
		Node ret = bestRootMove();
//		printTree(root);
		if (ret == null)
		{
			System.out.println("Returning null from MCTS");
			return null;
		}
		System.out.println("*********** Finished MCTS *************");
		root = null;
		System.out.println("Returning move: " + ret.moveToGetHere);
		prevState = root.state;
		return ret.moveToGetHere;
	}
	
	private void printTree(Node root)
	{
		LinkedList<Node> q = new LinkedList<Node>();
		Node rover = null;
		int depth = root.depth;
		q.add(root);
		while (!q.isEmpty())
		{
			rover = q.remove();
			//rover.printNode();
			if (rover.depth > depth)
			{
				depth = rover.depth;
				System.out.println("*********************** LEVEL " + depth + " *********************");
			}
			System.out.println("id: " + rover.id + ", parent: " + 
					(rover.parent == null ? " " : rover.parent.id) + ", move to get here: " + rover.moveToGetHere + "isTerminal: " + rover.isTerminal);
			//rover.printNode(););
			if (rover.children != null)
			{
				for (Node child : rover.children)
				{
					if (child.simulations > 0)
						q.add(child);
				}
			}
		}
		
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
		
		int iters = 0, maxIter = 300000;
		while (System.currentTimeMillis() < timeDue && iters++ < maxIter)
		{
			//System.out.println("New MCTS iteration: " + iters);
			//printTree(root);
			Node n = selection();
			//System.out.println("Selected Node:");
			//n.printNode();
			n = expansion(n);
//			System.out.println("After Expansion node:");
//			n.printNode();
			double score = simulate(n);
			backpropagate(n, score);
			
//			if(n.parent != null)
//			{
//				System.out.println("parent: " + n.parent.id + " sim: " + n.parent.simulations + ", child#: " + n.parent.children.size());
//			}
			if (n.parent instanceof ChanceNode)
			{
				if ((n.parent.parent.id == 0 && n.parent.parent.simulations == n.parent.parent.children.size()) ||
						(n.parent.parent.id > 0 && n.parent.parent.simulations > n.parent.parent.children.size())
						)
				{
	//				System.out.println("parent fully expanded!");
					n.parent.parent.isFullyExpanded = true;
				}
			}
			else
			{
				if ((n.parent.id == 0 && n.parent.simulations == n.parent.children.size()) ||
						(n.parent.id > 0 && n.parent.simulations > n.parent.children.size())
						)
				{
	//				System.out.println("parent fully expanded!");
					n.parent.isFullyExpanded = true;
				}
			}
		}
	}
	
	private Node selection()
	{
		Node chosen = root;
		while (chosen.isFullyExpanded && !chosen.isTerminal)
		{
			if (chosen instanceof ChanceNode)
			{
				chosen = ((ChanceNode) chosen).createChanceChild();
			}
			else
			{
				chosen = chosen.bestUCT();
			}
		}
		return chosen;
	}
	
	private Node expansion(Node n)
	{
		if (n.isTerminal) return n;
//		System.out.println("Expanding node: **********************************");
//		n.state.printGame();
		if (n.children == null)
		{
			n.children = new ArrayList<Node>();
			for (Move mv : n.state.getValidMoves(n.state.board, 1))
			{
				// add exclusion cases:
				// Resetting deck, already been through deck once
				if((mv.getToPosition().isDeck()  && n.state.deckFlips == 0 && n.state.deck.size() != 0) 
						|| (mv.getToPosition().getX() >= 0 && mv.getFromPosition() == null)){
					ChanceNode child = new ChanceNode();
					child.parent = n;
					child.depth = n.depth + 1;
					child.moveToGetHere = mv;
					child.state = n.state;
					child.isFullyExpanded = true;
					child.isTerminal = false;
					//child.createChanceChild();
					
					n.children.add(child);
				}
				else 
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
						child.isTerminal = true;
					}
					
					// add child to parent's list
					n.children.add(child);
				}
			}
		}
		for (Node c : n.children)
		{
			if (c.simulations == 0)
			{
				if (c instanceof ChanceNode)
				{
					c = ((ChanceNode) c).createChanceChild();
				}
				return c;
			}
		}
		
		System.out.println("This should be a fully expanded node: isChance -- " + (n instanceof ChanceNode));
		n.printNode();
		return null;
	}
	
	private double simulate(Node n)
	{
		List<Move> validMoves = n.state.getValidMoves(n.state.board, 1);
		Game newG = n.state;
		Game parentG = null;
		Game gParentG = null;
		Move lastMove = null;
		//System.out.println("START SIMULATION");
		//newG.printGame();
		while (newG.isWinningBoard(newG.board) == 0 && !validMoves.isEmpty() && !isLooping(newG, parentG, gParentG))
		{
			if (parentG != null) gParentG = parentG;
			parentG = newG;
			int randInt = rand.nextInt(validMoves.size());
			try {
				newG = newG.hiddenInfoSimulateMove(newG.board, validMoves.get(randInt));
				lastMove = validMoves.get(randInt);
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
//		System.out.println("END SIMULATION");
//		System.out.println("isWinning: " + newG.isWinningBoard(newG.board)
//				+ ", validMOves: " + !validMoves.isEmpty() 
//				+ ", isLooping: " + isLooping(newG, parentG, gParentG));
//		System.out.println("last move: " + lastMove);
//		newG.printGame();
//		gParentG.printGame();
		return newG.getBoardScore(newG.board);
		//return newG.isWinningBoard(newG.board);
	}
	
	private void backpropagate(Node n, double score)
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
			if (childAvgScore > bestAvgScore && !child.state.equals(prevState))
			{
				bestAvgScore = childAvgScore;
				bestChild = child;
			}
		}
		
		// return the move with highest win percentage
		return bestChild;
	}
	
	private boolean isLooping(Game newG, Game parentG, Game gParentG) {
		if(parentG != null && gParentG != null &&
				gParentG.board.equals(newG.board) &&
				gParentG.waste.equals(newG.waste)) {
			//System.out.println(node.state.board);
			//System.out.println(node.parent.parent.state.board);
			//node.parent.parent.state.printBoardText(node.parent.parent.state.board);
			//System.out.println("Current board");
			//node.state.printBoardText(node.state.board);
			//System.out.println("IS LOOPING NODE");
			return true;
		}
		else return false;
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
		boolean isTerminal;
		int id = nextID++;
		
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
					(2 * 100 * Math.sqrt( // added C_p parameter since score is outside [0,1]
							Math.log(this.parent.simulations) / (double) this.simulations)
							);
		}
		
		public void printNode()
		{
			System.out.println("===== Node Begin =====");
			state.printGame();
			System.out.println("id: " + this.id + ", score: " + this.score + ", simulations: " + this.simulations + ", uct: " + this.uct() +
					", isFullyExpanded: " + this.isFullyExpanded + ", isTerminal: " + this.isTerminal + 
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
		
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (! (o instanceof Node)) return false;
			if (o instanceof ChanceNode && !(this instanceof ChanceNode)) return false;
			if (this instanceof ChanceNode && !(o instanceof ChanceNode)) return false;
			
			Node n = (Node) o;
			return n.state.equals(this.state);
		}
	
	}

	private class ChanceNode extends Node {
		final boolean chance = true;
		//List<Node> chanceChildren;
		
		//this is probably not right even a little bit
		
		public Node createChanceChild() {
			Game g = this.state.hiddenInfoSimulateMove(this.state.board, this.moveToGetHere);
			Node child = new Node();
			child.depth = this.depth + 1;
			child.state = g;
			child.moveToGetHere = this.moveToGetHere;
			child.parent = this;
			child.simulations = 0;
			child.score = 0;
			
			if (child.state.isWinningBoard(child.state.board) != 0 || 
					child.state.getValidMoves(child.state.board, 1).isEmpty() ||
					child.state.maxPlays <= child.state.playsMade)
			{
				child.isFullyExpanded = true;
				child.isTerminal = true;
			}
			
			if (children == null)
				children = new ArrayList<Node>();
			if (!children.contains(child))
			{
				children.add(child);
			}
			return child;
		}
	}
}
