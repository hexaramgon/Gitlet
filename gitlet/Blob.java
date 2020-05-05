package gitlet;

import java.io.File;
import java.io.Serializable;

/** The Blob Object, used to represent contents of files.
 * @author Hector Ramos */

public class Blob implements Serializable {

    /** SHA1 Name Associated with Blob. */
    private String id;

    /** Contents of Commit. */
    private byte[] contents;

    /** Contents of Name. */
    private String name;

    /** Branch of blob. */
    private String branch;

    /** The Blob Object, used to represent fies in the Gitlet Directory.
     * @param file File you are converting to a blob. */
    public Blob(File file) {
        contents = Utils.readContents(file);
        name = file.getName();
        String trans = Utils.readContentsAsString(Directory.BRANCHTXT);
        branch = trans;
        id = Utils.sha1(contents, name);
    }

    /**SHA1 of the Blob.
     * @return SHA1 of Blob.*/
    public String toString() {
        return id;
    }

    /**Return name of file that Blob corrosponds to.*/
    public String grabName() {
        return name;
    }

    /**Return branch of file that Blob corrosponds to.*/
    public String grabBranch() {
        return branch;
    }

    /**Return Contents of the Blob.*/
    public byte[] contentReturner() {
        return contents;
    }

    /**Return Contents of the Blob represented in byte array.*/
    public void saveBlob() {
        File blob = new File(".gitlet/objects/" + id);
        Blob loaded = this;
        Utils.writeObject(blob, loaded);
    }
}
