package EfficiencyLayer.IterativeMethods;

import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.IdDuplicates;
import Utilities.ComparisonIterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */

public class BilateralIterativeBlocking {
    
    private final Set<IdDuplicates> duplicates;
    private final Set<Integer> entities1;
    private final Set<Integer> entities2;
    
    public BilateralIterativeBlocking(Set<IdDuplicates> matches) {
        duplicates = matches;
        entities1 = new HashSet<Integer>(2*duplicates.size());
        entities2 = new HashSet<Integer>(2*duplicates.size());
    }
    
    public void applyProcessing(List<AbstractBlock> blocks) {
        System.out.println("\n\nApplying processing...");
        
        double comparisons = 0;
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (!isSuperfluous(comparison)) {
                    comparisons++;
                }
            }
        }
        
        System.out.println("Detected duplicates\t:\t" + entities1.size());
        System.out.println("Executed comparisons\t:\t" + comparisons);
    } 
    
    private boolean isSuperfluous(Comparison comparison) {
        Integer id1 = comparison.getEntityId1();
        Integer id2 = comparison.getEntityId2();
        if (entities1.contains(id1) && entities2.contains(id2)) { 
            return true;
        }
        
        final IdDuplicates tempDuplicates = new IdDuplicates(id1, id2);
        if (duplicates.contains(tempDuplicates)) {
            entities1.add(id1);
            entities2.add(id2);
        }
        
        return false;
    }
}