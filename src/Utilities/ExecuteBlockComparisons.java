package Utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import DataStructures.AbstractBlock;
import DataStructures.Attribute;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;

public class ExecuteBlockComparisons {

	private HashMap<Integer, EntityProfile> dataset = new HashMap<>();

	public ExecuteBlockComparisons(List<EntityProfile>[] profiles) {
		for (EntityProfile ep : profiles[0]) {
			dataset.put(Integer.parseInt(ep.getEntityUrl()), ep);
		}
		System.out.println("Dataset size\t:\t" + dataset.size());
	}

	public HashMap<Integer, Set<Integer>> comparisonExecutionAll(List<AbstractBlock> blocks, Set<Integer> qIds) {
		int results = 0;
		int comparisons = 0;
		UnionFind uFind = new UnionFind(qIds);
		Set<String> matches = new HashSet<>();
		Set<AbstractBlock> nBlocks = new HashSet<>(blocks);
		Set<String> uComparisons = new HashSet<>();

		for (AbstractBlock block : nBlocks) {
			ComparisonIterator iterator = block.getComparisonIterator();
			while (iterator.hasNext()) {
				Comparison comparison = iterator.next();

				if (!qIds.contains(comparison.getEntityId1()) && !qIds.contains(comparison.getEntityId2()))
					continue;
				String uniqueComp = "";
				if (comparison.getEntityId1() > comparison.getEntityId2())
					uniqueComp = comparison.getEntityId1() + "u" + comparison.getEntityId2();
				else
					uniqueComp = comparison.getEntityId2() + "u" + comparison.getEntityId1();
				if (uComparisons.contains(uniqueComp))
					continue;
				uComparisons.add(uniqueComp);

				double similarity = ProfileComparison.getJaroSimilarity(
						dataset.get(comparison.getEntityId1()).getAttributes(),
						dataset.get(comparison.getEntityId2()).getAttributes());

//				double similarity = ProfileComparison.getJaccardSimilarity(
//						dataset.get(comparison.getEntityId1()).getAttributes(),
//						dataset.get(comparison.getEntityId2()).getAttributes());
				comparisons++;

//				if(uniqueComp==51537+"u"+65852) {
//					System.out.G@lex10u(comparison.getEntityId1()+" "+comparison.getEntityId2()+" "+similarity);
//				}
				if (similarity >= 0.92) {

					matches.add(uniqueComp);
					uFind.union(comparison.getEntityId1(), comparison.getEntityId2());
				}

			}
		}
		HashMap<Integer, Set<Integer>> revUF = new HashMap<>();
//		for (int child : uFind.getParent().keySet()) {
//			revUF.computeIfAbsent(uFind.getParent().get(child), x -> new HashSet<>()).add(child);
//		}
//		System.out.println("###########################################################################\n");
//		for (int id : revUF.keySet()) {
//			for (int idInner : revUF.get(id)) {
//				System.out.println("id:\t" + dataset.get(idInner).getEntityUrl());
//				results++;
//				for (Attribute attribute : dataset.get(idInner).getAttributes()) {
//					if (attribute.getValue() != null)
//						System.out.println(attribute.getName() + ":\t" + attribute.getValue());
//				}
//				System.out.println(
//						"----------------------------------------------------------------------------------------------");
//
//			}
//			System.out.println("###########################################################################\n");
//		}

		System.out.println("Matches Found " + matches.size() + "\nResults presented " + results);
		System.out.println("Total Comparisons " + comparisons);

		return revUF;
	}

	public HashMap<Integer, Set<Integer>> comparisonExecution(List<AbstractBlock> blocks, Set<Integer> qIds) {
		int matches = 0;
		int comparisonsC = 0;
		UnionFind uFind = new UnionFind(qIds);
		Set<String> comparisons = new HashSet<>();
		Set<AbstractBlock> nBlocks = new HashSet<>(blocks);
		for (AbstractBlock block : blocks) {

			UnilateralBlock uniBlock = (UnilateralBlock) block;
			
//			System.out.println("Block No Comp " + block.getNoOfComparisons());

			for (int entity : uniBlock.getEntities()) {
				for (int qid : qIds) {

					if (qid == entity)
						continue;

					String key = "";
					if (entity > qid)
						key = entity + "comp" + qid;
					else
						key = qid + "comp" + entity;
					if (comparisons.contains(key))
						continue;
					comparisons.add(key);

					double similarity = ProfileComparison.getJaroSimilarity(dataset.get(qid).getAttributes(),
							dataset.get(entity).getAttributes());
					comparisonsC++;
					

					if (similarity > 0.85) {
						matches++;
						uFind.union(qid, entity);
					}

				}

			}
//			System.out.println("Current Comp " + comparisonsC);
		}
		
//		}

		HashMap<Integer, Set<Integer>> revUF = new HashMap<>();
		for (int child : uFind.getParent().keySet()) {
			revUF.computeIfAbsent(uFind.getParent().get(child), x -> new HashSet<>()).add(child);
		}
		System.out.println("###########################################################################\n");
		for (int id : revUF.keySet()) {
			for (int idInner : revUF.get(id)) {
				System.out.println("id -- " + dataset.get(idInner).getEntityUrl());
				for (Attribute attribute : dataset.get(idInner).getAttributes()) {
					System.out.println(attribute.getName() + " -- " + attribute.getValue());
				}

			}
			System.out.println("###########################################################################\n");
		}

		System.out.println("Matches Found " + matches);
		System.out.println("Total Comparisons " + comparisonsC);

		return revUF;

	}

	public double comparisonExecutionBruteForce(Set<Integer> qIds) {
		long startingResTime = System.currentTimeMillis();
		int comparisons = 0;
		Set<String> uComparisons = new HashSet<>();

		for (int pId : dataset.keySet()) {
//			if (!qIds.contains(pId))
//				continue;

			for (int qid : qIds) {
				if (qid == pId)
					continue;
				String uniqueComp = "";
				if (qid > pId)
					uniqueComp = qid + "u" + pId;
				else
					uniqueComp = pId + "u" + qid;
				if (uComparisons.contains(uniqueComp))

					if (uComparisons.contains(uniqueComp))
						continue;
				uComparisons.add(uniqueComp);

				ProfileComparison.getJaroSimilarity(dataset.get(qid).getAttributes(), dataset.get(pId).getAttributes());

//				double similarity = ProfileComparison.getJaccardSimilarity(
//						dataset.get(comparison.getEntityId1()).getAttributes(),
//						dataset.get(comparison.getEntityId2()).getAttributes());
				comparisons++;

			}
		}

//		System.out.println("Matches Found " + matches.size());
		System.out.println("Total Comparisons " + comparisons);
		long endingResTime = System.currentTimeMillis();
		long resolutionTime = endingResTime - startingResTime;
		return resolutionTime;
	}

}