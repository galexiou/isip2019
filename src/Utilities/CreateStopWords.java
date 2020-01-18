package Utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class CreateStopWords {

	public static void main(String[] args) throws IOException {

			
			 String csvFile = "/Users/giorgos/Desktop/DBLP-SCHOLAR/stopwords.txt";
		        BufferedReader br = null;
		        String line = "";
		        
			
		        br = new BufferedReader(new FileReader(csvFile));
		        
		        HashSet<String> stopwords = new HashSet<>();
	            while ((line = br.readLine()) != null) {

	            	
	                // use comma as separator
	               
	                
	                System.out.println(line);
	         
stopwords.add(line.trim());

	            }
	            
	           SerializationUtilities.storeSerializedObject(stopwords, "/Users/giorgos/Desktop/DBLP-SCHOLAR/stopwords_SER"); 
br.close();		
	
	
}
}