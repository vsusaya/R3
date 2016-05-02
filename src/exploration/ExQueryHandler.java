package exploration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.json.JSONObject;

import com.google.common.collect.ObjectArrays;

import core.Review;

public class ExQueryHandler {

	static final String ERROR = "ERROR";
	static final String NAME = "name";
	static final String FEATURES = "features";
	static final int NUM_COLUMNS = 3;
	
	
	protected void execute(String searchTerms) {
		
		String[] searchTermArray = searchTerms.split(" ");
		String output = search(searchTermArray);
		showOutput(output);
	}
	
	/*
	 * Returns a JSON string of product Ids, each with a name and features field
	 */
	private String search(String[] searchTermArray) {
		
		
		if (searchTermArray[0].equals("TEST")) {
			performCleanup();
			return "";
		}
		
		
		try {	
			String output = "";
			String s = null;
			String[] noArgs = {File.separator+"usr"+File.separator+"local"+File.separator+"bin"+File.separator+"python", "src" + File.separator +"Search.py"};
			String[] args = ObjectArrays.concat(noArgs, searchTermArray, String.class);
			
			Process p = Runtime.getRuntime().exec(args);
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			while((s = stdInput.readLine()) != null) {	
				System.out.println("output");
				System.out.println(s);
				output = s;			
			}
			
			while((s = stdError.readLine()) != null) {
			}	
			
			p.destroy();
			return output;
			
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR;
		}	

	}
	
	private void showOutput(String output) {
		
		try {
			
			JSONObject jObject = new JSONObject(output);
			Iterator<String> keys = jObject.keys();
			
			Object rowData[][] = new Object[jObject.length()][NUM_COLUMNS];
			
			int i = 0;
			while (keys.hasNext()) {
				String id = keys.next();
				JSONObject fields = jObject.getJSONObject(id);
				String name = fields.getString(NAME);
				String features = fields.getString(FEATURES);
				//counts.put(key, value);
				rowData[i][0] = id;
				rowData[i][1] = name;
				rowData[i][2] = features;
				i++;
			}
				
			//send objects to Window
			ExplorationWindow exWindow = ExplorationWindow.getExplorationWindow();
			exWindow.makeSearchTable(rowData);
			
		} catch (Exception e) {
			e.printStackTrace();
			
			Object rowData[][] = new Object[1][NUM_COLUMNS];
			rowData[0][0] = "Not found";
			rowData[0][1] = "Not found";
			rowData[0][2] = "Not found";
			ExplorationWindow exWindow = ExplorationWindow.getExplorationWindow();
			exWindow.makeSearchTable(rowData);
		}
		

	}
	
	private void performCleanup() {
		
		boolean isKey = true;
		String key = null;
		
			try {
				String output = "";
				String s = null;
				String[] noArgs = {File.separator+"usr"+File.separator+"local"+File.separator+"bin"+File.separator+"python", "src" + File.separator +"Test.py"};
				
				Process p = Runtime.getRuntime().exec(noArgs);
				
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				
				
				while((s = stdInput.readLine()) != null) {	
					
					try {
						if (isKey) {
							key = s;
							isKey = false;
						} else {
							
							//try to convert to JSON object
							JSONObject jObject = new JSONObject(s);
							isKey = true;
						}
					} catch (Exception e) {
						isKey = true;
						System.out.println(key);
						e.printStackTrace();
					}							
				}
				
				while((s = stdError.readLine()) != null) {
				}	
				
				p.destroy();
			} catch (Exception ie) {
				ie.printStackTrace();
			}
		
		
		
		
	}
	
}
