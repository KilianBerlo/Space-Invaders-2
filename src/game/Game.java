package game;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import audioEngine.AudioController;
import audioEngine.AudioSource;
import enemy.Enemy;
import entities.Camera;
import entities.Entity;
import entities.Player;
import entities.Projectile;
import guis.FinishMenu;
import guis.GuiTexture;
import guis.Menus;
import guis.ObjectiveCounter;
import guis.ValueSlider;
import models.RawModel;
import models.TexturedModel;
import pathfinding.MinHeap;
import pathfinding.VoxelGraph;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import textures.ModelTexture;
import ultimatePower.TargetFinder;
import utils.Maths;
import voxelWorld.Light;
import voxelWorld.VoxelHitbox;
import voxelWorld.VoxelModel;
import voxelWorld.VoxelWorld;

public class Game {

	public static void main(String[] args) {
		DisplayManager.createDisplay();
		boolean running = true;
		MinHeap.main();

		showLoadingScreen();
		
		// ----------------------------------- World -----------------------------------------
		System.out.println("Setting up world...");
		Loader loader = new Loader();
		VoxelWorld world = new VoxelWorld(new Vector3f(0, 0, 0), "spacestationepic2", loader); // create world
		Player player = new Player(world, loader); // create player
		MasterRenderer renderer = new MasterRenderer(loader, player.getCamera()); // create renderer
		

		// ----------------------------------- Neural network -----------------------------------
		TargetFinder.init(); // initialize model
		

		// ----------------------------------- Shadows -----------------------------------------
		System.out.println("Setting up shadows...");
		renderer.registerShadowCaster(world.getWorldEntity());
		renderer.registerShadowCaster(world.getBrightWorldEntity());
		renderer.registerShadowCaster(player.getShadowEntity());

		// ----------------------------------- Guis ------------------------------------------
		System.out.println("Setting up guis...");
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiTexture crosshair = new GuiTexture(loader.loadTexture("crosshair"), new Vector2f(0, 0.0f), new Vector2f(0.03f, 0.03f));
		GuiTexture helmet = new GuiTexture(loader.loadTexture("helmet2"), new Vector2f(0.25f, -0.9f), new Vector2f(1.95f, 1.1f));
		GuiTexture powerActive = new GuiTexture(loader.loadTexture("power_activate"), new Vector2f(0.8f, 0.75f), new Vector2f(0.06f, 0.06f));
		GuiTexture powerInactive = new GuiTexture(loader.loadTexture("power_activate_unavailable"), new Vector2f(0.8f, 0.75f), new Vector2f(0.06f, 0.06f));
		guis.add(helmet);
		guis.add(crosshair);

		ValueSlider healthbar = new ValueSlider(loader.loadTexture("healthbar"), loader.loadTexture("mask"), 100f, new Vector2f(0.0f, 0.91f), new Vector2f(0.25f, 0.25f));
		ValueSlider cooldownbar = new ValueSlider(loader.loadTexture("cooldownbar"), loader.loadTexture("mask"), 0f, new Vector2f(0.0f, -0.93f), new Vector2f(0.25f, 0.25f));
		guis.add(healthbar.getBar());
		guis.add(cooldownbar.getBar());

		Menus pauseMenu = getPauseMenu(loader, renderer);
		FinishMenu deathMenu = getDeathMenu(loader, renderer);
		FinishMenu winMenu = getWinMenu(loader, renderer);
		boolean escIsDown = false;
		boolean showingMenu = false;
		
		ObjectiveCounter objCounter = getObjectiveCounter(loader);
		
		// ----------------------------------- Audio Engine Setup  ------------------------------------------
		System.out.println("Setting up audio engine...");
		AudioController audioController = initializeAudioController(player, world);
		AudioSource win = audioController.createPositionedSource(AudioController.WIN, 1.0f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),false);
		AudioSource death = audioController.createPositionedSource(AudioController.DEATH, 1.0f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),false);
		boolean finishIsPlaying = false; // if playing in finish menu

		// --------------------------------------- Main Menu -------------------------------------------------
		System.out.println("Starting main menu...");
		running = showMainMenu(loader, renderer);

		// ---------------------------------------- Main game loop -------------------------------------------
		System.out.println("Starting main loop...");
		while(!Display.isCloseRequested() && running) {
			// -----------------------------playtime menus----------------------------------------
			// Death menu
			if(!player.isAlive()) {
				if(!finishIsPlaying) {
					death.play();
					finishIsPlaying = true;
					
					deathMenu.setValues(Player.OBJECTIVE_GOAL, player.objectivesCollected);
				}

				showFinishMenu(deathMenu, renderer);

				if(!deathMenu.active && deathMenu.state == 1) break;
				if(!deathMenu.active && deathMenu.state == 0) {
					player.reset();
					world.reset(player);
					deathMenu.reset();
				}
				continue;
			}

			// Win menu
			if(player.isWin()) {
				if(!finishIsPlaying) {
					win.play();
					finishIsPlaying = true;
					
					winMenu.setValues(Player.OBJECTIVE_GOAL, player.objectivesCollected);
				}

				showFinishMenu(winMenu, renderer);

				if(!winMenu.active && winMenu.state == 1) break;
				if(!winMenu.active && winMenu.state == 0) {
					player.reset();
					world.reset(player);
					winMenu.reset();
				}
				continue;
			}
			finishIsPlaying = false; // outside of finish menu

			// pause menu
			if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
				if(!escIsDown) {
					showingMenu = !showingMenu;
					pauseMenu.resetState();
				}
				escIsDown = true;
			} else {
				escIsDown = false;
			}

			if(showingMenu) {
				showPauseMenu(pauseMenu, renderer);

				showingMenu = pauseMenu.isActive;
				if(!pauseMenu.isActive && !pauseMenu.isSuccess) break;
				continue;
			}

			// -----------------------------game logic----------------------------------------
			// player movement
			player.update(renderer.getProjectionMatrix());
			
			// world game tick
			world.update(player);

			// gui update
			cooldownbar.setValue(100 - player.getWeapon().getHeat());
			healthbar.setValue(player.getHealth());
			objCounter.setValue(Player.OBJECTIVE_GOAL, player.objectivesCollected);
			
			// sound setup
			audioController.setListenerValues(player);
			
			
			// -----------------------------rendering----------------------------------------
			renderer.renderShadowMap(world.getSun()); // calculate shadow buffer

			// render entities
			player.render(renderer);
			world.render(renderer);

			renderer.render(world.getSun(), world.getClosestLights(player), player.getCamera()); // render scene
			
			if(player.getWeapon().getCrosshairs() != null) {
				renderer.processGuis(player.getWeapon().getCrosshairs());
			}
			
			renderer.processGuis(guis); // render GUIs after the rest
			List<GuiTexture> masks = new ArrayList<GuiTexture>();
			masks.add(healthbar.getMask());
			masks.add(cooldownbar.getMask());
			
			// power active indicator
			if(player.getWeapon().canUsePower()) {
				masks.add(powerActive);
			} else {
				masks.add(powerInactive);
			}
			
			renderer.processGuis(masks);
			
			renderer.processGuis(objCounter.getGuiTextures());
			
			DisplayManager.update(); // update the screen and let the GPU do its thing
		}

		// clean up after closing window
		renderer.cleanUp();
		loader.cleanUp();
		audioController.cleanUp();
		DisplayManager.closeDisplay();
		TargetFinder.cleanUp();
	}

	private static AudioController initializeAudioController(Player player, VoxelWorld world) {
		AudioController audioController = new AudioController();
		audioController.loadAudio();
		AudioSource soundtrack = audioController.createSource(2, 1.0f, 0.4f, true);
		soundtrack.play();

		AudioSource laser = audioController.createPositionedSource(AudioController.LASER, 0.8f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),false);
		AudioSource powerActivate = audioController.createPositionedSource(AudioController.POWER_ACTIVATE, 1.0f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),false);
		AudioSource powerReady = audioController.createPositionedSource(AudioController.POWER_READY, 1.0f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),false);
		player.getWeapon().setAudioSources(laser, powerActivate, powerReady);
		AudioSource footsteps = audioController.createPositionedSource(AudioController.FOOTSTEPS, 1.0f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),true);
		player.setFootsteps(footsteps);

		AudioSource enemyHit = audioController.createPositionedSource(AudioController.ENEMY_HIT, 1.0f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),false);
		AudioSource enemyAttack = audioController.createPositionedSource(AudioController.ENEMY_ATTACK, 1.0f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),false);
		AudioSource coinPickup = audioController.createPositionedSource(AudioController.COIN_PICKUP, 1.0f, 0.75f, player.getPosition(), new Vector3f(0f,0f,0f),false);
		world.setSounds(enemyAttack, enemyHit, coinPickup);

		AudioSource playerDamage = audioController.createPositionedSource(AudioController.PLAYER_DAMAGE, 1.0f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),false);
		AudioSource playerHeartbeat = audioController.createPositionedSource(AudioController.PLAYER_HEARTBEAT, 1.0f, 1.0f, player.getPosition(), new Vector3f(0f,0f,0f),true);
		player.setDamageSound(playerDamage);
		player.setHeartbeatSound(playerHeartbeat);

		return audioController;
	}

	private static void showLoadingScreen() {
		Loader loader = new Loader();
		MasterRenderer renderer = new MasterRenderer(loader, new Camera(new Vector3f(0, 0, 0))); // create renderer

		GuiTexture playSreen = new GuiTexture(loader.loadTexture("loading"), new Vector2f(0, 0), new Vector2f(1f, 1f));
		List<GuiTexture> masks = new ArrayList<GuiTexture>();
		masks.add(playSreen);
		renderer.processGuis(masks);

		DisplayManager.update();

		renderer.cleanUp();
		loader.cleanUp();
	}

	private static boolean showMainMenu(Loader loader, MasterRenderer renderer) {
		GuiTexture playSreen = new GuiTexture(loader.loadTexture("welcome_play"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		GuiTexture instructionsScreen = new GuiTexture(loader.loadTexture("welcome_instructions"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		GuiTexture quitScreen = new GuiTexture(loader.loadTexture("welcome_quit"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		GuiTexture instructionScreen2 = new GuiTexture(loader.loadTexture("instructions"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		Menus menus = new Menus(playSreen, instructionsScreen, quitScreen, instructionScreen2);

		// main menu loop
		while(!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && menus.isActive) { // escape to exit
			menus.update();

			List<GuiTexture> masks = new ArrayList<GuiTexture>();
			masks.add(menus.getActiveTexture());
			renderer.processGuis(masks);

			DisplayManager.update(); // update the screen and let the GPU do its thing
		}

		return menus.isSuccess;
	}

	private static Menus getPauseMenu(Loader loader, MasterRenderer renderer) {
		GuiTexture playSreen = new GuiTexture(loader.loadTexture("menu_continue"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		GuiTexture instructionsScreen = new GuiTexture(loader.loadTexture("menu_instructions"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		GuiTexture quitScreen = new GuiTexture(loader.loadTexture("menu_quit"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		GuiTexture instructionScreen2 = new GuiTexture(loader.loadTexture("instructions"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		Menus menus = new Menus(playSreen, instructionsScreen, quitScreen, instructionScreen2);

		return menus;
	}

	private static FinishMenu getDeathMenu(Loader loader, MasterRenderer renderer) {
		GuiTexture restartScreen = new GuiTexture(loader.loadTexture("died_restart"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		GuiTexture quitScreen = new GuiTexture(loader.loadTexture("died_quit"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		
		int[] numberTextures = new int[] {
			loader.loadTexture("red_0"),	
			loader.loadTexture("red_1"),	
			loader.loadTexture("red_2"),	
			loader.loadTexture("red_3"),	
			loader.loadTexture("red_4"),	
			loader.loadTexture("red_5"),	
			loader.loadTexture("red_6"),	
			loader.loadTexture("red_7"),	
			loader.loadTexture("red_8"),	
			loader.loadTexture("red_9"),	
		};
		
		FinishMenu menu = new FinishMenu(restartScreen, quitScreen, numberTextures);

		return menu;
	}

	private static FinishMenu getWinMenu(Loader loader, MasterRenderer renderer) {
		GuiTexture restartScreen = new GuiTexture(loader.loadTexture("win_restart"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		GuiTexture quitScreen = new GuiTexture(loader.loadTexture("win_quit"), new Vector2f(0, 0), new Vector2f(1.15f, 1.15f));
		
		
		int[] numberTextures = new int[] {
			loader.loadTexture("green_0"),
			loader.loadTexture("green_1"),	
			loader.loadTexture("green_2"),	
			loader.loadTexture("green_3"),	
			loader.loadTexture("green_4"),	
			loader.loadTexture("green_5"),	
			loader.loadTexture("green_6"),	
			loader.loadTexture("green_7"),	
			loader.loadTexture("green_8"),	
			loader.loadTexture("green_9"),	
		};
		
		FinishMenu menu = new FinishMenu(restartScreen, quitScreen, numberTextures);

		return menu;
	}
	
	private static ObjectiveCounter getObjectiveCounter(Loader loader) {
		int[] numberTextures = new int[] {
				loader.loadTexture("blue_0"),
				loader.loadTexture("blue_1"),
				loader.loadTexture("blue_2"),
				loader.loadTexture("blue_3"),
				loader.loadTexture("blue_4"),
				loader.loadTexture("blue_5"),
				loader.loadTexture("blue_6"),
				loader.loadTexture("blue_7"),
				loader.loadTexture("blue_8"),
				loader.loadTexture("blue_9"),
			};
		
		return new ObjectiveCounter(loader.loadTexture("collectables_indicator"), loader.loadTexture("blue_slash"), numberTextures);
	}
	
	private static void showPauseMenu(Menus pauseMenu, MasterRenderer renderer) {
		pauseMenu.update();

		List<GuiTexture> masks = new ArrayList<GuiTexture>();
		masks.add(pauseMenu.getActiveTexture());
		renderer.processGuis(masks);

		DisplayManager.update(); // update the screen and let the GPU do its thing
	}

	private static void showFinishMenu(FinishMenu menu, MasterRenderer renderer) {
		menu.update();

		List<GuiTexture> masks = new ArrayList<GuiTexture>();
		masks.add(menu.getActiveTexture());
		renderer.processGuis(masks);
		
		renderer.processGuis(menu.getNumberTextures());
		
		DisplayManager.update(); // update the screen and let the GPU do its thing
	}
}
