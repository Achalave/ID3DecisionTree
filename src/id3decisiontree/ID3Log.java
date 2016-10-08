import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//@author Michael Haertling
public class ID3Log {

    String path;

    public ID3Log(String path) {
        this.path = path;
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }
    }

    public void log(String line) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(path, true));
            bw.write(line);
            bw.newLine();
            bw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ID3Log.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ID3Log.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void newLine(){
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(path, true));
            bw.newLine();
            bw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ID3Log.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ID3Log.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
