package guis;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

public class ObjectiveCounter {
	float xStart = -0.825f;
	float xStep = 0.035f;
	float yStart = 0.78f;
	
	private int[] numberTextures;
	private GuiTexture coinTexture;
	private GuiTexture slashTexture;
	
	private List<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
	
	private int score = -1;
	private int objective = -1;
	
	public ObjectiveCounter(int coinTexture, int slashTexture, int[] numberTextures) {
		this.coinTexture = new GuiTexture(coinTexture, new Vector2f(-0.875f, yStart - 0.01f), new Vector2f(0.035f, 0.035f));
		this.slashTexture = new GuiTexture(slashTexture, new Vector2f(xStart + 2 * xStep, yStart), new Vector2f(0.025f, 0.025f));
		this.numberTextures = numberTextures;
	}
	
	public List<GuiTexture> getGuiTextures() {
		return this.guiTextures;
	}
	
	public void setValue(int objective, int score) {
		// so its not changed when it doesnt need to be
		if(objective == this.objective && score == this.score) return;
		this.objective = objective;
		this.score = score;
		
		guiTextures = new ArrayList<GuiTexture>();
		
		guiTextures.add(coinTexture);
		guiTextures.add(slashTexture);
		
		int obj1 = objective / 10;
		int obj2 = objective % 10;
		int sco1 = score / 10;
		int sco2 = score % 10;
		

		guiTextures.add(new GuiTexture(numberTextures[sco1], new Vector2f(xStart,yStart), new Vector2f(0.025f, 0.025f)));
		guiTextures.add(new GuiTexture(numberTextures[sco2], new Vector2f(xStart + xStep, yStart), new Vector2f(0.025f, 0.025f)));
		guiTextures.add(new GuiTexture(numberTextures[obj1], new Vector2f(xStart + 3 * xStep,yStart), new Vector2f(0.025f, 0.025f)));
		guiTextures.add(new GuiTexture(numberTextures[obj2], new Vector2f(xStart + 4 * xStep,yStart), new Vector2f(0.025f, 0.025f)));

	}
}
