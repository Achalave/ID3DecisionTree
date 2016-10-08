import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

//@author Michael Haertling
public class ID3Main {

    public static void main(String[] args) throws FileNotFoundException {
//        if(args.length != 2){
//            System.out.println("Incorrect number of arguments.");
//            return;
//        }

        String trainingFilePath = args[0];
        String testFilePath = args[1];
//        String trainingFilePath = "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Intro To Machine Learning\\train.dat";
//        String testFilePath = "C:\\Users\\Michael\\Google Drive\\School\\UTD Year 4\\Intro To Machine Learning\\test.dat";

        ID3Trainer trainer = new ID3Trainer(trainingFilePath);
//        trainer.activateLogging();

        DecisionTree tree = trainer.generateTree();
        System.out.println(tree);

        NumberFormat form = DecimalFormat.getInstance();
        form.setMaximumFractionDigits(1);
        System.out.println("Accuracy on training set (" + trainer.data.numRecords() + " instances): " + form.format(testOverDataset(tree, trainer.data) * 100) + "%\n");
        Dataset data = new Dataset(testFilePath);
        System.out.println("Accuracy on training set (" + data.numRecords() + " instances): " + form.format(testOverDataset(tree, data) * 100) + "%");

    }

    public static double testOverDataset(DecisionTree tree, Dataset data) {
        int valid = 0;
        int total = data.numRecords();
        for (String[] record : data.records) {
            if (tree.test(record)) {
                valid++;
            }
        }
        return (double) valid / total;
    }

}
