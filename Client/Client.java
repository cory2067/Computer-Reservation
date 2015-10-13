import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * This is the main class run by the user.
 * It provides a graphical environment for the user
 * to submit desired Reservations to the server. Each
 * Reservation is represented by a ReservationSlot which
 * contains a JButton to make or remove a Reservation.
 * @see ReservationSlot
 * @version 1.1
 * @author Cory Lynch
 */
public class Client
{
	public static ObjectInputStream in;
	public static ObjectOutputStream out;
	public static Socket socket;
	public static Calendar date;
	public static Calendar today;
	public static String name; 
	public static ReservationSlot[][] slots;
	public static JLabel[] periods;
	public static char[][] calendar;
	public static JTextField emailField, changeEmailField, signupEmailField;
	public static JPasswordField signupPasswordField, codeField, passwordField;
	public static JFrame main, menu, signup;
	public static JLabel menuStatus, dateLabel;
	public static boolean userReady = false;

	public static final int[][] daySchedule = {
		{1, 2, 3, 4, 5, 6},
		{1, 2, 3, 4, 7, 8},
		{1, 2, 5, 6, 7, 8},
		{3, 4, 5, 6, 7, 8}
	};
	
	/**
	 * Initializes the connections and GUIs for the Client.
	 * It first shows the menu GUI where the teacher logs in.
	 * This is later replaced with the main GUI containing
	 * the grid of ReservationSlots.
	 */
	public static void main(String[] args)
	{
		initMenuGUI(); 
		menu.setVisible(true);	
		
		connect("reservecomp.zapto.org");
		initMainGUI();
	}

