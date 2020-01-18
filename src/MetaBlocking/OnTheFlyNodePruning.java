package MetaBlocking;

import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import EfficiencyLayer.ComparisonRefinement.AbstractDuplicatePropagation;
import Utilities.ComparisonIterator;
import Utilities.MetaBlockingConfiguration.WeightingScheme;
import java.util.List;

/**
 *
 * @author gap2
 */

public class OnTheFlyNodePruning extends NodePruning {

    private final AbstractDuplicatePropagation duplicatePropagation;

    public OnTheFlyNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme) {
        super("On-the-fly Node Pruning", scheme);
        duplicatePropagation = adp;
    }

    @Override
    protected void filterComparisons(List<AbstractBlock> blocks) {
        double comparisons = 0;
        duplicatePropagation.resetDuplicates();
        for (AbstractBlock block : blocks) {
            ComparisonIterator iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison comparison = iterator.next();
                if (isValidComparison(block.getBlockIndex(), comparison)) {
                    comparisons++;
                    duplicatePropagation.isSuperfluous(comparison);
                }
            }
        }
        System.out.println("Executed comparisons\t:\t" + comparisons);
        System.out.println("Detected Duplicates\t:\t" + duplicatePropagation.getNoOfDuplicates());
    }
    
}