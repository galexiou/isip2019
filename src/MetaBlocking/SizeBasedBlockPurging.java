package MetaBlocking;

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.UnilateralBlock;
import EfficiencyLayer.AbstractEfficiencyMethod;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */

public class SizeBasedBlockPurging extends AbstractEfficiencyMethod {
    
    private final static double PURGING_FACTOR = 0.005;
    
    public SizeBasedBlockPurging() {
        super("(Size-based) Block Purging");
    }
    
    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        //only applicable to bilateral block collections
        if (blocks.get(0) instanceof UnilateralBlock) {
            return;
        }
        
        int maxInnerBlockSize = getMaxInnerBlockSize(blocks);
        
        double totalComparisons = 0;
        final List<AbstractBlock> purgedBlocks = new ArrayList<AbstractBlock>();
        for (AbstractBlock block : blocks) {
            final BilateralBlock bilBlock = (BilateralBlock) block;
            if (maxInnerBlockSize < Math.min(bilBlock.getIndex1Entities().length, bilBlock.getIndex2Entities().length)) {
                purgedBlocks.add(block);
            } else {
                totalComparisons += block.getNoOfComparisons();
            }
        }
        blocks.removeAll(purgedBlocks);
        
        System.out.println("Purged blocks\t:\t" + purgedBlocks.size());
        System.out.println("Retained blocks\t:\t" + blocks.size());
        System.out.println("Retained comparisons\t:\t" + totalComparisons);
    }
    
    private int getMaxInnerBlockSize(List<AbstractBlock> blocks) {
        final Set<Integer> d1Entities = new HashSet<Integer>();
        final Set<Integer> d2Entities = new HashSet<Integer>();
        for (AbstractBlock block : blocks) {
            final BilateralBlock bilBlock = (BilateralBlock) block;
            for (int id1 : bilBlock.getIndex1Entities()) {
                d1Entities.add(id1);
            }
            
            for (int id2 : bilBlock.getIndex2Entities()) {
                d2Entities.add(id2);
            }
        }
        
        return (int) Math.round(Math.min(d1Entities.size(), d2Entities.size())*PURGING_FACTOR);
    }
}