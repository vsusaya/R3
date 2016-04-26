import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.AbstractButton;

import org.json.*;

/*
 * Holds a single queue to hold outstanding reviews to be processed
 * Contains two maps for each query/product entered, for comparison
 */
public class Aggregator implements Runnable{

	private BlockingQueue<Review> queue;
	private boolean finished;
	private Object lock;
	private Hashtable<String, Integer> map;
	private Hashtable<String, Integer> map2;
	private Visualizer visualizer;
	private ContentAnalyzer contentAnalyzer;
	
	private long timestamp;
	private final long DELAY = 3000;
	
	protected static final String BARNAME = "Bar Chart";
	protected static final String CLOUDNAME = "Word Cloud";
	
	
	public Aggregator(ContentAnalyzer contentAnalyzer) {
		queue = new LinkedBlockingQueue<Review>();
		//queue2 = new LinkedBlockingQueue<String>();
		finished = false;
		lock = new Object();
		map = new Hashtable<String, Integer>();
		map2 = new Hashtable<String, Integer>();
		visualizer = new Visualizer();
		this.contentAnalyzer = contentAnalyzer;
	}
	
	protected void addToQueue(Object item) {
		System.out.println("in add");
		//synchronized(lock) {
		if (item instanceof Review){
			queue.add((Review) item);
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
						Review reviewObj = queue.take();
						String jsonMap = reviewObj.getReview();
						JSONObject jObject = new JSONObject(jsonMap);
						int queryNum = reviewObj.getQueryNum();
								
						insertIntoCorrectMap(jObject, queryNum);
						/*
						Iterator<String> keys = jObject.keys();
						while(keys.hasNext()) {
							String key = keys.next();
							Integer value = jObject.getInt(key);
							
							Integer presentValue = map.get(key);
							if(presentValue != null) {
								map.put(key, presentValue + value); //increment the value of the word in this map by the count present in this review instance
							} else {
								map.put(key, value);
							}
									
						}
						*/
						
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
								visualizer.makeBarChartInteractive((new JSONObject(getHashtable()).toString()), (new JSONObject(getHashtable2()).toString()));
								//visualizer.makeWordCloudInteractive((new JSONObject(getHashtable()).toString()));
								break;
							} else {
								visualizer.makeWordCloudInteractive((new JSONObject(getHashtable()).toString()), (new JSONObject(getHashtable2()).toString()));
								break;
							}				
						}
						
						//also send copy of data to the Window so it can make a request to the visualizer if it needs to
					}

					String fullCounts = getHashtable().toString();
					String fullCounts2 = getHashtable2().toString();
					//String orderedCounts = orderHashtable(fullCounts);
					Window.getWindow().setOutputText(fullCounts, fullCounts2);
				}
			}
			
		}	
	}
	
	private void insertIntoCorrectMap(JSONObject jObject, int queryNum) {
		Iterator<String> keys = jObject.keys();
		Hashtable<String, Integer> correctMap = map;
		
		if (queryNum == ContentAnalyzer.QUERY_2) {
			correctMap = map2;
		}
		
		while(keys.hasNext()) {
			String key = keys.next();
			Integer value = jObject.getInt(key);
			
			Integer presentValue = correctMap.get(key);
			if(presentValue != null) {
				correctMap.put(key, presentValue + value); //increment the value of the word in this map by the count present in this review instance
			} else {
				correctMap.put(key, value);
			}
					
		}
	}

	protected Hashtable<String, Integer> getHashtable() {
		return map;
	}
	
	protected Hashtable<String, Integer> getHashtable2() {
		return map2;
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
