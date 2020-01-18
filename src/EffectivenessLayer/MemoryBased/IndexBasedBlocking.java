package EffectivenessLayer.MemoryBased;

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class IndexBasedBlocking extends AbstractBlockingMethod {
    
    protected Map<String, Set<Integer>> invertedIndex1;
    protected Map<String, Set<Integer>> invertedIndex2;
    protected final List<EntityProfile> entityProfiles1;
    protected final List<EntityProfile> entityProfiles2;

    public IndexBasedBlocking(List<EntityProfile> profiles1, List<EntityProfile> profiles2, String description) {
        super(description);
        entityProfiles1 = profiles1;
        entityProfiles2 = profiles2;
    }

    @Override
    public List<AbstractBlock> buildBlocks() {
        invertedIndex1 = indexEntities(0, entityProfiles1);
        if (entityProfiles2 == null) {
            return parseIndex(invertedIndex1);
        }

        invertedIndex2 = indexEntities(1, entityProfiles2);
        final Map<String, int[]> hashedBlocks = parseD1Index();
        return parseD2Index(hashedBlocks);
    }

    protected abstract Map<String, Set<Integer>> indexEntities(int sourceId, List<EntityProfile> profiles);

    protected List<AbstractBlock> parseIndex(Map<String, Set<Integer>> invertedIndex) {
        final List<AbstractBlock> blocks = new ArrayList<AbstractBlock>();
        for (Entry<String, Set<Integer>> term : invertedIndex.entrySet()) {
            if (1 < term.getValue().size()) {
                int[] idsArray = Converter.convertListToArray(term.getValue());
                UnilateralBlock uBlock = new UnilateralBlock(idsArray);
                blocks.add(uBlock);
            }
        }
        invertedIndex.clear();
        return blocks;
    }

    protected Map<String, int[]> parseD1Index() {
        final Map<String, int[]> hashedBlocks = new HashMap<String, int[]>();
        for (Entry<String, Set<Integer>> term : invertedIndex1.entrySet()) {
            if (!invertedIndex2.containsKey(term.getKey())) {
                continue;
            }
            int[] idsArray = Converter.convertListToArray(term.getValue());
            hashedBlocks.put(term.getKey(), idsArray);
        }
        return hashedBlocks;
    }

    protected List<AbstractBlock> parseD2Index(Map<String, int[]> hashedBlocks) {
        final List<AbstractBlock> blocks = new ArrayList<AbstractBlock>();
        for (Entry<String, Set<Integer>> term : invertedIndex2.entrySet()) {
            if (!invertedIndex1.containsKey(term.getKey())) {
                continue;
            }
            int[] idsArray = Converter.convertListToArray(term.getValue());
            int[] blockEntities1 = hashedBlocks.get(term.getKey());
            blocks.add(new BilateralBlock(blockEntities1, idsArray));
        }
        invertedIndex1.clear();
        invertedIndex2.clear();
        return blocks;
    }

	protected Map<String, Set<Integer>> indexEntities(int sourceId, List<EntityProfile> profiles,
			HashSet<String> stopwords) {
		// TODO Auto-generated method stub
		return null;
	}
}