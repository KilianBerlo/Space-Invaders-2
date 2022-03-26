package renderEngine;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

public class DisplayManager {
	private static final boolean FULLSCREEN = true;
	private static final boolean FRAMERATECAP = false;
	
	// for windowed mode
	private static final int WIDTH = 1280; // width of display in pixels
	private static final int HEIGHT = 720; // height of display in pixels
	
	private static final int MAX_FPS = 60; // framerate cap
	private static final String gameTitle = "Space Invaders 2: Attack of the aliens"; // display title
	
	// for fps counter
	private static long lastSecondTime;
	private static int frameCount = 0;
	
	// for physics
	private static long lastFrameTime;
	private static float delta;
	
	private static float speedMultiplier = 1.0f;
	private static float transitionTime = 0f;
	private static float target = speedMultiplier;
	
	// create new display and set it up
	public static void createDisplay() {
		ContextAttribs attribs = new ContextAttribs(3,2) // OpenGL 3.2
		.withForwardCompatible(true) // set it to forward compatible
		.withProfileCore(true);
		
		try {
			if(FULLSCREEN) {
				Display.setResizable(true);
				Display.setFullscreen(true);
			} else {
				Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			}

			Display.setTitle(gameTitle);
			Display.setVSyncEnabled(FRAMERATECAP);
			Display.create(new PixelFormat().withSamples(4), attribs);
			GL11.glEnable(GL13.GL_MULTISAMPLE); // anti aliasing
			
			Mouse.setGrabbed(true); // so the mouse sticks in the window and you have to alt tab to get out
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight()); // set part of display that is the viewport
		
		lastSecondTime = getCurrentTime();
	}
	
	// update display
	public static void update() {
		if(transitionTime > 0.01f) {
			float toGo = target - speedMultiplier;
			float step = toGo / transitionTime;
			speedMultiplier += getTrueFrameTimeSeconds() * step;
			transitionTime -= getTrueFrameTimeSeconds();
		} else {
			speedMultiplier = target;
			transitionTime = 0;
		}
		
		if (Display.wasResized()) GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		
		if(FRAMERATECAP) {
			Display.sync(MAX_FPS);
		}

		Display.update();
		
		// frame meter stuff
		frameCount++;
		
		if(getCurrentTime() - lastSecondTime > 1000f) { // second has passed
			System.out.println("FPS: " + frameCount);
			frameCount = 0;
			lastSecondTime = getCurrentTime();
		}
		
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime) / 1000f;
		lastFrameTime = currentFrameTime;
	}
	

	public static void changeSpeed(float target, float transitionTime) {
		if(Math.abs(speedMultiplier - target) > 0.01f) {
			DisplayManager.target = target;
			DisplayManager.transitionTime = transitionTime;
		}
	}
	
	public static float getSpeedMultiplier() {
		return speedMultiplier;
	}
	
	public static boolean inTransition() {
		return transitionTime > 0;
	}
	
	public static float getDeltaTimeSeconds() {
		if(delta > 1) delta = 0;
		
		return speedMultiplier * delta;
	}
	
	public static float getTrueFrameTimeSeconds() {
		if(delta > 1) delta = 0;
		
		return delta;
	}
	
	// close display on exit game
	public static void closeDisplay() {
		Display.destroy();
	}
	
	private static long getCurrentTime() {
		return 1000 * Sys.getTime() / Sys.getTimerResolution();
	}
	
	public static BufferedImage extractScreenCapture() {
		GL11.glReadBuffer(GL11.GL_FRONT);
		int width = Display.getDisplayMode().getWidth();
		int height= Display.getDisplayMode().getHeight();
		int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		   
		for(int x = 0; x < width; x++) 
		{
		    for(int y = 0; y < height; y++)
		    {
		        int i = (x + (width * y)) * bpp;
		        int r = buffer.get(i) & 0xFF;
		        int g = buffer.get(i + 1) & 0xFF;
		        int b = buffer.get(i + 2) & 0xFF;
		        image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
		    }
		}
		   
		return image;
	}
}
