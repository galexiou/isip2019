package MetaBlocking;

import DataStructures.AbstractBlock;
import EfficiencyLayer.AbstractEfficiencyMethod;
import EfficiencyLayer.ComparisonRefinement.AbstractDuplicatePropagation;
import java.util.List;


public class BlockPruning extends AbstractEfficiencyMethod {

    public BlockPruning() {
        super("Block Pruning");
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks, AbstractDuplicatePropagation adp) {
        int latestDuplicates = 0;
        double latestComparisons = 0;
        double totalComparisons = 0;
        double maxDuplicateOverhead = getMaxDuplicateOverhead(blocks);
        System.out.println("Maximum Duplicate Overhead\t:\t" + maxDuplicateOverhead);
        
        for (AbstractBlock block : blocks) {
            latestComparisons += block.processBlock(adp);
            
            int currentDuplicates = adp.getNoOfDuplicates();
            if (currentDuplicates == latestDuplicates) {
                continue;
            }

            int noOfNewDuplicates = currentDuplicates - latestDuplicates;
            double duplicateOverhead = latestComparisons / noOfNewDuplicates;
            if (maxDuplicateOverhead < duplicateOverhead) {
                totalComparisons += latestComparisons;
                break;
            }

            totalComparisons += latestComparisons;
            latestComparisons = 0;
            latestDuplicates = adp.getNoOfDuplicates();
        }

        System.out.println("Detected duplicates\t:\t" + adp.getNoOfDuplicates());
        System.out.println("Executed comparisons\t:\t" + totalComparisons);
    }

    private double getMaxDuplicateOverhead(List<AbstractBlock> blocks) {
        double totalComparisons = 0;
        for (AbstractBlock block : blocks) {
            totalComparisons += block.getNoOfComparisons();
        }
        return Math.pow(10, Math.log10(totalComparisons) / 2.0);
    }
}