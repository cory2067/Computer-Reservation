import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Handles sending notifications to teachers via email.
 * Specifically sends messages when a Reservation
 * gets cancelled by the administrator.
 * @author Cory Lynch
 */
public class MailManager
{
	private static Session session;
	private static final String email = "computer.reservation@gmail.com";
	private static ArrayList<Reservation> reservations;
	
	/**
	 * Initializes mail settings
	 */
	public static void init()
	{
		reservations = new ArrayList<Reservation>();
		
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(email, "GHSCompRes2013");
			}
		});
	}
	
	/**
	 * Sends all the messages in the queue.
	 * Invoked when an admin disconnects.
	 */
	public static void sendAll()
	{	
		if(reservations.size() > 0)
		{
			ArrayList<Reservation> rlist = new ArrayList<Reservation>();
			String current = reservations.get(0).name;
			
			for(Reservation r : reservations)
			{
				System.out.println("a"+r.name);
				System.out.println("b"+current);
				System.out.println(r.name == current);
				if(r.name == current)	
					rlist.add(r);
				else
				{
					send(rlist);
					
					rlist = new ArrayList<Reservation>();
					rlist.add(r);
					current = r.name;
				}
			}
			
			send(rlist);
			reservations = new ArrayList<Reservation>();
		}
	}
	
	/**
	 * Sends a list of reservations in one message.
	 */
	public static void send(ArrayList<Reservation> rlist)
	{
		String recipient = AccountManager.getEmail(rlist.get(0).name);
		if(recipient == null)
			return;
					
		String msg = "The administrator has removed the following reservation(s):\n";
		for(Reservation r : rlist)
			msg += formatReservation(r) + "\n";
		msg += "\nFor help with using the computer reservation tool, send an email to " +
			   "cjl2625@gmail.com";
		
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email));

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
			message.setSubject("Your computer reservation(s) have been removed");
			message.setText(msg);

			Transport.send(message);
			Server.println("Email sent to " + recipient);

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}   
	
	/**
	 * Adds a reservation to the queue
	 */
	public static void add(Reservation r)
	{
		reservations.add(search(r), r);
	}
	
	
	/**
	 * Finds the insertion point for a Reservation.
	 * Searches through the queue (called "reservations")
	 */
	public static int search(Reservation r)
	{
		int low = 0, high = reservations.size();
		String target = r.name;
		
	    while (low < high) 
	    {
	        int mid = (low + high) / 2; 
	        if (target.compareTo(reservations.get(mid).name) < 0)
	            high = mid;
	        else if (target.compareTo(reservations.get(mid).name) > 0) 
	            low = mid + 1;  
	         else 
	            return mid;   
	    }
	    return low;   
	}
	
	
	public static final int[][] daySchedule = {
		{1, 2, 3, 4, 5, 6, 0},
		{1, 2, 3, 4, 7, 8, 0},
		{1, 2, 5, 6, 7, 8, 0},
		{3, 4, 5, 6, 7, 8, 0}
	};

	private static final String roomInfo[] = {
		"in the ITC computer lab", "in room 6", "in room 50", 
		"with the science laptop cart", "with the social studies laptop cart", 
		"with the english laptop cart", "in the ITC for research", "in the ITC for research"};
	
	public static String formatReservation(Reservation r)
	{
		String str = r.toString();
		String formatted = str.substring(0, str.indexOf(" "));
		int period = daySchedule[SchoolCalendar.dates[r.month - 1][r.date - 1] - 'a'][r.period];
		formatted += (period == 0 ? " after school" : " during period " + period) 
				+ " " + roomInfo[r.roomID];
		
		return formatted;
	}
}
