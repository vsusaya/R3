package core;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class QueryHandler {
    static final String JDBC_DRIVER = "com.postgresql.jdbc.Driver";
    static final String DB_TYPE = "postgresql";
    static final String DB_DRIVER = "jdbc";
    static final String DB_NAME = "store";
    static final String DB_HOST = "localhost";
    static final String DB_PORT = "5432";
    static final String DB_URL = String.format("%s:%s://%s:%s/%s",DB_DRIVER, DB_TYPE, DB_HOST, DB_PORT, DB_NAME);
    static final String DB_USER = "vlad";
    static final String DB_PASSWORD = "vlad";
    static Connection conn;
    protected ContentAnalyzer contentAnalyzer;

    protected QueryHandler() {
    	//contentAnalyzer = new ContentAnalyzer();
    	contentAnalyzer = null;
    }
        
    public static Connection establishConnection() throws Exception {
    	//when running the program for the first time
    	/*
    	try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
        conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);

        return conn;          
    }
        
    public ArrayList<Hashtable<String, Integer>> executeQuery(PreparedStatement range, PreparedStatement filter, String pid, String constraints, String pid2, String constraints2) throws Exception {
    	//contentAnalyzer is set to null when the current instance of CA is told to stop its threads
    	if (contentAnalyzer == null) {
    		contentAnalyzer = new ContentAnalyzer(this);
    	}
    	
    	
    	ArrayList<String> constraintList = cleanConstraints(constraints);
    	ArrayList<String> constraintList2 = cleanConstraints(constraints2);
    	
    	ArrayList<String> reviews = queryDB(range, filter, constraintList, pid);
    	ArrayList<String> reviews2 = null;
    	if (constraintList2 != null && pid2 != null && !(pid2.equals(""))) {
    		reviews2 = queryDB(range, filter, constraintList2, pid2);
    	}
    	    
    	
    	ArrayList<Hashtable<String, Integer>> finalList = contentAnalyzer.analyze(reviews, reviews2);
        
        return finalList;
        
    }
    
    /*
     * Queries the database, using either a range or filter PreparedStatement based on the size of the constraint list
     */
    private ArrayList<String> queryDB(PreparedStatement range, PreparedStatement filter, ArrayList<String> constraintList, String pid) throws NumberFormatException, SQLException {
    	
    	ResultSet rs;
    	
    	if (constraintList.size() == 1) {
        	
        	filter.setString(1, pid);
        	filter.setFloat(2, Float.parseFloat(constraintList.get(0)));
        	rs = filter.executeQuery();
        	
        } else {
        	
        	range.setString(1, pid);
        	range.setFloat(2, Float.parseFloat(constraintList.get(0)));
        	range.setFloat(3, Float.parseFloat(constraintList.get(1)));
        	rs = range.executeQuery();
        	
        }
        
        ArrayList<String> reviews = new ArrayList<String>();
        while(rs.next()) {
        	reviews.add(rs.getString(1));
        }
        
        return reviews;
    	
    }

        
    /*
     * takes the string of user-entered constraints and returns containing 1 or 2 Float values
     * (since the 'overall' attribute in the review table is of type 'real')
     * Fails if the input contains other characters
     */
    public ArrayList<String> cleanConstraints(String constraints) throws Exception{
    	
    	if (constraints == null) {
    		return null;
    	}
    	
    	String[] constraintList = constraints.trim().split(",\\s*");
    	ArrayList<String> cleanedList = new ArrayList<String>();
    	
    	int index = 0;
    	for(String constraint : constraintList) {
    		cleanedList.add(constraint.trim());
    		index++;
    		if (index >= 2) {
    			break;
    		}
    	}
    	return cleanedList;
    }
        
	protected ArrayList<Product> getSampleProducts(PreparedStatement st) throws SQLException {
		
		ResultSet rs = st.executeQuery();
		ArrayList<Product> productList = new ArrayList<Product>();
		
		while(rs.next()) {
			Product product = new Product(rs.getString(1), rs.getString(2), rs.getString(3));
			productList.add(product);
		}
		
		return productList;
		
	}
	
	protected ContentAnalyzer getContentAnalyzer() {
		return contentAnalyzer;
	}
	
	protected void setContentAnalyzerNull() {
		contentAnalyzer = null;
	}
        
}
