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
	
	static final int MINIMUM = 5;
	static final int MAX_RESULTS = 15;
	
	public Visualizer() {
	}
	
	
	//for interactive version, check that the key from the hashtable does not already exist?
	//necessary?
	//if too slow, consider editing so that only a hashtable of diffs is sent
	public void makeBarChart(Hashtable<String, Integer> countsTable) {
		
		HashMap<String, Integer> counts = hashMapTopK(countsTable);
		
		DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
		
		for(Map.Entry<String, Integer> entry : counts.entrySet()) {
			
			if(entry.getValue() < MINIMUM) {
				continue;
			}
			
			objDataset.setValue(entry.getValue(), "Word", entry.getKey());
			//title, x-axis label, y-axis label, dataset, Plot orientation, show legend, use tooltips, generate URLs
			JFreeChart barChart = ChartFactory.createBarChart("Word Count", "Words", "Count", objDataset, PlotOrientation.VERTICAL, false, true, false);
			
			//make x-axis labels diagonal
			CategoryPlot plot = barChart.getCategoryPlot();
			CategoryAxis xAxis = (CategoryAxis) plot.getDomainAxis();
			xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
			
			ChartPanel cPanel = new ChartPanel(barChart);
			setChartPanelPref(cPanel);
			
			
			Window.getWindow().setOutputPanelChart(cPanel);
			
		}
		
		
		//setOutputPanelChart();
		
	}
	
	public void makeBarChartInteractive(String countString) {
		
		HashMap<String, Integer> counts = stringToHashMapTopK(countString);
		
		DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
		
		for(Map.Entry<String, Integer> entry : counts.entrySet()) {
			
			if(entry.getValue() < MINIMUM) {
				continue;
			}
			
			objDataset.setValue(entry.getValue(), "Word", entry.getKey());
			
		}	
		
		//title, x-axis label, y-axis label, dataset, Plot orientation, show legend, use tooltips, generate URLs
		JFreeChart barChart = ChartFactory.createBarChart("Word Count", "Words", "Count", objDataset, PlotOrientation.VERTICAL, false, true, false);
		
		//make x-axis labels diagonal
		CategoryPlot plot = barChart.getCategoryPlot();
		CategoryAxis xAxis = (CategoryAxis) plot.getDomainAxis();
		xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		
		ChartPanel cPanel = new ChartPanel(barChart);
		setChartPanelPref(cPanel);
		
		Window.getWindow().setOutputPanelChartInteractive(cPanel);
	}
	
	public void makeWordCloud(Hashtable<String, Integer> counts) {
		
		Cloud cloud = new Cloud();
		
		//add all words and their weights (as Tags) to cloud
		for(Map.Entry<String, Integer> entry : counts.entrySet()) {
			if(entry.getValue() < MINIMUM) {
				continue;
			}
			
			cloud.addTag(new Tag(entry.getKey(), entry.getValue()));
				
		}
		
		//send cloud to Window so it can create labels 
		Window.getWindow().setOutputPanelCloud(cloud);
		
	}
	
public void makeWordCloudInteractive(String countString) {
		
		HashMap<String, Integer> counts = stringToHashMap(countString);
	
		Cloud cloud = new Cloud();
		
		//add all words and their weights (as Tags) to cloud
		for(Map.Entry<String, Integer> entry : counts.entrySet()) {
			if(entry.getValue() < MINIMUM) {
				continue;
			}
			
			cloud.addTag(new Tag(entry.getKey(), entry.getValue()));
				
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

