package EffectivenessLayer.MemoryBased;

import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;
import EfficiencyLayer.ComparisonRefinement.AbstractDuplicatePropagation;
import Utilities.Converter;
import Utilities.ProfileComparison;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FastCanopyClustering extends AbstractFastCanopyClustering {

    private double w1;
    private double w2;

    public FastCanopyClustering(boolean cp, AbstractDuplicatePropagation adp, List<EntityProfile> profiles) {
        super(cp, adp, profiles);
    }

    public FastCanopyClustering(boolean cp, boolean ic, AbstractDuplicatePropagation adp, List<EntityProfile> profiles) {
        super(cp, ic, adp, profiles);
    }

    public FastCanopyClustering(boolean ic, AbstractDuplicatePropagation adp, List<EntityProfile> prof1, List<EntityProfile> prof2) {
        super(adp, prof1, prof2);
    }

    public double[] applyProcessing(double t1, double t2) {
        System.out.println("\n\nApplying processing...");

        w1 = t1;
        w2 = t2;

        comparisons = 0;
        duplicates.resetDuplicates();
        if (profiles2 != null) {
            getBilateralCanopies();
        } else {
            if (useComparisonPropagation) {
                initializeEntityIndex();
            }
            getUnilateralCanopies();
        }
        System.out.println("Executed comparisons\t:\t" + comparisons);
        System.out.println("Detected duplicates\t:\t" + duplicates.getNoOfDuplicates());
        
        double[] results = { duplicates.getExistingDuplicates(), comparisons };
        return results;
    }

    private void getBilateralCanopies() {
        System.out.println("\n\nGetting bilateral canopies...");

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

        int singletonEntities = 0;
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
                if (w1 <= jaccardSim) {
                    newBlockIds.add(currentId);
                }

                // Removal threshold:
                if (w2 <= jaccardSim) {
                    iter2.remove();
                    entityIds2.remove(currentId);
                }
            }

            if (!newBlockIds.isEmpty()) {
                int[] blockEntityIds1 = {firstId};
                int[] blockEntityIds2 = Converter.convertListToArray(newBlockIds);
                final BilateralBlock block = new BilateralBlock(blockEntityIds1, blockEntityIds2);
                for (Comparison comparison : block.getComparisons()) {
                    comparisons++;
                    duplicates.isSuperfluous(comparison);
                    if (integratedProfileComparisons) {
                        ProfileComparison.getJaccardSimilarity(profilesD1.get(comparison.getEntityId1()).getAttributes(),
                                profilesD1.get(comparison.getEntityId2()).getAttributes());
                    }
                }
            } else {
                singletonEntities++;
            }
        }
        System.out.println("Final singleton entities\t:\t" + singletonEntities);
    }

    private void getUnilateralCanopies() {
        int noOfEntities = profiles1.length;
        final Set<Integer> entityIds = new HashSet<Integer>(2 * noOfEntities);
        for (int i = 0; i < noOfEntities; i++) {
            entityIds.add(i);
        }

        int blockIndex = 0;
        int singletonEntities = 0;
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
                if (w1 <= jaccardSim) {
                    newBlockIds.add(currentId);
                }

                // Removal threshold:
                if (w2 <= jaccardSim) {
                    iter.remove();
                    entityIds.remove(currentId);
                }
            }

            if (1 < newBlockIds.size()) {
                int[] blockEntityIds = Converter.convertListToArray(newBlockIds);
                final UnilateralBlock block = new UnilateralBlock(blockEntityIds);
                if (useComparisonPropagation) {
                    blockIndex++;
                    for (int entityId : blockEntityIds) {
                        profiles1Blocks[entityId].add(blockIndex);
                    }

                    for (Comparison comparison : block.getComparisons()) {
                        if (!isUnilateralRepeated(blockIndex, comparison)) {
                            comparisons++;
                            duplicates.isSuperfluous(comparison);
                            if (integratedProfileComparisons) {
                                ProfileComparison.getJaccardSimilarity(profilesD1.get(comparison.getEntityId1()).getAttributes(),
                                        profilesD1.get(comparison.getEntityId2()).getAttributes());
                            }
                        }
                    }
                } else {
                    for (Comparison comparison : block.getComparisons()) {
                        comparisons++;
                        duplicates.isSuperfluous(comparison);
                        if (integratedProfileComparisons) {
                            ProfileComparison.getJaccardSimilarity(profilesD1.get(comparison.getEntityId1()).getAttributes(),
                                    profilesD1.get(comparison.getEntityId2()).getAttributes());
                        }
                    }
                }
            } else {
                singletonEntities++;
            }
        }

        System.out.println("Final singleton entities\t:\t" + singletonEntities);
    }
}
