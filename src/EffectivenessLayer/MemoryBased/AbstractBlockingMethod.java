package EffectivenessLayer.MemoryBased;

import DataStructures.AbstractBlock;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class AbstractBlockingMethod {
    
    private final String name;
    
    public AbstractBlockingMethod(String nm) {
        name = nm;
    }

    public String getName() {
        return name;
    }
    
    public abstract List<AbstractBlock> buildBlocks();
    public abstract void buildQueryBlocks();
    
}