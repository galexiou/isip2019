package Experiments;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import DataStructures.IdDuplicates;
import EffectivenessLayer.MemoryBased.QueryBlocking;
import EfficiencyLayer.AbstractEfficiencyMethod;
import EfficiencyLayer.ComparisonRefinement.AbstractDuplicatePropagation;
import EfficiencyLayer.ComparisonRefinement.UnilateralDuplicatePropagation;
import MetaBlocking.BlockFiltering;
import MetaBlocking.ComparisonsBasedBlockPurging;
import Utilities.BlockStatistics;
import Utilities.ExecuteBlockComparisons;
import Utilities.ExecuteBlockComparisonsBatch;
import Utilities.SerializationUtilities;

public class InMemoryExperiments {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		try (Connection conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/giorgos", "giorgos", "")) {

			if (conn != null) {
				System.out.println("Connected to the database!");
			} else {
				System.out.println("Failed to make connection!");
			}

//			String SQL_SELECT_1 = "select * from dirty.data where year=1996";
//			String SQL_SELECT_1 = "select * from dirty.data where authors like '%Mohan%'";
//			String SQL_SELECT_1 = "select * from dirty.data where title='A Theory of Redo Recovery'";
//			String SQL_SELECT_1 = "select * from dirty.data where title='Concurrency Control in Generalized Search Trees'";
//			String SQL_SELECT_1 = "select * from dirty.data where title='CURE: an efficient clustering algorithm for categorical attributes'";
//			String SQL_SELECT_1 = "select * from dirty.data where title like '%cluste%'";
//			String SQL_SELECT_1 = "select * from dirty.data where venue like '%Very Large%'";
//			String SQL_SELECT_1 = "select * from dirty.data where venue like '%SIGMOD%' AND year=2000";
//			String SQL_SELECT_1 = "select * from dirty.data where  authors like '%Papadias%'";
//			String SQL_SELECT_1 = "select * from dirty.data where title='Improved Histograms for Selectivity Estimation of Range Predicates'";
//			String SQL_SELECT_1 = "select * from dirty.data where title='Towards a Cooperative Transaction Model - The Cooperative Activity Model'";
//			String SQL_SELECT_1 = "select * from dirty.experiments where year between 2000 AND 2004"; 
//			String SQL_SELECT_1 = "Select * from dirty.data limit 30095";
//			3343, 6687 10031, 13375, 16719, 20063, 23407 26751, 30095, 33439
//			String SQL_SELECT_1 = "Select * from dirty.data where id=66260";
			String SQL_SELECT_1 = "select * from dirty.data where MOD(id,2)<1";

//			where MOD(id,20)<1    5%
//			where MOD(id,10)<1    10%
//			where MOD(id,6.6)<1   15%
//			where MOD(id,5)<1     20%
//			where MOD(id,4)<1     25%
//			where MOD(id,3.3)<1   30%
//			where MOD(id,2.85)<1  35%
//			where MOD(id,2.5)<1   40%
//			where MOD(id,2.22)<1  45%
//			where MOD(id,2)<1     50%

			PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT_1);
			ResultSet resultSet = preparedStatement.executeQuery();

			double queryStartTime = System.currentTimeMillis();

			PreparedStatement preparedStatement_time = conn.prepareStatement(SQL_SELECT_1);
			ResultSet teSet = preparedStatement_time.executeQuery();
			while (teSet.next()) {

			}
			double queryEndTime = System.currentTimeMillis();
			teSet.close();
			preparedStatement_time.close();

			final List<EntityProfile> entityProfiles = new ArrayList<EntityProfile>();
			Set<IdDuplicates> groundDups = new HashSet<IdDuplicates>();
			Set<Integer> qIds = new HashSet<>();
			while (resultSet.next()) {

				EntityProfile eP = new EntityProfile(resultSet.getString("id"));
				qIds.add(Integer.parseInt(resultSet.getString("id")));

				String title = " ";
				String authors = " ";
				String venue = " ";
				String year = " ";
				if (title != null)
					title = resultSet.getString("title");
				if (authors != null)
					authors = resultSet.getString("authors");
				if (venue != null)
					venue = resultSet.getString("venue");
				if (year != null)
					year = Integer.toString(resultSet.getInt("year"));

				eP.addAttribute("title", title);
				eP.addAttribute("authors", authors);
				eP.addAttribute("venue", venue);
				eP.addAttribute("year", year);

				entityProfiles.add(eP);

				String select = "select * from dirty.matches where iddblp='" + resultSet.getString("identifier")
						+ "' OR idscholar='" + resultSet.getString("identifier") + "'";

//				System.out.println(select);

				PreparedStatement preparedStatement_1 = conn.prepareStatement(select);
				ResultSet resultSet_1 = preparedStatement_1.executeQuery();
				while (resultSet_1.next()) {
//					System.out.println(resultSet_1.getInt("id_d") + "    " + resultSet_1.getInt("id_s"));
					IdDuplicates idd = new IdDuplicates(resultSet_1.getInt("id_d"), resultSet_1.getInt("id_s"));
					groundDups.add(idd);

				}

			}
			double queryTime = queryEndTime - queryStartTime;
			System.out.println("Query Entity Profiles\t:\t" + entityProfiles.size());

