import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The main Server class which listens for Clients.
 * This class provides simple a GUI for the server.
 * It initializes the databases and connections,
 * and then listens for Clients. When a Client 
 * connects, a ServerThread is instantiated to
 * handle the interactions with the user.
 * @see ServerThread
 * @version 1.0.1
 * @author Cory Lynch
 */
public class Server
{
	public static ServerSocket serverSocket;
	private static JTextArea console;
	
	/**
	 * Listens for Clients that attempt to connect.
	 * The full behavior of this method is described
	 * in the class description above.
	 */
	public static void main(String[] args)
	{
		JFrame server = new JFrame("Server");
		server.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		server.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				close();
			}});
		console = new JTextArea();
		server.add(new JScrollPane(console));
		server.setSize(400, 225);
		server.setVisible(true);
	
		try{
			Database.init();
			SchoolCalendar.init();
			AccountManager.init();
			MailManager.init();
		} catch(Exception e) {
			println("Could not load files");
			return;
		}
		   
		try {
		    serverSocket = new ServerSocket(27647);
		} catch (IOException e) {
		    println("Could not listen on port 27647");
		    return;
		}
		
		println("Server running");
		
		while(true)
		{  
			Socket clientSocket = null;
			
			try{
				clientSocket = serverSocket.accept();
			} catch(Exception e) {
				println("Could not connect to client"); 
			}
			
		    ServerThread serv = new ServerThread(clientSocket);
		    serv.start();  
		}
	}
	
	/**
	 * Prints a string to the GUI console.
	 */
	public static void println(String s)
	{
		console.append(s + "\n");
		console.setCaretPosition(console.getDocument().getLength());
	}
	
	/**
	 * Closes the serverSocket before exiting
	 */
	public static void close()
	{	
		try {
			serverSocket.close();
		} catch (IOException e) { }
	}
}
