import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

import java.awt.Color;

import javax.swing.JRadioButton;


public class Window {

	private JFrame frame;
	private JTextField pidField;
	private JTextField constraintField;
	private Connection conn;
	private PreparedStatement range;
	private PreparedStatement filter;
	private PreparedStatement sample;
	private QueryHandler queryHandler;
	private JTextArea outputArea;
	private JPanel outputPanel;
	private JLabel outputLabel;
	
	private JRadioButton rdbtnBarChart;
	private JRadioButton rdbtnWordCloud;
	private ButtonGroup vizButtonGroup;
	
	static Window instance;
	private JPanel productPanel;
	private ArrayList<Product> productList;
	private ArrayList<JButton> productBtnList;
	//private JButton btnProductbutton;
	//private JButton btnProductbutton_1;
	
	private Thread processingThread;
	private ArrayList<Thread> mapThreads; //from CA
	private Thread aggThread; //from CA
	private JButton stopButton;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window window = new Window();
					Window.instance = window;
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Window() throws Exception{
		queryHandler = new QueryHandler();
		conn = QueryHandler.establishConnection();
		range = conn.prepareStatement("SELECT content, overall FROM review as r, product as p WHERE r.productid = p.productid AND p.productid = ? AND r.overall >= ? AND r.overall <= ?");
		filter = conn.prepareStatement("SELECT content, overall FROM review as r, product as p WHERE r.productid = p.productid AND p.productid = ? AND r.overall = ?");
		sample = conn.prepareStatement("SELECT productid, name, imgurl FROM product as p LIMIT 3");
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1200, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		pidField = new JTextField();
		pidField.setText("B00B93KG1A");
		pidField.setBounds(183, 40, 134, 28);
		frame.getContentPane().add(pidField);
		pidField.setColumns(10);
		
		JLabel pidLabel = new JLabel("Product ID");
		pidLabel.setBounds(38, 46, 85, 16);
		frame.getContentPane().add(pidLabel);
		
		constraintField = new JTextField();
		constraintField.setText("4, 5");
		constraintField.setBounds(183, 68, 134, 28);
		frame.getContentPane().add(constraintField);
		constraintField.setColumns(10);
		
		JLabel constraintLabel = new JLabel("Value Constraint(s)");
		constraintLabel.setBounds(38, 74, 123, 16);
		frame.getContentPane().add(constraintLabel);
		
		outputLabel = new JLabel("Output:");
		outputLabel.setBounds(38, 636, 279, 16);
		frame.getContentPane().add(outputLabel);
		
		JScrollPane outputScrollPane = new JScrollPane();
		outputScrollPane.setBounds(38, 664, 1139, 90);
		frame.getContentPane().add(outputScrollPane);
		
		outputArea = new JTextArea();
		outputScrollPane.setViewportView(outputArea);
		outputArea.setEditable(false);
		
		JButton submitButton = new JButton("Submit");
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					//if another thread is processing and submit is hit again, kill the last thread.
					if (processingThread != null) {
						queryHandler.getContentAnalyzer().setKillThreads();
						
						for (Thread mThread : mapThreads) {
							mThread.interrupt();
						}

						aggThread.interrupt();
						processingThread.interrupt();
						
						processingThread = null;
					}
					
					//hand off parameters to query executor, get back results to display
								
					Thread queryThread = new Thread(() -> {
						String results;
						try {
							outputLabel.setText(String.format("Output for %s: (processing)", constraintField.getText()));
							results = queryHandler.executeQuery(range, filter, pidField.getText(), constraintField.getText());
							outputArea.setText(results);
							outputLabel.setText(String.format("Output for %s: (done)", constraintField.getText()));
						} catch (Exception e1) {
							e1.printStackTrace();
							//outputArea.setText("Ending previous process...");
							outputLabel.setText(String.format("Output for %s: (stopped)", constraintField.getText()));
							System.out.println("SUBMIT ERROR");
						}
						
					});
					processingThread = queryThread;
					queryThread.start();
							
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(null, "Please enter valid parameters");
					ex.printStackTrace();
				}
				
				
			}
		});
		submitButton.setBounds(38, 126, 117, 29);
		frame.getContentPane().add(submitButton);
		
		outputPanel = new JPanel(new BorderLayout());
		outputPanel.setBounds(38, 187, 1139, 437);
		frame.getContentPane().add(outputPanel);
		
		
		//Sample Product section
		//product panel
		productPanel = new JPanel();
		productPanel.setBackground(Color.WHITE);
		productPanel.setBounds(532, 40, 645, 95);
		frame.getContentPane().add(productPanel);
				
		productBtnList = new ArrayList<JButton>();
		
		//sample product buttons
		JButton productButton1 = new ProductButton("productButton1");
		productButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pidField.setText(((ProductButton) productButton1).getPid());
			}
		});
		productBtnList.add(productButton1);
		productPanel.add(productButton1);
		
		JButton productButton2 = new ProductButton("productButton2");
		productButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pidField.setText(((ProductButton) productButton2).getPid());
			}
		});
		productBtnList.add(productButton2);
		productPanel.add(productButton2);
		
		JButton productButton3 = new ProductButton("productButton3");
		productButton3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pidField.setText(((ProductButton) productButton3).getPid());
			}
		});
		productBtnList.add(productButton3);
		productPanel.add(productButton3);
		
		
		
		//Radio Button Group
		rdbtnBarChart = new JRadioButton("Bar Chart");
		rdbtnBarChart.setName("Bar Chart");
		rdbtnBarChart.setBounds(354, 42, 141, 23);
		rdbtnBarChart.setSelected(true);
		frame.getContentPane().add(rdbtnBarChart);
		
		rdbtnWordCloud = new JRadioButton("Word Cloud");
		rdbtnWordCloud.setName("Word Cloud");
		rdbtnWordCloud.setBounds(354, 70, 141, 23);
		frame.getContentPane().add(rdbtnWordCloud);
		
		vizButtonGroup = new ButtonGroup();
		vizButtonGroup.add(rdbtnBarChart);
		vizButtonGroup.add(rdbtnWordCloud);
		
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					queryHandler.getContentAnalyzer().setKillThreads();
					processingThread = null;
					outputLabel.setText(String.format("Output for %s: (stopping)", constraintField.getText()));
				} catch (Exception se) {
					//Possible NullPointer exception if process was already stopped
					se.printStackTrace();
					outputLabel.setText(String.format("Output for %s: (stopped)", constraintField.getText()));
				}
				
			}
		});
		stopButton.setBounds(354, 631, 117, 29);
		frame.getContentPane().add(stopButton);
		
		
		//get sample products
		try {
			productList = queryHandler.getSampleProducts(sample);
			
			for (Product product : productList) {
				JButton prodButton = productBtnList.remove(0);
				
				prodButton.setText(product.getName());
				((ProductButton) prodButton).setPid(product.getPid());
				((ProductButton) prodButton).setImgurl(product.getImgurl());
				((ProductButton) prodButton).setImage();
			}
			
		} catch (SQLException e1) {
			JOptionPane.showMessageDialog(null, "Error Retrieving Sample Products");
			e1.printStackTrace();
		}

	}
	
	protected void setOutputText(String output) {
		outputArea.setText(output);
	}
	
	protected void setOutputPanelChart(ChartPanel chart) {
		outputPanel.removeAll();
		outputPanel.setLayout(new BorderLayout());
		outputPanel.add(chart, BorderLayout.CENTER);
		outputPanel.validate();
		outputPanel.setVisible(true);
		//frame.setVisible(true);
		//outputPanel.setLayout(new BorderLayout());
		//outputPanel.validate();
	}
	
	protected void setOutputPanelChartInteractive(ChartPanel chart) {
		outputPanel.removeAll();
		outputPanel.setLayout(new BorderLayout());
		outputPanel.add(chart, BorderLayout.CENTER);
		outputPanel.validate();
		outputPanel.setVisible(true);
		//frame.setVisible(true);
		//outputPanel.setLayout(new BorderLayout());
		//outputPanel.validate();
	}
	
	protected void setOutputPanelCloud(Cloud cloud) {
		outputPanel.removeAll();
		outputPanel.setLayout(new FlowLayout());
		outputPanel.setBackground(Color.WHITE);
		for (Tag tag : cloud.tags()) {
			//String thing = tag.getName();
			final JLabel label = new JLabel(tag.getName());
			label.setOpaque(false);
			label.setFont(label.getFont().deriveFont((float) tag.getWeight() * 10));
			//label.setVisible(true);
			outputPanel.add(label);
			outputPanel.setVisible(true);
		}
	}
	
	protected void setOutputPanelCloudInteractive(Cloud cloud) {
		outputPanel.removeAll();
		outputPanel.setLayout(new FlowLayout());
		outputPanel.setBackground(Color.WHITE);
		for (Tag tag : cloud.tags()) {
			final JLabel label = new JLabel(tag.getName());
			label.setOpaque(false);
			label.setFont(label.getFont().deriveFont((float) tag.getWeight() * 30));
			outputPanel.add(label);
			outputPanel.setVisible(true);
		}
		outputPanel.validate();
	}

	
	protected static Window getWindow() {
		return Window.instance;
	}
	
	protected ButtonGroup getVizButtonGroup() {
		return vizButtonGroup;
	}
	
	protected void setMapThreads(ArrayList<Thread> mapThreads) {
		this.mapThreads = mapThreads;
	}
	
	protected void setAggThread(Thread aggThread) {
		this.aggThread = aggThread;
	}
	
	protected JLabel getOutputLabel() {
		return outputLabel;
	}
}
