package renderer;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import renderer.Scene.Polygon;

public class Renderer extends GUI {

	private Scene scene = null;

	@Override
	protected void onLoad(File file) {

		ArrayList<Polygon> polygonList = new ArrayList<Polygon>();
		Vector3D lightPos = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String currentLine;
			currentLine = reader.readLine();

			String lightValues[] = currentLine.split("\\s+");
			float lvx = Float.parseFloat(lightValues[0]);
			float lvy = Float.parseFloat(lightValues[1]);
			float lvz = Float.parseFloat(lightValues[2]);
			lightPos = new Vector3D(lvx, lvy, lvz);

			while((currentLine = reader.readLine()) != null){
				String polygonValues[] = currentLine.split("\\s+");

				float x1 = Float.parseFloat(polygonValues[0]);
				float y1 = Float.parseFloat(polygonValues[1]);
				float z1 = Float.parseFloat(polygonValues[2]);

				float x2 = Float.parseFloat(polygonValues[3]);
				float y2 = Float.parseFloat(polygonValues[4]);
				float z2 = Float.parseFloat(polygonValues[5]);

				float x3 = Float.parseFloat(polygonValues[6]);
				float y3 = Float.parseFloat(polygonValues[7]);
				float z3 = Float.parseFloat(polygonValues[8]);

				int r = Integer.parseInt(polygonValues[9]);
				int g = Integer.parseInt(polygonValues[10]);
				int b = Integer.parseInt(polygonValues[11]);

				Vector3D v1 = new Vector3D(x1, y1, z1);
				Vector3D v2 = new Vector3D(x2, y2, z2);
				Vector3D v3 = new Vector3D(x3, y3, z3);

				Color polygonColor = new Color(r, g, b);

				Polygon currentPolygon = new Polygon(v1, v2, v3, polygonColor);
				polygonList.add(currentPolygon);
			}
			reader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		//Having loaded the polygons, we need to scale and translate them to fit first thing.
		ArrayList<Polygon> nowScaled = Pipeline.scaleScene(polygonList);
		ArrayList<Polygon> nowTranslated = Pipeline.translateScene(nowScaled);

		this.scene = new Scene(nowTranslated, lightPos);
		render();
	}

	@Override
	protected void onKeyPress(KeyEvent ev) {

		Scene nowRotated;
		char c = ev.getKeyChar();
		switch(c) {
			case 'w': nowRotated = Pipeline.rotateScene(this.scene, 0, -75f);
			break;
			case 'a': nowRotated = Pipeline.rotateScene(this.scene, 75f, 0);
			break;
			case 's': nowRotated = Pipeline.rotateScene(this.scene, 0, 75f);
			break;
			case 'd': nowRotated = Pipeline.rotateScene(this.scene, -75f, 0);
			break;
			default: return;
		}
		this.scene = nowRotated;
		render();
	}

	@Override
	protected BufferedImage render() {

		if(this.scene == null) {
			return null;
		}
		//Compute the edge list and shade each polygon.
		for(Polygon p : this.scene.getPolygons()) {
			p.edgeList = Pipeline.computeEdgeList(p);
			int[] ambientLightArray = getAmbientLight();
			Color ambientLight = new Color(ambientLightArray[0], ambientLightArray[1], ambientLightArray[2]);
			p.pixelColor = Pipeline.getShading(p, scene.getLight(), new Color(200, 200, 200), ambientLight, this);
		}
		//Initialize the two arrays. Probably could have been done in one loop, but I broke it up for uh, clarity. Sure.
		Color[][] zColor = new Color[GUI.CANVAS_WIDTH][GUI.CANVAS_HEIGHT];
		for(int i = 0; i < zColor.length; i ++) {
			for(int j = 0; j < zColor.length; j++) {
				zColor[i][j] = new Color(100, 100, 100);
			}
		}
		float[][] zDepth = new float [GUI.CANVAS_WIDTH][GUI.CANVAS_HEIGHT];
		for(int i = 0; i < zDepth.length; i ++) {
			for(int j = 0; j < zDepth.length; j++) {
				zDepth[i][j] = Float.POSITIVE_INFINITY;
			}
		}
		for(Polygon p : scene.getPolygons()) {
			if(p.isHidden == false) {
				zColor = Pipeline.computeZBuffer(zColor, zDepth, p.edgeList, p.pixelColor);
			}
		}
		return convertBitmapToImage(zColor);
	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static void main(String[] args) {
		new Renderer();
	}
}
