//@author Michael Haertling
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dataset {

    private String classBias;

    ArrayList<String[]> records;
    String[] labels;
    List<String>[] attributeValues;
    List<Integer>[] attributeCounts;
    List<Double>[] attributeProportions;

    Dataset[][] dataSplits;
    int pastSplits;

    boolean finalized = false;

    //Used to create a dataset maually
    //Usually used in dataset splits
    public Dataset(String[] labels, List<String>[] attributeValues, int partitionIndex, String bias, int pastSplits) {
        this.labels = labels;
        records = new ArrayList<>();
        dataSplits = new Dataset[labels.length][];
        this.classBias = bias;
        this.pastSplits = pastSplits;
        this.attributeValues = attributeValues;
    }

    public void addRecord(String[] record) throws AlreadyFinalizedException {
        if (finalized) {
            throw new AlreadyFinalizedException();
        }
        records.add(record);
    }

    //Used to load a dataset from a file
    public Dataset(String path, int maxLines) throws FileNotFoundException {
        int count = 0;
        Scanner in = new Scanner(new File(path));
        records = new ArrayList<>();
        //Read in the information
        String[] headers = parseLine(in.nextLine());
        while (in.hasNextLine() && (count<maxLines||maxLines<0)) {
            String[] record = parseLine(in.nextLine());
            if (record != null) {
                records.add(record);
                count++;
            } else {
                System.out.println("Skipping line");
            }
        }

        //Remove the "class" final tag
        labels = new String[headers.length - 1];
        System.arraycopy(headers, 0, labels, 0, labels.length);
        finalized = true;
        dataSplits = new Dataset[labels.length][];
        pastSplits = 0;
        attributeValues = new ArrayList[headers.length];
        //Load all attribute values
        for(int i=0; i<this.numAttributes()+1;i++){
            findAttributeValues(i);
        }
    }

    public Dataset(String path) throws FileNotFoundException{
        this(path,-1);
    }
    
    private String[] parseLine(String line) {
        if (line.indexOf(' ') >= 0) {
            return null;
        } else {
            return line.split("\t");
        }
    }

    private void findAttributeValues(int index) {
        finalized = true;
        if (attributeCounts == null) {
            attributeCounts = new ArrayList[labels.length + 1];
            attributeProportions = new ArrayList[labels.length + 1];
        }
        if (attributeCounts[index] == null) {
            attributeCounts[index] = new ArrayList<>();
            attributeProportions[index] = new ArrayList<>();
        } else {
            return;
        }

        if (attributeValues[index] == null) {
            attributeValues[index] = new ArrayList<>();
            for (String[] record : records) {
                if (!attributeValues[index].contains(record[index])) {
                    attributeValues[index].add(record[index]);
                }
            }
            Collections.sort(attributeValues[index]);
        }

        for (int i = 0; i < attributeValues[index].size(); i++) {
            attributeCounts[index].add(0);
        }

        for (String[] record : records) {
            String av = record[index];
            int loc = attributeValues[index].indexOf(av);
            //Increment the count
            attributeCounts[index].set(loc, attributeCounts[index].get(loc) + 1);
        }
        //Calculate the attribute proportions
        //Find the total count
        int total = records.size();

        //Find the proportions
//        System.out.println("Finding proportions for index = "+index);
//        System.out.println(this.toString());
        for (int i = 0; i < attributeCounts[index].size(); i++) {
            if (total == 0) {
                attributeProportions[index].add(new Double(0));
            } else {
                attributeProportions[index].add((double) attributeCounts[index].get(i) / total);
            }
//            System.out.println("Attribute index "+i+"("+this.getAttributeValue(index, i)+"): "+attributeCounts[index].get(i)+"/"+total+" = "+(attributeProportions[index].get(attributeProportions[index].size()-1)));
        }

//        System.out.println();
    }

    public String[] getRecord(int index) {
        return records.get(index);
    }

    public String getAttribute(int r, int a) {
        return records.get(r)[a];
    }

    public int numAttributes() {
        return labels.length;
    }

    public int numRecords() {
        return records.size();
    }

    public List<String> getAttributeValues(int index) {
        findAttributeValues(index);
        return attributeValues[index];
    }

    public List<Integer> getAttributeCounts(int index) {
        findAttributeValues(index);
        return attributeCounts[index];
    }

    public List<Double> getAttributeProportions(int index) {
        findAttributeValues(index);
        return attributeProportions[index];
    }

    public String getAttributeName(int index) {
        return labels[index];
    }

    public String getAttributeValue(int index, int attribute) {
        return attributeValues[index].get(attribute);
    }

    public String getClass(int index) {
        return records.get(index)[records.get(index).length - 1];
    }

    public Dataset[] partition(int attribute) {
        if (dataSplits[attribute] != null) {
            return dataSplits[attribute];
        }

        if (!attributeCalculated(attribute)) {
            findAttributeValues(attribute);
        }
        //Setting this to 2 manually sucks
        Dataset[] dataSets = new Dataset[2];
        //Instantiate the datasets
        for (int i = 0; i < dataSets.length; i++) {
            dataSets[i] = new Dataset(labels, attributeValues, attribute, getClassBias(), pastSplits + 1);
        }

        //Split the data by the attribute
        List<String> attributeVals = attributeValues[attribute];
        for (String[] rec : records) {
            int index = attributeVals.indexOf(rec[attribute]);
            try {
                dataSets[index].addRecord(rec);
            } catch (AlreadyFinalizedException ex) {
                Logger.getLogger(Dataset.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Cache this work
        dataSplits[attribute] = dataSets;

        return dataSets;
    }

    private boolean attributeCalculated(int attribute) {
        return attributeValues != null && attributeValues[attribute] != null;
    }

    public String getClassBias() {
        if (classBias == null) {
            List<Integer> counts = getAttributeCounts(this.numAttributes());
            int max = -1;
            int biasIndex = -1;
            for (int i = 0; i < counts.size(); i++) {
                int tmp = counts.get(i);
                if (tmp > max) {
                    max = tmp;
                    biasIndex = i;
                }
            }

            classBias = this.getAttributeValue(this.numAttributes(), biasIndex);
        }
        return classBias;
    }

    public int getNumPastSplits() {
        return pastSplits;
    }

    public boolean splitsRemaining() {
        return pastSplits < this.numAttributes();
    }

    @Override
    public String toString() {
        String out = "-------------\n";
        for (String[] rec : records) {
            out += Arrays.toString(rec) + "\n";
        }
        out += "-------------\n";
        return out;
    }

}
