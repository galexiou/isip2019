package EffectivenessLayer.MemoryBased;

import DataStructures.Attribute;
import DataStructures.EntityProfile;
import Utilities.Constants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SuffixArrayBlocking extends IndexBasedBlocking implements Constants {
    
    private final int MINIMUM_SIZE;

    public SuffixArrayBlocking(int n, List<EntityProfile> profiles1, List<EntityProfile> profiles2) {
        super(profiles1, profiles2, "In-memory Suffix Array Blocking");
        MINIMUM_SIZE = n;
    }

    private Set<String> getSuffixes(String attributeValue) {
        final Set<String> suffixes = new HashSet<String>();
        String cleanValue = attributeValue.trim().toLowerCase();
        for (String token : cleanValue.split("[\\W_]")) {
            if (token.length() < MINIMUM_SIZE) {
                suffixes.add(token);
            } else {
                int limit = token.length()-MINIMUM_SIZE+1;
                for (int i = 0; i < limit; i++) {
                    suffixes.add(token.substring(i));
                }
            }
        }
        return suffixes;
    }

    @Override
    protected Map<String, Set<Integer>> indexEntities(int sourceId, List<EntityProfile> profiles) {
        int index = 0;
        final Map<String, Set<Integer>> invertedIndex = new HashMap<String, Set<Integer>>();
        for (EntityProfile profile : profiles) {
            for (Attribute attribute : profile.getAttributes()) {
                final Set<String> nGrams = getSuffixes(attribute.getValue().trim());
                for (String nGram : nGrams) {
                    if (0 < nGram.length()) {
                        Set<Integer> termEntities = invertedIndex.get(nGram);
                        if (termEntities == null) {
                            termEntities = new HashSet<Integer>();
                            invertedIndex.put(nGram, termEntities);
                        }
                        termEntities.add(index);
                    }
                }
            }
            index++;
        }
        return invertedIndex;
    }

	@Override
	public void buildQueryBlocks() {
		// TODO Auto-generated method stub
		
	}
}