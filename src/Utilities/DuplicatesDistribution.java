package Utilities;

import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import EffectivenessLayer.MemoryBased.AttributeClustering;
import EffectivenessLayer.MemoryBased.CanopyClustering;
import EffectivenessLayer.MemoryBased.CanopyClusteringNN;
import EffectivenessLayer.MemoryBased.TokenBlocking;
import EffectivenessLayer.MemoryBased.TYPiMatch;
import EfficiencyLayer.ComparisonRefinement.AbstractDuplicatePropagation;
import EfficiencyLayer.ComparisonRefinement.BilateralDuplicatePropagation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author gap2
 */

public class DuplicatesDistribution implements Constants {
    
    private final static int NO_OF_SLOTS = 14;
    
    private final int noOfMatches;
    private double[] frequencies;
    private final AbstractDuplicatePropagation duplicatePropagation;
    private final List<AbstractBlock> blocks;
    
    public DuplicatesDistribution(AbstractDuplicatePropagation abp, List<AbstractBlock> blocks) {
        this.blocks = blocks;
        System.out.println("Total blocks\t:\t" + blocks.size());
        duplicatePropagation = abp;
        duplicatePropagation.resetDuplicates();
        noOfMatches = duplicatePropagation.getExistingDuplicates();
        System.out.println("Original matches\t:\t" + noOfMatches);
    }
    
    public void getDistribution() {
        getAbsoluteFrequencies();
        normalizeFrequencies();
        printFrequencies();
    }
    
    private void getAbsoluteFrequencies() {
        frequencies = new double[NO_OF_SLOTS];
        for (AbstractBlock block : blocks) {
            int existingMatches = duplicatePropagation.getNoOfDuplicates();
            block.processBlock(duplicatePropagation);
            int newMatches = duplicatePropagation.getNoOfDuplicates()-existingMatches;
            if (newMatches < 1) {
                continue;
            }
            
            double comparisons = block.getNoOfComparisons();
            int index = (int) Math.log10(comparisons);
            if (index < 0) {
                index = 0;
            }
            frequencies[index]+=newMatches;
        }
        System.out.println("Total detected duplicates\t:\t" + duplicatePropagation.getNoOfDuplicates());
    }
    
    private void printFrequencies() {
        System.out.println("\n\nPrinting frequencies...");
        for (int i = 0; i < NO_OF_SLOTS; i++) {
            System.out.println(fourDigitsDouble.format(frequencies[i]));
        }
    }
    
    private void normalizeFrequencies() {
        for (int i = 0; i < NO_OF_SLOTS; i++) {
            frequencies[i] = frequencies[i]*100/noOfMatches;
        }
    }
    
    public static void main (String[] args) throws IOException, Exception {
        String mainDirectory = "/home/gpapadis/data/profiles/";
        String entitiesD1 = mainDirectory+"dbpediaMoviesUPD";
        String entitiesD2 = mainDirectory+"imdbMoviesUPD";
        String stopWordsPath = mainDirectory + "stopword-list.txt";
        
        String gtDirectory = "/home/gpapadis/data/groundtruth/";
        AbstractDuplicatePropagation adp = new BilateralDuplicatePropagation(gtDirectory+"moviesIdGroundTruthUPD");
        
        List<EntityProfile> entityProfiles1 = (ArrayList<EntityProfile>) SerializationUtilities.loadSerializedObject(entitiesD1);
        List<EntityProfile> entityProfiles2 = (ArrayList<EntityProfile>) SerializationUtilities.loadSerializedObject(entitiesD2);
        
        TokenBlocking imtb = new TokenBlocking(entityProfiles1, entityProfiles2);
        List<AbstractBlock> blocks = imtb.buildBlocks();
        
        DuplicatesDistribution dudi = new DuplicatesDistribution(adp, blocks);
        dudi.getDistribution();
        
        AttributeClustering imac = new AttributeClustering(entityProfiles1, entityProfiles2, RepresentationModel.TOKEN_UNIGRAMS);
        blocks = imac.buildBlocks();
        
        dudi = new DuplicatesDistribution(adp, blocks);
        dudi.getDistribution();
        
        CanopyClustering imcc = new CanopyClustering(0.05, 0.55, entityProfiles1, entityProfiles2);
        blocks = imcc.buildBlocks();
        
        dudi = new DuplicatesDistribution(adp, blocks);
        dudi.getDistribution();
        
        CanopyClusteringNN imccwnn = new CanopyClusteringNN(1, 19, entityProfiles1, entityProfiles2);
        blocks = imccwnn.buildBlocks();
        
        dudi = new DuplicatesDistribution(adp, blocks);
        dudi.getDistribution();
        
        final List<String> stopWords = FileUtilities.getFileLines(stopWordsPath);
        final HashSet<String> stopWordsList = new HashSet<String>(stopWords);
        System.out.println("Stop words\t:\t" + stopWordsList.size());
        
        TYPiMatch typm = new TYPiMatch(0.15, 0.40, entityProfiles1, entityProfiles2, stopWordsList);
        blocks = typm.buildBlocks();
        
        dudi = new DuplicatesDistribution(adp, blocks);
        dudi.getDistribution();
    }
}