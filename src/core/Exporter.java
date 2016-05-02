package core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.json.JSONObject;

public class Exporter implements Runnable{

	String output;
	
	public Exporter(String output) {
		this.output = output;
	}
	
	@Override
	public void run() {
		
		
		try {
			FileWriter writer = new FileWriter("output.csv");
			writer.append("word");
			writer.append(",");
			writer.append("count");
			writer.append("\n");
			
			String[] bothMaps = getOutput().split("\n");
			
			for (String someMap : bothMaps) {
				
				JSONObject jObject = null;
				String jsonMap = null;
				
				try {
					jsonMap = someMap.replaceAll("=", ":");
					jObject = new JSONObject(jsonMap);
					Iterator<String> it = jObject.keys();
					
					while(it.hasNext()) {
						String id = it.next();
						int count = jObject.getInt(id);
						writer.append(id);
						writer.append(",");
						writer.append(Integer.toString(count));
						writer.append("\n");
					}
							
				} catch (Exception je) {
					System.out.println(jsonMap);
					je.printStackTrace();
					writer.append("n/a");
					writer.append(",");
					writer.append("n/a");
					writer.append("\n");
				}
				
				writer.append("end of map");
				writer.append(",");
				writer.append("end of map");
				writer.append("\n");		
			}
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

	public String getOutput() {
		return output;
	}
	
}
