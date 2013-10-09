package pacman;

public class Game {

	private Sprite[] sprites;
	
	
	public Game() {
		World world = new NormalWorld(100);
		Pacman p = new Pacman(world);
		Ghost g1 = new Ghost(world);
		Ghost g2 = new Ghost(world);
		sprites = new Sprite[] {p, g1, g2};
	}
	
	public void run() {
		for (final Sprite sprite : sprites) {
			sprite.step();
		}
	}
	
	public static void main(final String[] args) {
		new Game().run();
	}
	
}
