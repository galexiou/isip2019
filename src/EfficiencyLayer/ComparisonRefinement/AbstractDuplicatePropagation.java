package EfficiencyLayer.ComparisonRefinement;

import DataStructures.Comparison;
import DataStructures.IdDuplicates;
import Utilities.SerializationUtilities;

import java.io.Serializable;
import java.util.Set;


public abstract class AbstractDuplicatePropagation implements Serializable {

	private static final long serialVersionUID = 1966768684326270148L;
	private final String name;
    protected final int existingDuplicates;
    protected final Set<IdDuplicates> duplicates;
    
    public AbstractDuplicatePropagation(Set<IdDuplicates> matches) {
        duplicates = matches;
        existingDuplicates = duplicates.size();
        name = "Duplicate Propagation";
    }
    
    public AbstractDuplicatePropagation(String groundTruthPath) {
        duplicates = (Set<IdDuplicates>) SerializationUtilities.loadSerializedObject(groundTruthPath);
        existingDuplicates = duplicates.size();
        name = "Duplicate Propagation";
    }
    
    public int getExistingDuplicates() {
        return existingDuplicates;
    }
    
    public String getName() {
        return name;
    }
    
    public abstract int getNoOfDuplicates();
    public abstract boolean isSuperfluous(Comparison comparison);
    public abstract void resetDuplicates();

    public Set<IdDuplicates> getDuplicates() {
        return duplicates;
    }
}