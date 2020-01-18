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
public class TopKEdges extends AbstractMetablocking {

    protected long kThreshold;
    protected double minimumWeight;
    protected Queue<Comparison> topKEdges;

    public TopKEdges(WeightingScheme scheme) {
        super("Top-K Edges", scheme);
    }
    
    protected TopKEdges(String description, WeightingScheme scheme) {
        super(description, scheme);
    }

    private void addComparison(Comparison comparison) {
        if (comparison.getUtilityMeasure() < minimumWeight) {
            return;
        }

        topKEdges.add(comparison);
        if (kThreshold < topKEdges.size()) {
            Comparison lastComparison = (Comparison) topKEdges.poll();
            minimumWeight = lastComparison.getUtilityMeasure();
        }
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        getKThreshold(blocks);
        filterComparisons(blocks);
        gatherComparisons(blocks);
    }

    protected void filterComparisons(List<AbstractBlock> blocks) {
        minimumWeight = Double.MIN_VALUE;
        topKEdges = new PriorityQueue<Comparison>((int) (2 * kThreshold), new ComparisonWeightComparator());
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

    private void gatherComparisons(List<AbstractBlock> blocks) {
        boolean cleanCleanER = blocks.get(0) instanceof BilateralBlock;
        blocks.clear();
        blocks.add(getDecomposedBlock(cleanCleanER, topKEdges.iterator()));
    }

    protected void getKThreshold(List<AbstractBlock> blocks) {
        long blockAssingments = 0;
        for (AbstractBlock block : blocks) {
            blockAssingments += block.getTotalBlockAssignments();
        }
        kThreshold = blockAssingments / 2;
        System.out.println("K-threshold\t:\t" + kThreshold);
    }
}
