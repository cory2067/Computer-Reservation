import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * A modified Client with administrator privileges.
 * The admin can block any Reservation time so that no
 * Client can reserve it. The admin can additionally
 * make alterations to the school calendar (not implemented).
 * @version 0.6
 * @author Cory Lynch
 */
public class AdminClient extends Client
{
	public static JButton[] rows, columns;
	public static JButton toCalGUI, blockDay, unblockDay;
	public static JFrame cal;
	
	public static void run()
	{
		initMainGUI();
		update(0);
		
		main.setVisible(true);
	}
	
	/**
	 * Builds the main GUI of the AdminClient.
	 * It is similar to the Client's main GUI,
	 * but it is comprised of AdminReservationSlots
	 * instead of ReservationSlots. It also contains 
	 * buttons for reserving entire rows, columns, or
	 * days. Additionally, the calendar GUI can be
	 * opened from a button on the main GUI.
	 */
	public static void initMainGUI()
	{
		main = new JFrame("Computer Reservation Admin");
		main.setSize(1350, 725);
		main.setLayout(new GridLayout(9, 10));
		main.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		main.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e)
			{ 
				int r = JOptionPane.showConfirmDialog(main, "Exit the program? Your changes will be saved.", 
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
		
		main.add(new JLabel("Admin Controls", JLabel.CENTER));
		
		blockDay = new JButton("Block Day");
		blockDay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int roomID = 0; roomID < 8; roomID++)
					for(int period = 0; period < 7; period++)
						slots[period][roomID].makeReservation();
			}
		});
		main.add(blockDay);
		
		columns = new JButton[8];
		for(int c = 0; c < 8; c++)
		{
			final int ROOM_ID = c;
			columns[c] = new JButton("Block Column");		
			columns[c].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(int period = 0; period < 7; period++)
						slots[period][ROOM_ID].makeReservation();
				}
			});	
			main.add(columns[c]);
		}
		
		unblockDay = new JButton("Unblock Day");
		unblockDay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int roomID = 0; roomID < 8; roomID++)
					for(int period = 0; period < 7; period++)
						slots[period][roomID].undoReservation();
			}
		});
		main.add(unblockDay);
		
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
		rows = new JButton[7];
		slots = new AdminReservationSlot[7][8];
		
		for(int period = 0; period < 7; period++)
		{
			final int PERIOD = period;
			rows[period] = new JButton("Block Row");
			rows[period].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(int roomID = 0; roomID < 8; roomID++)
						slots[PERIOD][roomID].makeReservation();
				}
			});
			
			main.add(rows[period]);
			periods[period] = new JLabel("", JLabel.CENTER);
			main.add(periods[period]);
			
			for(int roomID = 0; roomID < 8; roomID++)
			{
				slots[period][roomID] = new AdminReservationSlot(period, roomID);
				main.add(slots[period][roomID]);
			}
		}
		periods[6].setText("After School");
	}
}