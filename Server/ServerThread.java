import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Handles all the interactions with a Client.
 * An instance is created for each Client that 
 * connects. It parses messages and objects that
 * are sent by the Client and acts accordingly.
 * @author Cory Lynch
 */
public class ServerThread extends Thread
{
	private boolean isAdmin;
	private Socket socket;
	private String name;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public ServerThread(Socket s)
	{	
		super("Server");
		socket = s;
	}

	/**
	 * Constantly listens for Client interactions.
	 * When an object is received from the Client,
	 * the parseObject method is invoked. If the
	 * Client sends "end", the loop breaks and the
	 * sockets and streams are closed. If the client
	 * was an admin, then send any mail messages.
	 */
	public void run() 
	{
		try{
			name = socket.getInetAddress().getHostName();
		} catch(NullPointerException e) {
			return;
		}
		
		isAdmin = false;
		
		Server.println("Connected to " + name);
		
		initializeStreams();
		checkUpdated();
			
		while(true)
		{
			try{
				Object obj = receiveObject();
				if(obj.equals("end"))
					break;
				else
					parseObject(obj);
			} catch(Exception e) {
				Server.println("Error proccesing request: " + e.getMessage());
				e.printStackTrace();
				break;
			}
		}
		
		closeConnection();
		Server.println("Disconnected from " + name);
		Database.save();
		
		if(isAdmin)
			MailManager.sendAll();
	}

	/**
	 * Proccesses an object sent by the Client.
	 * If a Reservation is sent, add it to the database.
	 * If this Reservation is sent by admin, then force
	 * add it. If a Calendar is sent, return a String[][]
	 * indicating which slots are filled and by whom.
	 * If "remove" is sent, remove a specified object from
	 * the database. If "calendar" (a String) is sent, then
	 * return the calendar for the schoolyear. If "login"
	 * is sent, a user is trying to log in. If "signup" is
	 * sent, a user is trying to create a new account.
	 */
	private void parseObject(Object obj) 
	{
		if(obj instanceof Reservation)
		{
			Reservation r = (Reservation) obj;
			
			if(!r.name.equals("admin"))
				sendObject(Database.add(r));
			else
				Database.forceAdd(r);
		}
		else if(obj instanceof Calendar)
		{
			Calendar date = (Calendar) obj;
			String[][] names = new String[7][8];
			
			int min = -Database.search(new Reservation(date, 0, -1, "")) - 1;
			int max = -Database.search(new Reservation(date, 6, 8, "")) - 1;

			for(int x = min; x < max; x++)
			{
				Reservation r = Database.entries.get(x);
				names[r.period][r.roomID] = r.name;
			}
			
			sendObject(names);
		}
		else 
		{
			String message = (String) obj;
			
			if(message.equals("remove"))
				Database.remove((Reservation) receiveObject());
			else if(message.equals("calendar"))
				sendObject(SchoolCalendar.dates);
			else if(message.equals("login"))
			{
				String email = (String) receiveObject();
				Account a = AccountManager.getAccount(email);
				if(a == null)
				{
					sendObject("failure");
					return;
				}
				sendObject("success");
				
				sendObject(a.salt);
				if(!Arrays.equals((byte[]) receiveObject(), a.encryptedPassword))
				{
					sendObject("failure");
					return;
				}
				
				sendObject("success");
				
				if(email.equals("admin"))
				{
					isAdmin = true;
					Server.println(name + " has logged in as admin");
				}
				else
					Server.println(name + " has logged in as " +
							email.substring(0, email.indexOf('@')));
			}
			else if(message.equals("signup"))
			{
				String email = (String) receiveObject();
				byte[] encryptedPassword = (byte[]) receiveObject();
				byte[] salt = (byte[]) receiveObject();
				
				sendObject(AccountManager.registrationCodeSalt);
				byte[] registrationCode = (byte[]) receiveObject();
				
				sendObject(AccountManager.add(new Account(
						email, encryptedPassword, salt), registrationCode));
			}
		}
	}

	/**
	 * Sends an object to the Client.
	 */
	private void sendObject(Object obj)
	{
		try{
			out.writeObject(obj);
			out.flush();
		} catch(IOException e) {
			Server.println("Could not write to the stream");
		}
	}

	/**
	 * Receives and object from the Client
	 */
	private Object receiveObject()
	{
		Object obj = null;
		
		try{
			obj = in.readObject();
		} catch(Exception e) {
			Server.println("Could not read from the stream");
		}
		
		return obj;
	}
	
	/**
	 * Initializes the output and input streams.
	 */
	private void initializeStreams()
	{
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());
		} catch(Exception e) {
			System.out.println("Could not initialize streams");
		}
	}
	
	/**
	 * Checks if the SchoolCalendar is updated.
	 * If it is not, then invoke its init() method.
	 * @see SchoolCalendar#init()
	 */
	private void checkUpdated() 
	{
		Calendar a = Calendar.getInstance();
		int year = a.get(Calendar.MONTH) > 5 ? a.get(Calendar.YEAR) + 1 : a.get(Calendar.YEAR);
		
		if(SchoolCalendar.year != year)
		{
			try {
				SchoolCalendar.init();
			} catch(Exception e) {}
		}
	}
	
	/**
	 * Closes the streams and disconnects with the Client.
	 */
	private void closeConnection()
	{
		try {
			out.close();
			in.close();
			socket.close();
		} catch(Exception e) {
			System.out.println("Could not close connection");
		}
	}
}
