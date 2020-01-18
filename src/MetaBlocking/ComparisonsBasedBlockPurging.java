package MetaBlocking;

import DataStructures.AbstractBlock;
import EfficiencyLayer.AbstractEfficiencyMethod;
import Comparators.BlockCardinalityComparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ComparisonsBasedBlockPurging extends AbstractEfficiencyMethod {
    
    private double SMOOTHING_FACTOR = 1.05;
    
    public ComparisonsBasedBlockPurging() {
        super("(Comparisons-based) Block Purging");
    }
    
    public ComparisonsBasedBlockPurging(double smoothingFactor) {
        super("(Comparisons-based) Block Purging");
        SMOOTHING_FACTOR = smoothingFactor;
    }
    
    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        double maxComparisonsPerBlock = getMaxComparisonsPerBlock(blocks);

        Iterator<AbstractBlock> blocksIterator = blocks.iterator();
        while (blocksIterator.hasNext()) {
            AbstractBlock currentBlock = (AbstractBlock) blocksIterator.next();
            if (maxComparisonsPerBlock < currentBlock.getNoOfComparisons()) {
                blocksIterator.remove();
            } 
        }
    }

    private double getMaxComparisonsPerBlock(List<AbstractBlock> blocks) {
        Collections.sort(blocks, new BlockCardinalityComparator());
        final Set<Double> distinctComparisonsLevel = new HashSet<Double>();
        for (AbstractBlock block : blocks) {
            distinctComparisonsLevel.add(block.getNoOfComparisons());
        }

        int index = -1;
        double[] blockAssignments = new double[distinctComparisonsLevel.size()];
        double[] comparisonsLevel = new double[distinctComparisonsLevel.size()];
        double[] totalComparisonsPerLevel = new double[distinctComparisonsLevel.size()];
        for (AbstractBlock block : blocks) {
            if (index == -1) {
                index++;
                comparisonsLevel[index] = block.getNoOfComparisons();
                blockAssignments[index] = 0;
                totalComparisonsPerLevel[index] = 0;
            } else if (block.getNoOfComparisons() != comparisonsLevel[index]) {
                index++;
                comparisonsLevel[index] = block.getNoOfComparisons();
                blockAssignments[index] = blockAssignments[index-1];
                totalComparisonsPerLevel[index] = totalComparisonsPerLevel[index-1];
            }
            
//            System.out.println(block.getTotalBlockAssignments());

            blockAssignments[index] += block.getTotalBlockAssignments();
            totalComparisonsPerLevel[index] += block.getNoOfComparisons();
        }
        
        double currentBC = 0;
        double currentCC = 0;
        double currentSize = 0;
        double previousBC = 0;
        double previousCC = 0;
        double previousSize = 0;
        int arraySize = blockAssignments.length;
        for (int i = arraySize-1; 0 <= i; i--) {
            previousSize = currentSize;
            previousBC = currentBC;
            previousCC = currentCC;

            currentSize = comparisonsLevel[i];
            currentBC = blockAssignments[i];
            currentCC = totalComparisonsPerLevel[i];

            if (currentBC * previousCC < SMOOTHING_FACTOR * currentCC * previousBC) {
                break;
            }
        }
        
        return previousSize;
    }
}