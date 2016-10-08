import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//@author Michael Haertling
public class DecisionTreeNode {

    private final ArrayList<DecisionTreeNode> children;
    private final Dataset data;
    private final String prodominantClass;
    private final double certainty;
    private final int splitAttribute;

    public DecisionTreeNode(Dataset data, int splitAttribute) {
        this.splitAttribute = splitAttribute;
        children = new ArrayList<>();
        this.data = data;
        List<Integer> props = data.getAttributeCounts(data.numAttributes());
        int bestIndex = -1;
        int best = -1;
        int numEqual = 0;
        for (int i = 0; i < props.size(); i++) {
            if (best == props.get(i)) {
                numEqual++;
            }else if (best < props.get(i)) {
                bestIndex = i;
                best = props.get(i);
            }
        }
        if (numEqual + 1 < props.size() || props.size()==1) {
            prodominantClass = data.getAttributeValue(data.numAttributes(), bestIndex);
        } else {
            prodominantClass = data.getClassBias();
        }
        certainty = best;
    }

    public void addChild(DecisionTreeNode node) {
        children.add(node);
    }

    public List<DecisionTreeNode> getChildren() {
        return children;
    }

    public Dataset getData() {
        return data;
    }

    public String getProdominantClass() {
        return prodominantClass;
    }

    public double getCertainty() {
        return certainty;
    }
    
    public int getSplitAttributeIndex(){
        return splitAttribute;
    }

    public String getSplitAttribute(){
        return data.getAttributeName(splitAttribute);
    }
    
    @Override
    public String toString(){
        return toString("");
    }
    
    private String toString(String prefix){
        String out = "";
        for(int i=0; i<children.size(); i++){
            DecisionTreeNode node = children.get(i);
            out += prefix+this.getSplitAttribute()+" = "+data.getAttributeValue(splitAttribute, i)+" :";
            if(node.isLeaf()){
                out += "  "+node.getProdominantClass()+"\n";
            }else{
                out+="\n";
                out += node.toString(prefix+"| ");
            }
        }
        
        return out;
    }
    
    public boolean isLeaf(){
        return children.isEmpty();
    }
    
}
