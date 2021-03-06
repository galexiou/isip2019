package EfficiencyLayer.IterativeMethods;

import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.IdDuplicates;
import Utilities.ComparisonIterator;
import Utilities.ProfileComparison;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gap2
 */

public class BilateralIterativeBlockingWithInstanceMatching {
    
    private final EntityProfile[] profilesD1;
    private final EntityProfile[] profilesD2;
    private final Set<IdDuplicates> duplicates;
    private final Set<Integer> entities1;
    private final Set<Integer> entities2;
    
    public BilateralIterativeBlockingWithInstanceMatching(List<EntityProfile> profiles1, 
                                                          List<EntityProfile> profiles2, 
                                                          Set<IdDuplicates> matches) {
        duplicates = matches;
        entities1 = new HashSet<Integer>(2*duplicates.size());
        entities2 = new HashSet<Integer>(2*duplicates.size());
        profilesD1 = profiles1.toArray(new EntityProfile[profiles1.size()]);
        System.out.println("Entities 1\t:\t" + profilesD1.length);
        profilesD2 = profiles2.toArray(new EntityProfile[profiles2.size()]);
        System.out.println("Entities 2\t:\t" + profilesD2.length);
    }
    
    public void applyProcessing(ArrayList<AbstractBlock> blocks) {
        System.out.println("\n\nApplying processing...");
        
        double comparisons = 0;
        long startingTime = System.currentTimeMillis();
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (!isSuperfluous(comparison)) {
                    comparisons++;
                    ProfileComparison.getJaccardSimilarity(profilesD1[comparison.getEntityId1()].getAttributes(), 
                                                           profilesD2[comparison.getEntityId2()].getAttributes());
                }
            }
        }
        long endingTime = System.currentTimeMillis();
        
        System.out.println("Detected duplicates\t:\t" + entities1.size());
        System.out.println("Executed comparisons\t:\t" + comparisons);
        System.out.println("Elapsed time\t:\t" + (endingTime-startingTime));
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