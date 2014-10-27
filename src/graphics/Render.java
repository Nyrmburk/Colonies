package graphics;

import java.nio.FloatBuffer;

import main.Engine;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;

import world.World;

/**
 * Provide specialty rendering such as various ui elements.
 * Might deprecate soon.
 * @author Christopher Dombroski
 *
 */
public class Render {
	
	String version = GL11.glGetString(GL11.GL_VERSION);
	
	public static void initGL() {

		// init opengl

		Camera.perspective();
		
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		
		FloatBuffer ambient = asFloatBuffer(new float[]{0.2f, 0.2f, 0.2f, 1f});
		FloatBuffer light = asFloatBuffer(new float[]{0.5f, 0.5f, 0.5f, 1f});
		
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ambient);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, light);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, 
				asFloatBuffer(new float[]{127, 127, 500, 1}));
		GL11.glLoadIdentity();
		GLU.gluLookAt(200, 200, 100, 50, 50, 0, 0, 1, 0);
		
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,GL11.GL_LINE);//line point fill\
		GL11.glPointSize(3);

	}
	
	static FloatBuffer asFloatBuffer(float[] floatArray) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(floatArray.length);
		buffer.put(floatArray);
		buffer.flip();
		return buffer;
	}
	
	public static void draw() {
		
	}
	
	public static void drawSelectionGrid(World world, int[] startCoord, int[] endCoord) {
		
		
		int[] lowest = {Math.min(startCoord[0], endCoord[0]), Math.min(startCoord[1], endCoord[1])};
		int[] highest = {Math.max(startCoord[0], endCoord[0]), Math.max(startCoord[1], endCoord[1])};
		
		//change from fill polygons to draw wireframe
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,GL11.GL_LINE);
		//flat shading
		GL11.glShadeModel(GL11.GL_FLAT);
		//offset the line depth so it does not collide with the other polygons
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
		//set the offset distance
		GL11.glPolygonOffset( -2f, -2f );
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		//left vertical
		GL11.glBegin(GL11.GL_LINE_STRIP);
		for (int y = lowest[1]; y < highest[1]+1; y++) {

			GL11.glVertex3f(lowest[0], y, ((MapMesh) Engine.worldEntity.mdl)
					.getHeight(new float[] { lowest[0], y }));
		}
		GL11.glEnd();
		
		//right vertical
		GL11.glBegin(GL11.GL_LINE_STRIP);
		for (int y = lowest[1]; y < highest[1]+1; y++) {

			GL11.glVertex3f(highest[0], y, ((MapMesh) Engine.worldEntity.mdl)
					.getHeight(new float[] { highest[0], y }));
		}
		GL11.glEnd();
		
		//bottom horizontal
		GL11.glBegin(GL11.GL_LINE_STRIP);
		for (int y = lowest[0]; y < highest[0]+1; y++) {

			GL11.glVertex3f(y, lowest[1], ((MapMesh) Engine.worldEntity.mdl)
					.getHeight(new float[] { y, lowest[1] }));
		}
		GL11.glEnd();
		
		//top horizontal
		GL11.glBegin(GL11.GL_LINE_STRIP);
		for (int y = lowest[0]; y < highest[0]+1; y++) {

			GL11.glVertex3f(y, highest[1], ((MapMesh) Engine.worldEntity.mdl)
					.getHeight(new float[] { y, highest[1] }));
		}
		GL11.glEnd();
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		//disable line offset
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
		//return polygon mode to fill
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK,GL11.GL_FILL);
	}
	
	public static void drawLocalGrid(World world, int centerX, int centerY, int radius) {
		
		final float MID = 0.5f;
		
		byte[][] worldHeight = world.getWorldHeight();
		
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
//		GL11.glLineWidth(2);
		
		
		for (int x = -radius; x <= radius + 1; x++) {
			GL11.glBegin(GL11.GL_LINE_STRIP);

			int y = (int) Math.sqrt(((float) radius + MID)
					* ((float) radius + MID) - ((float) x - MID)
					* ((float) x - MID));

			for (int i = -y + 1; i <= y; i++) {
				if (x + centerX < world.getX() && x + centerX >= 0
						&& i + centerY < world.getY() && i + centerY >= 0) {

					GL11.glVertex3f(x + centerX - MID, i + centerY - MID, worldHeight[x + centerX][i + centerY]);
				}
			}
			GL11.glEnd();
		}
		
		for (int y = -radius; y <= radius + 1; y++) {
			GL11.glBegin(GL11.GL_LINE_STRIP);

			int x = (int) Math.sqrt(((float) radius + MID)
					* ((float) radius + MID) - ((float) y - MID)
					* ((float) y - MID));

			for (int i = -x + 1; i <= x; i++) {
				if (y + centerY < world.getY() && y + centerY >= 0 &&
						i + centerX < world.getX() && i + centerX >= 0) {

					GL11.glVertex3f(i + centerX - MID, y + centerY - MID, worldHeight[i + centerX][y + centerY]);
				}
			}
			GL11.glEnd();
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
	}
	
	public static void drawActionCircle(int[] cursorStart, int cursorEnd[], int options) {
		
		final int degreesInCircle = 360;
		final int circlePoints = 32;
		final int innerRadius = 50;
		final int outerRadius = 100;
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		for (int i = 0; i <= circlePoints; i++) {
			float angle = (float) Math.toRadians(((float) degreesInCircle / circlePoints) * i);
			
			float inX = cursorStart[0] + innerRadius * (float)Math.cos(angle);
			float inY = cursorStart[1] + innerRadius * (float)Math.sin(angle);
			float outX = cursorStart[0] + outerRadius * (float)Math.cos(angle);
			float outY = cursorStart[1] + outerRadius * (float)Math.sin(angle);
			
			GL11.glVertex3f(inX, inY, 0);
			GL11.glVertex3f(outX, outY, 0);
		}
		GL11.glEnd();
	}
}
