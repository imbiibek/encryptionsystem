package bibekashyap.EncryptionSystem;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class AllFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.isFile();
    }

    @Override
    public String getDescription() {
        return "All Files";
    }
}
