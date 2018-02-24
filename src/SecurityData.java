import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignedObject;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;


public class SecurityData implements Serializable {
	 	
	/**
	 * - Security Data -
	 */
	
	
	  //////////////////////////
	 // - Client Variables - //
	//////////////////////////
	
	private SignedObject secretKeyEncripted;
	private PublicKey publicKey;
    	
	public SecurityData(){}
	
	  ///////////////////////////
	 // - Getters & Setters - //
	///////////////////////////
	

	public SignedObject getSecret() {
		return secretKeyEncripted;
	}

	public void setSecret(SignedObject s) {
		this.secretKeyEncripted = s;
	}

	
	public PublicKey getPublic() {
		return publicKey;
	}

	public void setPublic(PublicKey p) {
		this.publicKey = p;
	}
	
	public void setPublic(byte[] p) throws InvalidKeySpecException, NoSuchAlgorithmException {
		this.publicKey = 
			    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(p));
	}

	@Override
	public String toString() {
		return "SecurityData [SecretEncripted=" + secretKeyEncripted + ", PublicKey=" + publicKey + "]";
	}
}
