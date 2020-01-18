package EfficiencyLayer.ComparisonRefinement;

import DataStructures.Comparison;
import DataStructures.IdDuplicates;
import java.util.HashSet;
import java.util.Set;


public class BilateralDuplicatePropagation extends AbstractDuplicatePropagation {
 

	private static final long serialVersionUID = -4327998361823526135L;
	private Set<Integer> entities1;
    private Set<Integer> entities2;
    
    public BilateralDuplicatePropagation(Set<IdDuplicates> matches) {
        super(matches);
        entities1 = new HashSet<Integer>(2*existingDuplicates);
        entities2 = new HashSet<Integer>(2*existingDuplicates);
    }
    
    public BilateralDuplicatePropagation(String groundTruthPath) {
        super(groundTruthPath);
        entities1 = new HashSet<Integer>(2*existingDuplicates);
        entities2 = new HashSet<Integer>(2*existingDuplicates);
    }
    
    @Override
    public int getNoOfDuplicates() {
        return entities1.size();
    }
    
    @Override
    public boolean isSuperfluous(Comparison comparison) {
        Integer id1 = comparison.getEntityId1();
        Integer id2 = comparison.getEntityId2();
        if (entities1.contains(id1) || entities2.contains(id2)) {
            return true;
        }
        
        final IdDuplicates tempDuplicates = new IdDuplicates(id1, id2);
        if (duplicates.contains(tempDuplicates)) {
            entities1.add(id1);
            entities2.add(id2);
        }
        return false;
    }
    
    @Override
    public void resetDuplicates() {
        entities1 = new HashSet<Integer>(2*existingDuplicates);
        entities2 = new HashSet<Integer>(2*existingDuplicates);
    }
}