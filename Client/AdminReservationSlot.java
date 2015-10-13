import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A modified ReservationSlot used in the AdminClient.
 * Acts the same as ReservationSlot, except for
 * different label texts, and it force adds all
 * Reservations, so it will submit a Reservation
 * even if it already reserved by a teacher.
 * @see ReservationSlot
 * @author Cory Lynch
 */
public class AdminReservationSlot extends ReservationSlot
{
	private static final long serialVersionUID = 5325220626843461132L;

	public AdminReservationSlot(int period, int roomID)
	{
		super(period, roomID);
		
		button.setText("Block");
		button.removeActionListener(button.getActionListeners()[0]);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(button.getText() == "Block")
					makeReservation();
				else
					undoReservation();
			}	
		});
	}
	
	public void updateReservation(String name)
	{
		info = new Reservation(AdminClient.date, period, roomID, "admin");
		
		if(name != null)
		{	
			if(name.equals("admin"))
			{
				label.setText("Blocked");
				person.setText(" ");
				button.setText("Unblock");
				
			}
			else
			{
				label.setText("Reserved by:");
				person.setText(name);
				button.setText("Block");
			}
		}
		else
		{
			label.setText("Reserved by:");
			person.setText("Nobody");
			button.setText("Block");
		}
	}
	
	public void makeReservation()
	{
		if(label.getText().equals("Blocked"))
			return;
		
		AdminClient.sendObject("forceAdd");
		AdminClient.sendObject(info);
		
		label.setText("Blocked");
		person.setText(" ");
		button.setText("Unblock");
	}
	
	public void undoReservation()
	{
		if(!label.getText().equals("Blocked"))
			return;
		
		AdminClient.sendObject("remove");
		AdminClient.sendObject(info);
		
		label.setText("Reserved by:");
		person.setText("Nobody");
		button.setText("Block");
	}
}