			final AbstractDuplicatePropagation duplicatePropagation = new UnilateralDuplicatePropagation(groundDups);

			System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

//			SerializationUtilities.storeSerializedObject(entityProfiles, "/Users/giorgos/Desktop/DBLP-SCHOLAR/scholar_Profiles");

			List<EntityProfile>[] profiles = new List[1];
			profiles[0] = (List<EntityProfile>) SerializationUtilities
					.loadSerializedObject("/Users/giorgos/Desktop/DBLP-SCHOLAR/scholar_Profiles");

			long startingTime = System.currentTimeMillis();

			QueryBlocking queryBlockIndex = new QueryBlocking(entityProfiles);
			queryBlockIndex.buildQueryBlocks();
			List<AbstractBlock> blocks = queryBlockIndex
					.joinBlockIndices("/Users/giorgos/Desktop/DBLP-SCHOLAR/Bblocks_SER");

//			queryBlocking.storeBlockIndex("/Users/giorgos/Desktop/DBLP-SCHOLAR/Bblocks_SER");

			System.out.println("QueryBLocking - Blocks Ready\t:\t" + blocks.size());

			AbstractEfficiencyMethod blockPurging = new ComparisonsBasedBlockPurging();
			blockPurging.applyProcessing(blocks);
			
			

			System.out.println("B Purging - Blocks Ready\t:\t" + blocks.size());

			if (blocks.size() > 10) {
				BlockFiltering bFiltering = new BlockFiltering(0.30);
				bFiltering.applyProcessing(blocks);
				System.out.println("B Filtering - Blocks Ready\t:\t" + blocks.size());
			}

//			EdgePruning edgePruning = new EdgePruning(WeightingScheme.ARCS);
//			edgePruning.applyProcessing(blocks);
//			System.out.println("EP - Blocks Ready\t:\t" + blocks.size());

//			EfficientEdgePruning eEP = new EfficientEdgePruning();
//			eEP.applyProcessing(blocks);
//			System.out.println("BF - Blocks Ready\t:\t" + blocks.size());

//			ComparisonPropagation cp = new ComparisonPropagation();
//			cp.applyProcessing(blocks);
//			System.out.println("BF - Blocks Ready\t:\t" + blocks.size());

//			ComparisonPruning cPruning = new ComparisonPruning();
//			cPruning.applyProcessing(blocks);
//			System.out.println("Blocks Ready\t:\t" + blocks.size());

			long endingTime = System.currentTimeMillis();
			double overheadTime = endingTime - startingTime;
			long startingResTime = System.currentTimeMillis();

			ExecuteBlockComparisons ebc = new ExecuteBlockComparisons(profiles);
//			HashMap<Integer, Set<Integer>> revUF = ebc.comparisonExecutionAll(blocks, qIds);
//			Double bruteTime = ebc.comparisonExecutionBruteForce(qIds);
			HashMap<Integer, Set<Integer>> revUF1 = ebc.comparisonExecutionAll(blocks, qIds);
//			ExecuteBlockComparisonsBatch ebcb= new ExecuteBlockComparisonsBatch(profiles);
//			ebcb.comparisonExecution(blocks);
			long endingResTime = System.currentTimeMillis();
//			System.out.println("Entities from Query " + qIds.size() + "  " + qIds.toString());
//			System.out.println("Entities after Resolution " + revUF.toString());
			long resolutionTime = endingResTime - startingResTime;
			System.out.println("Entities from Query\t:\t" + qIds.size());
			System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());
			System.out.println("Query time\t:\t" + queryTime);
			System.out.println("Overhead time\t:\t" + overheadTime);
			System.out.println("Resolution time\t:\t" + resolutionTime);
			double totalTime = resolutionTime + overheadTime + queryTime;
			System.out.println("Total time\t:\t" + totalTime);

			duplicatePropagation.resetDuplicates();
			BlockStatistics bStats = new BlockStatistics(blocks, duplicatePropagation);
			bStats.applyProcessing();

		} catch (

		SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}