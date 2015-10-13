import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Manages a calendar of the whole schoolyear.
 * This calendar takes all holidays into account,
 * which are read from a text file. It also
 * calculates if each day is an a/b/c/d day.
 * @author Cory Lynch
 */
public class SchoolCalendar
{
	public static char[][] dates;
	public static int year;
	
	/**
	 * Initializes the SchoolCalendar.
	 * Holidays are read from a text file and
	 * then for every day of the year, it 
	 * calculates if it is an a/b/c/d day.
	 */
	public static void init() throws Exception
	{
		Calendar a = Calendar.getInstance();
		year = a.get(Calendar.MONTH) > 5 ? a.get(Calendar.YEAR) + 1 : a.get(Calendar.YEAR);
		dates = new char[12][31];
		
		BufferedReader read =
				new BufferedReader(new FileReader(year + ".txt"));
		
		String line = read.readLine();
		int month = Integer.parseInt(line.substring(0, line.indexOf('/')));
		int day = Integer.parseInt(line.substring(line.indexOf('/') + 1));
		char c = 'a';
		boolean get = true;
		int m = 0, d = 0, m2 = 0, d2 = 0;
		
		Calendar cal = new GregorianCalendar(year - 1, month - 1, day);
		
		while(!(cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) > 5))
		{
			m = cal.get(Calendar.MONTH) + 1;
			d = cal.get(Calendar.DATE);
			
			if(get)
			{
				if((line = read.readLine()) != null)
				{
					m2 = Integer.parseInt(line.substring(0, line.indexOf('/')));
					d2 = Integer.parseInt(line.substring(line.indexOf('/') + 1));
				}
				else
					m2 = -1;
				get = false;
			}
			
			if(cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
			{
				if(!(m == m2 && d == d2))
				{
					dates[m - 1][d - 1] = c;
				
					c++;
					if(c > 'd')
						c = 'a';
				}
				else
					get = true;
			}
			
			cal.add(Calendar.DATE, 1);
		}
		
		read.close();
	}
}