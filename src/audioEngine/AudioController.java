package audioEngine;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;
import org.lwjgl.util.vector.Vector3f;

import entities.Player;

public class AudioController {
	
	public static final int LASER = 0;
	public static final int FOOTSTEPS = 1;
	public static final int SOUNDTRACK = 2;
	public static final int ENEMY_HIT = 3;
	public static final int ENEMY_ATTACK = 4;
	public static final int COIN_PICKUP = 5;
	public static final int PLAYER_DAMAGE = 6;
	public static final int PLAYER_HEARTBEAT = 7;
	public static final int WIN = 8;
	public static final int DEATH = 9;
	public static final int POWER_ACTIVATE = 10;
	public static final int POWER_READY = 11;
	
	public static final int NUM_BUFFERS = 12;
	
	private boolean mute = false;

	//Buffers for audio data
	IntBuffer buffer = BufferUtils.createIntBuffer(NUM_BUFFERS);

	//Audio sources
	IntBuffer source = BufferUtils.createIntBuffer(128);

	
	public AudioController() {
		//Flip Buffers
		try {
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
				
		//Create audio buffers
		AL10.alGenBuffers(buffer);
		
		//Load Audio into buffers
		loadAudio();
	}
	
	public boolean loadAudio() {
		//Load in files
		loadAudioFile(LASER, "Laser");
		loadAudioFile(FOOTSTEPS, "Steps");
		loadAudioFile(SOUNDTRACK, "soundtrackv1");
		loadAudioFile(ENEMY_HIT, "enemyHit");
		loadAudioFile(ENEMY_ATTACK, "enemy");
		loadAudioFile(COIN_PICKUP, "pickup");
		loadAudioFile(PLAYER_DAMAGE, "playerHit");
		loadAudioFile(PLAYER_HEARTBEAT, "heartbeat");
		loadAudioFile(WIN, "win");
		loadAudioFile(DEATH, "death");
		loadAudioFile(POWER_ACTIVATE, "powerup");
		loadAudioFile(POWER_READY, "powerupReady");
		
		//Check for Errors
		if (AL10.alGetError() == AL10.AL_NO_ERROR) {
			 return true;
		 }else {
			 return false;
		 }
	}
	
	public void loadAudioFile(int fileNum, String fileName) {
		WaveData waveFile = null;
		//Try to open File
		try {
			waveFile = WaveData.create(new BufferedInputStream(new FileInputStream("res/audio/"+fileName+".wav")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//Load file into audio buffer
		AL10.alBufferData(buffer.get(fileNum), waveFile.format, waveFile.data, waveFile.samplerate);
	    waveFile.dispose();
	    return;
	    
	}
	
	//Set position velocity and orientation of listener
	public void setListenerValues(Vector3f position, Vector3f velocity, Vector3f orientation) {
		FloatBuffer playerOrientation = BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f });
		playerOrientation.flip();
		
	    AL10.alListener(AL10.AL_POSITION,    storeDataInFloatBuffer(position));
	    AL10.alListener(AL10.AL_VELOCITY,    storeDataInFloatBuffer(velocity));
	    AL10.alListener(AL10.AL_ORIENTATION, playerOrientation);
	}
	
	// overloader for when the listener is the player
	public void setListenerValues(Player player) {
		this.setListenerValues(player.getPosition(), new Vector3f(0f,0f,0f), new Vector3f(0f,0f,0f));
	}
	
	public void cleanUp() {
	    // set to 0, num_sources
	    int positionSource = source.position();
	    source.position(0).limit(positionSource);
	    AL10.alDeleteSources(source);
	    
	    AL10.alDeleteBuffers(buffer);
	    AL.destroy();
	}
	
	public AudioSource createSource(int clip, float pitch, float gain, boolean loop) {
		//Get current source index
		int position = source.position();
		source.limit(position + 1);
		//Generate new Source
		AL10.alGenSources(source);
		//Check for Errors
		if(AL10.alGetError() != AL10.AL_NO_ERROR) {
		      System.out.println("Error generating audio source.");
		      System.exit(-1);
		}
		
		//Set parameters of source
		AL10.alSourcei(source.get(position), AL10.AL_BUFFER,   buffer.get(clip) );
	    AL10.alSourcef(source.get(position), AL10.AL_PITCH,    pitch            );
	    if(!mute) {
	    AL10.alSourcef(source.get(position), AL10.AL_GAIN,     gain             );
	    }else {
	    AL10.alSourcef(source.get(position), AL10.AL_GAIN,     0f               );
	    }
	    if(loop) {
	    AL10.alSourcei(source.get(position), AL10.AL_LOOPING,  AL10.AL_TRUE     );
	    }else {
	    AL10.alSourcei(source.get(position), AL10.AL_LOOPING,  AL10.AL_FALSE     );
	    }
	    //Increase index of next source
	    source.position(position+1);
	    
		return new AudioSource(position, this);
	}
	
	public AudioSource createPositionedSource(int clip, float pitch, float gain, Vector3f sourcePos, Vector3f sourceVel, boolean loop) {
		AudioSource audioSource = createSource(clip, pitch,gain, loop);
		//Set position/velocity of source
		AL10.alSource (source.get(audioSource.getSourceID()), AL10.AL_POSITION, storeDataInFloatBuffer(sourcePos)        );
	    AL10.alSource (source.get(audioSource.getSourceID()), AL10.AL_VELOCITY, storeDataInFloatBuffer(sourceVel)        );
	    return audioSource;
	}
	
	protected void play(AudioSource audioSource) {
		AL10.alSourcePlay(source.get(audioSource.getSourceID()));
	}
	
	protected void pause(AudioSource audioSource) {
		AL10.alSourcePause(source.get(audioSource.getSourceID()));
	}
	
	protected void stop(AudioSource audioSource) {
		AL10.alSourceStop(source.get(audioSource.getSourceID()));
	}
	
	protected void setClip(AudioSource audioSource, int fileNum) {
		AL10.alSourcei(source.get(audioSource.getSourceID()), AL10.AL_BUFFER,   buffer.get(fileNum) );
	}
	
	protected void setPitch(AudioSource audioSource, float pitch) {
		AL10.alSourcef(source.get(audioSource.getSourceID()), AL10.AL_PITCH, pitch);
	}
	
	protected void setGain(AudioSource audioSource, float gain) {
		AL10.alSourcef(source.get(audioSource.getSourceID()), AL10.AL_GAIN, gain);
	}
	
	protected void setPosition(AudioSource audioSource, Vector3f position) {
		AL10.alSource(source.get(audioSource.getSourceID()), AL10.AL_GAIN, storeDataInFloatBuffer(position));
	}
	
	protected void setVelocity(AudioSource audioSource, Vector3f velocity) {
		AL10.alSource(source.get(audioSource.getSourceID()), AL10.AL_VELOCITY, storeDataInFloatBuffer(velocity));
	}
	
	protected void setLoop(AudioSource audioSource, boolean loop) {
		if(loop) {
			AL10.alSourcei(source.get(audioSource.getSourceID()), AL10.AL_LOOPING, AL10.AL_TRUE);
		}else {
			AL10.alSourcei(source.get(audioSource.getSourceID()), AL10.AL_LOOPING, AL10.AL_FALSE);
		}
	}
	
	private FloatBuffer storeDataInFloatBuffer(Vector3f position) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
		position.store(buffer);
		buffer.flip(); // prepare it for being read
		
		return buffer;
	}
	
}
