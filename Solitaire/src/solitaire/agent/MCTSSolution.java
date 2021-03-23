package solitaire.agent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import solitaire.game.Game;
import solitaire.game.Move;
import solitaire.game.Position;

public class MCTSSolution extends Agent {
	private Game game = null;
	Random rand = new Random();

	@Override
	public Move getMove(Game game, long timeLimit) 
	{
		// initialize class variables and create root node (passed in game state)
		this.game = game;
		Node root = new Node();
		root.boardState = this.game.clone();
		root.turn = this.game.getTurn();
		
		// here's where the magic happens
		performMCTS(root, timeLimit);
		
		// choose the child with the best win percentage
		return bestChildWinPct(root);
	}
	
	private void performMCTS(Node root, long timeLimit)
	{
		System.out.println("starting MCTS");
		
		// some time formatting so see how long the algorithm takes
		DateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS"); 
		long startTime = System.currentTimeMillis();
        Date startDate = new Date(startTime); 
		System.out.println("start time: " + format.format(startDate));
		long timeDue = System.currentTimeMillis() + timeLimit;
        Date timeDueDate = new Date(timeDue); 
		System.out.println("time due: " + format.format(timeDueDate));
		
		// start from root
		Node node = root;
		int iterationCount = 0;
		// stop in timeDue milliseconds or after 10 million iterations
		while (System.currentTimeMillis() < timeDue && iterationCount++ < 10000000)
		{
			// select node to play from
			node = selection(root);
			// simulate play and get game result
			int rolloutResult = rollout(node);
			// propagate 
			backpropagate(node, rolloutResult);
			
			// check if selected node is fully expanded now
			// if it is, checkNodeExpansion will set isExpanded for us
			if (node.parent != null && !node.parent.isExpanded)
			{
				checkNodeExpansion(node.parent);
			}
		}
		// more time logging
		System.out.println("iteration count: " + iterationCount);
		long endTime = System.currentTimeMillis();
        Date endDate = new Date(endTime); 
		System.out.println("time end: " + format.format(endDate));
	}
	
	private Node selection(Node node)
	{
		// continue down the tree until we find a node that is not fully expanded
		// choose the highest uct child at each stage until we get there
		while(node.isExpanded && !node.isTerminal)
		{
			// get the highest uct value child from this expanded node
			node = node.bestUCT();
		}
		// now that we're here, we know the node is not expanded or is terminal
		
		// check if we've seen this node before, if not we need to create children
		// if it's terminal, will be visited but not expanded (return the terminal node)
		if (!node.isVisited)
		{
			if (!node.isTerminal)
			{
				// create children for each move
				addChildren(node);
			}
			// node is now visited
			node.isVisited = true;
		}
		
		// if node is terminal, simply return the terminal node
		// this handles end-of-game states when all children have been created
		if (node.isTerminal)
			return node;
		
		// pick unvisited child
		for (Node child : node.children)
		{
			if (child.isVisited)
			{
				continue;
			}
			else
			{
				// found an unvisited child
				// check if it's not terminal and add it's children
				// need to do this now so the algorithm doesn't get hung up on the next tree level
				if (!child.isTerminal)
					addChildren(child);
				// return the unvisited child
				return child;
			}
		}
		// something is wrong if we've hit this place
		System.out.println("returning null");
		return null;
	}
	
	private int rollout(Node leaf)
	{
		// simulating playouts to the end of the game with random moves
		
		// mark this node as visited in case we haven't done it before
		leaf.isVisited = true;
		// set the node's turn
		int turn = leaf.turn;
		// copy board so we don't mess any future nodes up
		Game rolloutBoard = leaf.boardState.clone();
		// get possible next moves
		List<Move> validMoves = rolloutBoard.getValidMoves(rolloutBoard.board, turn);
		
		// continue getting the next board until we reach a terminal state
		while (rolloutBoard.isWinningBoard(rolloutBoard.board) == 0 && !validMoves.isEmpty())
		{
			int randInt = rand.nextInt(validMoves.size());
			// simulate move with random validMove choice
			rolloutBoard = rolloutBoard.simulateMove(rolloutBoard.board, validMoves.get(randInt));
			// switch player turns
			//turn *= -1;
			// set validMoves for next loop iteration
			validMoves = rolloutBoard.getValidMoves(rolloutBoard.board, turn);
		}

		// return the game result
		return rolloutBoard.isWinningBoard(rolloutBoard.board);
	}
	
