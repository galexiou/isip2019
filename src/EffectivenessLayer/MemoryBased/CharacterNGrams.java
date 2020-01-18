package EffectivenessLayer.MemoryBased;

import DataStructures.Attribute;
import DataStructures.EntityProfile;
import Utilities.Constants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CharacterNGrams extends IndexBasedBlocking implements Constants {

    private final int N_GRAM_SIZE;

    public CharacterNGrams(int n, List<EntityProfile> profiles1, List<EntityProfile> profiles2) {
        super(profiles1, profiles2, "In-memory Character N-Grams");
        N_GRAM_SIZE = n;
    }

    private Set<String> getNGrams(String attributeValue) {
        final Set<String> nGrams = new HashSet<String>();
        final String cleanValue = attributeValue.trim().toLowerCase();
        if (cleanValue.length() < N_GRAM_SIZE) {
            return nGrams;
        }

        int currentPosition = 0;
        final int length = cleanValue.length() - (N_GRAM_SIZE - 1);
        while (currentPosition < length) {
            nGrams.add(cleanValue.substring(currentPosition, currentPosition + N_GRAM_SIZE));
            currentPosition++;
        }

        return nGrams;
    }

    @Override
    protected Map<String, Set<Integer>> indexEntities(int sourceId, List<EntityProfile> profiles) {
        int index = 0;
        final Map<String, Set<Integer>> invertedIndex = new HashMap<String, Set<Integer>>();
        for (EntityProfile profile : profiles) {
            for (Attribute attribute : profile.getAttributes()) {
                final Set<String> nGrams = getNGrams(attribute.getValue());
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
