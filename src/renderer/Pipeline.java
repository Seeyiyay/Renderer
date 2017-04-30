package renderer;

import java.awt.Color;
import java.util.ArrayList;

import renderer.Scene.Polygon;

public class Pipeline {

	public static float leftLightX = -1.0f; public static float rightLightX = 1.0f;
	public static float leftLightY = 0f; public static float rightLightY = 0f;
	public static float leftLightZ = 0f; public static float rightLightZ = 0f;

	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise.
	 */
	public static boolean isHidden(Polygon poly) {
		// TODO fill this in.
		if(poly.unitNormal.z >= 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Computes the colour of a polygon on the screen, once the lights, their
	 * angles relative to the polygon's face, and the reflectance of the polygon
	 * have been accounted for.
	 *
	 * @param lightDirection
	 *            The Vector3D pointing to the directional light read in from
	 *            the file.
	 * @param lightColor
	 *            The color of that directional light.
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend
	 *            on the direction.
	 */
	public static Color getShading(Polygon poly, Vector3D lightDirection, Color lightColor, Color ambientLight, Renderer r) {
		//This is the main light source and its values.
		float lightDirectionVectorValue = (float)Math.sqrt(Math.pow(lightDirection.x, 2) + Math.pow(lightDirection.y, 2) + Math.pow(lightDirection.z, 2));
		Vector3D lightDirectionVector = new Vector3D (lightDirection.x/lightDirectionVectorValue, lightDirection.y/lightDirectionVectorValue, lightDirection.z/lightDirectionVectorValue);
		float cosineTheta = Float.max(0, poly.unitVector.dotProduct(lightDirectionVector));

		//We scale the reflectivity to avoid out of bounds errors.
		double refRScale = (poly.getReflectance().getRed()/255.0);
		double refGScale = (poly.getReflectance().getGreen()/255.0);
		double refBScale = (poly.getReflectance().getBlue()/255.0);

		//The following are all values for the left and right directional lights. We only have them as anything other than zero if the sliders
		//are above zero and the checkboxes are checked.
		int lDirLightRed = 0; int rDirLightRed = 0;
		int lDirLightGreen = 0; int rDirLightGreen = 0;
		int lDirLightBlue = 0; int rDirLightBlue = 0;
		float leftDirectionVectorValue; float rightDirectionVectorValue;
		Vector3D rightDirectionVector; Vector3D leftDirectionVector;
		float leftCosineTheta = 0; float rightCosineTheta = 0;
		if(r.getLCheckBox()) {
			lDirLightRed = r.getLeftDirectLight()[0];
			lDirLightGreen = r.getLeftDirectLight()[1];
			lDirLightBlue = r.getLeftDirectLight()[2];
			leftDirectionVectorValue = (float)Math.sqrt(Math.pow(-1.0f, 2) + Math.pow(0f, 2) + Math.pow(0f, 2));
			leftDirectionVector = new Vector3D (leftLightX/leftDirectionVectorValue, leftLightY/leftDirectionVectorValue, leftLightZ/leftDirectionVectorValue);
			leftCosineTheta = Float.max(0, poly.unitVector.dotProduct(leftDirectionVector));
		}
		if(r.getRCheckBox()) {
			rDirLightRed = r.getRightDirectLight()[0];
			rDirLightGreen = r.getRightDirectLight()[1];
			rDirLightBlue = r.getRightDirectLight()[2];
			rightDirectionVectorValue = (float)Math.sqrt(Math.pow(1.0f, 2) + Math.pow(0f, 2) + Math.pow(0f, 2));
			rightDirectionVector = new Vector3D (rightLightX/rightDirectionVectorValue, rightLightY/rightDirectionVectorValue, rightLightZ/rightDirectionVectorValue);
			rightCosineTheta = Float.max(0, poly.unitVector.dotProduct(rightDirectionVector));
		}

		//We keep the colors in range, and we sum all the various light sources and their effects.
		int ocR = (int)(Integer.min(255, (int)(ambientLight.getRed() + (lightColor.getRed() *  cosineTheta) + (lDirLightRed * leftCosineTheta) + (rDirLightRed * rightCosineTheta))) * refRScale);
		int ocG = (int)(Integer.min(255, (int)(ambientLight.getGreen() + (lightColor.getGreen() *  cosineTheta) + (lDirLightGreen * leftCosineTheta) + (rDirLightGreen * rightCosineTheta))) * refGScale);
		int ocB = (int)(Integer.min(255, (int)(ambientLight.getBlue() + (lightColor.getBlue() *  cosineTheta) + (lDirLightBlue * leftCosineTheta) + (rDirLightBlue * rightCosineTheta))) * refBScale);

		return new Color(ocR, ocG, ocB);
	}

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 *
	 * @param scene
	 *            The original Scene.
	 * @param xRot
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yRot
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xRot, float yRot) {
		ArrayList<Polygon> toOperate = new ArrayList<Polygon>(scene.getPolygons());
		//Decide on an x or y rotation.
		Transform rotation = null;
		if(yRot == 0) {
			rotation = Transform.newYRotation(xRot);

		}
		if(xRot == 0) {
			rotation = Transform.newXRotation(yRot);
		}

		ArrayList<Polygon> toReturn = new ArrayList<Polygon>();
		Vector3D temp1 = null;
		Vector3D temp2 = null;
		Vector3D temp3 = null;
		//I really just don't trust Java sometimes, so I did these methods this ridiculous way.
		//I would fix this because of course Java can be trusted when you know what you're doing,
		//which I don't.
		for(Polygon p : toOperate) {
			for(int i = 0; i < 3; i ++) {
				Vector3D v = rotation.multiply(p.getVertices()[i]);
				if(i == 0) {
					temp1 = v;
				}
				if(i == 1) {
					temp2 = v;
				}
				if(i == 2) {
					temp3 = v;
				}
			}
			toReturn.add(new Polygon(temp1, temp2, temp3, p.reflectance));
		}
		//Rotate the main light.
		Vector3D newLight = rotation.multiply(scene.getLight());
		//Rotate the left and right lights. I uh, did this style to make it clearer
		//to the user what was going on. Yeah. Totally didn't do this last minute, no way.
		Vector3D leftLight = new Vector3D(leftLightX, leftLightY, leftLightZ);
		Vector3D rightLight = new Vector3D(rightLightX, rightLightY, rightLightZ);
		Vector3D newLeftLight = rotation.multiply(leftLight);
		Vector3D newRightLight = rotation.multiply(rightLight);
		leftLightX = newLeftLight.x; rightLightX = newRightLight.x;
		leftLightY = newLeftLight.y; rightLightY = newRightLight.y;
		leftLightZ = newLeftLight.z; rightLightZ = newRightLight.z;

		toReturn = scaleScene(toReturn);
		toReturn = translateScene(toReturn);
		return new Scene(toReturn, newLight);
	}

	/**
	 * This should translate the scene by the appropriate amount.
	 *
	 * @param scene
	 * @return
	 */
	public static ArrayList<Polygon> translateScene(ArrayList<Polygon> pList) {
		ArrayList<Polygon> toOperate = new ArrayList<Polygon>(pList);
		float largestX = Float.NEGATIVE_INFINITY;
		float smallestX = Float.POSITIVE_INFINITY;
		float largestY = Float.NEGATIVE_INFINITY;
		float smallestY = Float.POSITIVE_INFINITY;
		//I really just don't trust Java sometimes, so I did these methods this ridiculous way.
		//I would fix this because of course Java can be trusted when you know what you're doing,
		//which I don't.
		for(Polygon p : toOperate) {
			for(Vector3D v : p.getVertices()) {
				if(v.x > largestX) {
					largestX = v.x;
				}
				if(v.x < smallestX) {
					smallestX = v.x;
				}
				if(v.y > largestY) {
					largestY = v.y;
				}
				if(v.y < smallestY) {
					smallestY = v.y;
				}
			}
		}
		float centreCanvasX = (GUI.CANVAS_WIDTH - 1) / 2;
		float centreObjectX = (largestX + smallestX) / 2;
		float xMove = centreCanvasX - centreObjectX;
		float centreCanvasY = (GUI.CANVAS_HEIGHT - 1) / 2;
		float centreObjectY = (largestY + smallestY) / 2;
		float yMove = centreCanvasY - centreObjectY;

		Transform differentialTrans = Transform.newTranslation(xMove, yMove, 0);
		ArrayList<Polygon> toReturn = new ArrayList<Polygon>();
		Vector3D temp1 = null;
		Vector3D temp2 = null;
		Vector3D temp3 = null;

		for(Polygon p : toOperate) {
			for(int i = 0; i < 3; i ++) {
				Vector3D v = differentialTrans.multiply(p.getVertices()[i]);
				if(i == 0) {
					temp1 = v;
				}
				if(i == 1) {
					temp2 = v;
				}
				if(i == 2) {
					temp3 = v;
				}
			}
			toReturn.add(new Polygon(temp1, temp2, temp3, p.reflectance));
		}
		return toReturn;
	}

	/**
	 * This should scale the scene.
	 *
	 * @param scene
	 * @return
	 */
	public static ArrayList<Polygon> scaleScene(ArrayList<Polygon> pList) {
		ArrayList<Polygon> toOperate = new ArrayList<Polygon>(pList);
		float largestX = Float.NEGATIVE_INFINITY;
		float smallestX = Float.POSITIVE_INFINITY;
		float largestY = Float.NEGATIVE_INFINITY;
		float smallestY = Float.POSITIVE_INFINITY;
		//I really just don't trust Java sometimes, so I did these methods this ridiculous way.
		//I would fix this because of course Java can be trusted when you know what you're doing,
		//which I don't.
		for(Polygon p : toOperate) {
			for(Vector3D v : p.getVertices()) {
				if(v.x > largestX) {
					largestX = v.x;
				}
				if(v.x < smallestX) {
					smallestX = v.x;
				}
				if(v.y > largestY) {
					largestY = v.y;
				}
				if(v.y < smallestY) {
					smallestY = v.y;
				}
			}
		}

		float objectWidth = largestX - smallestX;
		float objectHeight = largestY - smallestY;

		float scale = Float.min(((GUI.CANVAS_WIDTH-1) / objectWidth), ((GUI.CANVAS_HEIGHT-1) / objectHeight));
		Transform scaling = Transform.newScale(scale, scale, scale);

		ArrayList<Polygon> toReturn = new ArrayList<Polygon>();
		Vector3D temp1 = null;
		Vector3D temp2 = null;
		Vector3D temp3 = null;

		for(Polygon p : toOperate) {
			for(int i = 0; i < 3; i ++) {
				Vector3D v = scaling.multiply(p.getVertices()[i]);
				if(i == 0) {
					temp1 = v;
				}
				if(i == 1) {
					temp2 = v;
				}
				if(i == 2) {
					temp3 = v;
				}
			}
			toReturn.add(new Polygon(temp1, temp2, temp3, p.reflectance));
		}
		return toReturn;
	}

	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		float edgeList[][] = new float[GUI.CANVAS_HEIGHT][4];
		for(int i = 0; i < edgeList.length; i ++) {
			edgeList[i][0] = Float.POSITIVE_INFINITY;
			edgeList[i][1] = Float.POSITIVE_INFINITY;
			edgeList[i][2] = Float.NEGATIVE_INFINITY;
			edgeList[i][3] = Float.POSITIVE_INFINITY;
		}
		for(int i = 0; i < poly.getVertices().length; i++) {
			if(poly.getVertices()[i].y < minY) {
				minY = (int)poly.getVertices()[i].y;
			}
			if(poly.getVertices()[i].y > maxY) {
				maxY = (int)poly.getVertices()[i].y;
			}
		}
		//We iterate through, looking at the next vertice. If we are at the
		//end of the array, we go back and look at the first vertice. Thus a
		//"loop" is achieved, allowing us to create an edge list.
		for(int index = 0; index < poly.getVertices().length; index ++) {
			Vector3D currentVertice = poly.getVertices()[index];
			float mX; float mZ; float x; float z; int i; float maxI;
			if(index != 2) {
				mX = ((poly.getVertices()[index + 1].x - currentVertice.x) / (Math.abs(poly.getVertices()[index + 1].y - currentVertice.y)));
				mZ = ((poly.getVertices()[index + 1].z - currentVertice.z) / (Math.abs(poly.getVertices()[index + 1].y - currentVertice.y)));
				x = currentVertice.x;
				z = currentVertice.z;
				i = (int)currentVertice.y;
				maxI = (poly.getVertices()[index + 1].y - currentVertice.y);
			}
			else {
				mX = ((poly.getVertices()[0].x - currentVertice.x) / (Math.abs(poly.getVertices()[0].y - currentVertice.y)));
				mZ = ((poly.getVertices()[0].z - currentVertice.z) / (Math.abs(poly.getVertices()[0].y - currentVertice.y)));
				x = currentVertice.x;
				z = currentVertice.z;
				i = (int)currentVertice.y;
				maxI = (poly.getVertices()[0].y - currentVertice.y);
			}
			//We are going "down" the polygon - add to left side.
			if(maxI >= 0f) {
				while(i < (int)(maxI + currentVertice.y)) {
					edgeList[i][0] = x;
					edgeList[i][1] = z;
					x = x + mX;
					z = z + mZ;
					i++;
				}
			}
			//We are going "back up" the polygon - add to right side.
			if(maxI < 0f) {
				while(i >= (int)(currentVertice.y - Math.abs(maxI))) {
					edgeList[i][2] = x;
					edgeList[i][3] = z;
					x = x + mX;
					z = z + mZ;
					i--;
				}
			}
		}
		return new EdgeList(edgeList, minY, maxY);
	}

	/**
	 * Fills a zbuffer with the contents of a single edge list according to the
	 * lecture slides.
	 *
	 * The idea here is to make zbuffer and zdepth arrays in your main loop, and
	 * pass them into the method to be modified.
	 *
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static Color[][] computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor) {
		Color[][] zBufferReturn = zbuffer;
		for(int y = polyEdgeList.getStartY(); y < polyEdgeList.getEndY(); y++) {
			float x = polyEdgeList.getLeftX(y);
			float z = polyEdgeList.getLeftZ(y);
			float mZ = ((polyEdgeList.getRightZ(y) - polyEdgeList.getLeftZ(y)) / (polyEdgeList.getRightX(y) - polyEdgeList.getLeftX(y)));
			while(x <= polyEdgeList.getRightX(y)) {
				if(x < zdepth.length && z < zdepth[(int)x][y]) {
					zdepth[(int)x][y] = z;
					zBufferReturn[(int)x][y] = polyColor;
				}
				z = z + mZ;
				x ++;
			}
		}
		return zBufferReturn;
	}
}
