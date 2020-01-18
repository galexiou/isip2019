package EffectivenessLayer.MemoryBased;

import DataStructures.AbstractBlock;
import DataStructures.Attribute;
import DataStructures.BilateralBlock;
import DataStructures.EntityProfile;
import DataStructures.UnilateralBlock;
import Utilities.Constants;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Blocking method presented in 
 * "TYPiMatch: type-specific unsupervised learning of keys and key values for heterogeneous web data integration" 
 * by Yongtao Ma, Thanh Tran, ACM WSDM 2013.
 * 
 */

public class TYPiMatch extends AbstractBlockingMethod implements Constants {
    
    private final static int MINIMUM_CLIQUE_SIZE = 1;
    private final static int MINIMUM_FEATURE_LENGTH = 1;
    private final static int MINIMUM_TERM_FREQUENCY = 1;
    
    private double EPSILON = 0.35;
    private double THETA = 0.25;

    private int noOfEntities;
    private int[][] featureEntities; 
    
    private final List<EntityProfile> entityProfiles1;
    private final List<EntityProfile> entityProfiles2;
    private final Set<String> stopWords;
    private Set<Integer>[] typeEntities;
    private Set<String> psFeatures;
    private String[] features;
    
    public TYPiMatch(double ep, double th, List<EntityProfile> profiles1, List<EntityProfile> profiles2, Set<String> sWords) {
        super("TYPiMatch");
        
        EPSILON = ep;
        THETA = th;
        entityProfiles1 = profiles1;
        entityProfiles2 = profiles2;
        stopWords = sWords;
    }

    private void addEntityToIndex(int entityId, List<String> values, Map<String, List<Integer>> index) {
        for (String value : values) {
            for (String token : getTokens(value)) {
                String currentToken = token.trim();
                if (MINIMUM_FEATURE_LENGTH < currentToken.length() && !stopWords.contains(currentToken)) {
                    List<Integer> entities = index.get(currentToken);
                    if (entities == null) {
                        entities = new ArrayList<Integer>();
                        index.put(currentToken, entities);
                    }
                    entities.add(entityId);
                }
            }
        }
    }

    private void addEntityToFinalIndex(int entityId, List<String> values, HashMap<String, List<Integer>> index) {
        for (String value : values) {
            for (String token : getTokens(value)) {
                String currentToken = token.trim();
                if (MINIMUM_FEATURE_LENGTH < currentToken.length() 
                        && !stopWords.contains(currentToken)
                        && psFeatures.contains(currentToken)) {
                    List<Integer> entities = index.get(currentToken);
                    if (entities == null) {
                        entities = new ArrayList<Integer>();
                        index.put(currentToken, entities);
                    }
                    entities.add(entityId);
                }
            }
        }
    }
    
    @Override
    public List<AbstractBlock> buildBlocks() {
        // extract features
        // key of index -> token, value of index -> list of entity ids
        Map<String, List<Integer>> invertedIndex = indexEntities();
        
        // convert index to blocks
        // first dimension -> token, second dimension -> list of entity ids
        featureEntities = parseIndex(invertedIndex);
        System.out.println("Initial features\t:\t" + featureEntities.length);
        
        // extract pseudo-schema features
        // group them according to the number of entities they have in common
        final Set<Integer>[] featureCliques = extractPseudoSchemaFeatures();
        System.out.println("Number of cliques\t:\t" + featureCliques.length);

        // pseudo-schema features are those retained in cliques)
        getPseudoSchemaFeatures(featureCliques);
        
        //cluster cliques to get types
        getConnectedComponents(featureCliques);

        //select best attribute for each cluster
        final String[] selectedAttribute = selectBlockingKeys();
        return selectKeyValues(selectedAttribute);
    }

    private Set<Integer>[] extractPseudoSchemaFeatures() {
        int noOfFeatures = featureEntities.length;
        final UndirectedGraph<Integer, DefaultEdge> featuresGraph = getGraph(noOfFeatures);
        for (int i = 0; i < noOfFeatures; i++) {
            for (int j = i + 1; j < noOfFeatures; j++) {
                double noOfCommonEntities = Converter.getSortedListsOverlap(featureEntities[i], featureEntities[j]);
                if (0 < noOfCommonEntities) {
                    double probFiFj = noOfCommonEntities / featureEntities[j].length;
                    double probFjFi = noOfCommonEntities / featureEntities[i].length;
                    if (probFiFj >= THETA && probFjFi >= THETA) {
                        featuresGraph.addEdge(i, j);
                    }
                }
            }
        }
//        System.out.println("Number of edges between features\t:\t" + featuresGraph.edgeSet().size());

        BronKerboschCliqueFinder cliqueFinder = new BronKerboschCliqueFinder(featuresGraph);
        Collection<Set<Integer>> cliquesList = cliqueFinder.getAllMaximalCliques();
        return getCleanCliques(cliquesList);
    }

