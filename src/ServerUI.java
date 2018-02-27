
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sockets.LineCoordinatesOnly;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.GregorianCalendar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ServerUI {

	protected static final Logger logger = LogManager.getLogger(ServerUI.class);

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JCheckBox chckbxLocations;
	JCheckBox chckbxSkyscanner;
	JCheckBox chckbxEstreaming;

	/**
	 * Launch the application.
	 
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerUI window = new ServerUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/
	
	public void setVisible(boolean visibility){
		frame.setVisible(visibility);
	}

	/**
	 * Create the application.
	 */
	public ServerUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 732, 507);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnStartServer = new JButton("Start Server");
		btnStartServer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				(new Thread(new ServerStart())).start();
				
			}
		});
		btnStartServer.setBounds(433, 376, 269, 25);
		frame.getContentPane().add(btnStartServer);
		
		JButton btnNewButton = new JButton("Start Update");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				try{
					boolean updatePlaces = chckbxLocations.isSelected();
					boolean useSkyscanner = chckbxSkyscanner.isSelected();
					boolean useEStreaming = chckbxEstreaming.isSelected();
					int year = Integer.parseInt(textField.getText());
					int month = Integer.parseInt(textField_1.getText());
					int day = Integer.parseInt(textField_2.getText());
					
					(new Thread(new database.updateTables.UpdateDatabase(new GregorianCalendar(year, month - 1, day, 0, 0, 0), updatePlaces, useSkyscanner, useEStreaming))).start();
				}catch(NumberFormatException a){
					logger.error("Date cant be passed to Gregorian Calendar: " + e.toString());
				}
				
			}
		});
		btnNewButton.setBounds(12, 422, 231, 25);
		frame.getContentPane().add(btnNewButton);
		
		JLabel lblUpdateComponents = new JLabel("Update Components:");
		lblUpdateComponents.setBounds(12, 13, 165, 16);
		frame.getContentPane().add(lblUpdateComponents);
		
		chckbxLocations = new JCheckBox("Locations");
		chckbxLocations.setSelected(true);
		chckbxLocations.setBounds(12, 45, 113, 25);
		frame.getContentPane().add(chckbxLocations);
		
		chckbxSkyscanner = new JCheckBox("Skyscanner");
		chckbxSkyscanner.setSelected(true);
		chckbxSkyscanner.setBounds(12, 75, 113, 25);
		frame.getContentPane().add(chckbxSkyscanner);
		
		chckbxEstreaming = new JCheckBox("eStreaming");
		chckbxEstreaming.setSelected(true);
		chckbxEstreaming.setBounds(12, 105, 113, 25);
		frame.getContentPane().add(chckbxEstreaming);
		
		JLabel lblUpdateDate = new JLabel("Update Date:");
		lblUpdateDate.setBounds(12, 155, 113, 16);
		frame.getContentPane().add(lblUpdateDate);
		
		textField = new JTextField();
		textField.setText("2018");
		textField.setBounds(80, 184, 72, 22);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JLabel lblYear = new JLabel("Year:");
		lblYear.setBounds(12, 187, 56, 16);
		frame.getContentPane().add(lblYear);
		
		JLabel lblMonth = new JLabel("Month:");
		lblMonth.setBounds(12, 222, 56, 16);
		frame.getContentPane().add(lblMonth);
		
		textField_1 = new JTextField();
		textField_1.setText("4");
		textField_1.setColumns(10);
		textField_1.setBounds(80, 219, 72, 22);
		frame.getContentPane().add(textField_1);
		
		JLabel lblDay = new JLabel("Day:");
		lblDay.setBounds(12, 254, 56, 16);
		frame.getContentPane().add(lblDay);
		
		textField_2 = new JTextField();
		textField_2.setText("10");
		textField_2.setColumns(10);
		textField_2.setBounds(80, 251, 72, 22);
		frame.getContentPane().add(textField_2);
		
		JButton btnCloseServer = new JButton("Close Server");
		btnCloseServer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				LineCoordinatesOnly.closeSocket();
			}
		});
		btnCloseServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnCloseServer.setBounds(433, 422, 269, 25);
		frame.getContentPane().add(btnCloseServer);
		
		JButton btnAddTimeZonen = new JButton("Add Time Zonen Info To Database");
		btnAddTimeZonen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				System.out.println(database.updates.correctDuration.addTimezoneToAirports() + " Airports updated!");
			}
		});
		btnAddTimeZonen.setBounds(302, 45, 400, 98);
		frame.getContentPane().add(btnAddTimeZonen);
	}
}
