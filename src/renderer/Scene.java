package renderer;

import java.awt.Color;
import java.util.ArrayList;

/**
 * The Scene class is where we store data about a 3D model and light source
 * inside our renderer. It also contains a static inner class that represents one
 * single polygon.
 
 */
public class Scene {

	private ArrayList<Polygon> polygons;
	private Vector3D lightPos;

	public Scene(ArrayList<Polygon> polygons, Vector3D lightPos) {
		this.polygons = polygons;
		this.lightPos = lightPos;
	}

	public ArrayList<Polygon> getPolygons() {
        return this.polygons;
	}

	public Vector3D getLight() {
          return this.lightPos;
	}

	public void setPolygons(ArrayList<Polygon> polygons) {
		this.polygons = polygons;
	}



	/**
	 * Polygon stores data about a single polygon in a scene, keeping track of
	 * (at least!) its three vertices and its reflectance.
	 */
	public static class Polygon {
		Vector3D[] vertices;
		Color reflectance;
		Vector3D unitNormal;
		Vector3D unitVector;
		public boolean isHidden;
		public EdgeList edgeList;
		public Color pixelColor;

		/**
		 * @param points
		 *            An array of floats with 9 elements, corresponding to the
		 *            (x,y,z) coordinates of the three vertices that make up
		 *            this polygon. If the three vertices are A, B, C then the
		 *            array should be [A_x, A_y, A_z, B_x, B_y, B_z, C_x, C_y,
		 *            C_z].
		 * @param color
		 *            An array of three ints corresponding to the RGB values of
		 *            the polygon, i.e. [r, g, b] where all values are between 0
		 *            and 255.
		 */
		public Polygon(float[] points, int[] color) {
			this.vertices = new Vector3D[3];

			float x, y, z;
			for (int i = 0; i < 3; i++) {
				x = points[i * 3];
				y = points[i * 3 + 1];
				z = points[i * 3 + 2];
				this.vertices[i] = new Vector3D(x, y, z);
			}

			int r = color[0];
			int g = color[1];
			int b = color[2];
			this.reflectance = new Color(r, g, b);
		}

		/**
		 * An alternative constructor that directly takes three Vector3D objects
		 * and a Color object.
		 */
		public Polygon(Vector3D a, Vector3D b, Vector3D c, Color color) {
			this.vertices = new Vector3D[] { a, b, c };
			this.reflectance = color;
			this.assignUnitNormals();
			this.assignIsHidden();
		}

		public void assignIsHidden() {
			boolean toHide = Pipeline.isHidden(this);
			this.isHidden = toHide;

		}

		//We assign a unit normal to a polygon upon creation, for simplicity in later calculations.
		public void assignUnitNormals() {
			Vector3D vectorA = this.getVertices()[0];
			Vector3D vectorB = this.getVertices()[1];
			Vector3D vectorC = this.getVertices()[2];
			float x1 = vectorA.x; float y1 = vectorA.y; float z1 = vectorA.z;
			float x2 = vectorB.x; float y2 = vectorB.y; float z2 = vectorB.z;
			float x3 = vectorC.x; float y3 = vectorC.y; float z3 = vectorC.z;
			float nx = ((y2 - y1) * (z3 - z2)) - ((z2 - z1) * (y3 - y2));
			float ny = ((z2 - z1) * (x3 - x2)) - ((x2 - x1) * (z3 - z2));
			float nz = ((x2 - x1) * (y3 - y2)) - ((y2 - y1) * (x3 - x2));
			this.unitNormal = new Vector3D(nx, ny, nz);
			float unitVectorValue = (float) Math.sqrt((Math.pow(nx, 2)) + (Math.pow(ny, 2)) + (Math.pow(nz, 2)));
			this.unitVector = new Vector3D(nx/unitVectorValue, ny/unitVectorValue, nz/unitVectorValue); //Majorly suspect.
		}

		public Vector3D[] getVertices() {
			return vertices;
		}

		public Color getReflectance() {
			return reflectance;
		}

		@Override
		public String toString() {
			String str = "polygon:";

			for (Vector3D p : vertices)
				str += "\n  " + p.toString();

			str += "\n  " + reflectance.toString();

			return str;
		}
	}
}