    private List<String>[] getArrayOfLists(int noOfLists) {
        final List<String>[] lists = new ArrayList[noOfLists];
        for (int i = 0; i < noOfLists; i++) {
            lists[i] = new ArrayList<String>();
        }
        return lists;
    }
    
    private Map<String, List<Integer>>[] getArrayOfMaps(int noOfMaps) {
        final Map<String, List<Integer>>[] maps = new HashMap[noOfMaps];
        for (int i = 0; i < noOfMaps; i++) {
            maps[i] = new HashMap<String, List<Integer>>();
        }
        return maps;
    }

    private HashMap<String, Integer> getAttributeNames(Set<Integer> typeEntities) {
        int index = 0;
        final HashMap<String, Integer> attributeNames = new HashMap<String, Integer>();
        for (int entityId : typeEntities) {
            for (Attribute entityAttribute : getEntityAttributes(entityId)) {
                if (!attributeNames.containsKey(entityAttribute.getName())) {
                    attributeNames.put(entityAttribute.getName(), index++);
                }
            }
        }
        return attributeNames;
    }

    // implementation of Algorith 2 in the paper TYPiMatch
    private int getBestAttributeIndex(int typeEntities, int[][][] attributeBlocks) {
        int noOfAttributes = attributeBlocks.length;
        double[][] entropyMatrix = new double[noOfAttributes][noOfAttributes];
        for(int i = 0; i < noOfAttributes; i++){
            for(int j = 0; j < noOfAttributes; j++){
                if(i != j){
                    double dependencyStrenght = 0;
                    for (int[] entityList1 : attributeBlocks[i]) {
                        for (int[] entityList2 : attributeBlocks[j]) {
                            double overlap = Converter.getSortedListsOverlap(entityList1, entityList2);
                            dependencyStrenght += overlap/typeEntities*Math.log(entityList1.length/overlap);
                        }
                    }
                    entropyMatrix[i][j] = 1/(1+dependencyStrenght);
                }
            }
        }
	
        // find attribute with maximum entropy
        int maxIndex = Integer.MIN_VALUE;
        double maxEntropy = Double.MIN_VALUE;
        for(int i = 0; i < noOfAttributes; i++){
            int validPairs = 0;
            double score = 0.0;
            for(int j = 0; j  < noOfAttributes; j++){
                if(i!=j && entropyMatrix[i][j]>=0){
                    validPairs++;
                    score += entropyMatrix[i][j];
                }
            }
            score /= validPairs;
            if (maxEntropy < score) {
                maxIndex = i;
                maxEntropy = score;
            }
        }
        if (maxIndex == Integer.MIN_VALUE) {
            return 0;
        }
        return maxIndex;
    }

    private Set<Integer>[] getCleanCliques(Collection<Set<Integer>> cliquesList) {
        int singletonCliques = 0;
        Iterator iterator = cliquesList.iterator();
        while (iterator.hasNext()) {
            Set<Integer> currentClique = (Set<Integer>) iterator.next();
            if (currentClique.size() <= MINIMUM_CLIQUE_SIZE) {
                singletonCliques++;
                iterator.remove();
            } 
        }
//        System.out.println("Singleton cliques\t:\t" + singletonCliques);
        return cliquesList.toArray(new Set[cliquesList.size()]);
    }
    
