package pacman;

public class MagicWorld extends World {

	public MagicWorld(final int bound) {
		super(bound);
	}
	
	public boolean contains(int position) {
		return Math.random()>0.5;
	}
	
}
