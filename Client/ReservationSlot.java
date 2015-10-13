import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Represents a Reservation on the Client GUI.
 * Each ReservationSlot contains a label that
 * says who is currently reserving it. If it
 * is not reserved by someone else, there is a
 * button where the user can submit a Reservation.
 * @author Cory Lynch
 */
public class ReservationSlot extends JPanel
{	
	private static final long serialVersionUID = 3196291220340508882L;
	
	public Reservation info;
	public JButton button;
	public JLabel label, person;
	public int period, roomID;
	
	/**
	 * Builds a ReservationSlot for the Client.
	 * The constructor receives which period and
	 * which room it is on the Client's grid.
	 */
	public ReservationSlot(int per, int room)
	{
		period = per; roomID = room;
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setLayout(new FlowLayout(FlowLayout.CENTER, 65535, 5));
		label = new JLabel("Reserved by:");
		add(label);
		person = new JLabel("Nobody");
		add(person);
		button = new JButton("Reserve");
		button.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(button.getText() == "Reserve")
						makeReservation();
					else
						undoReservation();
				}	
			});
		add(button);
	}

	/**
	 * Updates the ReservationSlot's labels and button
	 * It takes a String that states who, if anyone,
	 * is currently reserving the slot and updates
	 * the labels and button accordingly.
	 */
	public void updateReservation(String name)
	{
		info = new Reservation(Client.date, period, roomID, Client.name);
		
		label.setText("Reserved by:");
		if(name != null)
		{	
			if(name.equals(Client.name))
			{
				person.setText("You");
				button.setText("Undo");
				button.setEnabled(true);
			}
			else
			{
				button.setText("Reserve");
				button.setEnabled(false);
				
				if(name.equals("admin"))
				{
					label.setText("Unavailable");
					person.setText(" ");
				}
				else 
					person.setText(name);
			}
		}
		else
		{
			person.setText("Nobody");
			button.setText("Reserve");
			button.setEnabled(true);
		}
	}
	
	/**
	 * Submits a Reservation to the server.
	 * If the Reservation was not valid, then
	 * call the Client's gotInvalid() method.
	 */
	public void makeReservation()
	{
		Client.sendObject(info);
		boolean valid = (Boolean) Client.receiveObject();
		
		person.setText("You");
		button.setText("Undo");
		
		if(!valid)
			Client.gotInvalid();
	}
	
	/**
	 * Removes a Reservation from the server.
	 */
	public void undoReservation()
	{
		Client.sendObject("remove");
		Client.sendObject(info);
		
		person.setText("Nobody");
		button.setText("Reserve");
	}
}
