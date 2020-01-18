package RepresentationModels;

import Utilities.RepresentationModel;
import java.io.Serializable;

/**
 *
 * @author G.A.P. II
 */

public abstract class AbstractModel implements Serializable {
    
    private static final long serialVersionUID = 328759404L;

    protected final int nSize;
    protected double noOfDocuments;
    
    protected final RepresentationModel modelType;
    protected final String instanceName;
    
    public AbstractModel(int n, RepresentationModel md, String iName) {
        instanceName = iName;
        modelType = md;
        nSize = n;
        noOfDocuments = 0;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public RepresentationModel getModelType() {
        return modelType;
    }
    
    public double getNoOfDocuments() {
        return noOfDocuments;
    }
    
    public int getNSize() {
        return nSize;
    }
    
    public abstract double getSimilarity(AbstractModel oModel);
    public abstract void updateModel(String text);
}