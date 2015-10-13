/**
 * Represents an account.
 * Used in AccountManager to
 * store all existing accounts.
 * @author Cory Lynch
 */
public class Account 
{
	public String email;
	public byte[] encryptedPassword;
	public byte[] salt;
	
	public Account(String e, byte[] encrypted, byte[] s)
	{
		email = e;
		encryptedPassword = encrypted;
		salt = s;
	}
}
