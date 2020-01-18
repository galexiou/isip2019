package EffectivenessLayer.MemoryBased;

import DataStructures.Attribute;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import EfficiencyLayer.ComparisonRefinement.AbstractDuplicatePropagation;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AbstractFastCanopyClustering {

    protected boolean integratedProfileComparisons;
    protected boolean useComparisonPropagation;
    protected double comparisons;
    protected int noOfDuplicates;
    protected int[][] profiles1;
    protected int[][] profiles2;
    protected final AbstractDuplicatePropagation duplicates;
    protected List<Integer>[] profiles1Blocks; // only needed for detecting redundant comparisons in Dirty ER
                                               //remember that CC yields no redundant comparisons in Clean-Clean ER
    protected List<EntityProfile> profilesD1;
    protected List<EntityProfile> profilesD2;
    
    public AbstractFastCanopyClustering(boolean cp, AbstractDuplicatePropagation adp, List<EntityProfile> profiles) {        
        final Map<String, Integer> distinctTokens = new HashMap<String, Integer>();
        updateDistinctTokens(profiles, distinctTokens);
        
        duplicates = adp;
        integratedProfileComparisons = false;
        profiles1 = buildProfiles(profiles, distinctTokens);
        profilesD1 = profiles;
        profilesD2 = null;
        useComparisonPropagation = cp;
    }
    
    public AbstractFastCanopyClustering(boolean cp, boolean ic, AbstractDuplicatePropagation adp, List<EntityProfile> profiles) {        
        this(cp, adp, profiles);
        integratedProfileComparisons = ic;
    }
    
    public AbstractFastCanopyClustering(AbstractDuplicatePropagation adp, List<EntityProfile> prof1, List<EntityProfile> prof2) {
        final Map<String, Integer> distinctTokens = new HashMap<String, Integer>();
        updateDistinctTokens(prof1, distinctTokens);
        updateDistinctTokens(prof2, distinctTokens);
        
        duplicates = adp;
        profiles1 = buildProfiles(prof1, distinctTokens);
        profiles2 = buildProfiles(prof2, distinctTokens);
        profilesD1 = prof1;
        profilesD2 = prof2;
    }
    
    public AbstractFastCanopyClustering(boolean ic, AbstractDuplicatePropagation adp, List<EntityProfile> prof1, List<EntityProfile> prof2) {
        this(adp, prof1, prof2);
        integratedProfileComparisons = ic;
    }
    
    private int[][] buildProfiles(List<EntityProfile> profiles, Map<String, Integer> distinctTokens) {
        int index = 0;
        final int[][] integerProfiles = new int[profiles.size()][];
        for (EntityProfile profile : profiles) {
            integerProfiles[index++] = getIntegerProfiles(profile, distinctTokens);
        }
        return integerProfiles;
    }
    
    private int[] getIntegerProfiles(EntityProfile profile, Map<String, Integer> distinctTokens) {
        final Set<Integer> integers = new HashSet<Integer>();
        for (Attribute attribute : profile.getAttributes()) {
            String[] tokens = attribute.getValue().split("[\\W_]");
                for (String token : tokens) {
                    integers.add(distinctTokens.get(token));
                }
        }
        
        final List<Integer> sortedIntegers = new ArrayList<Integer>(integers);
        Collections.sort(sortedIntegers);
        return Converter.convertListToArray(sortedIntegers);
    }
    
    protected double getJaccardSimilarity(int[] tokens1, int[] tokens2) {
        double commonTokens = 0.0;
        int noOfTokens1 = tokens1.length;
        int noOfTokens2 = tokens2.length;
        for (int i = 0; i < noOfTokens1; i++) {
            for (int j = 0; j < noOfTokens2; j++) {
                if (tokens2[j] < tokens1[i]) {
                    continue;
                }

                if (tokens1[i] < tokens2[j]) {
                    break;
                }

                if (tokens1[i] == tokens2[j]) {
                    commonTokens++;
                }
            }
        }
        return commonTokens/(noOfTokens1+noOfTokens2-commonTokens);
    }
    
    protected void initializeEntityIndex() {
        profiles1Blocks = new List[profiles1.length];
        for (int i = 0; i < profiles1Blocks.length; i++) {
            profiles1Blocks[i] = new ArrayList<Integer>();
        }
    }
    
    protected boolean isUnilateralRepeated(int blockIndex, Comparison comparison) {  
        int entity1 = comparison.getEntityId1();
        int entity2 = comparison.getEntityId2();
        for (int blockId1 : profiles1Blocks[entity1]) {
            for (int blockId2 : profiles1Blocks[entity2]) {
                if (blockId2 < blockId1) {
                    continue;
                }

                if (blockId1 < blockId2) {
                    break;
                }

                if (blockId1 == blockId2) {
                    return blockId1 != blockIndex;
                }
            }
        }
        
        System.err.println("Error!!!!");
        return false;
    }
    
    private void updateDistinctTokens (List<EntityProfile> profiles, Map<String, Integer> distinctTokens) {
        int index = distinctTokens.size();
        for (EntityProfile entity : profiles) {
            for (Attribute attribute : entity.getAttributes()) {
                String[] tokens = attribute.getValue().split("[\\W_]");
                for (String token : tokens) {
                    if (!distinctTokens.containsKey(token)) {
                        distinctTokens.put(token, index);
                        index++;
                    }
                }
            }
        }
    }
}