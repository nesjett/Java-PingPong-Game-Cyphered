import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javazoom.jl.decoder.*;
import javazoom.jl.player.*;
public class Music {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			File file = new File("C:/Users/Dima/Desktop/Pong/main.mp3");
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			
			try {
				Player player = new Player(bis);
				player.play();
			}catch(JavaLayerException ex) {}
			
		}catch(IOException e) {}
	}

}
