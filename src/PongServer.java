import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignedObject;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class PongServer extends JFrame implements KeyListener, Runnable, WindowListener{

	/**
	 * Pong_Server 
	 */
	
	private static final long serialVersionUID = 1L;
	  
	  ///////////////////
 	 // - Variables - //
	///////////////////
	
	// - Frame - //
	private static final String TITLE  = "PONG Game (server)";	
	private static final int    WIDTH  = 800;		  // - Width  size for window - //
	private static final int    HEIGHT = 460;		  // - Height size for window - //
	private Test parent;
	
	// - Game Variables - //
	boolean isRunning = false;
	boolean check = true;
	boolean initgame = false;
	
	// - Players & Objects - //
	Ball movingBALL;
	private PlayerServer playerS;
	private PlayerClient playerC;
	
	private int ballVEL   = 3;		// - Ball Velocity - //
	private int barR      = 10;		// - Player bar width - //
	private int playerH   = 120; 	// - Player bar height - //
	private int max_Score = 10; 		// - Maximum match score - //
	private int mPLAYER   = 25; 		// - Moving of the player bar - //
	private boolean Restart   = false;  // - Check Restart - //
	private boolean restartON = false;
	
	private int ballRadius = 28;
	
	// - Server - //
	private static Socket                clientSoc  = null;
	private static ServerSocket          serverSoc  = null;
	private int portAdd;
	
	// - Graphical - //
	private Graphics g;
	private Font sFont = new Font("TimesRoman",Font.BOLD,60);
	private Font mFont = new Font("TimesRoman",Font.BOLD,25);
	private Font nFont = new Font("TimesRoman",Font.BOLD,32);
	private Font rFont = new Font("TimesRoman",Font.BOLD,18);
    	private String[] message;	// - Split Message to two piece in an array - //
    	private Thread movB;
    	
    	
    // - Cipher - //	
	private PongSecurity PSecurity;
	
    
    // - Constructor - //
	public PongServer(String servername, String portAdd, Test parent){
		this.parent = parent;
		
		// - Create player classes - //
		playerS = new PlayerServer(WIDTH, HEIGHT, 65, ballRadius);
		playerC = new PlayerClient("");
		playerS.setName(servername);
				
		// - Setting Frame - //
		this.portAdd = Integer.parseInt(portAdd);
		this.isRunning = true;
		this.setTitle(TITLE + "::port number["+portAdd+"]");
		this.setSize(WIDTH,HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setResizable(false);
			
		// - Create Ball For Moving - //
		movingBALL = new Ball(playerS.getBallx(),playerS.getBally(),ballVEL,ballVEL,ballRadius,WIDTH,HEIGHT);
			
		// - Listener - //
		addKeyListener(this);
		//addWindowListener(this);
	   }	
		
	  /////////////
	 // - Run - //
	/////////////
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		// TODO Auto-generated method stub
		// Server Socket //
   	 try {
   		 	 serverSoc = new ServerSocket(portAdd);
        	 System.out.println("Server has started to running on the "+portAdd+" port.");
        	 System.out.println("Waiting for connection...");
        	 playerS.setImessage("[Esperando jugadores]");
        	 clientSoc = serverSoc.accept();
   			 
        	 System.out.println("Connected a player...");
        	 
        	 
        	 String key = "Bar12345Bar12345"; // 128 bit key
             String initVector = "RandomInitVector"; // 16 bytes IV

             /*System.out.println(decrypt(key, initVector,
                     encrypt(key, initVector, "Hello World")));*/
             
 
            
        	
        	 if(clientSoc.isConnected()){ // - If connected a player start to loop - //
        		 
        		 // - Get client public key from client - //
        		 ObjectInputStream getObj = new ObjectInputStream(clientSoc.getInputStream());
				 SecurityData sDataC = (SecurityData) getObj.readObject();
				 getObj = null;
				 System.out.println("Client security data received: " + sDataC.toString());
        		 
        		 // - Generate new secret key for the match (AES) - //
        		 PSecurity = new PongSecurity();
        		 byte[] secretKey = PSecurity.getKey();
        		 System.out.println("Secret generated: " + secretKey.toString());
        		 
        		 
        		 // - Encript AES secret key with Client's public RSA key - //
        		 String secretKeyEncripted = PongSecurity.encryptWithPublicKey(secretKey, sDataC.getPublic());
        		 System.out.println("Secret encripted: " + secretKeyEncripted);
        		 
        		 // - Sign encripted secret to confirm origin - //
        		 SignedObject signedSecretKeyEncripted = PSecurity.signObject(secretKeyEncripted);
        		 System.out.println("Secret encripted and signed: " + signedSecretKeyEncripted);
        		 
        		 // - Save encrypted AES into object to send over the network - //
        		 SecurityData sDataS = new SecurityData();
        		 sDataS.setSecret(signedSecretKeyEncripted);
        		 sDataS.setPublic(PSecurity.getPublic());
        		 System.out.println("Server security data sent: " + sDataS.toString());
        		 
        		 
        		 // - Send cyphered AES (with RSA) to client - //
        		 ObjectOutputStream sendObj = new ObjectOutputStream(clientSoc.getOutputStream());
              	 sendObj.writeObject(sDataS);
              	 sendObj = null;
        		 
        		 
        		 boolean notchecked = true; // - Client isChecked? - //
        		 movB = new Thread(movingBALL);
        		 while(true){
        			  
        			 // - Checking game situation - //
        			 if(playerS.getScoreP() >= max_Score || playerS.getScoreS()>= max_Score && Restart==false){
        				 
        				 if(playerS.getScoreS()>playerS.getScoreP()){        				 
        					 playerS.setOmessage("Won               Loss-Play Again: Press any key || Exit: Esc|N");
        					 playerS.setImessage("Won               Loss-Play again? ");
        					 Restart = true;
        				 }
        				 else{
        					 playerS.setImessage("Loss              Won-Play Again: Press any key || Exit: Esc|N");
        					 playerS.setOmessage("Loss              Won-Play Again: Press any key || Exit: Esc|N");
        					 Restart = true;
        					 }
                      	movB.suspend();	// - Stop the ball object - //
                    }
        			 
        			
        			 // - Check -> is client ready... //
            		 if(playerC.ok && notchecked){
            			 playerS.setImessage("");
                  	    movB.start();
                  	    notchecked = false;
                 	}
        			 
            		 // - Update Ball - //
            		 updateBall();
        			
            		 // - Creating Streams - //
        			//ObjectInputStream getObj = new ObjectInputStream(clientSoc.getInputStream());
        			playerC = (PlayerClient) PSecurity.AESDecryptStream(clientSoc.getInputStream());
					//playerC = (PlayerClient) getObj.readObject();
					//getObj = null;
        			//System.out.println(playerC);
            		 

					// - Send Object to Client - //
					//ObjectOutputStream sendObj = new ObjectOutputStream(clientSoc.getOutputStream());
					PSecurity.secretEncryptAndSend(playerS, clientSoc.getOutputStream());
					
                 	//sendObj.writeObject(playerS);
                 	//sendObj = null;

                 
                 	// - Check Restart Game - //
                 	if(restartON){
                 	
                 		if(playerC.restart){
            			playerS.setScoreP(0);
            			playerS.setScoreS(0);
            			playerS.setOmessage("");
            			playerS.setImessage("");
            			Restart = false;
                 		playerS.setRestart(false);
                 		playerS.setBallx(380);
                 		playerS.setBally(230);
                 		movingBALL.setX(380);
                 		movingBALL.setY(230);
                 		movB.resume();
                 		restartON = false;
                 		}
                 	}
                 	// - Repaint - //
                 	repaint();
                 	}
        	}
        	 else{
        		 System.out.println("Disconnected...");
        	 }
        }
        catch (Exception e) {
        	System.out.println(e);
        	try {
        		clientSoc.close();
				serverSoc.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	JOptionPane.showMessageDialog(this, e.getMessage(), "Error", 1);
        	this.parent.setVisible(true);
        	this.dispose();
        }
}
 

	  ///////////////
	 // - Paint - //
	///////////////
	
	private Image createImage(){
		
		// - BufferedImage Keep the Screen Frames - //
	    BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	    g = bufferedImage.createGraphics();
	    
	    // - Table - //
	    g.setColor(new Color(15,9,9));
	    g.fillRect(0, 0, WIDTH, HEIGHT);
	    
	    // - Lines - //
	    g.setColor(Color.white);
	    g.fillRect(WIDTH/2-2, 0, 4, HEIGHT);
	    //g.fillRect(WIDTH/2+5, 0, 5, HEIGHT);
	    
	    // - Score - //
	    g.setFont(sFont);
	    g.setColor(new Color(250,250,250));
	    g.drawString(""+playerS.getScoreS(), WIDTH/2-60, 120);
	    g.drawString(""+playerS.getScoreP(), WIDTH/2+20, 120);
	    
	    // - Player Names - //
	    g.setFont(nFont);
	    g.setColor(Color.white);
	    g.drawString(playerS.getName(),WIDTH/10,HEIGHT-20);
	    g.drawString(playerC.getName(),600,HEIGHT-20);
	    
	    // - Players - //
	    g.setColor(Color.white);
	    g.fillRect(playerS.getX(), playerS.getY(), barR, playerH);
	    g.setColor(Color.white);
	    g.fillRect(playerC.getX(), playerC.getY(), barR, playerH);
	    
	    // - Ball - //
	    g.setColor(Color.white);
	    g.fillOval(playerS.getBallx(), playerS.getBally(), ballRadius, ballRadius);
	    //g.setColor(new Color(228,38,36));
	    //g.fillOval(playerS.getBallx()+5, playerS.getBally()+5, ballRadius-10, ballRadius-10);
	    
	    // - Message - //
	    message = playerS.getImessage().split("-");
	    g.setFont(mFont);
	    g.setColor(Color.white);
	    if(message.length!=0){
	    g.drawString(message[0],WIDTH/2-g.getFontMetrics().stringWidth(message[0]),HEIGHT-90);
	    if(message.length>1){
	    	if(message[1].length()>6){
	    	    	g.setFont(rFont);
	    			g.setColor(Color.white);
	    			g.drawString(message[1],WIDTH/4-31,HEIGHT/2+100);
	    	}
	    }
	   }
	   return bufferedImage;
	}
	
	@Override
	public void paint(Graphics g){
		g.drawImage(createImage(), 0, 0, this);
	}
	
	  /////////////////////
	 // - Update Ball - //
	/////////////////////
	
	public void updateBall(){
		
		// - Checking collisions - //
		checkCol();
		
		// - update the ball - //
		playerS.setBallx(movingBALL.getX());
		playerS.setBally(movingBALL.getY());
		
	}
	
	  ///////////////////////
	 // - Update Player - //
	///////////////////////
	
	// - Move bar to Up - //
	public void playerUP(){
		if(playerS.getY() - mPLAYER > playerH/2-10){
			
			playerS.setY(playerS.getY()-mPLAYER);
		}
	}
	
	// - Move bar to Down - //
	public void playerDOWN(){
		if(playerS.getY() + mPLAYER < HEIGHT - playerH - 30){
			
			playerS.setY(playerS.getY()+mPLAYER);
		}
	}
	
	  /////////////////////////
	 // - Check Collision - //
	/////////////////////////
	
	public void checkCol(){
		
		
		// - Checking ball side, when a player got a score check -> false * if ball behind of the players check -> true
		if(playerS.getBallx() < playerC.getX() && playerS.getBallx() > playerS.getX()){
			check = true;
		}
		
		// - Server Player Score - //
		if(playerS.getBallx()>playerC.getX() && check){
			
			playerS.setScoreS(playerS.getScoreS()+1);
			
			check = false;
		}
		
		// - Client Player Score - //
		else if (playerS.getBallx()<=playerS.getX() && check){
			
			playerS.setScoreP(playerS.getScoreP()+1);
			
			check = false;
			
		}
		
		
		// - Checking Server Player Bar - //
		if(movingBALL.getX()<=playerS.getX()+barR && movingBALL.getY()+movingBALL.getRadius()>= playerS.getY() && movingBALL.getY()<=playerS.getY()+playerH ){
			movingBALL.setX(playerS.getX()+barR);
			playerS.setBallx(playerS.getX()+barR);
			movingBALL.setXv(movingBALL.getXv()*-1);
		}
		
		
		// - Checking Client Player Bar - //
		if(movingBALL.getX()+movingBALL.getRadius()>=playerC.getX() && movingBALL.getY() + movingBALL.getRadius() >= playerC.getY() && movingBALL.getY()<=playerC.getY()+playerH ){
			movingBALL.setX(playerC.getX()-movingBALL.getRadius());
			playerS.setBallx(playerC.getX()-movingBALL.getRadius());
			movingBALL.setXv(movingBALL.getXv()*-1);
		}
		
	}
	

	  /////////////////////
	 // - KeyListener - //
	/////////////////////

	@Override
	public void keyPressed(KeyEvent arg0) {
	
		// TODO Auto-generated method stub
		int keycode = arg0.getKeyCode();
		if(keycode == KeyEvent.VK_UP){
			playerUP();
			repaint();
		}
		if(keycode == KeyEvent.VK_DOWN){
			playerDOWN();
			repaint();
		}
		if(Restart == true){
			restartON = true;
			playerS.setRestart(true);
		}
		
		if(keycode == KeyEvent.VK_N || keycode == KeyEvent.VK_ESCAPE && Restart == true){
			try {
        		clientSoc.close();
				serverSoc.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	this.parent.setVisible(true);
        	this.dispose();
		}
	}

	
	@SuppressWarnings("deprecation")
	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
	
		Thread.currentThread().stop();
		this.setVisible(false);
		try {
			serverSoc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		System.exit(1);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		System.exit(1);
	}


	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
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
