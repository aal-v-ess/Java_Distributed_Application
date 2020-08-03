

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


public class MusicPlayer {

	public static int sel = 1;	
	public static Clip clip;
	public static long clipTimePosition;
	//	public float gainControl;

	public MusicPlayer(String musicLocation)
	{
		try {
			//estado reproduzir
			if (sel == 1) {
				File musicPath = new File(musicLocation);
				clip = AudioSystem.getClip();
				AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
				clip.open(audioInput);
				clip.start();
				clip.loop(Clip.LOOP_CONTINUOUSLY);
				sel = 2;
				//Stop
			} else if (sel == 2) {
				clipTimePosition = clip.getMicrosecondPosition();
				clip.stop();
				sel = 3;
				//estado reproduzir
			} else if (sel == 3) {
				clip.setMicrosecondPosition(clipTimePosition);
				clip.start();
				sel = 2;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}


