package gr.server.common;



import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.junit.Test;  
public class QuickTest { 
	
	
	// Function to create a secret key
    public static SecretKey createAESKey()
        throws Exception
    {
 
        // Creating a new instance of
        // SecureRandom class.
        SecureRandom securerandom
            = new SecureRandom();
 
        // Passing the string to
        // KeyGenerator
        KeyGenerator keygenerator
            = KeyGenerator.getInstance("AES");
 
        // Initializing the KeyGenerator
        // with 256 bits.
        keygenerator.init(256, securerandom);
        SecretKey key = keygenerator.generateKey();
        return key;
    }
 
    // Driver code
    @Test
    public void test2()
        throws Exception
    {
        SecretKey Symmetrickey = createAESKey();
        System.out.println("The Symmetric Key is :"
                         + DatatypeConverter.printHexBinary(
                               Symmetrickey.getEncoded()));
        
        String encodedKey = Base64.getEncoder().encodeToString(Symmetrickey.getEncoded());
        
		byte[] encoded = Base64.getDecoder().decode(encodedKey);
		SecretKey originalKey = new SecretKeySpec( encoded, 0, encoded.length, "AES"); 
		
		 System.out.print("The Symmetric Key is :"
                 + DatatypeConverter.printHexBinary(
                		 originalKey.getEncoded()));
    }
	
	
    static Cipher cipher;  

//    @Test
    public void test() throws Exception {
        /* 
         create key 
         If we need to generate a new key use a KeyGenerator
         If we have existing plaintext key use a SecretKeyFactory
        */ 
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // block size is 128bits
        SecretKey secretKey = keyGenerator.generateKey();
        
        
        /*
          Cipher Info
          Algorithm : for the encryption of electronic data
          mode of operation : to avoid repeated blocks encrypt to the same values.
          padding: ensuring messages are the proper length necessary for certain ciphers 
          mode/padding are not used with stream cyphers.  
         */
        cipher = Cipher.getInstance("AES"); //SunJCE provider AES algorithm, mode(optional) and padding schema(optional)  

        String plainText = "AES Symmetric Encryption Decryption";
        System.out.println("Plain Text Before Encryption: " + plainText);

        String encryptedText = encrypt(plainText, secretKey);
        System.out.println("Encrypted Text After Encryption: " + encryptedText);

        String decryptedText = decrypt(encryptedText, secretKey);
        System.out.println("Decrypted Text After Decryption: " + decryptedText);
    }

    public static String encrypt(String plainText, SecretKey secretKey)
            throws Exception {
        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedText = encoder.encodeToString(encryptedByte);
        return encryptedText;
    }

    public static String decrypt(String encryptedText, SecretKey secretKey)
            throws Exception {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedTextByte = decoder.decode(encryptedText);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        String decryptedText = new String(decryptedByte);
        return decryptedText;
    }
}