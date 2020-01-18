package EffectivenessLayer.MemoryBased;

import DataStructures.AbstractBlock;
import DataStructures.Attribute;
import DataStructures.EntityProfile;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class AbstractCanopies extends AbstractBlockingMethod {
    
    protected int[][] profiles1;
    protected int[][] profiles2;

    public AbstractCanopies(List<EntityProfile> profilesD1, List<EntityProfile> profilesD2, String description) {
        super(description);
        final Map<String, Integer> distinctTokens = new HashMap<String, Integer>();
        updateDistinctTokens(profilesD1, distinctTokens);
        if (profilesD2 != null) {
            updateDistinctTokens(profilesD2, distinctTokens);
        }
        profiles1 = buildProfiles(profilesD1, distinctTokens);
        profiles2 = null;
        if (profilesD2 != null) {
            profiles2 = buildProfiles(profilesD2, distinctTokens);
        }
    }
    
    @Override
    public List<AbstractBlock> buildBlocks() {
        if (profiles2 != null) {
            return getBilateralBlocks();
        }
        return getUnilateralBlocks();
    }
    
    private int[][] buildProfiles(List<EntityProfile> profiles, Map<String, Integer> distinctTokens) {
        int index = 0;
        final int[][] integerProfiles = new int[profiles.size()][];
        for (EntityProfile profile : profiles) {
            integerProfiles[index++] = getIntegerProfiles(profile, distinctTokens);
        }
        return integerProfiles;
    }
    
    protected abstract List<AbstractBlock> getBilateralBlocks();
    protected abstract List<AbstractBlock> getUnilateralBlocks();
    
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
