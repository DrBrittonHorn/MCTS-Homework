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
	
	@Override
	public boolean equals(Object o)
	{
		if (o == this) { 
            return true; 
        } 
  
        if (!(o instanceof Move)) { 
            return false; 
        } 
          
        Move m = (Move) o; 
          
        return m.fromPosition.equals(this.fromPosition) && m.toPosition.equals(this.toPosition);
	}
	
	@Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (fromPosition == null ? 0 : fromPosition.hashCode());
        result = 31 * result + (toPosition == null ? 0 : toPosition.hashCode());
        return result;
    }
}
