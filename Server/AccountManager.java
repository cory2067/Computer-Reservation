import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Manages accounts.
 * Handles the creation of accounts
 * and logging in to existing accounts.
 * @author Cory Lynch
 */
public class AccountManager 
{
	private static ArrayList<Account> accounts;
	private static byte[] registrationCode;
	public static byte[] registrationCodeSalt;
	
	/**
	 * Initializes account database.
	 * Reads accounts from accounts.txt and puts
	 * them into an ArrayList. Also reads the
	 * registrationCode and registrationCodeSalt
	 * and stores them in the appropriate fields.
	 */
	public static void init() throws Exception
	{
		accounts = new ArrayList<Account>();
		
		BufferedReader read =
				new BufferedReader(new FileReader("accounts.txt"));
		String line;
		
		boolean firstRun = true;
		while((line = read.readLine()) != null)
		{
			String email = line.substring(0, line.indexOf(':'));
			byte[] encrypted = parseBytes(line.substring(line.indexOf(':') + 1, line.indexOf('|')));
			byte[] salt = parseBytes(line.substring(line.indexOf("|") + 1));
			
			if(firstRun)
			{
				registrationCode = encrypted;
				registrationCodeSalt = salt;
				firstRun = false;
			}
			else	
				accounts.add(new Account(email, encrypted, salt));
		}
		
		Server.println(accounts.size() + " accounts loaded");
		read.close();
	}
	
	/**
	 * Parses an array of bytes from a String.
	 */
	private static byte[] parseBytes(String a)
	{
		String[] stringBytes = a.substring(1, a.length() - 1).split(",");
		byte[] bytes = new byte[stringBytes.length];
		
		for (int x = 0; x < bytes.length; x++) 
		{
			bytes[x] = Byte.parseByte(stringBytes[x].trim());     
		}
		
		return bytes;
	}
	
	/**
	 * Finds the account associated with an email.
	 */
	public static Account getAccount(String email)
	{
		int index = search(email);
		
		if(index < 0)
			return null;
		
		return accounts.get(index);
	}
	
	/**
	 * Registers an account
	 */
	public static String add(Account a, byte[] regCode)
	{
		if(!Arrays.equals(regCode, registrationCode))
			return "Incorrect registration code";
					
		if(a.email.indexOf('@') == -1)
			return "Malformed email address";
		
		int index = -search(a.email) - 1;
		if(index < 0)
			return "Account already exists";
		
		accounts.add(index, a);
		save();
		
		return "success";
		
	}
	
	/**
	 * Saves the ArrayList of accounts
	 * The accounts are written to account.txt.
	 * Invoked when a new account is created.
	 */
	public static void save()
	{
		Formatter writer = null;
			
		try {
			writer = new Formatter("accounts.txt");
		} catch (FileNotFoundException e) { }
		
		writer.format("RegistrationCode:%s|%s\n", Arrays.toString(registrationCode),
				Arrays.toString(registrationCodeSalt));
		
		for(Account a : accounts)
		{
			writer.format("%s:%s|%s\n", a.email,
					Arrays.toString(a.encryptedPassword), Arrays.toString(a.salt));
		}
		
		writer.close();
	}
	
	/**
	 * Binary search to find an account in the database.
	 * If it is not found, it returns -(insertionPoint + 1)
	 */
	public static int search(String target)
	{
		int low = 0, high = accounts.size();
		
	    while (low < high) 
	    {
	        int mid = (low + high) / 2; 
	        if (target.compareTo(accounts.get(mid).email) < 0)
	            high = mid;
	        else if (target.compareTo(accounts.get(mid).email) > 0) 
	            low = mid + 1;  
	         else 
	            return mid;   
	    }
	    return -(low + 1);   
	}

	/**
	 * Finds the email associated with a given username.
	 */
	public static String getEmail(String name)
	{
		Account a = accounts.get(-search(name) - 1);
		
		if(a.email.startsWith(name))
			return a.email;
		else
			return null;
	}
}