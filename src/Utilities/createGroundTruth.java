package Utilities;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import DataStructures.IdDuplicates;
import EfficiencyLayer.ComparisonRefinement.AbstractDuplicatePropagation;
import EfficiencyLayer.ComparisonRefinement.UnilateralDuplicatePropagation;
public class createGroundTruth {



		public static void main(String[] args) {

			try (Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/giorgos", "giorgos", "")) {

				if (conn != null) {
					System.out.println("Connected to the database!");
				} else {
					System.out.println("Failed to make connection!");
				}

				
				 String csvFile = "/Users/giorgos/Desktop/DBLP-SCHOLAR/DBLP-Scholar_perfectMapping.csv";
			        BufferedReader br = null;
			        String line = "";
			        String cvsSplitBy = ",";
				
			        br = new BufferedReader(new FileReader(csvFile));
			        Set<IdDuplicates> test = new HashSet<IdDuplicates>();
			        
			        
		            while ((line = br.readLine()) != null) {

		            	
		                // use comma as separator
		                String[] match = line.split(cvsSplitBy);
		                
//		                System.out.println(match[0]+"    "+match[1]);
		         

		        String clean1= match[0].replaceAll("\"", "'");
		        String clean2= match[1].replaceAll("\"", "'");
				
		        String SQL_SELECT_1 = "Select * from dirty.data where identifier="+clean1;
		        String SQL_SELECT_2 = "Select * from dirty.data where identifier="+clean2;

		        int id_1=0;
		        int id_2=0;
		        
		        PreparedStatement preparedStatement_1 = conn.prepareStatement(SQL_SELECT_1);
		        ResultSet resultSet_1 = preparedStatement_1.executeQuery();
		        
		        PreparedStatement preparedStatement_2 = conn.prepareStatement(SQL_SELECT_2);
		        ResultSet resultSet_2 = preparedStatement_2.executeQuery();
//		        final List<EntityProfile> entityProfiles= new ArrayList<EntityProfile>();
		        while(resultSet_1.next()) {
		        	
		        	id_1=resultSet_1.getInt("id");
		        }
	while(resultSet_2.next()) {
		        	
		        	id_2=resultSet_2.getInt("id");
		        }
		        


//				System.out.println(id_1+"    "+id_2);
		        IdDuplicates idDuplicates = new IdDuplicates(id_1, id_2);
		        test.add(idDuplicates);
		        	
		        
		        	//	        	EntityProfile eP = new EntityProfile(resultSet.getString("id"));
//		        	String id = resultSet.getString("id");
//		        	String title = resultSet.getString("title");
//		        	String authors = resultSet.getString("authors");
//		        	String venue = resultSet.getString("venue");
//		        	Integer year = resultSet.getInt("year");
//		        	
//		        	eP.addAttribute("title",resultSet.getString("title"));
//		        	eP.addAttribute("authros", resultSet.getString("authors"));
//		        	eP.addAttribute("venue", resultSet.getString("venue"));
//		        	eP.addAttribute("year", Integer.toString(resultSet.getInt("year")));
//		        	
//		        	entityProfiles.add(eP);
//		        	
		        	
//		        	System.out.println(id+" , "+title+" , "+authors+" , "+venue+" , "+year);
//		        }
		        
		            }
	                br.close();
	                
	                final AbstractDuplicatePropagation duplicatePropagation = new UnilateralDuplicatePropagation(test);
	                SerializationUtilities.storeSerializedObject(test, "/Users/giorgos/Desktop/DBLP-SCHOLAR/groundTruth_scholar");

			}catch(

		SQLException e)
		{
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}catch(
		Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
