package EffectivenessLayer.MemoryBased;

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class CanopyClustering extends AbstractCanopies {
    
    private final double t1;
    private final double t2;
    
    public CanopyClustering(double w1, double w2, List<EntityProfile> profiles1, List<EntityProfile> profiles2) {
        super(profiles1, profiles2, "In-memory Canopy Clustering with weights");
        t1 = w1;
        t2 = w2;
    }
    
    @Override
    protected List<AbstractBlock> getBilateralBlocks() {
        int noOfProfiles1 = profiles1.length;
        final Set<Integer> entityIds1 = new HashSet<Integer>(2 * noOfProfiles1);
        for (int i = 0; i < noOfProfiles1; i++) {
            entityIds1.add(i);
        }

        int noOfProfiles2 = profiles2.length;
        final Set<Integer> entityIds2 = new HashSet<Integer>(2 * noOfProfiles2);
        for (int i = 0; i < noOfProfiles2; i++) {
            entityIds2.add(i);
        }

        final List<AbstractBlock> blocks = new ArrayList<AbstractBlock>();
        while (!entityIds1.isEmpty() && !entityIds2.isEmpty()) {
            // Get first element:
            Iterator iter1 = entityIds1.iterator();
            int firstId = (Integer) iter1.next();

            // Remove first element:
            iter1.remove();
            entityIds1.remove(firstId);

            // Start a new cluster:
            final List<Integer> newBlockIds = new ArrayList<Integer>();
            // Compare to remaining objects:
            Iterator iter2 = entityIds2.iterator();
            while (iter2.hasNext()) {
                int currentId = (Integer) iter2.next();
                double jaccardSim = getJaccardSimilarity(profiles1[firstId], profiles2[currentId]);

                // Inclusion threshold:
                if (t1 <= jaccardSim) {
                    newBlockIds.add(currentId);
                }

                // Removal threshold:
                if (t2 <= jaccardSim) {
                    iter2.remove();
                    entityIds2.remove(currentId);
                }
            }

            if (!newBlockIds.isEmpty()) {
                int[] blockEntityIds1 = {firstId};
                int[] blockEntityIds2 = Converter.convertListToArray(newBlockIds);
                blocks.add(new BilateralBlock(blockEntityIds1, blockEntityIds2));
            }
        }
        return blocks;
    }
    
    @Override
    protected List<AbstractBlock> getUnilateralBlocks() {
        int noOfEntities = profiles1.length;
        final Set<Integer> entityIds = new HashSet<Integer>(2 * noOfEntities);
        for (int i = 0; i < noOfEntities; i++) {
            entityIds.add(i);
        }

        final List<AbstractBlock> blocks = new ArrayList<AbstractBlock>();
        while (!entityIds.isEmpty()) {
            // Get first element:
            Iterator iter = entityIds.iterator();
            int firstId = (Integer) iter.next();

            // Remove first element:
            iter.remove();
            entityIds.remove(firstId);

            // Start a new cluster:
            final List<Integer> newBlockIds = new ArrayList<Integer>();
            newBlockIds.add(firstId);

            // Compare to remaining objects:
            while (iter.hasNext()) {
                int currentId = (Integer) iter.next();
                double jaccardSim = getJaccardSimilarity(profiles1[firstId], profiles1[currentId]);

                // Inclusion threshold:
                if (t1 <= jaccardSim) {
                    newBlockIds.add(currentId);
                }

                // Removal threshold:
                if (t2 <= jaccardSim) {
                    iter.remove();
                    entityIds.remove(currentId);
                }
            }

            if (1 < newBlockIds.size()) {
                int[] blockEntityIds = Converter.convertListToArray(newBlockIds);
                blocks.add(new UnilateralBlock(blockEntityIds));
            } 
        }
        return blocks;
    }

	@Override
	public void buildQueryBlocks() {
		// TODO Auto-generated method stub
		
	}
}