package Comparators;

import DataStructures.AbstractBlock;
import java.util.Comparator;

public class DescendingCardinalityComparator implements Comparator<AbstractBlock> {

    @Override
    public int compare(AbstractBlock block1, AbstractBlock block2) {
        return new Double(block2.getNoOfComparisons()).compareTo(block1.getNoOfComparisons());
    }
}
