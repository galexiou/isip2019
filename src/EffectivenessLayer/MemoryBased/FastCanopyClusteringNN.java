package EffectivenessLayer.MemoryBased;

import Comparators.ComparisonWeightComparator;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;
import EfficiencyLayer.ComparisonRefinement.AbstractDuplicatePropagation;
import EfficiencyLayer.ComparisonRefinement.BilateralDuplicatePropagation;
import EfficiencyLayer.ComparisonRefinement.UnilateralDuplicatePropagation;
import Utilities.Converter;
import Utilities.ProfileComparison;
import Utilities.SerializationUtilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;


public class FastCanopyClusteringNN extends AbstractFastCanopyClustering {

    private int n1;
    private int n2;
    private double minimumWeight;
    private Queue<Comparison> n2NearestEntities;

    public FastCanopyClusteringNN(boolean cp, AbstractDuplicatePropagation adp, List<EntityProfile> profiles) {
        super(cp, adp, profiles);
    }

    public FastCanopyClusteringNN(boolean cp, boolean ic, AbstractDuplicatePropagation adp, List<EntityProfile> profiles) {
        super(cp, ic, adp, profiles);
    }

    public FastCanopyClusteringNN(boolean ic, AbstractDuplicatePropagation adp, List<EntityProfile> prof1, List<EntityProfile> prof2) {
        super(adp, prof1, prof2);
        integratedProfileComparisons = ic;
    }

    private void addComparison(Comparison comparison) {
        if (comparison.getUtilityMeasure() < minimumWeight) {
            return;
        }

        n2NearestEntities.add(comparison);
        if (n2 < n2NearestEntities.size()) {
            Comparison lastComparison = (Comparison) n2NearestEntities.poll();
            minimumWeight = lastComparison.getUtilityMeasure();
        }
    }

    public double[] applyProcessing(int n1, int n2) {
        System.out.println("\n\nApplying processing...");

        this.n1 = n1;
        this.n2 = n2;

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
            minimumWeight = Double.MIN_VALUE;
            n2NearestEntities = new PriorityQueue<Comparison>((int) (2 * n2), new ComparisonWeightComparator());

            // Compare to remaining objects:
            Iterator iter2 = entityIds2.iterator();
            while (iter2.hasNext()) {
                int currentId = (Integer) iter2.next();
                double jaccardSim = getJaccardSimilarity(profiles1[firstId], profiles2[currentId]);

                Comparison comparison = new Comparison(false, firstId, currentId);
                comparison.setUtilityMeasure(jaccardSim);
                addComparison(comparison);
            }

            for (int i = 0; i < n2 && !n2NearestEntities.isEmpty(); i++) {
                Comparison lastComparison = (Comparison) n2NearestEntities.poll();
                newBlockIds.add(lastComparison.getEntityId2());
                if (i >= (n2 - n1)) {
                    entityIds2.remove(lastComparison.getEntityId2());
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
                                profilesD2.get(comparison.getEntityId2()).getAttributes());
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

            minimumWeight = Double.MIN_VALUE;
            n2NearestEntities = new PriorityQueue<Comparison>((int) (2 * n2), new ComparisonWeightComparator());

            // Compare to remaining objects:
            while (iter.hasNext()) {
                int currentId = (Integer) iter.next();
                double jaccardSim = getJaccardSimilarity(profiles1[firstId], profiles1[currentId]);

                Comparison comparison = new Comparison(false, firstId, currentId);
                comparison.setUtilityMeasure(jaccardSim);
                addComparison(comparison);
            }

            for (int i = 0; i < n2 && !n2NearestEntities.isEmpty(); i++) {
                Comparison lastComparison = (Comparison) n2NearestEntities.poll();
                newBlockIds.add(lastComparison.getEntityId2());
                if (i >= (n2 - n1)) {
                    entityIds.remove(lastComparison.getEntityId2());
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
