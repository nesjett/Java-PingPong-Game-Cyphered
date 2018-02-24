import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class PongSecurity {
	private byte[] secret; // Secret key unEncrypted
	private String transformation = "AES";
	private PublicKey PublicKey;
	private PrivateKey PrivateKey;
	
	private static String PublicKeyPath = "pubkey.txt";
	private static String PrivateKeyPath = "privkey.txt";
	
	
	public PongSecurity(byte[] key){
		key = key;
		initKeyPair();
	}
	
	public PongSecurity(){
		generateNewSecert();
		initKeyPair();
	}
	
	public byte[] getKey(){
		return secret;
	}
	
	public byte[] renewKey(){
		generateNewSecert();
		return secret;
	}
	
	public void setSecret(byte[] s){
		this.secret = s;
	}
	
	public PublicKey getPublic(){
		return PublicKey;
	}
	
	public PrivateKey getPrivate(){
		return PrivateKey;
	}

	
	private void generateNewSecert(){
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance(transformation);
			keyGen.init(128); // for example
			SecretKey secretKey = keyGen.generateKey();
			secret = secretKey.getEncoded();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/***
	 * Encripts serializable object with AES key (secret key) and sends it over the network with output stream
	 * @param object serializable object to encrypt
	 * @param ostream socket output stream
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	 public void secretEncryptAndSend(Serializable object, OutputStream ostream) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
	        try {
	            // Length is 16 byte
	            SecretKeySpec sks = new SecretKeySpec(secret, transformation);

	            // Create cipher
	            Cipher cipher = Cipher.getInstance(transformation);
	            cipher.init(Cipher.ENCRYPT_MODE, sks);
	            SealedObject sealedObject = new SealedObject(object, cipher);

	            // Wrap the output stream
	            //CipherOutputStream cos = new CipherOutputStream(ostream, cipher);
	            ObjectOutputStream outputStream = new ObjectOutputStream(ostream);
	            outputStream.writeObject(sealedObject);
	            //outputStream.close();
	        } catch (IllegalBlockSizeException e) {
	            e.printStackTrace();
	        }
	    }

	 /***
	  * Decrypts serializable object with AES key (secret key) got from network socket (input stream)
	  * @param istream input stream to decrypt
	  * @return SealedObject (deserialized object from network stream)
	  * @throws IOException
	  * @throws NoSuchAlgorithmException
	  * @throws NoSuchPaddingException
	  * @throws InvalidKeyException
	  */
	    public Object AESDecryptStream(InputStream istream) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
	        SecretKeySpec sks = new SecretKeySpec(secret, transformation);
	        Cipher cipher = Cipher.getInstance(transformation);
	        cipher.init(Cipher.DECRYPT_MODE, sks);

	        //CipherInputStream cipherInputStream = new CipherInputStream(istream, cipher);
	        ObjectInputStream inputStream = new ObjectInputStream(istream);
	        SealedObject sealedObject;
	        try {
	            sealedObject = (SealedObject) inputStream.readObject();
	            return sealedObject.getObject(cipher);
	        } catch (ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
	    
	    
	    /***
	     * Generates a new RSA Key pair and stores it as byte[] files under the same directory, with debug options
	     * @throws NoSuchAlgorithmException
	     * @throws NoSuchProviderException
	     * @throws IOException 
	     */
	    public static void generateKeyPair(boolean debug) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
	        keyGen.initialize(1024, new SecureRandom());
	        KeyPair keys = keyGen.genKeyPair();
	        byte[] publicKey = keys.getPublic().getEncoded();
	        byte[] privateKey = keys.getPrivate().getEncoded();
	        
	        if(debug){
		        StringBuffer retString = new StringBuffer();
		        retString.append("[");
		        for (int i = 0; i < publicKey.length; ++i) {
		            retString.append(publicKey[i]);
		            retString.append(", ");
		        }
		        retString = retString.delete(retString.length()-2,retString.length());
		        retString.append("]");
		        System.out.println(retString); //e.g. [48, 92, 48, .... , 0, 1]
	        }
	        
	        File pubFile, privFile;
	        FileOutputStream fop = null;
	        
	        pubFile = new File(PublicKeyPath);
	        privFile = new File(PrivateKeyPath);
	        
	       
        	fop = new FileOutputStream(pubFile);
			fop.write( publicKey );
			fop.close();
			fop = new FileOutputStream(privFile);
			fop.write( privateKey );
			fop.close();
	    }
	    
	    /***
	     * Generates a new RSA Key pair and stores it as byte[] files under the same directory
	     * @throws NoSuchAlgorithmException
	     * @throws NoSuchProviderException
	     * @throws IOException 
	     */
	    public void generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
	        keyGen.initialize(1024, new SecureRandom());
	        KeyPair keys = keyGen.genKeyPair();
	        byte[] publicKey = keys.getPublic().getEncoded();
	        byte[] privateKey = keys.getPrivate().getEncoded();

	        File pubFile, privFile;
	        FileOutputStream fop = null;
	        
	        pubFile = new File(PublicKeyPath);
	        privFile = new File(PrivateKeyPath);

        	fop = new FileOutputStream(pubFile);
			fop.write( publicKey );
			fop.close();
			fop = new FileOutputStream(privFile);
			fop.write( privateKey );
			fop.close();
	    }
	    
	    
	    private void readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{
	    	byte[] keyBytes = Files.readAllBytes(Paths.get(PrivateKeyPath));

	        PKCS8EncodedKeySpec spec =
	          new PKCS8EncodedKeySpec(keyBytes);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        PrivateKey = kf.generatePrivate(spec);
	    }
	    
	    private void readPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{
	    	byte[] keyBytes = Files.readAllBytes(Paths.get(PublicKeyPath));

	        X509EncodedKeySpec spec =
	          new X509EncodedKeySpec(keyBytes);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        PublicKey = kf.generatePublic(spec);
	    }
	    
	    private void initKeyPair(){
	    	try {
				File f1 = new File(PublicKeyPath);
				File f2 = new File(PrivateKeyPath);
				if(f1.exists() && !f2.isDirectory() && f2.exists() && !f2.isDirectory()){
					readPrivateKey();
					readPublicKey();
				}else{
					generateKeyPair();
				}
			} catch (NoSuchAlgorithmException | InvalidKeySpecException
					| IOException | NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    
	    /***
	     * Returns a PublicKey object from a byte[]
	     * @param p Public key in byte[]
	     * @return PublicKey object
	     * @throws InvalidKeySpecException
	     * @throws NoSuchAlgorithmException
	     */
	    public static PublicKey getPublicFromBytes(byte[] p) throws InvalidKeySpecException, NoSuchAlgorithmException{
	    	PublicKey publicKey = 
				    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(p));
	    	return publicKey;
	    }
	    
	    /***
	     * Returns a PrivateKey object from a byte[]
	     * @param p Private key in byte[]
	     * @return PrivateKey object
	     * @throws InvalidKeySpecException
	     * @throws NoSuchAlgorithmException
	     */
	    public static PrivateKey getPrivateFromBytes(byte[] p) throws InvalidKeySpecException, NoSuchAlgorithmException{
	    	PrivateKey privateKey = 
				    KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(p));
	    	return privateKey;
	    }
	    
	    
	    /***
	     * Encripts a byte[] message with an RSA PublicKey
	     * @param message Message to encript
	     * @param publicKey RSA key to encript the message
	     * @return String with the decripted message
	     * @throws Exception
	     */
	    public static String encryptWithPublicKey(byte[] message, PublicKey publicKey) throws Exception {
	        PublicKey apiPublicKey = publicKey;
	        Cipher rsaCipher = Cipher.getInstance("RSA");
	        rsaCipher.init(Cipher.ENCRYPT_MODE, apiPublicKey);
	        byte[] encVal = rsaCipher.doFinal(message);
	        String encryptedValue = new BASE64Encoder().encode(encVal);
	        return encryptedValue;
	    }

	    /***
	     * Decripts a byte[] message with an RSA PrivateKey
	     * @param message Message to decript
	     * @param privateKey RSA key to decript the message
	     * @return String with the decripted message
	     * @throws Exception
	     */
	    public static byte[] decryptWithPrivateKey(String message, PrivateKey privateKey) throws Exception {
	        PrivateKey pKey = privateKey;
	        Cipher rsaCipher = Cipher.getInstance("RSA");
	        rsaCipher.init(Cipher.DECRYPT_MODE, pKey);
	        byte[] decVal = rsaCipher.doFinal(new BASE64Decoder().decodeBuffer(message));
	        return decVal;
	    }
	    
	    
	    public SignedObject signObject(String message) throws InvalidKeyException, SignatureException, IOException, NoSuchAlgorithmException{
	    	// We can sign Serializable objects only
	    	Signature signature = Signature.getInstance("SHA1with" + PrivateKey.getAlgorithm()); // SHA1withRSA
	    	
	    	//signature.initSign(PrivateKey, new SecureRandom());
	    	SignedObject signedObject = new SignedObject(message, PrivateKey, signature);

	    	return signedObject;
	    }
	    
	    public String unsignObject(SignedObject object) throws NoSuchAlgorithmException, ClassNotFoundException, IOException, InvalidKeyException, SignatureException{
	    	// Verify the signed object
    	    Signature sig = Signature.getInstance("SHA1with" + PublicKey.getAlgorithm());
    	    boolean verified = object.verify(PublicKey, sig);
    	    
    	    // Retrieve the object
    	    if(verified)
    	    	return (String) object.getObject();
    	    else
    	    	return null;
	    }
	
}
