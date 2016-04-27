import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractButton;

import com.google.common.collect.Lists;


public class ContentAnalyzer {
	
	static final int PARTITIONS = 4;
	static final int TEST_LOAD = 100;
	
	protected boolean killThreads = false;

	private Aggregator aggregator;
	private EndOfFileMarker eof;
	private Visualizer visualizer;
	private QueryHandler queryHandler;
	
	private ArrayList<Thread> mapThreads;
	private ArrayList<Thread> mapThreads2;
	private Thread aggThread;
	
	protected static final int QUERY_1 = 1;
	protected static final int QUERY_2 = 2;
	
	public ContentAnalyzer(QueryHandler queryHandler) {
		//aggregator = new Aggregator();
		eof = new EndOfFileMarker();
		visualizer = new Visualizer();
		this.queryHandler = queryHandler;
	}
	
	protected ArrayList<Hashtable<String, Integer>> analyze(List<String> reviews, List<String> reviews2) throws InterruptedException {
		
		System.out.println("BEGIN");
		aggregator = new Aggregator(this);
		
		long startTime = System.currentTimeMillis();
		
		//create threads to begin processing
		ArrayList<Thread> mapperThreads = createAndRunMapperThreads(reviews, QUERY_1);
		mapThreads = mapperThreads;
		
		ArrayList<Thread> mapperThreads2 = null;
		if (reviews2 != null) {
			mapperThreads2 = createAndRunMapperThreads(reviews2, QUERY_2);
			mapThreads2 = mapperThreads2;
		}
		
		
		///	
		Thread aggregateThread = new Thread(aggregator);
		aggregateThread.start();
			
		//send threads to window so it can stop it on a submit
		//hack-y implementation
		aggThread = aggregateThread;
		sendThreadsToWindow();
		
		
		for(Thread jThread : mapperThreads) {
			jThread.join();
		}
		
		if (mapperThreads2 != null) {
			for(Thread jThread : mapperThreads2) {
				jThread.join();
			}
		}
		
		//EOF object to let aggregator know all threads have completed (joined)
		aggregator.addToQueue(eof);
		
		aggregateThread.join();
		
		long stopTime = System.currentTimeMillis();
		System.out.println("Elapsed: " + Long.toString(stopTime - startTime));
		
		//feed to Visualizer
		//visualizer.makeBarChart(aggregator.getHashtable());
		
		
		Enumeration<AbstractButton> buttons = Window.getWindow().getVizButtonGroup().getElements();
		AbstractButton button = null;
		
		while (buttons.hasMoreElements()) {
			button = buttons.nextElement();
			
			if (button.isSelected()) {
				if (button.getName() == Aggregator.BARNAME) {
					visualizer.makeBarChart(aggregator.getHashtable(), aggregator.getHashtable2());
					break;
				} else {
					visualizer.makeWordCloud(aggregator.getHashtable(), aggregator.getHashtable2());
					break;
				}				
			}
		}
				
		ArrayList<Hashtable<String, Integer>> finalList = new ArrayList<Hashtable<String, Integer>>();
		finalList.add(aggregator.getHashtable());
		finalList.add(aggregator.getHashtable2());
		
		return finalList;
	}
	
	private ArrayList<Thread> createAndRunMapperThreads(List<String> reviews, int queryNumber) {
		
		//partition the arraylist
		ArrayList<Thread> mapperThreads = new ArrayList<Thread>();
		int size = reviews.size();
		
		
		//check for cases when there are more partitions than reviews
			
		//divides a list into lists of length (size/Partitions)
		System.out.println("SIZE");
		System.out.println(size/PARTITIONS);
		if (size/PARTITIONS > 0) {
			for (List<String> review : Lists.partition(reviews, size/PARTITIONS)) {
				
				Thread jsonMapper = new Thread(() -> {
					runLoad(review, queryNumber);
				});
				jsonMapper.start();
				mapperThreads.add(jsonMapper);
				
			}			
		} else {
			//if can't partition into four partitions of at least size 1, just make a single thread to handle all reviews
			Thread jsonMapper = new Thread(() -> {
				runLoad(reviews, queryNumber);
			});
			jsonMapper.start();
			mapperThreads.add(jsonMapper);
					
		}
		
		return mapperThreads;
		
	}
	
	/*
	 * run the python script that performs IR techniques on the raw review string
	 */
	protected void runLoad(List<String> reviews, int queryNumber) {
		
		int count = 0;
		for (String review : reviews) {
			
			if (killThreads) {
				break;
			}
			
			if (count >= TEST_LOAD/PARTITIONS) {
				break;
			}
			
			try {	
				String s = null;
				String[] args = {File.separator+"usr"+File.separator+"local"+File.separator+"bin"+File.separator+"python", "src" + File.separator +"LanguageProcessor.py", review};
				Process p = Runtime.getRuntime().exec(args);
				
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				
				while((s = stdInput.readLine()) != null) {	
					count++;
					final String copy = s;
					Review reviewObj = new Review(copy, queryNumber);	
					aggregator.addToQueue(reviewObj);			
				}
				
				while((s = stdError.readLine()) != null) {
				}	
				
				p.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	
	private class EndOfFileMarker {
		EndOfFileMarker() {		
		}
	}
	
	private void sendThreadsToWindow() {
		Window.getWindow().setAggThread(getAggThread());
		Window.getWindow().setMapThreads(getMapThreads());
	}
	
	protected Thread getAggThread() {
		return aggThread;
	}
	
	protected ArrayList<Thread> getMapThreads() {
		return mapThreads;
	}
	
	protected ArrayList<Thread> getMapThreads2() {
		return mapThreads2;
	}
	
	//called by the Window through the queryHandler when submit is clicked again
	//we set the queryHandler's reference to CA to be null so that QH can create a new CA instance
	protected void setKillThreads() {
		this.queryHandler.setContentAnalyzerNull();
		killThreads = true;
	}
	
	//called by aggregator on every loop in run()
	protected boolean getKillThreads() {
		return killThreads;
	}
	
}
