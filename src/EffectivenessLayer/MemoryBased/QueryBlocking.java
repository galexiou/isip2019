package EffectivenessLayer.MemoryBased;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import DataStructures.AbstractBlock;
import DataStructures.Attribute;
import DataStructures.EntityProfile;
import Utilities.Constants;
import Utilities.SerializationUtilities;

public class QueryBlocking extends QueryBasedBlocking implements Constants {

	private Map<String, Set<Integer>> invertedIndex;

	public QueryBlocking(List<EntityProfile> profiles1) {
		super(profiles1, "In-memory Query Token Blocking");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Set<Integer>> indexEntities(int sourceId, List<EntityProfile> profiles) {
		invertedIndex = new HashMap<String, Set<Integer>>();
		HashSet<String> stopwords = (HashSet<String>) SerializationUtilities
				.loadSerializedObject("/Users/giorgos/Desktop/DBLP-SCHOLAR/stopwords_SER");
		for (EntityProfile profile : profiles) {
			for (Attribute attribute : profile.getAttributes()) {
				if (attribute.getValue() == null)
					continue;
				String cleanValue = attribute.getValue().replaceAll("_", " ").trim().replaceAll("\\s*,\\s*$", "")
						.toLowerCase();
				for (String token : cleanValue.split("[\\W_]")) {
					if (2 < token.trim().length()) {
						if (stopwords.contains(token.toLowerCase()))
							continue;
						Set<Integer> termEntities = invertedIndex.computeIfAbsent(token.trim(),
								x -> new HashSet<Integer>());

						termEntities.add(Integer.parseInt(profile.getEntityUrl()));

					}
				}
			}

		}

//		final Map<String, Set<Integer>> bBlocks = (Map<String, Set<Integer>>) SerializationUtilities
//				.loadSerializedObject("/Users/giorgos/Desktop/DBLP-SCHOLAR/Bblocks_SER");

//		bBlocks.keySet().retainAll(invertedIndex.keySet());
//			System.out.println(bBlocks.keySet().toString());
//			System.out.println(invertedIndex.keySet().toString());
		return invertedIndex;
//	        return invertedIndex;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AbstractBlock> joinBlockIndices(String path) {
		final Map<String, Set<Integer>> bBlocks = (Map<String, Set<Integer>>) SerializationUtilities
				.loadSerializedObject(path);
		bBlocks.keySet().retainAll(invertedIndex.keySet());
		return parseIndex(bBlocks);

	}

	public void storeBlockIndex(String path) {
		SerializationUtilities.storeSerializedObject(invertedIndex, path);

	}
}
