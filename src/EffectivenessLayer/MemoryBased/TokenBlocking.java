package EffectivenessLayer.MemoryBased;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import DataStructures.Attribute;
import DataStructures.EntityProfile;
import Utilities.Constants;
import Utilities.SerializationUtilities;

public class TokenBlocking extends IndexBasedBlocking implements Constants {

	public TokenBlocking(List<EntityProfile> profiles1, List<EntityProfile> profiles2) {
		super(profiles1, profiles2, "In-memory Token Blocking");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Set<Integer>> indexEntities(int sourceId, List<EntityProfile> profiles) {
//		int index = 0;
		Map<String, Set<Integer>> invertedIndex = new HashMap<String, Set<Integer>>();
		HashSet<String> stopwords = (HashSet<String>) SerializationUtilities
				.loadSerializedObject("/Users/giorgos/Desktop/DBLP-SCHOLAR/stopwords_SER");
		for (EntityProfile profile : profiles) {
			for (Attribute attribute : profile.getAttributes()) {
				if (attribute.getValue() == null)
					continue;
				String cleanValue = attribute.getValue().replaceAll("_", " ").trim().replaceAll("\\s*,\\s*$", "").toLowerCase();
//				System.out.println(cleanValue);
				for (String token : cleanValue.split("[\\W_]")) {
					if (2 < token.trim().length()) {
						if (stopwords.contains(token.toLowerCase()))
							continue;
//						System.out.println("token  "+token);
						Set<Integer> termEntities = invertedIndex.computeIfAbsent(token.trim(),
								x -> new HashSet<Integer>());
						// invertedIndex.get(token.trim());
//						invertedIndex.put(token.trim(), termEntities);
//						if (termEntities == null) {
//							termEntities = new HashSet<Integer>();

//							invertedIndex.put(token, termEntities);
//						}
//						System.out.println("id  "+profile.getEntityUrl());
						termEntities.add(Integer.parseInt(profile.getEntityUrl()));
//						termEntities.add(index);
					}
				}
			}
//			index++;
		}

//		SerializationUtilities.storeSerializedObject(invertedIndex, "/Users/giorgos/Desktop/DBLP-SCHOLAR/Bblocks_SER");
		final Map<String, Set<Integer>> bBlocks = (Map<String, Set<Integer>>) SerializationUtilities
				.loadSerializedObject("/Users/giorgos/Desktop/DBLP-SCHOLAR/Bblocks_SER");

		bBlocks.keySet().retainAll(invertedIndex.keySet());
//		System.out.println(bBlocks.keySet().toString());
//		System.out.println(invertedIndex.keySet().toString());
		return bBlocks;
//        return invertedIndex;
	}

	@Override
	public void buildQueryBlocks() {
		// TODO Auto-generated method stub
		
	}
}