	/**
	 * Builds the menu GUI.
	 * In the menu GUI, the user can log in,
	 * calling the login() method, or he can
	 * sign up, opening the signup GUI.
	 * @see login()
	 * @see initSignupGUI()
	 */
	public static void initMenuGUI()
	{
		menu = new JFrame("Reserve");
		menu.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		menu.setLayout(new GridLayout(8, 1));
		menu.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) { 
				exit();
			}});
		menuStatus = new JLabel("Enter email and password", JLabel.CENTER);
		emailField = new JTextField(16);
		passwordField = new JPasswordField(16);
		JButton login = new JButton("Log in");
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				login();
			}});
		JButton signup = new JButton("Create account");
		signup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initSignupGUI();
			}});
		JLabel title = new JLabel("Computer Reservation", JLabel.CENTER);
		title.setFont(new Font("Arial", Font.BOLD, 16));
		JLabel author = new JLabel("Cory Lynch 2013", JLabel.CENTER);
		author.setFont(new Font("Arial", Font.PLAIN, 10));
		menu.add(title);
		menu.add(author);
		menu.add(new JLabel());	
		menu.add(menuStatus);
		menu.add(emailField);
		menu.add(passwordField);
		menu.add(login);
		menu.add(signup);
		menu.pack();
	}
	
	/**
	 * Called when a user clicks "Log In" on the menu GUI.
	 * The password is encrypted and stored as a Password 
	 * object. If a valid email is entered, and the password
	 * is authenticated, proceed to the main GUI.
	 * @see Password
	 */
	public static void login()
	{
		String e = emailField.getText();
		
		if(e.equals(""))
			return;
		
		if(e.equals("admin"))
			name = "admin";
		else if(e.indexOf('@') > 0)
			name = e.substring(0, e.indexOf('@'));
		else
			return;
		
		menuStatus.setText("Please wait...");
		
		while(in == null);
		
		sendObject("login");
		sendObject(e);
		
		if(((String) receiveObject()).equals("failure"))
		{
			printErr(menu, "Login Error", "Account doesn't exist");
			menuStatus.setText("Enter email and password");
			return;
		}
		
		byte[] salt = (byte[]) receiveObject();
		Password p = new Password(passwordField.getPassword(), salt);
		sendObject(p.encrypted);
		
		if(((String) receiveObject()).equals("failure"))
		{
			printErr(menu, "Login Error", "Incorrect password");
			menuStatus.setText("Enter email and password");
			return;
		}
		
		update(0);
		menu.setVisible(false);
		
		if(name.equals("admin"))
		{
			final Object[] options = {"Log in as teacher", "Block times"};
			int s = JOptionPane.showOptionDialog(null, "Admin Options:\nLog in as any other " +
					"teacher or block reservation times", "Admin", JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			
			if(s == 0)
				initNameChangeGUI();
			else
				AdminClient.run();
		}
		else
			main.setVisible(true);
	}
	
	/**
	 * Initiallizes the GUI used for signup.
	 * The user enters an email and password,
	 * and also a registration code provided
	 * only to teachers. When the user clicks
	 * "Submit," signup() is called.
	 * @see signup()
	 */
	public static void initSignupGUI()
	{
		signup = new JFrame("Signup");
		signup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		signup.setLayout(new GridLayout(7, 1));
		signup.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) { 
				exit();
			}});
		signupEmailField = new JTextField(16);
		signupPasswordField = new JPasswordField(16);
		codeField = new JPasswordField(16);
		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				signup();
			}});
		signup.add(new JLabel("Enter your email"));
		signup.add(signupEmailField);
		signup.add(new JLabel("Enter your password"));	
		signup.add(signupPasswordField);
		signup.add(new JLabel("Enter registration code"));
		signup.add(codeField);
		signup.add(submit);
		signup.pack();
		
		signup.setVisible(true);
		menu.setVisible(false);
	}
	
	/**
	 * Used by admin to change name to any teacher
	 * Used to place reservations for other teachers.
	 */
	public static void initNameChangeGUI()
	{
		JFrame nameChange;
		nameChange = new JFrame("Name");
		nameChange.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		nameChange.setLayout(new GridLayout(3, 1));
		nameChange.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) { 
				exit();
			}});

		changeEmailField = new JTextField(16);
		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeName();
			}});
		
		nameChange.add(new JLabel("Enter a teacher's email"));
		nameChange.add(changeEmailField);
		nameChange.add(submit);
		
		nameChange.pack();
		nameChange.setVisible(true);
	}
	
	/**
	 * Changes name of the user.
	 * Uses the value of changeEmailField.
	 */
	public static void changeName()
	{
		String email = changeEmailField.getText();
		if(email.indexOf('@') == -1)
			return;
		
		name = email.substring(0, email.indexOf('@'));
		update(0);
		
		if(!main.isVisible())
			main.setVisible(true);
	}
	
	/**
	 * Called when a user submits signup information.
	 * The password is stored as a Password object
	 * and encrypted. After sthe Server verifies that
	 * the email, password, and registration code are
	 * valid, it stores the email/password in a database
	 * and the user can log in.
	 * @see Password
	 */
	public static void signup()
	{
		while(in == null);
		
		Password newPass = new Password(signupPasswordField.getPassword());
		
		String email = signupEmailField.getText();
		
		sendObject("signup");
		sendObject(email);
		sendObject(newPass.encrypted);
		sendObject(newPass.salt);
		
		byte[] salt = (byte[]) receiveObject();
		Password code = new Password(codeField.getPassword(), salt);
		sendObject(code.encrypted);
		
		String msg = (String) receiveObject();
		if(!msg.equals("success"))
		{
			printErr(signup, "Signup Error", msg);
			return;
		}
       
		JOptionPane.showMessageDialog(signup, "Success. You can now log in.");
		signup.setVisible(false);
		menu.setVisible(true);
	}
	
	/**
	 * Builds the main GUI of the Client.
	 * The main component of this GUI is
	 * a grid of ReservationSlots.
	 */
	public static void initMainGUI()
	{
		main = new JFrame("Computer Reservation");
		main.setSize(1300, 700);
		main.setLayout(new GridLayout(8, 9));
		main.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		main.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) 
			{ 
				int r = JOptionPane.showConfirmDialog(main, "Exit the program? Your reservations will be saved.", 
						"Exit?", JOptionPane.YES_NO_OPTION);
				if(r == JOptionPane.NO_OPTION)
					return;
				exit();
			}});
		BasicArrowButton left = new BasicArrowButton(BasicArrowButton.WEST);
		BasicArrowButton right = new BasicArrowButton(BasicArrowButton.EAST);
		left.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				update(-1);
			}});
		right.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {		
				update(1);
			}});
		today = Calendar.getInstance();
		date = (Calendar)today.clone();
		dateLabel = new JLabel("", JLabel.CENTER);
		dateLabel.setFont(new Font("Arial", Font.BOLD, 11));
		JPanel dateSelector = new JPanel();
		dateSelector.setLayout(new BorderLayout());
		dateSelector.add(dateLabel, BorderLayout.CENTER);
		dateSelector.add(left, BorderLayout.WEST);
		dateSelector.add(right, BorderLayout.EAST);
		main.add(dateSelector);
	 	main.add(new JLabel("ITC Lab", JLabel.CENTER));
		main.add(new JLabel("Room 6", JLabel.CENTER));		
		main.add(new JLabel("Room 50", JLabel.CENTER));
		main.add(new JLabel("Mobile Cart Sci", JLabel.CENTER));
		main.add(new JLabel("Mobile Cart SS", JLabel.CENTER));
		main.add(new JLabel("Mobile Cart Eng", JLabel.CENTER));
		main.add(new JLabel("ITC (Researc", JLabel.RIGHT));
		main.add(new JLabel("h Floor Only)", JLabel.LEFT));
		
		sendObject("calendar");
		calendar = (char[][]) receiveObject();
	
		periods = new JLabel[7];
		slots = new ReservationSlot[7][8];
		
		for(int period = 0; period < 7; period++)
		{
			periods[period] = new JLabel("", JLabel.CENTER);
			main.add(periods[period]);
			
			for(int roomID = 0; roomID < 8; roomID++)
			{
				slots[period][roomID] = new ReservationSlot(period, roomID);
				main.add(slots[period][roomID]);
			}
		}
		periods[6].setText("After School");
	}
	
	/**
	 * Updates the calendar and grid of ReservationSlots.
	 * It first updates the calendar by incrementing the
	 * date by int dateChange. If the resulting date is
	 * not valid (such as a weekend), then move to the
	 * next valid date. It then retrieves information
	 * for all the Reservations of that day and uses
	 * it to update the ReservationSlots.
	 */
	public static void update(int dateChange)
	{
		if(dateChange != 0)
			date.add(Calendar.DATE, dateChange);
		
		char day = calendar[date.get(Calendar.MONTH)][date.get(Calendar.DATE) - 1];
		
		while((day = calendar[date.get(Calendar.MONTH)][date.get(Calendar.DATE) - 1]) < 'a')
			if(dateChange < 0)
				date.add(Calendar.DATE, -1);
			else
				date.add(Calendar.DATE, 1);
		
		
		if(date.get(Calendar.MONTH) == Calendar.JULY)
		{				
			update(-1);
			return;
		}
		else if(date.compareTo(today) < 0)
		{
			update(1);
			return;
		}

		
		for(int period = 0; period < 6; period++)
		{
			int periodActual = daySchedule[day - 'a'][period];
			periods[period].setText("Period " + periodActual);
		}
		
		dateLabel.setText(
				date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) + ", " +
				date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + " " +
				date.get(Calendar.DAY_OF_MONTH) + ", " + Character.toUpperCase(day) + " Day");
		
		String[][] names = new String[7][8];	
		try{ 
			sendObject(date.clone());
			names = (String[][]) receiveObject();
			
		} catch(Exception e) {}
		
		for(int p = 0; p < 7; p++)
			for(int r = 0; r < 8; r++)
				slots[p][r].updateReservation(names[p][r]);
	}
	
	/**
	 * Connects to the server.
	 */
	public static void connect(String serverAddress)
	{
		try {
			socket = new Socket(InetAddress.getByName(serverAddress), 27647);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			printErr(menu, "Connection Error", "Couldn't connect to the server");
			System.exit(1);
		}
	}
	
	/**
	 * Disconnects from the server and exits peacefully.
	 */
	public static void exit()
	{
		if(out != null)
			sendObject("end");

		try{
			in.close();
			out.close();
			socket.close();
		} catch(Exception e) { }
		
		System.exit(0);
	}
	
	/**
	 * Sends an object to the server
	 */
	public static void sendObject(Object obj)
	{
		try {
			out.writeObject(obj);
			out.flush();
		} catch(IOException e) {
			printErr(main, "Connection Error", "Couldn't communicate to the server");
			System.exit(1);
		}
	}
	
	/**
	 * Receives an object from the server
	 */
	public static Object receiveObject()
	{
		Object obj = null;
		
		try {
			obj = in.readObject();
		} catch(Exception e) {
			printErr(main, "Connection Error", "Couldn't communicate with the server");
			System.exit(1);
		}
		
		return obj;
	}

	/**
	 * Invoked if a submitted Reservation is invalid.
	 */
	public static void gotInvalid()
	{
		printErr(main, "Reservation Error", "Couldn't submit the reservation");
		update(0);
	}
	
	/**
	 * Shows an error message in a JOptionPane.
	 */
	public static void printErr(JFrame frame, String title, String message)
	{
		JOptionPane.showMessageDialog(frame, message,
				title, JOptionPane.ERROR_MESSAGE);
	}	

}