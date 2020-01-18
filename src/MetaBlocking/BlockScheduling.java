package MetaBlocking;

import Comparators.BlockUtilityComparator;
import DataStructures.AbstractBlock;
import EfficiencyLayer.AbstractEfficiencyMethod;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gap2
 */

public class BlockScheduling extends AbstractEfficiencyMethod {
    
    public BlockScheduling() {
        super("Block Scheduling");
    }
    
    @Override
    public void applyProcessing(List<AbstractBlock> blocks) {
        for (AbstractBlock block : blocks) {
            block.setUtilityMeasure();
        }
        Collections.sort(blocks, new BlockUtilityComparator());
    }
} 
