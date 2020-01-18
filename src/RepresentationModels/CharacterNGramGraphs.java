package RepresentationModels;

import Utilities.RepresentationModel;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramHGraph;

/**
 *
 * @author gap2
 */

public class CharacterNGramGraphs extends GraphModel {
    
    private final static int SEGMENTS_UNIT = 100;
    
    public CharacterNGramGraphs (int n, RepresentationModel model, String iName) {
        super(n, model, iName);
        
        graphModel = new DocumentNGramHGraph(nSize, nSize, nSize, nSize*SEGMENTS_UNIT);
    }
    
    @Override
    public void updateModel(String text) {
        final DocumentNGramGraph tempGraph = new DocumentNGramGraph(nSize, nSize, nSize);
        tempGraph.setDataString(text);
        
        noOfDocuments++;
        graphModel.merge(tempGraph, 1 - (noOfDocuments-1)/noOfDocuments);
    }
}