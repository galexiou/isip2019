package MetaBlocking;

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.Comparison;
import Utilities.ComparisonIterator;
import Utilities.MetaBlockingConfiguration.WeightingScheme;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */
public class NodePruning extends AbstractMetablocking {

    protected double[] averageWeight;

    public NodePruning(WeightingScheme scheme) {
        super("Node Pruning", scheme);
    }
    
    protected NodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
    }

    protected void addWeight(double weight, Comparison comparison) {
        int entityId2 = comparison.getEntityId2() + entityIndex.getDatasetLimit();

        averageWeight[comparison.getEntityId1()] += weight;
        averageWeight[entityId2] += weight;
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        setWeights(blocks);
        setAverageWeights();
        filterComparisons(blocks);
    }

    protected void filterComparisons(List<AbstractBlock> blocks) {
        boolean cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        final List<AbstractBlock> newBlocks = new ArrayList<AbstractBlock>();
        for (AbstractBlock block : blocks) {
            final List<Integer> entities1 = new ArrayList<Integer>();
            final List<Integer> entities2 = new ArrayList<Integer>();
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (isValidComparison(block.getBlockIndex(), comparison)) {
                    entities1.add(comparison.getEntityId1());
                    entities2.add(comparison.getEntityId2());
                }
            }
            newBlocks.add(getDecomposedBlock(cleanCleanER, entities1, entities2));
        }
        blocks.clear();
        blocks.addAll(newBlocks);
    }

    protected boolean isValidComparison(int blockIndex, Comparison comparison) {
        double weight = getWeight(blockIndex, comparison);
        if (weight < 0) {
            return false;
        }

        int entityId2 = comparison.getEntityId2() + entityIndex.getDatasetLimit();
        if (averageWeight[comparison.getEntityId1()] <= weight
                || averageWeight[entityId2] <= weight) {
            return true;
        }

        return false;
    }

    protected void setWeights(List<AbstractBlock> blocks) {
        averageWeight = new double[entityIndex.getNoOfEntities()];
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                double weight = getWeight(block.getBlockIndex(), comparison);
                if (weight < 0) {
                    continue;
                }

                addWeight(weight, comparison);
            }
        }
    }

    protected void setAverageWeights() {
        int noOfEntities = averageWeight.length;
        for (int i = 0; i < noOfEntities; i++) {
            averageWeight[i] /= comparisonsPerEntity[i];
        }
    }
}
