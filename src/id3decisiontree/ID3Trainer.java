import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@author Michael Haertling
public class ID3Trainer {

    Dataset data;
    DecisionTree tree;
    ID3Log log;

    public ID3Trainer(Dataset data){
        this.data = data;
    }
    
    public ID3Trainer(String datasetPath) throws FileNotFoundException {
        data = new Dataset(datasetPath);
    }

    public void activateLogging() {
        log = new ID3Log("TreeLog.txt");
    }

    public DecisionTree generateTree() {
        if (log != null) {
            log.log("Starting Tree Generation...");
        }
        boolean[] pastSplits = new boolean[data.numAttributes()];
        Arrays.fill(pastSplits, false);
        DecisionTreeNode root = generateTree(data, pastSplits);
        if (log != null) {
            log.log("Done.");
        }
        return new DecisionTree(root);
    }

    private DecisionTreeNode generateTree(Dataset data, boolean[] pastSplits) {
        //Should we split?
        //Are there any more attributes to be split upon?
        boolean attributesRemaining = data.splitsRemaining();
        boolean unanimousClassData = true;

        //We only need to caclulate this is there are attributes remaining
        if (attributesRemaining) {
            //Get the class proportions
            List<Double> proportions = data.getAttributeProportions(data.numAttributes());
            //Check if the data is unanimous
            for (Double p : proportions) {
                if (p != 0 && p != 1) {
                    unanimousClassData = false;
                    break;
                }
            }
        }

        if (log != null) {
            if (!attributesRemaining) {
                log.log("There are no attributes remaining in this data set. Cannot split.");
                log.log(data.toString());
            } else if (unanimousClassData) {
                System.out.println("UNANIMOUS");
                log.log("The data is unanimous. No need to split.");
                log.log(data.toString());
            }
        }

        if (attributesRemaining && !unanimousClassData ) {
            //Calculate the IG for each attribute
            int bestAttributeIndex = -1;
            double bestIG = -1;
            if (log != null) {
                log.log("Calculating IG for attributes.");
            }
            for (int i = 0; i < data.numAttributes(); i++) {
                if (!pastSplits[i]) {
                    if (log != null) {
                        log.log("IG(" + data.getAttributeName(i) + ")");
                    }
                    double tmpIG = calculateInformationGain(i, data);
                    if (bestIG < tmpIG) {
                        bestIG = tmpIG;
                        bestAttributeIndex = i;
                    }
                    if (log != null) {
                        log.newLine();
                    }
                }
            }
//            if(bestIG == 0){
//                if(log != null){
//                    log.log("The highest IG = 0. There is no need to split.");
//                }
//                return new DecisionTreeNode(data,-1);
//            }

            if (log != null) {
                log.log("Splitting By: " + data.getAttributeName(bestAttributeIndex));
                log.log("Generating node");
            }
            DecisionTreeNode node = new DecisionTreeNode(data, bestAttributeIndex);
            Dataset[] dataSets = data.partition(bestAttributeIndex);
            
            //Check if one of the dataSets is empty
            for (Dataset d : dataSets) {
                if(d.numRecords() == 0){
                    return new DecisionTreeNode(data,-1);
                }
            }

            for (Dataset d : dataSets) {
                if (log != null) {
                    log.newLine();
                    log.log("Recursing for Child Node\n" + d.toString());
                    log.newLine();
                }
                boolean[] splits = Arrays.copyOf(pastSplits, pastSplits.length);
                splits[bestAttributeIndex] = true;
                node.addChild(generateTree(d, splits));
            }
            if (log != null) {
                log.log("Node Complete");
                log.newLine();
            }
            return node;
        } else {
            return new DecisionTreeNode(data, -1);
        }

    }

    private double calculateEntropy(Dataset data) {
        String logLine = "";

        //H=sum(prob(i)*log_2(prob(i)))
        double sum = 0;
        //Get proportions for the class
        List<Double> proportions = data.getAttributeProportions(data.numAttributes());

        //Do the summation
        if (log != null) {
            List<Integer> counts = data.getAttributeCounts(data.numAttributes());
            log.log("Counts: " + counts.toString());
            log.log("Proportions: " + proportions.toString());
        }
        logLine += "H(S) = ";
        for (Double p : proportions) {
            double log2;
            if (p != 0) {
                log2 = (Math.log(p) / Math.log(2));
            } else {
                log2 = 0;
            }

            logLine += -p + " * " + log2 + " + ";
            sum += (-p * log2);
        }

        logLine = logLine.substring(0, logLine.length() - 3) + " = " + sum;

        if (log != null) {
            log.log(logLine);
        }

        return sum;
    }

    private double calculateConditionalEntropy(int attribute, Dataset data) {
        String logLine = "";

        //Get entropies
        if (log != null) {
            log.log("Finding Entropies for Sub Nodes.");
        }

        Dataset[] dataSets = data.partition(attribute);
        double[] entropies = new double[dataSets.length];
        for (int i = 0; i < dataSets.length; i++) {
            if (log != null) {
                log.log("H(S) of (" + data.getAttributeName(attribute) + " = " + data.getAttributeValue(attribute, i) + ") split");
            }
            entropies[i] = calculateEntropy(dataSets[i]);
        }

        //Get proportions
        List<Double> props = data.getAttributeProportions(attribute);

        if (log != null) {
            log.log("Entropies Collected for Sub Nodes.");
        }

        //Caclulate the conditional entropy
        logLine += "H(S|" + data.getAttributeName(attribute) + ") = ";
        double ce = 0;
        
        for (int i = 0; i < entropies.length; i++) {
            logLine += entropies[i] + " * " + props.get(i) + " + ";
            ce += entropies[i] * props.get(i);
        }

        logLine = logLine.substring(0, logLine.length() - 3) + " = " + ce;

        if (log != null) {
            log.log(logLine);
        }

        return ce;
    }

    private double calculateInformationGain(int attribute, Dataset data) {
        double ig = calculateEntropy(data) - calculateConditionalEntropy(attribute, data);
        if (log != null) {
            log.log("IG(" + data.getAttributeName(attribute) + ") = H(S) - H(S|" + data.getAttributeName(attribute) + ") = " + ig);
        }
        return ig;
    }

}
