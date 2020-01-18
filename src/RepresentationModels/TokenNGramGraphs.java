package RepresentationModels;

import Utilities.RepresentationModel;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentWordGraph;

/**
 *
 * @author gap2
 */

public class TokenNGramGraphs extends GraphModel {
    
    public TokenNGramGraphs (int n, RepresentationModel model, String iName) {
        super(n, model, iName);

        graphModel = new DocumentWordGraph(nSize, nSize, nSize);
    }

    @Override
    public void updateModel(String text) {
        final DocumentWordGraph tempGraph = new DocumentWordGraph(nSize, nSize, nSize);
        tempGraph.setDataString(text);
        
        noOfDocuments++;
        getGraphModel().merge(tempGraph, 1 - (noOfDocuments-1)/noOfDocuments);
    }
}