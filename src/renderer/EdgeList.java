package renderer;

public class EdgeList {
	private float[][] list;
	private int startY;
	private int endY;

	public EdgeList(float[][] list, int startY, int endY) {
		// TODO fill this in.
		this.list = list;
		this.startY = startY;
		this.endY = endY;
	}

	public int getStartY() {
		// TODO fill this in.
		return this.startY;
	}

	public int getEndY() {
		// TODO fill this in.
		return this.endY;
	}

	public float getLeftX(int y) {
		// TODO fill this in.
		return list[(y)][0];
	}

	public float getRightX(int y) {
		// TODO fill this in.
		return list[(y)][2];
	}

	public float getLeftZ(int y) {
		// TODO fill this in.
		return list[(y)][1];
	}

	public float getRightZ(int y) {
		// TODO fill this in.
		return list[(y)][3];
	}

	public float[][] getList() {
		return this.list;
	}
}
