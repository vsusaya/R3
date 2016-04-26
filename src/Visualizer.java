import java.awt.Dimension;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONObject;
import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;


/*
 * Has no stateful data
 */
public class Visualizer {
	
	static final int MINIMUM = 3;
	static final int MAX_RESULTS = 15;
	
	public Visualizer() {
	}
	
	
	//for interactive version, check that the key from the hashtable does not already exist?
	//necessary?
	//if too slow, consider editing so that only a hashtable of diffs is sent
	public void makeBarChart(Hashtable<String, Integer> countsTable, Hashtable<String, Integer> countsTable2) {
		
		HashMap<String, Integer> counts = hashMapTopK(countsTable);
		HashMap<String, Integer> counts2 = null;
		if (countsTable2 != null) {
			counts2 = hashMapTopK(countsTable2);
		}
		
		DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
		
		for(Map.Entry<String, Integer> entry : counts.entrySet()) {
			
			if(entry.getValue() < MINIMUM) {
				continue;
			}
					
			objDataset.setValue(entry.getValue(), "Product1", entry.getKey());
			/*
			//title, x-axis label, y-axis label, dataset, Plot orientation, show legend, use tooltips, generate URLs
			JFreeChart barChart = ChartFactory.createBarChart("Word Count", "Words", "Count", objDataset, PlotOrientation.VERTICAL, true, true, false);
			
			//make x-axis labels diagonal
			CategoryPlot plot = barChart.getCategoryPlot();
			CategoryAxis xAxis = (CategoryAxis) plot.getDomainAxis();
			xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
			
			ChartPanel cPanel = new ChartPanel(barChart);
			setChartPanelPref(cPanel);
					
			Window.getWindow().setOutputPanelChart(cPanel);
			*/	
		}
		
		if (counts2 != null) {
			for(Map.Entry<String, Integer> entry : counts2.entrySet()) {
				if(entry.getValue() < MINIMUM) {
					continue;
				}
				objDataset.setValue(entry.getValue(), "Product2", entry.getKey());	
			}
		}
		
		//title, x-axis label, y-axis label, dataset, Plot orientation, show legend, use tooltips, generate URLs
		JFreeChart barChart = ChartFactory.createBarChart("Word Count", "Words", "Count", objDataset, PlotOrientation.VERTICAL, true, true, false);
		
		//make x-axis labels diagonal
		CategoryPlot plot = barChart.getCategoryPlot();
		CategoryAxis xAxis = (CategoryAxis) plot.getDomainAxis();
		xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		
		ChartPanel cPanel = new ChartPanel(barChart);
		setChartPanelPref(cPanel);
		
		
		Window.getWindow().setOutputPanelChart(cPanel);
		
	}
	
	public void makeBarChartInteractive(String countString, String countString2) {
		
		HashMap<String, Integer> counts = stringToHashMapTopK(countString);
		HashMap<String, Integer> counts2 = null;
		if (countString2 != null || countString2 != "") {
			counts2 = stringToHashMapTopK(countString2);
		}
		
		DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
		
		for(Map.Entry<String, Integer> entry : counts.entrySet()) {
			if(entry.getValue() < MINIMUM) {
				continue;
			}
			objDataset.setValue(entry.getValue(), "Product1", entry.getKey());		
		}	
		
		if (counts2 != null) {
			for(Map.Entry<String, Integer> entry : counts2.entrySet()) {
				if(entry.getValue() < MINIMUM) {
					continue;
				}
				objDataset.setValue(entry.getValue(), "Product2", entry.getKey());	
			}
		}
	
		//title, x-axis label, y-axis label, dataset, Plot orientation, show legend, use tooltips, generate URLs
		JFreeChart barChart = ChartFactory.createBarChart("Word Count", "Words", "Count", objDataset, PlotOrientation.VERTICAL, true, true, false);
		
		//make x-axis labels diagonal
		CategoryPlot plot = barChart.getCategoryPlot();
		CategoryAxis xAxis = (CategoryAxis) plot.getDomainAxis();
		xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		
		ChartPanel cPanel = new ChartPanel(barChart);
		setChartPanelPref(cPanel);
		
		Window.getWindow().setOutputPanelChartInteractive(cPanel);
	}
	
