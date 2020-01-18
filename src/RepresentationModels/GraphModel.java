package RepresentationModels;

import Utilities.RepresentationModel;
import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;

/**
 *
 * @author gap2
 */

public abstract class GraphModel extends AbstractModel {
    
    protected DocumentNGramGraph graphModel;
    protected final static NGramCachedGraphComparator comparator = new NGramCachedGraphComparator();
    
    public GraphModel (int n, RepresentationModel model, String iName) {
        super(n, model, iName);
    }
    
    public DocumentNGramGraph getGraphModel() {
        return graphModel;
    }
    
    public void setModel(String text) {
        noOfDocuments++;
        graphModel.setDataString(text);
    }
    
    public double getValue(AbstractModel model1, AbstractModel model2) {
        try {
            final GraphModel graphModel1 = (GraphModel) model1;
            final GraphModel graphModel2 = (GraphModel) model2;

            final GraphSimilarity graphSimilarity =  comparator.getSimilarityBetween(graphModel1.getGraphModel(), graphModel2.getGraphModel());
            return graphSimilarity.ValueSimilarity;
        } catch (Exception excp) {
            excp.printStackTrace();
            return -1.0;
        }
    }
    
    @Override
    public double getSimilarity(AbstractModel oModel) {//Value Similarity
        final GraphSimilarity graphSimilarity =  comparator.getSimilarityBetween(this.getGraphModel(), ((GraphModel) oModel).getGraphModel());
        return graphSimilarity.ValueSimilarity;
    }
    
    public void updateModel(GraphModel model) {
        noOfDocuments++;
        graphModel.merge(model.getGraphModel(), 1.0 - (noOfDocuments-1.0)/noOfDocuments);
    }
}