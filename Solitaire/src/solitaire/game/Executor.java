package solitaire.game;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import solitaire.agent.Agent;
import solitaire.agent.HiddenMCTSSolution;
import solitaire.agent.Human;
import solitaire.agent.MCTSSolution;
import solitaire.agent.RandomAgent;


public class Executor implements Runnable{
	private long runTime = 1000;
	private long timeBuffer = 1000;
	private Class agentType;
	private static final int NTHREDS = Runtime.getRuntime().availableProcessors();
//	private static final int NTHREDS = 2;
	private static final int times = NTHREDS;
	private int finalscore;
	private static int nextID = 0;
	private int id;
	
	public static void main(String[] args) throws InterruptedException {
		Executor exec = new Executor();
//		exec.runGame(new Human());
//		exec.runGame(new RandomAgent());
//		exec.runGame(new MCTSSolution());
//		exec.runHeadlessGame(new HiddenMCTSSolution(), 5);
//		exec.runGame(new HiddenMCTSSolution());
//		exec.testGame();
		runThreaded();
	}
	
	private static void runThreaded() throws InterruptedException
	{
		List<Runnable> allTasks = new ArrayList<Runnable>();
		
		int totalScore = 0;
		ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
        for (int i = 0; i < times; i++) {
//            Runnable worker = new Executor(MCTSSolution.class);
            Runnable worker = new Executor(HiddenMCTSSolution.class, i);
            executor.execute(worker);
            allTasks.add(worker);
        }
        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        executor.awaitTermination(10000000L, TimeUnit.MILLISECONDS);
        System.out.println("Finished all threads");
        for (Runnable r : allTasks)
        {
        	System.out.println("id: " + ((Executor)r).id);
        	System.out.println("score: " + ((Executor)r).getScore());
        	totalScore += ((Executor)r).getScore();
        }
        System.out.println("Avg: " + (totalScore/(double)times));
	}
	
	Executor(Class agentType, int id) {
		this.agentType = agentType;
		this.id = id;
	}
	
	public Executor() {	}

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
	
	private boolean isLooping(Game nextState, Game prevState)
	{
		if(prevState != null &&
				prevState.board.equals(nextState.board) &&
				prevState.waste.equals(nextState.waste)) {
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
	
	private void createGameTree(Game origGame)
	{
		class Node{
			int id;
			int depth;
			ArrayList<Move> movesMade;
			Node parent;
			ArrayList<Node> children = new ArrayList<Node>();
			boolean isTerminal;
			double score;
			Node(){
				this.id = nextID++;
			}
		}
		System.out.println("Creating game tree.");
		int maxDepth = 15;
		Queue<Node> nodes = new LinkedList<Node>();
		Node treeRoot = new Node();
		treeRoot.depth = 0;
		treeRoot.movesMade = new ArrayList<Move>();
		nodes.add(treeRoot);
		boolean isRoot = true;
		int depthCreated = 0;
		while(!nodes.isEmpty())
		{
			Node current = nodes.poll();
			if (current.depth > depthCreated) 
			{
				depthCreated = current.depth;
				System.out.println("Reached depth: " + depthCreated + " on node " + current.id);
			}
			if (current.isTerminal) continue;
			Game state = origGame;
			Game prevState = null;
			if (current.parent != null) isRoot = false;
			// build current board
			for (Move m : current.movesMade)
			{
				prevState = state;
				state = state.simulateMove(origGame.board, m);
			}
			for (Move m : state.getValidMoves(state.board, state.turn))
			{
				Game newState = state.simulateMove(state.board, m);
				Node newChild = new Node();
				newChild.movesMade = new ArrayList<Move>(current.movesMade);
				newChild.movesMade.add(m);
				newChild.parent = current;
				newChild.depth = current.depth + 1;
				
				// check if terminal node
				if (state.isWinningBoard(state.board) != 0 || 
						state.getValidMoves(state.board, state.turn).isEmpty() ||
						state.maxPlays <= state.playsMade)
				{
					newChild.isTerminal = true;
					newChild.score = state.getBoardScore(state.board);
				}
				
				if (newChild.depth <= maxDepth && 
						(isRoot || !isLooping(newState,prevState)))
				{
					current.children.add(newChild);
					nodes.add(newChild);
				}
			}
			
		}

		LinkedList<Node> q = new LinkedList<Node>();
		Node rover = null;
		int depth = treeRoot.depth;
		q.add(treeRoot);
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
					(rover.parent == null ? " " : rover.parent.id) + 
					", move to get here: " + (rover.id == 0 ? "[]" : rover.movesMade.get(rover.movesMade.size()-1)) + 
					", isTerminal: " + rover.isTerminal);
			//rover.printNode(););
			for (Node child : rover.children)
			{
				q.add(child);
			}
		}
			
		System.out.println("Completed game tree creation.");
	}
	
