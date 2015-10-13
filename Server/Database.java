import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;

/**
 * Manages a database of Reservations for the system
 * Upon initialization, it parses Reservations from
 * a text file. It handles any reservation additions
 * or removals, which are stored in a sorted ArrayList.
 * The ArrayList of Reservations is periodically saved
 * to the text file, which is called data.txt.
 * @author Cory Lynch
 */
public class Database 
{
	public static ArrayList<Reservation> entries;
	
	/**
	 * Initializes the database by reading from a text file.
	 * The file is parsed into an ArrayList of Reservations.
	 */
	public static void init() throws Exception
	{
		entries = new ArrayList<Reservation>();
		
		BufferedReader read =
				new BufferedReader(new FileReader("data.txt"));
		String line;
		while((line = read.readLine()) != null)
		{
			int month = Integer.parseInt(line.substring(0, line.indexOf('[')));
			int day = getInt(line, '[', '{');
			int year = getInt(line, '{', '|');
			int period = getInt(line, '|', '}');
			int roomID = getInt(line, '}', ']');
			String teacher = line.substring(line.indexOf(']') + 1);
			
			entries.add(new Reservation(month, day, year, period, roomID, teacher));
		}
		
		Server.println(entries.size() + " reservations loaded");
		read.close();
	}
	
	/**
	 * Saves the ArrayList of Reservations to the text file.
	 * It will also delete any Reservations that are outdated.
	 * This method is invoked whenever a Client disconnects.
	 */
	public static void save()
	{
		Calendar cal = Calendar.getInstance();
		
		int old = -search(new Reservation(cal, 0, -1, "")) - 1;
		
		for(int x = 0; x < old; x++)
			entries.remove(0);
		
		Formatter writer = null;
			
		try {
			writer = new Formatter("data.txt");
		} catch (FileNotFoundException e) { }
		
		for(Reservation r : entries)
		{
			writer.format("%d[%d{%d|%d}%d]%s\n", r.month, r.date,
					r.year, r.period, r.roomID, r.name);
		}
		
		writer.close();
	}
	
	/**
	 * Used in the init() method to help parse Reservations.
	 * @return An int parsed from a substring in String x 
	 * from the indices of char a to char b, exclusive.
	 * @see Database#init()
	 */
	private static int getInt(String x, char a, char b)
	{
		return Integer.parseInt(x.substring(x.indexOf(a) + 1, x.indexOf(b)));
	}
	
	/**
	 * If the spot is available, it adds a Reservation to the database.
	 * The Reservation is inserted into the ArrayList at
	 * its correct position in chronological order
	 * @return If the Reservation was successfully added
	 */
	public static boolean add(Reservation r)
	{
		int index = search(r);
		
		if(index >= 0)
			return false;
		
		index = -index - 1;
		entries.add(index, r);
		
		Server.println("Add: " + r);
		return true;
	}
	
	/**
	 * Adds a Reservation to the database, regardless of availability.
	 * This is only used by the admin, and will overwrite any 
	 * Reservations that conflict with it.
	 */
	public static void forceAdd(Reservation r) 
	{	
		int index = search(r);
		
		if(index >= 0)
		{
			MailManager.add(entries.get(index));
			entries.remove(index);
		}
		else
			index = -index - 1;
			
		entries.add(index, r);
		Server.println("Force Add: " + r);
	}
	
	/**
	 * Removes a reservation from the database
	 */
	public static void remove(Reservation r)
	{		
		entries.remove(search(r));
		Server.println("Remove: " + r);
	}
	
	/**
	 * Binary search to find a Reservation in the database.
	 * Because the database is sorted, and because Reservations
	 * implement Comparable, a binary search can be used.
	 * If it is not found, it returns -(insertionPoint + 1)
	 */
	public static int search(Reservation target)
	{
		int low = 0, high = entries.size();
		
	    while (low < high) 
	    {
	        int mid = (low + high) / 2; 
	        if (target.compareTo(entries.get(mid)) < 0)
	            high = mid;
	        else if (target.compareTo(entries.get(mid)) > 0) 
	            low = mid + 1;  
	         else 
	            return mid;   
	    }
	    return -(low + 1);   
	}
}
