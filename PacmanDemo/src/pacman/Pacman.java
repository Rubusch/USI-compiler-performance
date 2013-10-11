package pacman;


public class Pacman implements Sprite {

	private final World world;
	private int position;
	
	
	public Pacman(final World world) {
		this.world = world;
	}
	
	public void step() {
		if (world.contains(position+1)) {
			position++;
		}
	}
	
}