    private void getConnectedComponents(Set<Integer>[] cliques) {
//        System.out.println("Getting clique entities...");
        int noOfCliques = cliques.length;
        final int[][] cliqueEntities = new int[noOfCliques][];
        for (int i = 0; i < noOfCliques; i++) {
            Set<Integer> currentEntities = new TreeSet<Integer>();
            for (int featureId : cliques[i]) {
                for (int j = 0; j < featureEntities[featureId].length; j++) {
                    currentEntities.add(featureEntities[featureId][j]);
                }
            }
            cliqueEntities[i] = Converter.convertListToArray(currentEntities);
        }
        
//        System.out.println("Building the cliques graph...");
        final UndirectedGraph<Integer, DefaultEdge> graph = getGraph(noOfCliques);
        for (int i = 0; i < noOfCliques; i++) {
            for (int j = i+1; j < noOfCliques; j++) {
                double overlap = Converter.getSortedListsOverlap(cliqueEntities[i], cliqueEntities[j]);
                if (0 < overlap) {
                    double probFiFj = overlap/cliqueEntities[i].length;
                    double probFjFi = overlap/cliqueEntities[j].length;
                    if (probFiFj > EPSILON && probFjFi > EPSILON) {
                        graph.addEdge(i, j);
                    }
                }
            }
        }
        
//        System.out.println("Getting connected components...");
        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int noOfConComs = connectedComponents.size();
//        System.out.println("No of connected components\t:\t" + noOfConComs);
        
        typeEntities = new Set[noOfConComs+1];
        for (int i = 0; i < noOfConComs+1; i++) {
            typeEntities[i] = new HashSet<Integer>();
        }
        
        boolean[] entityInType = new boolean[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            entityInType[i] = false;
        }
        
        int index = 0;
        for (Set<Integer> component : connectedComponents) {
            for (Integer clique : component) {
                for (int j = 0; j < cliqueEntities[clique].length; j++) {
                    entityInType[cliqueEntities[clique][j]] = true;
                    typeEntities[index].add(cliqueEntities[clique][j]);
                }
            }
            index++;
        }
        
        for (int i = 0; i < noOfEntities; i++) {
            if (!entityInType[i]) {
                typeEntities[noOfConComs].add(i);
            }
        }
//        System.out.println("\n\nSize of unknown type\t:\t" + typeEntities[noOfConComs].size());
    }

    private Set<Attribute> getEntityAttributes(int entityId) {
        if (entityId < entityProfiles1.size()) {
            return entityProfiles1.get(entityId).getAttributes();
        }

        return entityProfiles2.get(entityId - entityProfiles1.size()).getAttributes();
    }
    
