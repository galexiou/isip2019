package MetaBlocking;

import DataStructures.AbstractBlock;
import Utilities.MetaBlockingConfiguration.WeightingScheme;
import java.util.List;

/**
 *
 * @author gap2
 */
public class EfficientEdgePruning extends EdgePruning {

    public EfficientEdgePruning() {
        super("Efficient Edge Pruning", WeightingScheme.CBS);
        averageWeight = 2.0;
    }

    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        getStatistics(blocks);
        filterComparisons(blocks);
    }
}