	private void backpropagate(Node node, int result)
	{
		// need to negate this for back propagation
		// turn is for the NEXT move, not who's move it was INTO this game state
		result *= -1;
		Node tmp = node;
		// update all parent nodes
		while (tmp != null)
		{
			// regardless of winner, add simulation
			tmp.simulations++;
			// if turn for this node wins, add a win
			if (result == tmp.turn)
				tmp.wins++;
			// if no one wins, add half a win
			// this differentiates between draws and losses
			if (result == 0)
				tmp.wins += .5;
			tmp = tmp.parent;
		}
	}
	
	private void addChildren(Node node)
	{
		List<Move> validMoves = node.boardState.getValidMoves(node.boardState.board, node.turn);
		// create children for each valid move
		for (Move p : validMoves)
		{
			Node newChild = new Node();
			newChild.moveToGetHere = p;
			System.out.println("MOVE TO GET HERE: " + newChild.moveToGetHere.getFromPosition() + " -> " + newChild.moveToGetHere.getToPosition());
			// negate the turn from the parent
			//newChild.turn = node.turn * -1;
			newChild.turn = node.turn;
			newChild.parent = node;
			newChild.depth = node.depth + 1;
			// get child's board state by simulating the valid move from current board state
			newChild.boardState = node.boardState.simulateMove(node.boardState.board, newChild.moveToGetHere);
			
			// check if terminal node
			if (newChild.boardState.isWinningBoard(newChild.boardState.board) != 0 || newChild.boardState.getValidMoves(newChild.boardState.board, newChild.turn).isEmpty() || newChild.depth >= 100)
			{
				newChild.isTerminal = true;
				newChild.isVisited = false;
				newChild.isExpanded = false;
			}
			
			// add child to parent's list
			node.children.add(newChild);
		}
	}
	
	private Move bestChildWinPct(Node parent)
	{
		// go through all children and print win percentages, return the one with the highest
		// draws == .5 wins lets us choose drawing moves more than losing moves we can't win
		System.out.println(":::::::::::::::Win Percentages:::::::::::");
		double bestWinPct = -1;
		Node bestChild = null;
		for (Node child : parent.children)
		{
			child.printNode();
			double childWinPct = (child.simulations == 0) ? 0.0 : (childWinPct = child.wins / (double) child.simulations);
			if (childWinPct > bestWinPct)
			{
				bestWinPct = childWinPct;
				bestChild = child;
			}
		}
		
		// return the move with highest win percentage
		return bestChild.moveToGetHere;
	}
	
	private void checkNodeExpansion(Node node)
	{
		// nodes without children will never be expanded
		if (node.children.isEmpty())
		{
			return;
		}
		// if any child is not visited, the node can't be fully expanded
		for (Node child : node.children)
		{
			if (!child.isVisited)
			{
				return;
			}
				
		}
		// reached the end so all children are visited, node is fully expanded
		node.isExpanded = true;
		return;
	}
	
	
	/*
	 * Node class
	 * Tracks visited, expanded, terminal, board state, parent/children, winner/wins/simulations, move to get here
	 */
	private class Node
	{
		private boolean isExpanded;
		private boolean isVisited;
		public Node parent;
		public List<Node> children;
		public int simulations;
		public double wins;
		public Game boardState = null;
		public int turn;
		public boolean isTerminal;
		public int winner;
		public Move moveToGetHere;
		public int depth;
		
		Node()
		{
			this.isExpanded = false;
			this.isVisited = false;
			this.isTerminal = false;
			this.parent = null;
			this.simulations = 0;
			this.wins = 0;
			this.children = new ArrayList<Node>();
		}
		
		/*
		 * return the child with the highest UCT value
		 */
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
		
		/*
		 * calculate the uct value
		 */
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
			return (this.wins / (double) this.simulations) + 
					(1.41 * Math.sqrt(
							Math.log(this.parent.simulations) / (double) this.simulations)
							);
		}
		
		/*
		 * print node for debug purposes
		 */
		public void printNode()
		{
			System.out.println("===== Node Begin =====");
			boardState.printBoardText(boardState.board);
			System.out.println("wins: " + this.wins + ", simulations: " + this.simulations + ", uct: " + this.uct() + ", turn: " + this.turn + 
					", isTerminal: " + this.isTerminal + ", winner: " + this.winner + 
					", moveToGetHere: " + ((this.moveToGetHere != null) ? this.moveToGetHere.getToPosition().getPiece().getOwner() + "@" + this.moveToGetHere.getToPosition().getX() + "," + this.moveToGetHere.getToPosition().getY() 
					: "null"));
			System.out.println("===== Node End =====");
		}
	}

}
