package guis;

import org.lwjgl.input.Keyboard;

public class Menus {
	private final static int STATE_NUM = 3; // number of states in the main menu, excluding instructions
	
	private GuiTexture playSreen;
	private GuiTexture instructionsScreen;
	private GuiTexture quitScreen;
	private GuiTexture instructionsScreen2;
	
	private int state = 0;
	
	public boolean isSuccess = false;
	public boolean isActive = true;
	
	public boolean upIsPressed = false;
	public boolean downIsPressed = false;
	public boolean returnIsPressed = false;

	public Menus(GuiTexture playSreen, GuiTexture instructionsScreen, GuiTexture quitScreen, GuiTexture instructionScreen2) {
		super();
		this.playSreen = playSreen;
		this.instructionsScreen = instructionsScreen;
		this.quitScreen = quitScreen;
		this.instructionsScreen2 = instructionScreen2;
	}

	public GuiTexture getActiveTexture() {
		if(state == 0) return this.playSreen;
		if(state == 1) return this.instructionsScreen;
		if(state == 2) return this.quitScreen;
		if(state == 3) return this.instructionsScreen2;
		
		return this.playSreen;
	}
	
	public void update() {
		if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
			if(!returnIsPressed) {
				if(state == 0) {
					isSuccess = true;
					isActive = false;
				} else if(state == 1) {
					state = 3;
				} else if(state == 2) {
					isSuccess = false;
					isActive = false;
				} else if(state == 3) {
					state = 1;
				}
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
	
	public void resetState() {
		state = 0;
		isSuccess = false;
		isActive = true;
		
		upIsPressed = false;
		downIsPressed = false;
		returnIsPressed = false;
	}
}
