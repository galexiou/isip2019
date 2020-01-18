package Comparators;

import DataStructures.AbstractBlock;
import java.util.Comparator;

public class BlockUtilityComparator implements Comparator<AbstractBlock> {
    
    @Override
    public int compare(AbstractBlock block1, AbstractBlock block2) {
        return new Double(block2.getUtilityMeasure()).compareTo(block1.getUtilityMeasure());
    }
}