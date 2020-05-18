package solitaire.game;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import solitaire.agent.Agent;
import solitaire.agent.Human;
import solitaire.agent.MCTSSolution;
import solitaire.agent.RandomAgent;



public class Executor {
	private long runTime = 1000;
	private long timeBuffer = 1000;
	
	public static void main(String[] args) {
		Executor exec = new Executor();
		exec.runGame(new Human());
//		exec.testGame();
	}
	
	private void testGame()
	{
		Game game = new Game();
		game.printDeck(game.deck);
		game.printBoardText(game.board);
		GameView gv = new GameView(game).showGame();
//		List<Position> testBoard = new ArrayList<Position>();
//		Position p00 = new Position(0,0,new GamePiece("", 1, null));
//		Position p10 = new Position(1,0,new GamePiece("", 0, null));
//		Position p20 = new Position(2,0,new GamePiece("", 0, null));
//		Position p01 = new Position(0,1,new GamePiece("", 0, null));
//		Position p11 = new Position(1,1,new GamePiece("", 1, null));
//		Position p21 = new Position(2,1,new GamePiece("", 0, null));
//		Position p02 = new Position(0,2,new GamePiece("", -1, null));
//		Position p12 = new Position(1,2,new GamePiece("", 1, null));
//		Position p22 = new Position(2,2,new GamePiece("", -1, null));
//		game.turn = -1;
//		testBoard.add(p00);
//		testBoard.add(p01);
//		testBoard.add(p02);
//		testBoard.add(p10);
//		testBoard.add(p11);
//		testBoard.add(p12);
//		testBoard.add(p20);
//		testBoard.add(p21);
//		testBoard.add(p22);
//		game.board = testBoard;
//		
//		new MCTSSolution().getMove(game, 1000);
	}
	
	private void runGame(Agent agent1)
	{
		Game game = new Game();
		game.printDeck(game.deck);
		game.printBoardText(game.board);
		/*for (Position p : game.getValidMoves(game.getBoard(), game.getTurn()))
		{
			System.out.println(p.getPiece().getOwner() + ": " + p.getX() + "," + p.getY());
		}*/
		GameView gv = new GameView(game).showGame();
		Agent h = null;
		if (agent1 instanceof Human)
			h = agent1;

		final Agent humanAgent = h;
		gv.addMouseListener(new MouseAdapter() {

	        public void mouseClicked(MouseEvent e) {
	        	int mouseX, mouseY;
	            mouseX=e.getX();
	            mouseY=e.getY();
	            //System.out.println(mouseX+","+mouseY);
	            gv.handleMouseClick(mouseX, mouseY, humanAgent);
	        }

		});
		
		long start = System.currentTimeMillis();
		/*
		while (!game.gameOver())
		{
			game.advanceGame(agent1.getMove(game, runTime));
			if (start + runTime + timeBuffer < System.currentTimeMillis() && !(agent1 instanceof Human))
			{
				System.out.println("Agent 1 took too long to respond");
			}
			start = System.currentTimeMillis();
			
			gv.repaint();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		gv.repaint();
	}


}
