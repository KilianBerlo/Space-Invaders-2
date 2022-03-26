package audioEngine;

import org.lwjgl.util.vector.Vector3f;

public class AudioSource {
	
	private int sourceID;
	private AudioController audioController;

	public AudioSource(int sourceID, AudioController audioController) {
		this.sourceID = sourceID;
		this.audioController = audioController;
	}

	public int getSourceID() {
		return sourceID;
	}
	
	public void play() {
		audioController.play(this);
	}
	
	public void pause() {
		audioController.pause(this);
	}
	
	public void stop() {
		audioController.stop(this);
	}
	
	public void setClip(int fileNum) {
		audioController.setClip(this, fileNum);
	}
	
	public void setPitch(float pitch) {
		audioController.setPitch(this, pitch);
	}
	
	public void setGain(AudioSource audioSource, float gain) {
		audioController.setGain(this, gain);
	}
	
	public void setPosition(Vector3f position) {
		audioController.setPosition(this,position);
	}
	
	public void setVelocity(Vector3f velocity) {
		audioController.setPosition(this,velocity);
	}
	
	public void setLoop(AudioSource audioSource, boolean loop) {
		audioController.setLoop(this, loop);
	}
}
