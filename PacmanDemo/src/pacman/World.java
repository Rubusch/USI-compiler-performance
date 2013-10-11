package pacman;

public abstract class World {

	private final int bound;
	
	
	public World(final int bound) {
		this.bound = bound;
	}
	
	public abstract boolean contains(int location);
	
	protected final int getBound() {
		return bound;
	}
	
}
