import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Contains all the information necessary for one reservation.
 * It is sent from the client to the server in order to place
 * a reservation. Reservations implement Comparable, so they 
 * can be sorted, as shown in the Database class.
 * @see Database
 * @author Cory Lynch
 */
public class Reservation implements Serializable, Comparable<Reservation>
{
	private static final long serialVersionUID = 4991248902836711728L;
	public int month, date, year, period, roomID;
	public String name;
	
	public Reservation(Calendar cal, int p, int r, String n)
	{
		month = cal.get(Calendar.MONTH) + 1;
		date = cal.get(Calendar.DATE);
		year = cal.get(Calendar.YEAR) - 2000;
		period = p; roomID = r; name = n;
	}
	
	public Reservation(int m, int d, int y, int p, int r, String n)
	{
		month = m; date = d; year = y;
		period = p; roomID = r; name = n;
	}
	
	public int compareTo(Reservation r)
	{
		int dateCompare = (new GregorianCalendar(year+2000, month-1, date).compareTo(
								new GregorianCalendar(r.year+2000, r.month-1, r.date)));
		
		if(dateCompare != 0)
			return dateCompare;
		else if(period != r.period)
			return Integer.compare(period, r.period);
		else
			return Integer.compare(roomID, r.roomID);
	}
	
	public String toString()
	{
		return String.format("%d/%d/%d at %d, %d by %s", month,
				date, year, period, roomID, name);
	}
}
