//package solitaire.agent;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import solitaire.agent.HiddenMCTSSolution.Node;
//import solitaire.game.Game;
//import solitaire.game.Move;
//
//public class HiddenMCTSRen extends Agent {
//
//	@Override
//	public Move getMove(Game game, long timeDue) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	private void performMCTS(long timeDue)
//	{
//		Node node = traverse();
//		int score = simulate(node);
//		backpropagate(node, score);
//	}
//	
//	private Node traverse() {
//		//select node to expand
//		Node ret = root;
//		while(ret.isExpanded) {
//			ret = ret.bestUCT();
//		}
//		
//		//expand node
//		if(ret.children == null) {
//			ret.children = new ArrayList<Node>();
//			for(Move m : ret.state.getValidMoves(ret.state.board, ret.state.turn)) {
//				Node child = new Node();
//				child.move = m;
//				child.state = ret.state.hiddenInfoSimulateMove(ret.state.board,m);
//				child.parent = ret;
//				//check not over
//				if(child.state.isWinningBoard(child.state.board) != 1 ||
//						child.state.maxPlays <= child.state.playsMade) {
//					child.isExpanded = true;
//				}
//				ret.children.add(child);
//			}
//		}
//		for (Node c : ret.children)
//		{
//			if (c.simulations == 0)
//				return c;
//		}
//		return null;
//	}
//	
//	private int simulate(Node n) {
//		List<Move> validMoves = n.state.getValidMoves(n.state.board, 1);
//	}
//	
//	private void backpropagate(Node n, int score)
//	{
//		while(n != null)
//		{
//			n.score += score;
//			n.simulations++;
//			n = n.parent;
//		}
//	}
//	
//	private class Node {
//		Node parent;
//		List<Node> children;
//		Game state;
//		Move move;
//		int simulations;
//		int score;
//		boolean isExpanded;
//	}
//}
//
