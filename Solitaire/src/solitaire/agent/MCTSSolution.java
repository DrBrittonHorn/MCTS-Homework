package solitaire.agent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import solitaire.game.Game;
import solitaire.game.Move;
import solitaire.game.Position;

public class MCTSSolution extends Agent {
	private Game game = null;
	private static final Random rand = new Random();
	Node root = null;
	private static int nextID = 0;

	@Override
	public Move getMove(Game game, long timeLimit) 
	{
		// initialize class variables and create root node (passed in game state)
		this.game = game;
		if (root == null)
		{
			root = new Node();
			root.boardState = game.clone();
			root.turn = game.getTurn();
		}
		root.depth = 0;
		
		System.out.println("starting MCTS -- original board:******");
		root.boardState.printGame();
		System.out.println("**************************************");
		List<Move> valid = game.getValidMoves(game.board, game.turn);
		for (Move m : valid)
			System.out.println(m.toString());
		//if (valid.size() == 1) return valid.get(0);
		
		// here's where the magic happens
		performMCTS(root, timeLimit);
		
		// choose the child with the best win percentage
		Node ret = bestChildWinPct(root);
		//printTree(root);
		if (ret == null) return null;
		
		for (Node n : root.children)
		{
			if (!n.equals(ret))
			{
				n.deleteChildren();
				n = null;
			}
		}
		root = ret;
		root.parent = null;
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
			for (Node child : rover.children)
			{
				if (child.simulations > 0)
					q.add(child);
			}
		}
		
	}
	
	private void performMCTS(Node root, long timeLimit)
	{
		
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
		while (System.currentTimeMillis() < timeDue && iterationCount++ < 10000)
		{
			/*for (Node child : root.children)
			{
				child.printNode();
			}*/
			//System.out.println("Starting loop");
			// select node to play from
			node = selection(root);
			//System.out.println("after selection");
			// simulate play and get game result
			double rolloutResult = rollout(node);
			//double rolloutResult = greedyRollout(node);
			//System.out.println("after rollout");
			// propagate 
			backpropagate(node, rolloutResult);
			//System.out.println("after backpropagate");
			
			// check if selected node is fully expanded now
			// if it is, checkNodeExpansion will set isExpanded for us
			if (node.parent != null && !node.parent.isExpanded)
			{
				//System.out.println("checking node expansion");
				checkNodeExpansion(node.parent);
			}
		}
		// more time logging
		System.out.println("iteration count: " + (iterationCount-1));
		long endTime = System.currentTimeMillis();
        Date endDate = new Date(endTime); 
		System.out.println("time end: " + format.format(endDate));
		// add stop here
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
		//System.out.println("selected node stats");
		//node.printNodeStats();
		// now that we're here, we know the node is not expanded or is terminal
		
		// check if we've seen this node before, if not we need to create children
		// if it's terminal, will be visited but not expanded (return the terminal node)
		boolean added = false;
		if (!node.isVisited)
		{
			if (!node.isTerminal)
			{
				added = true;
				// create children for each move
				addChildren(node);
			}
			// node is now visited
			node.isVisited = true;
		}
		/*if (added)
			System.out.println("added children");
		else
			System.out.println("no children added: " + node.isTerminal + ", " + node.isVisited);
		*/
		// if node is terminal, simply return the terminal node
		// this handles end-of-game states when all children have been created
		if (node.isTerminal)
		{
			//System.out.println("Node is terminal");
			return node;
		}
		//else
		//	System.out.println("Node not terminal");
		
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
				{
					//System.out.println("added children to child");
					addChildren(child);
				}
				// return the unvisited child
				//System.out.println("return child stats");
				//child.printNodeStats();
				return child;
			}
		}
		// something is wrong if we've hit this place
		System.out.println("returning null");
		return null;
	}
	
	private double greedyRollout(Node leaf)
	{
		// mark this node as visited in case we haven't done it before
		leaf.isVisited = true;
		// set the node's turn
		int turn = leaf.turn;
		// copy board so we don't mess any future nodes up
		Game rolloutBoard = leaf.boardState.clone();
		// get possible next moves
		List<Move> validMoves = rolloutBoard.getValidMoves(rolloutBoard.board, turn);
		
		// continue getting the next board until we reach a terminal state
		int i = 0;
		//System.out.println("%%%%%%%%%%%%%%%% Rolling out %%%%%%%%%%%%%%%%%%%");
		//rolloutBoard.printGame();
		while (rolloutBoard.isWinningBoard(rolloutBoard.board) == 0 && !validMoves.isEmpty() && rolloutBoard.playsMade <= rolloutBoard.maxPlays) // && i++ < 50)
		{
			//System.out.println("Rolling out -- plays: " + rolloutBoard.playsMade);
			//rolloutBoard.printGame();
			List<Move> bestMoves = new ArrayList<Move>(); // what about equal best moves? Need to pick at random I think.
			double bestScore = -1;
			double s = -1;
			for (Move m : validMoves)
			{
				try
				{
					s = getInterimScore(rolloutBoard.simulateMove(rolloutBoard.board, m));
					//s = rolloutBoard.getBoardScore(rolloutBoard.simulateMove(rolloutBoard.board, m).getBoard());
					//System.out.println("Move: " + m);
					//rolloutBoard.simulateMove(rolloutBoard.board, m).printGame();
					//System.out.println("Int Score: " + s);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					rolloutBoard.printGame();
					System.out.println(m);
					System.exit(1);
				}
				if (s == bestScore)
				{
					bestMoves.add(m);
				}
				if (s > bestScore)
				{
					bestScore = s;
					bestMoves.clear();
					bestMoves.add(m);
				}
			}
			int randInt = rand.nextInt(bestMoves.size());
			//System.out.println("Move: " + bestMoves.get(randInt));
			//rolloutBoard.simulateMove(rolloutBoard.board, bestMoves.get(randInt)).printGame();
			//System.out.println("Int Score: " + s);
			rolloutBoard = rolloutBoard.simulateMove(rolloutBoard.board, bestMoves.get(randInt));
			//rolloutBoard.printGame();
			validMoves = rolloutBoard.getValidMoves(rolloutBoard.board, turn);
		}
		//System.out.println("%%%%%%%%%%%%%%%% ROLL DONE %%%%%%%%%%%%%%%%%%%");
		//System.out.println(" $$$$$$$$$$$$$$$$$$$$$ " + rolloutBoard.getBoardScore(rolloutBoard.board) + " ||| " + leaf.moveToGetHere);
		/*System.out.println("i: " + i + ", plays: " + rolloutBoard.playsMade);
		rolloutBoard.printGame();
		System.out.println(" $$$$$$$$$$$$$$$$$$$$$$ END OF ROLLOUT $$$$$$$$$$$$$$$$$$$$$");*/
		return rolloutBoard.getBoardScore(rolloutBoard.board);
	}
	
	private double getInterimScore(Game g)
	{
		double score = 0.0;
		for (Position p : g.board) // get flipped cards
		{
			if (p.getPiece() != null && p.getPiece().isFlipped())
				score += .5;
		}
		int f0 = g.foundation0.size();
		int f1 = g.foundation1.size();
		int f2 = g.foundation2.size();
		int f3 = g.foundation3.size();
		
		return score + ((f0+f1+f2+f3)<<3);
	}
	
	private double rollout(Node leaf)
	{
		// simulating playouts to the end of the game with random moves
		
		// mark this node as visited in case we haven't done it before
		leaf.isVisited = true;
		// set the node's turn
		int turn = leaf.turn;
		// copy board so we don't mess any future nodes up
		Game rolloutBoard = leaf.boardState;//.clone();
		// get possible next moves
		List<Move> validMoves = rolloutBoard.getValidMoves(rolloutBoard.board, turn);
		
		// continue getting the next board until we reach a terminal state
		int i = 0;
		//System.out.println("Rolling out");
		while (rolloutBoard.isWinningBoard(rolloutBoard.board) == 0 && !validMoves.isEmpty() && rolloutBoard.playsMade <= rolloutBoard.maxPlays)// && i < 50)
		{
			i++;
			//if ((i % 100) == 0)
			//	System.out.println("another rollout level: " + i);
			int randInt = rand.nextInt(validMoves.size());
			// simulate move with random validMove choice
			try {
				rolloutBoard = rolloutBoard.simulateMove(rolloutBoard.board, validMoves.get(randInt));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				rolloutBoard.printGame();
				System.out.println(validMoves.get(randInt).toString());
				System.exit(1);
			}
			// switch player turns
			//turn *= -1;
			// set validMoves for next loop iteration
			validMoves = rolloutBoard.getValidMoves(rolloutBoard.board, turn);
			//if (i == 500) System.out.println("Reached max playout length!");
		}

		// return the game result
		//System.out.println(" $$$$$$$$$$$$$$$$$$$$$ " + rolloutBoard.getBoardScore(rolloutBoard.board));
		/*System.out.println("i: " + i + ", plays: " + rolloutBoard.playsMade);
		rolloutBoard.printGame();
		System.out.println(" $$$$$$$$$$$$$$$$$$$$$$ END OF ROLLOUT $$$$$$$$$$$$$$$$$$$$$");*/
		return rolloutBoard.getBoardScore(rolloutBoard.board);
	}
	
	private void backpropagate(Node node, double result)
	{
		// need to negate this for back propagation -- not for solitaire
		// turn is for the NEXT move, not who's move it was INTO this game state
		//result *= -1;
		Node tmp = node;
		// update all parent nodes
		while (tmp != null)
		{
			// regardless of winner, add simulation
			tmp.simulations++;
			// if turn for this node wins, add a win
			//if (result == tmp.turn)
			tmp.score += result;
			// if no one wins, add half a win
			// this differentiates between draws and losses
			// if (result == 0)
			//	tmp.wins += .5;
			tmp = tmp.parent;
		}
	}
	
	private void addChildren(Node node)
	{
		List<Move> validMoves = node.boardState.getValidMoves(node.boardState.board, node.turn);
		//System.out.println("Adding children");
		// create children for each valid move
		for (Move p : validMoves)
		{
			Node newChild = new Node();
			newChild.moveToGetHere = p;
			//System.out.println("MOVE TO GET HERE: " + newChild.moveToGetHere.getFromPosition() + " -> " + newChild.moveToGetHere.getToPosition());
			// negate the turn from the parent
			//newChild.turn = node.turn * -1;
			newChild.turn = node.turn;
			newChild.parent = node;
			newChild.depth = node.depth + 1;
			// get child's board state by simulating the valid move from current board state
			newChild.boardState = node.boardState.simulateMove(node.boardState.board, newChild.moveToGetHere);
			
			// check if terminal node
			if (newChild.boardState.isWinningBoard(newChild.boardState.board) != 0 || 
					newChild.boardState.getValidMoves(newChild.boardState.board, newChild.turn).isEmpty() ||
					newChild.boardState.maxPlays <= newChild.boardState.playsMade)
			{
				newChild.isTerminal = true;
				newChild.isVisited = false;
				newChild.isExpanded = false;
				newChild.score = newChild.boardState.getBoardScore(newChild.boardState.board);
			}
			
			// add child to parent's list
			node.children.add(newChild);
		}
	}
	
	private Node bestChildWinPct(Node parent)
	{
		// go through all children and print win percentages, return the one with the highest
		// draws == .5 wins lets us choose drawing moves more than losing moves we can't win
		//System.out.println(":::::::::::::::Win Percentages:::::::::::");
		double bestAvgScore = -1;
		Node bestChild = null;
		for (Node child : parent.children)
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
		public double score;
		public Game boardState = null;
		public int turn;
		public boolean isTerminal;
		public int winner;
		public Move moveToGetHere;
		public int depth;
		public int id;
		
		Node()
		{
			this.isExpanded = false;
			this.isVisited = false;
			this.isTerminal = false;
			this.parent = null;
			this.simulations = 0;
			this.score = 0;
			this.children = new ArrayList<Node>();
			this.id = nextID++;
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
			return (this.score / (double) this.simulations) + 
					(2 * 100 * Math.sqrt( // added C_p parameter since score is outside [0,1]
							Math.log(this.parent.simulations) / (double) this.simulations)
							);
		}
		
		/*
		 * print node for debug purposes
		 */
		public void printNode()
		{
			System.out.println("===== Node Begin =====");
			boardState.printGame();
			System.out.println("score: " + this.score + ", simulations: " + this.simulations + ", uct: " + this.uct() + ", turn: " + this.turn + 
					", isTerminal: " + this.isTerminal + ", winner: " + this.winner + 
					", moveToGetHere: " + ((this.moveToGetHere != null) ? moveToGetHere.toString() 
					: "null"));
			System.out.println("===== Node End =====");
		}
		
		public void printNodeStats()
		{
			System.out.println("score: " + this.score + ", simulations: " + this.simulations + ", parent sim: " + this.parent.simulations + ", uct: " + this.uct() + ", avg: " + (score/(double)simulations) + 
					", isTerminal: " + this.isTerminal + ", winner: " + this.winner + 
					", moveToGetHere: " + ((this.moveToGetHere != null) ? moveToGetHere.toString() 
					: "null"));
		}
		
		public void deleteChildren()
		{
			for (Node child : children)
			{
				child.deleteChildren();
			}
			return;
		}
	}

}
