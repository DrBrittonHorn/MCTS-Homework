package solitaire.agent;

import java.util.ArrayList;
import java.util.List;

import solitaire.game.Game;
import solitaire.game.Move;
import solitaire.game.Position;

public class FullSolitaireTree {
	int endstateNum = 0;
	
	public void buildFullTree(Game game) {
		System.out.println("Starting to build");
		Node root = new Node();
		root.state = game;
		root.endstate = 0;
		build(root);
		System.out.println("Finished building");
		printTree(root);
	}
	
	public void build(Node node) {
		addChildren(node);
		for(Node child: node.children) {
			if(child.endstate == 0 /*&& !isLoop(child)*/) 
			{
				build(child);
			}
		}
	}
	
	public void addChildren(Node node) {
		boolean kingToTab = false;
		List<Move> moves = node.state.getValidMoves(node.state.board, 1);
		node.children = new ArrayList<Node>();
		for(Move m: moves) {
			Position moveFrom = m.getFromPosition();
//			if(moveFrom!= null && !(moveFrom.isFoundation() && moveFrom.getPiece().getCard().rank==1)
//					&& !(moveFrom.getPiece().getCard().rank == 13 && moveFrom.getY() == 0
//						&& m.getToPosition().getY() == 0)) {
				Node child = new Node();
				child.state = node.state.simulateMove(node.state.board, m);
				child.parent = node;
				child.move = m;
				child.endstate = child.state.isWinningBoard(child.state.board);
				node.children.add(child);
//			} else System.out.println("Move king to king" + m.toString());
		}
	}
	
	@SuppressWarnings("unused")
	private void deleteLosingBranches(Node child) {
		if(child.children.size() == 1) {
			Node parent = child.parent;
			parent.children.remove(0);
			parent.children = null;
			deleteLosingBranches(parent);
		}
	}
	
	private boolean isLoop(Node node) {
		Game origState = node.state;
		for(int i = 0; i < 3; i++) {
			node = node.parent;
			if(node == null) return false;
			if(node.state.board.equals(origState.board)) return true;
		}
		return false;
	}
	
	private void printTree(Node node) {
		if(node.move!=null) {
			System.out.println("Move and result: "+node.endstate+" "+node.move.toString());
		} else 
			System.out.println("Root: "+node.endstate);
		if(node.endstate == 0 && node.children != null) {
			for(Node child: node.children) {
				printTree(child);
			}
		}
	}
	
	private class Node {
		Node parent;
		List<Node> children;
		Game state;
		Move move;
		int endstate;
	}
}
