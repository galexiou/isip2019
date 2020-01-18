package EfficiencyLayer;

import DataStructures.AbstractBlock;
import EfficiencyLayer.ComparisonRefinement.AbstractDuplicatePropagation;
import java.util.List;



public abstract class AbstractEfficiencyMethod {
    
    private final String name;
    
    public AbstractEfficiencyMethod(String nm) {
        name = nm;
    }

    public String getName() {
        return name;
    }
    
    public abstract void applyProcessing(List<AbstractBlock> blocks);
    
    public void applyProcessing(List<AbstractBlock> blocks, AbstractDuplicatePropagation adp) {
        applyProcessing(blocks);

        double comparisons = 0;
        for (AbstractBlock block : blocks) {            
            comparisons += block.processBlock(adp);
        }

        System.out.println("Detected duplicates\t:\t" + adp.getNoOfDuplicates());
        System.out.println("Executed comparisons\t:\t" + comparisons);
    }
}