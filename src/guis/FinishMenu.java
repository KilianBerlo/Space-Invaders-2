package guis;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

public class FinishMenu {
	private final static int STATE_NUM = 2; // number of states in the main menu, excluding instructions
	
	private GuiTexture restartScreen;
	private GuiTexture quitScreen;
	private int[] numberTextures;
	
	private List<GuiTexture> numberGuiTextures = new ArrayList<GuiTexture>();
	
	public int state = 0;
	public boolean active = true;
	
	public boolean upIsPressed = true;
	public boolean downIsPressed = true;
	public boolean returnIsPressed = true;
	
	public FinishMenu(GuiTexture restartScreen, GuiTexture quitScreen, int[] numberTextures) {
		this.restartScreen = restartScreen;
		this.quitScreen = quitScreen;
		this.numberTextures = numberTextures;
	}
	
	public GuiTexture getActiveTexture() {
		if(state == 0) return restartScreen;
		if(state == 1) return quitScreen;
		return restartScreen;
	}
	
	public List<GuiTexture> getNumberTextures() {
		return numberGuiTextures;
	}
	
	public void setValues(int objective, int score) {
		numberGuiTextures = new ArrayList<GuiTexture>();
		
		int obj1 = objective / 10;
		int obj2 = objective % 10;
		int sco1 = score / 10;
		int sco2 = score % 10;
		
		numberGuiTextures.add(new GuiTexture(numberTextures[obj1], new Vector2f(0.325f,0.08f), new Vector2f(0.045f, 0.045f)));
		numberGuiTextures.add(new GuiTexture(numberTextures[obj2], new Vector2f(0.385f,0.08f), new Vector2f(0.045f, 0.045f)));
		numberGuiTextures.add(new GuiTexture(numberTextures[sco1], new Vector2f(0.325f,-0.13f), new Vector2f(0.045f, 0.045f)));
		numberGuiTextures.add(new GuiTexture(numberTextures[sco2], new Vector2f(0.385f,-0.13f), new Vector2f(0.045f, 0.045f)));
	}
	
	public void reset() {
		state = 0;
		active = true;
		
		upIsPressed = false;
		downIsPressed = false;
		returnIsPressed = false;
	}
	
	public void update() {
		if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
			if(!returnIsPressed) {
				active = false;
			}
			returnIsPressed = true;
		} else {
			returnIsPressed = false;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN) && state >= 0 && state <= 2) {
			if(!downIsPressed) {
				state += 1;
				if(state >= STATE_NUM) state = 0;
			}
			
			downIsPressed = true;
			
			
		} else {downIsPressed = false;}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_UP) && state >= 0 && state <= 2) {
			if(!upIsPressed) {
				state -= 1;
				if(state < 0) state = STATE_NUM - 1;
			}
			
			upIsPressed = true;
			
			
		} else {upIsPressed = false;}
		
		// to exit the instructions screen
		if(Keyboard.isKeyDown(Keyboard.KEY_BACK)  && state == 3) state = 1;
	}
}