    private UndirectedGraph<Integer, DefaultEdge> getGraph(int noOfEdges) {
        final UndirectedGraph<Integer, DefaultEdge> featuresGraph = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < noOfEdges; i++) {
            featuresGraph.addVertex(i);
        }
        return featuresGraph;
    }
    
    private List<AbstractBlock> getListOfBlocks(int[][] blocks) {
        final List<AbstractBlock> blockObjects = new ArrayList<AbstractBlock>();
        if (entityProfiles2 == null) {
            for (int[] block : blocks) {
                blockObjects.add(new UnilateralBlock(block));
            } 
        } else {
            for (int[] block : blocks) {
                final ArrayList<Integer> entitiesD1 = new ArrayList<Integer>();
                final ArrayList<Integer> entitiesD2 = new ArrayList<Integer>();
                for (int entityId : block) {
                    if (entityId < entityProfiles1.size()) {
                        entitiesD1.add(entityId);
                    } else {
                        entitiesD2.add(entityId - entityProfiles1.size());
                    }
                }
                Collections.sort(entitiesD1);
                Collections.sort(entitiesD2);
                int[] idsArray1 = Converter.convertListToArray(entitiesD1);
                int[] idsArray2 = Converter.convertListToArray(entitiesD2);
                blockObjects.add(new BilateralBlock(idsArray1, idsArray2));
            }
        }
        return blockObjects;
    }
    
    private void getPseudoSchemaFeatures(Set<Integer>[] featureCliques) {
        psFeatures = new HashSet<String>();
        for (Set<Integer> clique : featureCliques) {
            for (int featureId : clique) {
                psFeatures.add(features[featureId]);
            }
        }
//        System.out.println("Number of pseudo-schema features\t:\t" + psFeatures.size());
    }
        
    private String[] getTokens(String value) {
        value = value.toLowerCase();
        Pattern p = Pattern.compile("[.,\"\\?!:';()_/-<>]");
        Matcher m = p.matcher(value);
        String cleanValue = m.replaceAll(" ");
        cleanValue = cleanValue.replaceAll("-", "  ").replaceAll("/", " ").replaceAll("\\\\", " ");
        return cleanValue.split(" ");
    }

    private int[][] getTypeBlocks(int typeId, String typeAttribute) {
        final HashMap<String, List<Integer>> invertedIndex = new HashMap<String, List<Integer>>();
        for (Integer entityId : typeEntities[typeId]) {
            final ArrayList<String> attributeValues = new ArrayList<String>();
            for (Attribute entityAttribute : getEntityAttributes(entityId)) {
                if (entityAttribute.getName().equals(typeAttribute)) {
                    attributeValues.add(entityAttribute.getValue());
                }
            }
            addEntityToFinalIndex(entityId, attributeValues, invertedIndex);
        }
        return parseIndex(invertedIndex);
    }

    private Map<String, List<Integer>> indexEntities() {
        int entityId = 0;
        final Map<String, List<Integer>> invertedIndex = new HashMap<String, List<Integer>>();
        for (EntityProfile profile : entityProfiles1) {
            final List<String> values = new ArrayList<String>();
            for (Attribute attribute : profile.getAttributes()) {
                values.add(attribute.getValue());
            }
            addEntityToIndex(entityId++, values, invertedIndex);
        }

        noOfEntities = entityProfiles1.size();
        if (entityProfiles2 != null) {
            noOfEntities += entityProfiles2.size();
            for (EntityProfile profile : entityProfiles2) {
                final ArrayList<String> values = new ArrayList<String>();
                for (Attribute attribute : profile.getAttributes()) {
                    values.add(attribute.getValue());
                }
                addEntityToIndex(entityId++, values, invertedIndex);
            }
        }

        return invertedIndex;
    }

    private int[][] parseIndex(Map<String, List<Integer>> index) {
        final List<String> featuresList = new ArrayList<String>();
        final List<List<Integer>> featuresToEntities = new ArrayList<List<Integer>>();
        for (Entry<String, List<Integer>> entry : index.entrySet()) {
            if (MINIMUM_TERM_FREQUENCY < entry.getValue().size()) {
                featuresList.add(entry.getKey());
                featuresToEntities.add(entry.getValue());
            }
        }

        if (features == null) {
            features = featuresList.toArray(new String[featuresList.size()]);
        }

        int blockIndex = 0;
        int[][] blocks = new int[featuresToEntities.size()][];
        for (List<Integer> entities : featuresToEntities) {
            Collections.sort(entities);
            blocks[blockIndex++] = Converter.convertListToArray(entities);
        }
        return blocks;
    }

    private String[] selectBlockingKeys() {
        int index = 0;
        final String[] attributes = new String[typeEntities.length];
        for (Set<Integer> currentEntities : typeEntities) {
            if (!currentEntities.isEmpty()) {
                attributes[index++] = selectTypeBLKs(currentEntities);
//            System.out.println("Selected attribute for type " + index + "\t:\t" + attributes[index - 1]);
            }
        }
        return attributes;
    }

    private List<AbstractBlock> selectKeyValues(String[] selectedAttribute) {
        final List<AbstractBlock> blocks = new ArrayList<AbstractBlock>();
        for (int i = 0; i < selectedAttribute.length; i++) {
            int[][] newBlocks = getTypeBlocks(i, selectedAttribute[i]);
            if (newBlocks != null) {
                blocks.addAll(getListOfBlocks(newBlocks));
            }
        }
        return blocks;
    }

    private String selectTypeBLKs(Set<Integer> currentTypeEntities) {
        //key -> attribute name, value -> attribute index
        final HashMap<String, Integer> attributeNames = getAttributeNames(currentTypeEntities);
        
        // find the entities and the values of each attribute name
        int noOfAttributes = attributeNames.size();
        // one inverted index for each attribute
        final Map<String, List<Integer>>[] invertedIndices = getArrayOfMaps(noOfAttributes);
        for (Integer entityId : currentTypeEntities) { 
            final List<String>[] attributeValues = getArrayOfLists(noOfAttributes);
            for (Attribute entityAttribute : getEntityAttributes(entityId)) {
                int attrIndex = attributeNames.get(entityAttribute.getName());
                attributeValues[attrIndex].add(entityAttribute.getValue());
            }

            for (int i = 0; i < noOfAttributes; i++) {
                addEntityToIndex(entityId, attributeValues[i], invertedIndices[i]);
            }
        }

        //first dimension -> attribute names
        //second dimension -> tokens
        //third dimension -> list of entity ids
        final int[][][] typeAttributeBlocks = new int[noOfAttributes][][];
        final String[] typeAttributes = new String[attributeNames.size()];
        for (Entry<String, Integer> entry : attributeNames.entrySet()) {
            typeAttributes[entry.getValue()] = entry.getKey();
            typeAttributeBlocks[entry.getValue()] = parseIndex(invertedIndices[entry.getValue()]);
        }
        
        int bestAtributeId = getBestAttributeIndex(currentTypeEntities.size(), typeAttributeBlocks);
        return typeAttributes[bestAtributeId];
    }

	@Override
	public void buildQueryBlocks() {
		// TODO Auto-generated method stub
		
	}
}