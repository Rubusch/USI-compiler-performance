package pacman;

public class NormalWorld extends World {

	public NormalWorld(final int bound) {
		super(bound);
	}
	
	public boolean contains(int position) {
		return position>getBound();
	}
	
}
