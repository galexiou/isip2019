package EffectivenessLayer.MemoryBased;

import DataStructures.Attribute;
import DataStructures.ConnectedComponents;
import DataStructures.EntityProfile;
import DataStructures.Graph;
import RepresentationModels.AbstractModel;
import Utilities.Constants;
import Utilities.RepresentationModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class AttributeClustering extends IndexBasedBlocking implements Constants {
    
    private int noOfClusters;
    
    private final Map<String, Integer>[] attributeClusters;
    private final RepresentationModel model;

    public AttributeClustering(List<EntityProfile> profiles1, 
                                       List<EntityProfile> profiles2, RepresentationModel md) {
        super(profiles1, profiles2, "In-memory Attribute Clustering");
        
        model = md;
        attributeClusters = new HashMap[2];
        AbstractModel[] attributeModels1 = buildAttributeModels(profiles1);
        if (profiles2 != null) {
            AbstractModel[] attributeModels2 = buildAttributeModels(profiles2);
            Graph graph = compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            Graph graph = compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }
    }
    
    private AbstractModel[] buildAttributeModels(List<EntityProfile> profiles) {
        final HashMap<String, List<String>> attributeProfiles = new HashMap<String, List<String>>();
        for (EntityProfile entity : profiles) {
            for (Attribute attribute : entity.getAttributes()) {
                List<String> values = attributeProfiles.get(attribute.getName());
                if (values == null) {
                    values = new ArrayList<String>();
                    attributeProfiles.put(attribute.getName(), values);
                }
                values.add(attribute.getValue());
            }
        }
           
        int index = 0;
        AbstractModel[] attributeModels = new AbstractModel[attributeProfiles.size()];
        for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
            attributeModels[index] = RepresentationModel.getModel(model, entry.getKey());
            for (String value : entry.getValue()) {
                attributeModels[index].updateModel(value);
            }
            index++;
        }
        return attributeModels;
    }
    
    private void clusterAttributes(AbstractModel[] attributeModels, Graph graph) {
        int noOfAttributes = attributeModels.length;
        
        ConnectedComponents concom = new ConnectedComponents(graph);
        int[] clusterCardinality = getClusterCardinalities(noOfAttributes, concom);
        int singletonId = noOfClusters+1;
        
        attributeClusters[0] = new HashMap<String, Integer>(2*noOfAttributes);
        for (int i = 0; i < noOfAttributes; i++) {
            int clusterId = concom.id(i);
            if (clusterCardinality[clusterId] == 1) {
                clusterId = singletonId;
            }
            attributeClusters[0].put(attributeModels[i].getInstanceName(), clusterId);
        }
        attributeClusters[1] = null;
    }

    private void clusterAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2, Graph graph) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;
        int totalAttributes = d1Attributes+d2Attributes;
        
        ConnectedComponents concom = new ConnectedComponents(graph);
        int[] clusterCardinality = getClusterCardinalities(totalAttributes, concom);
        int singletonId = noOfClusters+1;
        
        attributeClusters[0] = new HashMap<String, Integer>(2*d1Attributes);
        for (int i = 0; i < d1Attributes; i++) {
            int clusterId = concom.id(i);
            if (clusterCardinality[clusterId] == 1) {
                clusterId = singletonId;
            }
            attributeClusters[0].put(attributeModels1[i].getInstanceName(), clusterId);
        }
        
        attributeClusters[1] = new HashMap<String, Integer>(2*d2Attributes);
        for (int i = d1Attributes; i < totalAttributes; i++) {
            int clusterId = concom.id(i);
            if (clusterCardinality[clusterId] == 1) {
                clusterId = singletonId;
            }
            attributeClusters[1].put(attributeModels2[i-d1Attributes].getInstanceName(), clusterId);
        }
    }
    
    private Graph compareAttributes(AbstractModel[] attributeModels) {
        int noOfAttributes = attributeModels.length;
        int[] mostSimilarName = new int[noOfAttributes];
        double[] maxSimillarity = new double[noOfAttributes];
        for (int i = 0; i < noOfAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
        }
        
        for (int i = 0; i < noOfAttributes; i++) {
            for (int j =i+1; j < noOfAttributes; j++) {
                double simValue = attributeModels[i].getSimilarity(attributeModels[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j;
                }
                
                if (maxSimillarity[j] < simValue) {
                    maxSimillarity[j] = simValue;
                    mostSimilarName[j] = i;
                }
            }
        }

        final Graph graph = new Graph(noOfAttributes);
        for (int i = 0; i < noOfAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                graph.addEdge(i, mostSimilarName[i]);
            }
        }
        return graph;
    }
    
    private Graph compareAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;
        int totalAttributes = d1Attributes+d2Attributes;
        
        int[] mostSimilarName = new int[totalAttributes];
        double[] maxSimillarity = new double[totalAttributes];
        for (int i = 0; i < totalAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
        }
        
        for (int i = 0; i < d1Attributes; i++) {
            for (int j = 0; j < d2Attributes; j++) {
                double simValue = attributeModels1[i].getSimilarity(attributeModels2[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j+d1Attributes;
                }
                
                if (maxSimillarity[j+d1Attributes] < simValue) {
                    maxSimillarity[j+d1Attributes] = simValue;
                    mostSimilarName[j+d1Attributes] = i;
                }
            }
        }

        final Graph graph = new Graph(totalAttributes);
        for (int i = 0; i < totalAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                graph.addEdge(i, mostSimilarName[i]);
            }
        }
        return graph;
    }
    

    private int[] getClusterCardinalities(int noOfAttributes, ConnectedComponents concom) {
        noOfClusters = concom.count();
        int[] clusterCardinality = new int[noOfClusters];
        for (int i = 0; i < noOfAttributes; i++) {
            clusterCardinality[concom.id(i)]++;
        }
        return clusterCardinality;
    }

    @Override
    protected Map<String, Set<Integer>> indexEntities(int sourceId, List<EntityProfile> profiles) {
        int index = 0;
        final Map<String, Set<Integer>> invertedIndex = new HashMap<String, Set<Integer>>();
        for (EntityProfile profile : profiles) {
            for (Attribute attribute : profile.getAttributes()) {
                Integer clusterId = attributeClusters[sourceId].get(attribute.getName());
                if (clusterId == null) {
                    System.err.println(attribute.getName() + "\t\t" + attribute.getValue());
                    continue;
                }
                String clusterSuffix = ATTRIBUTE_CLUSTER_PREFIX+clusterId+ATTRIBUTE_CLUSTER_SUFFIX;
                    
                String cleanValue = attribute.getValue().replaceAll("_", " ").trim();
                String[] tokens = cleanValue.split("[\\W_]");
                for (String token : tokens) {
                    if (0 < token.trim().length()) {
                        Set<Integer> termEntities = invertedIndex.get(token.trim()+clusterSuffix);
                        if (termEntities == null) {
                            termEntities = new HashSet<Integer>();
                            invertedIndex.put(token.trim()+clusterSuffix, termEntities);
                        }
                        termEntities.add(index);
                    }
                }
            }
            index++;
        }
        return invertedIndex;
    }

	@Override
	public void buildQueryBlocks() {
		// TODO Auto-generated method stub
		
	}
}