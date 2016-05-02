package exploration;
import java.awt.EventQueue;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import core.QueryHandler;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.Connection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;


public class ExplorationWindow {

	public JFrame frame;
	private JTextField textField;
	private ExQueryHandler exQueryHandler;
	Connection conn;
	private JTable searchTable;
	JScrollPane scrollPane;
	
	static final Object COLUMN_NAMES[] = {"ID", "Name", "Features"};
	
	static ExplorationWindow exWindow;
	private JButton btnFillProduct;
	private JButton btnFillProduct_1;

	/**
	 * Create the application.
	 */
	public ExplorationWindow(Connection conn) {
		exQueryHandler = new ExQueryHandler();
		this.conn = conn;
		//range = conn.prepareStatement("SELECT content, overall FROM review as r, product as p WHERE r.productid = p.productid AND p.productid = ? AND r.overall >= ? AND r.overall <= ?");
		//filter = conn.prepareStatement("SELECT content, overall FROM review as r, product as p WHERE r.productid = p.productid AND p.productid = ? AND r.overall = ?");
		//sample = conn.prepareStatement("SELECT productid, name, imgurl FROM product as p LIMIT 3");
		ExplorationWindow.exWindow = this;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(200, 50, 1000, 739);
		frame.getContentPane().setLayout(null);
		
		JLabel searchLabel = new JLabel("Keywords:");
		searchLabel.setBounds(35, 49, 78, 16);
		frame.getContentPane().add(searchLabel);
		
		textField = new JTextField();
		textField.setBounds(115, 43, 674, 28);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnSearch = new JButton("Search!");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String searchTerms = textField.getText();
				exQueryHandler.execute(searchTerms);
				
			}
		});
		btnSearch.setBounds(850, 44, 117, 29);
		frame.getContentPane().add(btnSearch);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(35, 98, 932, 563);
		frame.getContentPane().add(scrollPane);
		
		searchTable = new JTable();
		scrollPane.setViewportView(searchTable);
		
		btnFillProduct = new JButton("Fill Product 1");
		btnFillProduct.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				core.Window window = core.Window.getWindow();
				JTextField field = window.getPIDField1();
				int row = searchTable.getSelectedRow();
				String id = (String) searchTable.getValueAt(row, 0);
				window.setPIDField1(id);
				
			}
		});
		btnFillProduct.setBounds(35, 673, 117, 29);
		frame.getContentPane().add(btnFillProduct);
		
		btnFillProduct_1 = new JButton("Fill Product 2");
		btnFillProduct_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				core.Window window = core.Window.getWindow();
				JTextField field = window.getPIDField2();
				int row = searchTable.getSelectedRow();
				String id = (String) searchTable.getValueAt(row, 0);
				window.setPIDField2(id);
				
			}
		});
		btnFillProduct_1.setBounds(172, 673, 117, 29);
		frame.getContentPane().add(btnFillProduct_1);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
	}
	
	protected void makeSearchTable(Object rowData[][]) {
		searchTable = new JTable(rowData, COLUMN_NAMES);
		scrollPane.setViewportView(searchTable);
	}
	
	public static ExplorationWindow getExplorationWindow() {
		return exWindow;
	}
}
