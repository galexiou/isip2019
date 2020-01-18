package Utilities;

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.UnilateralBlock;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.index.*;


public class ExportBlocks {
    
    private final List<AbstractBlock> blocks;
    private IndexReader d1Index;
    private IndexReader d2Index;
    private final String index1Path;
    private final String index2Path;
    
    public ExportBlocks (String d1IndexPath, String d2IndexPath) {
        blocks = new ArrayList<AbstractBlock>();
        index1Path = d1IndexPath;
        index2Path = d2IndexPath;
    }
    
    private void closeIndexes() throws IOException {
        d1Index.close();
        if (d2Index != null) {
            d2Index.close();
        }
    }
    
    public List<AbstractBlock> getBlocks() {
        if (blocks != null && !blocks.isEmpty()) {
            return blocks;
        }
        
        try {
            d1Index = openIndex(index1Path);
            if (index2Path != null) { //Clean-Clean ER
                d2Index = openIndex(index2Path);
                Set<String> commonTerms = setCommonTerms();
                Map<String, int[]> hashedBlocks = parseD1Index(commonTerms);
                parseD2Index(hashedBlocks);
            } else { //Dirty ER
                parseIndex();
            }
            closeIndexes();
            return blocks;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    private IndexReader openIndex(String indexPath) throws IOException {
        File indexDir = new File(indexPath);
        System.out.println("Opening index\t:\t" + indexPath);
        return IndexReader.open(indexDir);
    }
    
     private Map<String, int[]> parseD1Index(Set<String> commonTerms) throws IOException {
        final Map<String, int[]> hashedBlocks = new HashMap<String, int[]>(2*commonTerms.size());
        TermEnum termsEnumberation = d1Index.terms();
        while (termsEnumberation.next()) {
            Term currentTerm = termsEnumberation.term();

            if (!commonTerms.contains(currentTerm.text())) {
                continue;
            }

            final List<Integer> entityIds = new ArrayList<Integer>();
            TermDocs termDocs = d1Index.termDocs(currentTerm);
            while (termDocs.next()) {
                entityIds.add(termDocs.doc());
            }
            
            int[] idsArray = Converter.convertListToArray(entityIds);
            hashedBlocks.put(currentTerm.text(), idsArray);
        }
        return hashedBlocks;
    }
    
    private void parseD2Index(Map<String, int[]> hashedBlocks) throws IOException {
        blocks.clear();
        TermEnum termsEnumberation = d2Index.terms();
        while (termsEnumberation.next()) {
            Term currentTerm = termsEnumberation.term();
            if (!hashedBlocks.containsKey(currentTerm.text())) {
                continue;
            }

            TermDocs termDocs = d2Index.termDocs(currentTerm);
            final List<Integer> entityIds = new ArrayList<Integer>();
            while (termDocs.next()) {
                entityIds.add(termDocs.doc());
            }
            
            int[] idsArray = Converter.convertListToArray(entityIds);
            int[] d1Entities = hashedBlocks.get(currentTerm.text());
            blocks.add(new BilateralBlock(d1Entities, idsArray));
        }
    }
    
    private void parseIndex() throws IOException {
        blocks.clear();
        TermEnum termsEnumberation = d1Index.terms();
        while (termsEnumberation.next()) {
            Term currentTerm = termsEnumberation.term();
            int docFrequency = d1Index.docFreq(currentTerm);
            if (docFrequency < 2) {
                continue;
            }

            final List<Integer> entityIds = new ArrayList<Integer>();
            TermDocs termDocs = d1Index.termDocs(currentTerm);
            while (termDocs.next()) {
                entityIds.add(termDocs.doc());
            }
            
            int[] idsArray = Converter.convertListToArray(entityIds);
            UnilateralBlock block = new UnilateralBlock(idsArray);
            blocks.add(block);
        }
    }
    
    private Set<String> setCommonTerms() {
        final Set<String> commonTerms = new HashSet<String>();
        try {
            TermEnum d1IndexTerms = d1Index.terms();
            while (d1IndexTerms.next()) {
                Term currentTerm = d1IndexTerms.term();
                int d2DocFrequency = d2Index.docFreq(currentTerm);
                if (d2DocFrequency == 0) {
                    continue;
                }
                commonTerms.add(currentTerm.text());
            }
        } catch (CorruptIndexException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return commonTerms;
    }
    
    public void storeBlocks(String outputPath) {
        System.out.println("\n\nStoring blocks...");
        Utilities.SerializationUtilities.storeSerializedObject(blocks, outputPath);
        System.out.println("Blocks were stored!");
    }

    public static void main (String[] args) throws IOException {
        String mainDirectory = "/opt/data/frameworkData/";
        String blocksPath = mainDirectory+"blocks/movies/tokenUnigramsBlocking";
        String d1IndexDir = mainDirectory+"indices/movies/tokenUnigramsDBP";
        String d2IndexDir = mainDirectory+"indices/movies/tokenUnigramsIMDB";
        ExportBlocks expbl = new ExportBlocks(d1IndexDir, d2IndexDir);
        expbl.storeBlocks(blocksPath);
    }
}