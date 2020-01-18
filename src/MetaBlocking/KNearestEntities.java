package MetaBlocking;

import Comparators.ComparisonWeightComparator;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import Utilities.ComparisonIterator;
import Utilities.MetaBlockingConfiguration.WeightingScheme;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author gap2
 */
public class KNearestEntities extends AbstractMetablocking {

    protected long kThreshold;
    protected double[] minimumWeight;
    protected Queue<Comparison>[] nearestEntities;

    public KNearestEntities(WeightingScheme scheme) {
        super("K-Nearest Entities", scheme);
    }
    
    protected KNearestEntities(String description, WeightingScheme scheme) {
        super(description, scheme);
    }

    protected void addComparison(Comparison comparison) {
        if (minimumWeight[comparison.getEntityId1()] == -1) {
            nearestEntities[comparison.getEntityId1()] = new PriorityQueue<Comparison>((int) (2 * kThreshold), new ComparisonWeightComparator());
            nearestEntities[comparison.getEntityId1()].add(comparison);
            minimumWeight[comparison.getEntityId1()] = 0;
        } else if (minimumWeight[comparison.getEntityId1()] < comparison.getUtilityMeasure()) {
            nearestEntities[comparison.getEntityId1()].add(comparison);
            if (kThreshold < nearestEntities[comparison.getEntityId1()].size()) {
                Comparison lastComparison = (Comparison) nearestEntities[comparison.getEntityId1()].poll();
                minimumWeight[comparison.getEntityId1()] = lastComparison.getUtilityMeasure();
            }
        }

        int entityId2 = comparison.getEntityId2() + entityIndex.getDatasetLimit();
        if (minimumWeight[entityId2] == -1) {
            nearestEntities[entityId2] = new PriorityQueue<Comparison>((int) (2 * kThreshold), new ComparisonWeightComparator());
            nearestEntities[entityId2].add(comparison);
            minimumWeight[entityId2] = 0;
        } else if (minimumWeight[entityId2] < comparison.getUtilityMeasure()) {
            nearestEntities[entityId2].add(comparison);
            if (kThreshold < nearestEntities[entityId2].size()) {
                Comparison lastComparison = (Comparison) nearestEntities[entityId2].poll();
                minimumWeight[entityId2] = lastComparison.getUtilityMeasure();
            }
        }
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        initializeDataStructures();
        getKThreshold(blocks);
        filterComparisons(blocks);
        gatherComparisons(blocks);
    }

    protected void filterComparisons(List<AbstractBlock> blocks) {
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                double weight = getWeight(block.getBlockIndex(), comparison);
                if (weight < 0) {
                    continue;
                }

                comparison.setUtilityMeasure(weight);
                addComparison(comparison);
            }
        }
    }

    protected void gatherComparisons(List<AbstractBlock> blocks) {
        boolean cleanCleanER = blocks.get(0) instanceof BilateralBlock;

        blocks.clear();
        for (int i = 0; i < entityIndex.getNoOfEntities(); i++) {
            if (nearestEntities[i] == null) {
                continue;
            }
            blocks.add(getDecomposedBlock(cleanCleanER, nearestEntities[i].iterator()));
        }
    }

    protected void getKThreshold(List<AbstractBlock> blocks) {
        long blockAssingments = 0;
        for (AbstractBlock block : blocks) {
            blockAssingments += block.getTotalBlockAssignments();
        }

        kThreshold = (int) Math.max(1, blockAssingments / entityIndex.getNoOfEntities());
    }

    protected void initializeDataStructures() {
        int noOfEntities = entityIndex.getNoOfEntities();
        minimumWeight = new double[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            minimumWeight[i] = -1;
        }

        nearestEntities = new PriorityQueue[noOfEntities];
    }
}