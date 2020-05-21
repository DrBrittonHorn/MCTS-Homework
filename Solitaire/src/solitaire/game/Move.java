package solitaire.game;

public class Move
{
	private Position fromPosition;
	private Position toPosition;
	
	private Move() {}
	
	public Move(Position from, Position to)
	{
		fromPosition = from;
		toPosition = to;
	}
	
	public Move shallowCopy()
	{
		return new Move(this.fromPosition, this.toPosition);
	}
	
	public Position getFromPosition()
	{
		return fromPosition;
	}
	
	public Position getToPosition()
	{
		return toPosition;
	}
}