	public void makeWordCloud(Hashtable<String, Integer> counts, Hashtable<String, Integer> counts2) {
		
		Cloud cloud = new Cloud();
		
		//add all words and their weights (as Tags) to cloud
		for(Map.Entry<String, Integer> entry : counts.entrySet()) {
			if(entry.getValue() < MINIMUM) {
				continue;
			}
			
			cloud.addTag(new Tag(entry.getKey(), String.format("%d", ContentAnalyzer.QUERY_1), entry.getValue()));
				
		}
		
		if (counts2 != null) {
			for(Map.Entry<String, Integer> entry : counts2.entrySet()) {
				if(entry.getValue() < MINIMUM) {
					continue;
				}
				
				//adding symbols to the end of the word so that the cloud does not combine the totals of words that appeared in both queries
				//the symbol will be removed in the in the Window method below
				cloud.addTag(new Tag(entry.getKey() + "**", String.format("%d", ContentAnalyzer.QUERY_2) ,entry.getValue()));			
			}
		}
		
		//send cloud to Window so it can create labels 
		Window.getWindow().setOutputPanelCloud(cloud);
		
	}
	
public void makeWordCloudInteractive(String countString, String countString2) {
		
		HashMap<String, Integer> counts = stringToHashMap(countString);
	
		HashMap<String, Integer> counts2 = null;
		if (countString2 != null || countString2 != "") {
			counts2 = stringToHashMap(countString2);
		}
		
		Cloud cloud = new Cloud();
		
		//add all words and their weights (as Tags) to cloud
		for(Map.Entry<String, Integer> entry : counts.entrySet()) {
			if(entry.getValue() < MINIMUM) {
				continue;
			}
			
			cloud.addTag(new Tag(entry.getKey(), String.format("%d", ContentAnalyzer.QUERY_1) ,entry.getValue()));
				
		}
		
		if (counts2 != null) {
			for(Map.Entry<String, Integer> entry : counts2.entrySet()) {
				if(entry.getValue() < MINIMUM) {
					continue;
				}
				
				//adding symbols to the end of the word so that the cloud does not combine the totals of words that appeared in both queries
				//the symbol will be removed in the in the Window method below
				cloud.addTag(new Tag(entry.getKey() + "**", String.format("%d", ContentAnalyzer.QUERY_2) ,entry.getValue()));
					
			}
		}
		
		//send cloud to Window so it can create labels 
		Window.getWindow().setOutputPanelCloudInteractive(cloud);
		
	}
	
	
	private void setChartPanelPref(ChartPanel cPanel) {
		cPanel.setPreferredSize(new Dimension(502, 225));
		cPanel.setMouseWheelEnabled(true);
		cPanel.setDomainZoomable(true);
		cPanel.setVisible(true);
		//cPanel.validate();
	}
	
	protected HashMap<String, Integer> stringToHashMap(String countString) {
		JSONObject jObject = new JSONObject(countString);
		Iterator<String> keys = jObject.keys();
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		while (keys.hasNext()) {
			String key = keys.next();
			Integer value = jObject.getInt(key);
			counts.put(key, value);
		}
		
		return counts;
	}
	
	private HashMap<String, Integer> stringToHashMapTopK(String countString) {
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(countString);
		} catch (org.json.JSONException je) {
			return new HashMap<String, Integer>();
		}
		
		Iterator<String> keys = jObject.keys();
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		int inMap = 0;
		Integer minimum = null;
		String min_key = null;
		
		while (keys.hasNext()) {
			String key = keys.next();
			Integer value = jObject.getInt(key);
			
			//always insert if less than MAX_RESULTS
			//otherwise, only insert if we get a word with a count higher than the minimum
			//then evict the key tied to that minimum
			if (inMap < MAX_RESULTS) {
				counts.put(key, value);
				inMap++;
				
				if (minimum == null || value < minimum) {
					minimum = value;
					min_key = key;
				}
			} else if (value > minimum) {
				counts.remove(min_key);
				counts.put(key, value);
				
				//find new minimum
				Entry<String, Integer> mapMin = null;
				for (Entry<String, Integer> entry : counts.entrySet()) {
					if (mapMin == null || mapMin.getValue() > entry.getValue()) {
						mapMin = entry;
					}
				}
				
				minimum = mapMin.getValue();
				min_key = mapMin.getKey();
				
			}
		}
		
		return counts;
	}

	private HashMap<String, Integer> hashMapTopK(Hashtable<String, Integer> countMap) {

		int inMap = 0;
		Integer minimum = null;
		String min_key = null;
		
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		for (Entry<String, Integer> entry : countMap.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			
			//always insert if less than MAX_RESULTS
			//otherwise, only insert if we get a word with a count higher than the minimum
			//then evict the key tied to that minimum
			if (inMap < MAX_RESULTS) {
				counts.put(key, value);
				inMap++;
				
				if (minimum == null || value < minimum) {
					minimum = value;
					min_key = key;
				}
			} else if (value > minimum) {
				counts.remove(min_key);
				counts.put(key, value);
				
				//find new minimum
				Entry<String, Integer> mapMin = null;
				for (Entry<String, Integer> countsEntry : counts.entrySet()) {
					if (mapMin == null || mapMin.getValue() > countsEntry.getValue()) {
						mapMin = countsEntry;
					}
				}
				
				minimum = mapMin.getValue();
				min_key = mapMin.getKey();
				
			}
		}
		
		return counts;
	}
	
}