	private void runGame(Agent agent1)
	{
		boolean testing = false;
		Game game = new Game();
		game.printDeck(game.deck);
		game.printBoardText(game.board);
		if (testing)
		{
			createGameTree(game);
			return;
		}
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
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		long start = System.currentTimeMillis();
		
		while (!game.gameOver())
		{
			if (agent1.responded)
			{
				//System.out.println("Human responded");
				if (agent1.getClass().equals(MCTSSolution.class))
				{
					Move m = agent1.getMove(game, runTime);
					if (m == null)
					{
						System.out.println("Agent returned null. Ending game.");
						break;
					}
					game.advanceGame(m);
				}
				else if (agent1.getClass().equals(HiddenMCTSSolution.class))
				{
					Move m = agent1.getMove(game.createHiddenInfoVersion(), runTime);
					if (m == null)
					{
						System.out.println("Agent returned null. Ending game.");
						break;
					}
					game.advanceGame(m);
				}

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
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("GAME FINISHED RUNNING");
		gv.repaint();
	}
	
	private void runHeadlessGame(Agent agent1, int times)
	{
		for (int t = 0; t != times; t++)
		{
			if (agent1.getClass().equals(MCTSSolution.class))
			{
				System.out.println("Equal!");
				agent1 = new MCTSSolution();
			}
			if (agent1.getClass().equals(HiddenMCTSSolution.class))
			{
				System.out.println("HiddenInfo Equal!");
				agent1 = new HiddenMCTSSolution();
			}
			else
			{
				System.out.println("Class mismatch. Ending.");
				return;
			}
			Game game = new Game();
			game.printDeck(game.deck);
			game.printBoardText(game.board);
			long start = System.currentTimeMillis();
			while (!game.gameOver())
			{
				if (agent1.responded)
				{
					//System.out.println("Human responded");
					if (agent1.getClass().equals(MCTSSolution.class))
					{
						Move m = agent1.getMove(game, runTime);
						if (m == null)
						{
							System.out.println("Agent returned null. Ending game.");
							break;
						}
						game.advanceGame(m);
					}
					else if (agent1.getClass().equals(HiddenMCTSSolution.class))
					{
						Move m = agent1.getMove(game.createHiddenInfoVersion(), runTime);
						if (m == null)
						{
							System.out.println("Agent returned null. Ending game.");
							break;
						}
						game.advanceGame(m);
					}
					
					if (start + runTime + timeBuffer < System.currentTimeMillis() && !(agent1 instanceof Human))
					{
						System.out.println("Agent 1 took too long to respond");
					}
					start = System.currentTimeMillis();
				}
			}
			System.out.println("GAME FINISHED RUNNING");
			game.printGame();
		}
	}

	@Override
	public void run()
	{
		Agent agent1;
		Game game = new Game();

		if (agentType.equals(MCTSSolution.class))
		{
			System.out.println("MCTSSolution!");
			agent1 = new MCTSSolution(this.id);
		}
		else if (agentType.equals(HiddenMCTSSolution.class))
		{
			System.out.println("HiddenMCTS!");
			agent1 = new HiddenMCTSSolution(this.id);
		}
		else
		{
			System.out.println("Class mismatch. Ending.");
			return;
		}
		
		game.printDeck(game.deck);
		game.printBoardText(game.board);
		long start = System.currentTimeMillis();
		while (!game.gameOver())
		{
			if (agent1.responded)
			{
				//System.out.println("Human responded");
				if (agentType.equals(MCTSSolution.class))
				{
					Move m = agent1.getMove(game, runTime);
					if (m == null)
					{
						System.out.println("Agent returned null. Ending game.");
						break;
					}
					game.advanceGame(m);
				}
				else if (agentType.equals(HiddenMCTSSolution.class))
				{
					Move m = agent1.getMove(game.createHiddenInfoVersion(), runTime);
					if (m == null)
					{
						System.out.println("Agent returned null. Ending game.");
						break;
					}
					game.advanceGame(m);
				}
				else
				{
					Move m = agent1.getMove(game, runTime);
					if (m == null)
					{
						System.out.println("Agent returned null. Ending game.");
						break;
					}
					game.advanceGame(m);
				}
				if (start + runTime + timeBuffer < System.currentTimeMillis() && !(agent1 instanceof Human))
				{
					System.out.println("Agent 1 took too long to respond");
				}
				//System.out.println("After play game: ");
				//game.printGame();
				start = System.currentTimeMillis();
			}
		}
		System.out.println("GAME FINISHED RUNNING -- " + this.id);
		game.printGame();
		this.finalscore = game.getNumberOfCardsInFoundation();
	}
	
	public int getScore()
	{
		return finalscore;
	}

}
