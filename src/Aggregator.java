import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.AbstractButton;
import javax.swing.JRadioButton;

import org.json.*;


public class Aggregator implements Runnable{

	private BlockingQueue<String> queue;
	private boolean finished;
	private Object lock;
	private Hashtable<String, Integer> map;
	private Visualizer visualizer;
	private ContentAnalyzer contentAnalyzer;
	
	private long timestamp;
	private final long DELAY = 3000;
	
	protected static final String BARNAME = "Bar Chart";
	protected static final String CLOUDNAME = "Word Cloud";
	
	
	public Aggregator(ContentAnalyzer contentAnalyzer) {
		queue = new LinkedBlockingQueue<String>();
		finished = false;
		lock = new Object();
		map = new Hashtable<String, Integer>();
		visualizer = new Visualizer();
		this.contentAnalyzer = contentAnalyzer;
	}
	
	protected void addToQueue(Object item) {
		System.out.println("in add");
		//synchronized(lock) {
		if (item instanceof String){
			queue.add((String) item);
		} else {
			synchronized(lock) {
				finished = true;
				lock.notify();
			}
		}
			//lock.notify();
		//}	
	}

	@Override 
	public void run() {
		
		//aggregate until we are done
		synchronized(lock) {
			
			timestamp = System.currentTimeMillis();
			
			while(!finished || queue.size() != 0) {
				System.out.println("In run");
				
				if (contentAnalyzer.getKillThreads()) {
					break;
				}
				
				if ((System.currentTimeMillis() - timestamp) > DELAY) {
					timestamp = System.currentTimeMillis();
				}
				//System.out.println("WOO");
				//System.out.println(Window.getWindow().getOutputLabel().getText());
				Window.getWindow().getOutputLabel().setText(Window.getWindow().getOutputLabel().getText().replaceAll("\\(.*\\)", "(Processing)"));
				
				try {
					if (queue.size() == 0) {
						lock.wait(3000); //artificial wait so the visual does not change too fast
					} else {
						String jsonMap = queue.take();
						JSONObject jObject = new JSONObject(jsonMap);
						Iterator<String> keys = jObject.keys();
						
						while(keys.hasNext()) {
							String key = keys.next();
							Integer value = jObject.getInt(key);
							
							Integer presentValue = map.get(key);
							if(presentValue != null) {
								map.put(key, presentValue + value);
							} else {
								map.put(key, value);
							}
									
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
				
				//interactive results
				//convert hashtable to string to get new object to avoid concurrency issues
				if ((System.currentTimeMillis() - timestamp) > DELAY) {
					
					Enumeration<AbstractButton> buttons = Window.getWindow().getVizButtonGroup().getElements();
					AbstractButton button = null;
					
					while (buttons.hasMoreElements()) {
						button = buttons.nextElement();
						
						if (button.isSelected()) {
							if (button.getName() == BARNAME) {
								visualizer.makeBarChartInteractive((new JSONObject(getHashtable()).toString()));
								//visualizer.makeWordCloudInteractive((new JSONObject(getHashtable()).toString()));
								break;
							} else {
								visualizer.makeWordCloudInteractive((new JSONObject(getHashtable()).toString()));
								break;
							}				
						}
					}

					//visualizer.makeBarChartInteractive((new JSONObject(getHashtable()).toString()));
					//visualizer.makeBarChart(getHashtable());
					String fullCounts = getHashtable().toString();
					//String orderedCounts = orderHashtable(fullCounts);
					Window.getWindow().setOutputText(fullCounts);
				}
			}
			
		}	
	}

	protected Hashtable<String, Integer> getHashtable() {
		return map;
	}
	
	//not currently used
	protected String orderHashtable(String countString) {
		
		/*
		HashMap<String, Integer> hMap = visualizer.stringToHashMap(counts);
		
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(hMap.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
				return (entry1.getValue().compareTo(entry2.getValue()) );
			}
			
		});
		
		return list.toString();
		*/
		
		JSONObject jObject = new JSONObject(countString);
		Iterator<String> keys = jObject.keys();
		Comparator<Entry<String, Integer>> comparator = new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
				return (entry1.getValue().compareTo(entry2.getValue()) );
			}
		};
		//SortedSet<Entry<String, Integer>> counts = new TreeSet<String, Integer>(comparator);
		
		while (keys.hasNext()) {
			String key = keys.next();
			Integer value = jObject.getInt(key);
			//counts.put(key, value);
		}
		
		return "";//counts;
			
	}
	
	
}
