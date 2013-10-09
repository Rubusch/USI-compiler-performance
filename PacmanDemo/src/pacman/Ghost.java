package pacman;


public class Ghost implements Sprite {

	private final World world;
	private int position;
	
	
	public Ghost(final World world) {
		this.world = world;
	}
	
	public void step() {
		if (world.contains(position+1)) {
			position++;
		}
	}
	
}
