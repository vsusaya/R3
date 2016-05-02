package core;
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
import java.util.Hashtable;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

import java.awt.Color;

import javax.swing.JRadioButton;

import java.awt.GridLayout;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;

import exploration.ExplorationWindow;

import javax.swing.border.LineBorder;

import java.awt.Font;


public class Window {

	private JFrame frame;
	private JTextField pidField;
	private JTextField pid2Field;
	private JTextField constraintField;
	private JTextField constraint2Field;
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
	private JLabel searchLabel;
	private JLabel p1Label;
	private JLabel p2Label;
	private JPanel panel;
	private JLabel lblVisualizationType;
	private JButton exportBtn;
	private JLabel lblNotWhatYoure;
	private JLabel lblSampleProducts;
	private JButton btnShuffle;
	
	
	

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
		sample = conn.prepareStatement("SELECT productid, name, imgurl FROM product as p ORDER BY RANDOM() LIMIT 3");
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1440, 900);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		outputLabel = new JLabel("Output:");
		outputLabel.setBounds(104, 714, 279, 16);
		frame.getContentPane().add(outputLabel);
		
		JScrollPane outputScrollPane = new JScrollPane();
		outputScrollPane.setBounds(102, 742, 1222, 47);
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
						
						if (mapThreads != null) {
							for (Thread mThread : mapThreads) {
								mThread.interrupt();
							}
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
							ArrayList<Hashtable<String, Integer>> finalList = queryHandler.executeQuery(range, filter, pidField.getText(), constraintField.getText(), pid2Field.getText(), constraint2Field.getText());
							setOutputText(finalList.get(0).toString(), finalList.get(1).toString());
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
		submitButton.setBounds(639, 154, 117, 29);
		frame.getContentPane().add(submitButton);
		
		outputPanel = new JPanel(new BorderLayout());
		outputPanel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		outputPanel.setBounds(102, 195, 1222, 507);
		frame.getContentPane().add(outputPanel);
		
		
		//Sample Product section
		//product panel
		productPanel = new JPanel();
		productPanel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		productPanel.setBackground(Color.WHITE);
		productPanel.setBounds(781, 51, 622, 95);
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
		
		
		vizButtonGroup = new ButtonGroup();
		
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
		stopButton.setBounds(440, 709, 117, 29);
		frame.getContentPane().add(stopButton);
		
		JPanel prod1Panel = new JPanel();
		prod1Panel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		prod1Panel.setBounds(38, 17, 345, 129);
		frame.getContentPane().add(prod1Panel);
		prod1Panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		p1Label = new JLabel("Product 1:");
		p1Label.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		prod1Panel.add(p1Label, "4, 2");
		
		JLabel pidLabel = new JLabel("Product ID");
		prod1Panel.add(pidLabel, "4, 6");
		
		pidField = new JTextField();
		prod1Panel.add(pidField, "8, 6");
		pidField.setText("B00B93KG1A");
		pidField.setColumns(10);
		
		JLabel constraintLabel = new JLabel("Value Constraint(s)");
		prod1Panel.add(constraintLabel, "4, 8");
		
		constraintField = new JTextField();
		prod1Panel.add(constraintField, "8, 8");
		constraintField.setText("4, 5");
		constraintField.setColumns(10);
		
		
		
		//Panel for Product 2
		JPanel prod2Panel = new JPanel();
		prod2Panel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		prod2Panel.setBounds(416, 17, 337, 129);
		frame.getContentPane().add(prod2Panel);
		prod2Panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		p2Label = new JLabel("Product 2: (optional)\n");
		p2Label.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		prod2Panel.add(p2Label, "4, 2, default, bottom");
		
		JLabel pid2Label = new JLabel("Product ID");
		prod2Panel.add(pid2Label, "4, 6");
		
		pid2Field = new JTextField();
		prod2Panel.add(pid2Field, "8, 6");
		pid2Field.setText("B00B93KG1A");
		pid2Field.setColumns(10);
		
		JLabel constraint2Label = new JLabel("Value Constraint(s)");
		prod2Panel.add(constraint2Label, "4, 8");
		
		constraint2Field = new JTextField();
		prod2Panel.add(constraint2Field, "8, 8");
		constraint2Field.setText("4, 5");
		constraint2Field.setColumns(10);
		
		JButton exploreButton = new JButton("Explore...");
		exploreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					ExplorationWindow eWindow = new ExplorationWindow(conn);
					eWindow.frame.setVisible(true);
				} catch (Exception ew) {
					ew.printStackTrace();
				}
				
			}
		});
		exploreButton.setBounds(1143, 154, 117, 29);
		frame.getContentPane().add(exploreButton);
		
		panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel.setBounds(38, 153, 345, 34);
		frame.getContentPane().add(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		lblVisualizationType = new JLabel("Visualization:");
		panel.add(lblVisualizationType, "2, 2");
		
		
		
		//Radio Button Group
		rdbtnBarChart = new JRadioButton("Bar Chart");
		panel.add(rdbtnBarChart, "6, 2");
		rdbtnBarChart.setName("Bar Chart");
		rdbtnBarChart.setSelected(true);
		vizButtonGroup.add(rdbtnBarChart);
		
		rdbtnWordCloud = new JRadioButton("Word Cloud");
		panel.add(rdbtnWordCloud, "10, 2");
		rdbtnWordCloud.setName("Word Cloud");
		vizButtonGroup.add(rdbtnWordCloud);
		
		exportBtn = new JButton("Export");
		exportBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String output = outputArea.getText();
				Thread exportThread = new Thread(new Exporter(output));
				exportThread.run();
			}
		});
		exportBtn.setBounds(1207, 709, 117, 29);
		frame.getContentPane().add(exportBtn);
		
		lblNotWhatYoure = new JLabel("Not what you're looking for?");
		lblNotWhatYoure.setBounds(936, 159, 195, 16);
		frame.getContentPane().add(lblNotWhatYoure);
		
		lblSampleProducts = new JLabel("Sample Products");
		lblSampleProducts.setBounds(977, 17, 109, 16);
		frame.getContentPane().add(lblSampleProducts);
		
		btnShuffle = new JButton("Shuffle!");
		btnShuffle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//get sample products
				//should be refactored
				try {
					
					//sample product buttons
					//should be refactored
					productBtnList.clear();
					productPanel.removeAll();
					productPanel.validate();
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
		});
		btnShuffle.setBounds(1098, 10, 117, 29);
		frame.getContentPane().add(btnShuffle);
		
		
		
		
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
	
	protected void setOutputText(String output, String output2) {
		if (output2 != null) {
			outputArea.setText(output + "\n" + output2);
		} else {
			outputArea.setText(output);
		}
		
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
		outputPanel.repaint();
		outputPanel.setLayout(new FlowLayout());
		outputPanel.setBackground(Color.WHITE);
		for (Tag tag : cloud.tags()) {
			String tagName = tag.getName();
			if (tag.getLink().equals(String.format("%d", ContentAnalyzer.QUERY_2))) {
				tagName = tagName.substring(0, tagName.length() - 2);
			}
			
			final JLabel label = new JLabel(tag.getName());
			label.setOpaque(false);
			label.setFont(label.getFont().deriveFont((float) tag.getWeight() * 30));	
			if (tag.getLink().equals(String.format("%d",ContentAnalyzer.QUERY_2))) {
				label.setForeground(Color.CYAN);
			}
			//label.setVisible(true);
			outputPanel.add(label);
			outputPanel.setVisible(true);
		}
	}
	
	protected void setOutputPanelCloudInteractive(Cloud cloud) {
		outputPanel.removeAll();
		outputPanel.repaint();
		outputPanel.setLayout(new FlowLayout());
		outputPanel.setBackground(Color.WHITE);
		for (Tag tag : cloud.tags()) {
			
			String tagName = tag.getName();
			if (tag.getLink().equals(String.format("%d", ContentAnalyzer.QUERY_2))) {
				tagName = tagName.substring(0, tagName.length() - 2);
			}
			
			final JLabel label = new JLabel(tagName);
			label.setOpaque(false);
			label.setFont(label.getFont().deriveFont((float) tag.getWeight() * 30));
			if (tag.getLink().equals(String.format("%d",ContentAnalyzer.QUERY_2))) {
				label.setForeground(Color.CYAN);
			}
			outputPanel.add(label);
			outputPanel.setVisible(true);
		}
		outputPanel.validate();
	}

	
	public static Window getWindow() {
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
	
	public JTextField getPIDField1() {
		return pidField;
	}
	
	public JTextField getPIDField2() {
		return pid2Field;
	}
	
	public void setPIDField1(String id) {
		pidField.setText(id);
	}
	
	public void setPIDField2(String id) {
		pid2Field.setText(id);;
	}
	
}
