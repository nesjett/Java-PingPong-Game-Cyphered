import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
//import java.util.Base64;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
//import org.apache.commons.codec.binary.Base64;
//import java.util.Base64;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * @author Burak Sahin
 * Java Network Programming | Pong Game
*/  

public class Test extends JFrame implements KeyListener, Runnable{
	
	/**
	 * Test of the game [*Main Class]
	 */
	
	private static final long serialVersionUID = 1L;
	  
	  ///////////////////
  	 // - Variables - //
	///////////////////
	
	private static Image  image;
	private Graphics g;
	private static final String TITLE  = "Pong Online";	
	private static final int    WIDTH  = 800;		  // - Width  size for window - //
	private static final int    HEIGHT = 460;		  // - Height size for window - //
	private String servername = "servername" , clientname = "clientname";
	
	//private static PongSecurity PSecurity;
 
	  /////////////////////
	 // - Constructor - //
	/////////////////////
	
	public Test(){
		//PSecurity = new PongSecurity();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.setVisible(true);
		this.setTitle(TITLE);
		this.setSize(WIDTH,HEIGHT);
		this.setResizable(true);
		this.addKeyListener(this);
		try {
			File file = new File("main.mp3");
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			
			try {
				Player player = new Player(bis);
				player.play();
			}catch(JavaLayerException ex) {}
			
		}catch(IOException e) {}
	} 
	
	public static void main(String[] args){
		Toolkit tk = Toolkit.getDefaultToolkit();
		image = tk.getImage("background_pong.png"); // - Set background texture of main menu - //
		Test newT = new Test();
		newT.run();

		try {
			File f1 = new File("pubkey.txt");
			File f2 = new File("privkey.txt");
			if(f1.exists() && !f2.isDirectory() && f2.exists() && !f2.isDirectory()){
				System.out.println("Par de claves existente");
			}else{
				PongSecurity.generateKeyPair(true);
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		new HTTPClient().testConnection();

	}
	

	  ///////////////
	 // - Paint - //
	///////////////
	
	private Image createImage(){
		
	    BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	    g = bufferedImage.createGraphics();
	    g.fillRect(0, 0, WIDTH, HEIGHT);
	    g.drawImage(image,0, 0, this);
	    return bufferedImage;
	    
	}
	@Override
	public void paint(Graphics g){
		g.drawImage(createImage(), 0, 20, this);
	}
	

	  /////////////////////
	 // - KeyListener - //
	/////////////////////
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
		int    keyCode = arg0.getKeyCode();
		String portAdd = null;
		String ipAdd   = null;
		
		// - Create a Server - //
		if(keyCode==KeyEvent.VK_S){
			
			// - Input Dialog for get a port address - //
			portAdd = JOptionPane.showInputDialog(null, "ex. 1024", "Enter server port:", 1);
			
			// - Alert Message - //
			if(portAdd!=null){
				if(!isPort(portAdd)){
					JOptionPane.showMessageDialog(null, "Enter port number as a right format!", "Error!", 1);
				}
			
			else{
				
				// - Input Dialog for get a nick name for server player - //
				servername = JOptionPane.showInputDialog(null, "Nick name:", "Enter server name:", 1);
				servername+="";
				
				// - Alert Message - //
				if(servername.length()>10 || servername.length()<3 || servername.startsWith("null")){
					JOptionPane.showMessageDialog(null, "Enter name as a right format!", "Error!", 1);
					
				} 
				
				// - Create a server - //
				else{
					
					PongServer myServer = new PongServer(servername,portAdd, this);
					Thread myServerT = new Thread(myServer);
					myServerT.start();
					this.setVisible(false);
				}
				}
			}
		}
			
		
		
		// - Create a Client - //
		if(keyCode==KeyEvent.VK_C){
			
			// - Input Dialog [IP Address] - // 
			ipAdd = JOptionPane.showInputDialog(null, "ex. 127.0.0.1", "Enter server ip:", 1);
			
			if(ipAdd!=null){
				
				// - Alert Message - //
				if(!isIPAddress(ipAdd)){
					JOptionPane.showMessageDialog(null, "Enter ip number as a right format!", "Enter server ip:", 1);
				}
				
				else{
					// - Input Dialog [Port Number] - // 
					portAdd = JOptionPane.showInputDialog(null, "ex. 1024", "Enter server port number:", 1);
					
					// - Alert Message - //
					if(portAdd!=null){
						if(!isPort(portAdd)){
							JOptionPane.showMessageDialog(null, "Enter port number as a right format!", "Error!:", 1);
						}
						// - Input Dialog for get a nick name for client player - //
						else{
							clientname = JOptionPane.showInputDialog(null, "Nick name:", "Enter server name:", 1);
							clientname += "";
							if(clientname.length()>10 || clientname.length()<3 || clientname.startsWith("null")){
								JOptionPane.showMessageDialog(null, "Enter name as a right format!", "Error!", 1);
							}
							// - Start a Client - //
							else{
								PongClient myClient = new PongClient(clientname, portAdd, ipAdd, this);
								Thread myClientT = new Thread(myClient);
								myClientT.start();
								this.setVisible(false);
							}	
						}
					}
				}
			}
		}//<--end_of_the_key_cond.-->//
}//<--end_of_the_switch-->//

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	  //////////////////////
	 // - Check Inputs - //
	//////////////////////
	
	// - Check PORT number type- //
	private boolean isPort(String str) {  
		  Pattern pPattern = Pattern.compile("\\d{1,4}");  
		  return pPattern.matcher(str).matches();  
		}  
	 // - Check IP address type- //
	private boolean isIPAddress(String str) {  
		  Pattern ipPattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");  
		  return ipPattern.matcher(str).matches();  
		}
	
	
	public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: "
                    + Base64.getEncoder().encodeToString(encrypted));

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

 
}
