import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Handles the encryption of passwords.
 * Password objects take in char arrays
 * as opposed to Strings for extra 
 * security. The password undergoes a
 * one-way encryption and the unencrypted
 * char array is immediately zeroed.
 * @author Cory Lynch
 */
public class Password
{
	public byte[] encrypted;
	public byte[] salt;

	/**
	 * Password constructor 1.
	 * An unencrypted password is 
	 * provided. A salt is generated
	 * and the password is encrypted.
	 */
	public Password(char[] password)
	{ 
		generateSalt();
		encrypt(password);
	}
	
	/**
	 * Password constructor 2.
	 * An unencrypted password and
	 * a salt are provided. The
	 * password is encrypted using
	 * the provided salt.
	 */
	public Password(char[] password, byte[] s)
	{
		salt = s;
		encrypt(password);
	}
	
	/**
	 * Password constructor 3.
	 * An encrypted password and
	 * salt are provided.
	 */
	public Password(byte[] encryptedPassword, byte[] s)
	{
		encrypted = encryptedPassword;
		salt = s;
	}
	
	/**
	 * Generates an 8 byte salt.
	 * A SecureRandom generator is used.
	 */
	public void generateSalt()
	{
		SecureRandom random = null;

		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e1) {}
		
		salt = new byte[8];
		random.nextBytes(salt);
	}
	
	/**
	 * Encrypts a password.
	 * The unencrypted password is
	 * zeroed immediately after usage
	 * for improved security.
	 */
	public void encrypt(char[] password)
	{
		KeySpec spec = new PBEKeySpec(password, salt, 10000, 160);
		Arrays.fill(password, '\0');
		
		SecretKeyFactory f = null;
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		} catch (NoSuchAlgorithmException e) {}

		try {
			encrypted = f.generateSecret(spec).getEncoded();
		} catch (InvalidKeySpecException e) {}
	}
	
	/**
	 * Compares two encrypted passwords.
	 */
	public boolean equals(Password pw)
	{
		return Arrays.equals(encrypted, pw.encrypted);
	}
}