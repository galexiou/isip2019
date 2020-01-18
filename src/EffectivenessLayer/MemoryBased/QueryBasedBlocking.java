package EffectivenessLayer.MemoryBased;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;
import Utilities.Converter;

public abstract class QueryBasedBlocking extends AbstractBlockingMethod {

	protected Map<String, Set<Integer>> invertedIndex1;
	protected final List<EntityProfile> entityProfiles1;

	public QueryBasedBlocking(List<EntityProfile> profiles1, String description) {
		super(description);
		entityProfiles1 = profiles1;
	}

	@Override
	public List<AbstractBlock> buildBlocks() {
		invertedIndex1 = indexEntities(0, entityProfiles1);
		return parseIndex(invertedIndex1);

	}
	
	@Override
	public  void buildQueryBlocks() {
		invertedIndex1 = indexEntities(0, entityProfiles1);
	}

	protected abstract Map<String, Set<Integer>> indexEntities(int sourceId, List<EntityProfile> profiles);

	public abstract List<AbstractBlock> joinBlockIndices(String path);

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

	protected Map<String, Set<Integer>> indexEntities(int sourceId, List<EntityProfile> profiles,
			HashSet<String> stopwords) {
		// TODO Auto-generated method stub
		return null;
	}
}
