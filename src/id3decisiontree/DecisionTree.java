//@author Michael Haertling
public class DecisionTree {

    private final DecisionTreeNode root;

    public DecisionTree(DecisionTreeNode node) {
        root = node;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public boolean test(String[] record) {
        DecisionTreeNode node = getNode(record, root);
        return node.getProdominantClass().equals(record[record.length - 1]);
    }

    public DecisionTreeNode getNode(String[] record) {
        return getNode(record, root);
    }

    private DecisionTreeNode getNode(String[] record, DecisionTreeNode node) {
        if (node.isLeaf()) {
            return node;
        }
        String splitBy = record[node.getSplitAttributeIndex()];
//        if (record[node.getSplitAttributeIndex()].length() == 1) {
//            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + splitBy);
//        }
        if (node.getChildren().size()==1) {
            System.err.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + splitBy);
        }
        int index = node.getData().getAttributeValues(node.getSplitAttributeIndex()).indexOf(splitBy);
        return getNode(record, node.getChildren().get(index));
    }

    public DecisionTreeNode getRoot(){
        return root;
    }
    
}
