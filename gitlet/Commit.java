package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/** The Commit Object, used in the Gitlet Directory.
 * @author Hector Ramos */

public class Commit implements Serializable {

    /** Merge Parents of merge commits. */
    private String mergents;

    /** Is Commit merge or not. */
    private Boolean merge = false;

    /** Message Associated with Commit. */
    private String message;

    /** Timestamp of Commit. */
    private String timestamp;

    /** Commit parent of instance Commit represented as String. */
    private String parent1;

    /** Commit parent of instance Commit represented as String. */
    private String parent2;

    /** Blobs of Commit represented as Strings. */
    private HashMap<String, String> blobs = new HashMap<>();

    /** Blobs of Commit represented as Strings. */
    private String name;

    /** Commit parent of instance Commit represented as String.
     * @param mess The message you want for the commit.
     * @param par The parent of the commit.
     * @param blobos blobs of old commit*/
    public Commit(String mess, Commit par, HashMap<String, String> blobos) {
        message = mess;
        parent1 = par.toString();
        timestamp = normaltimeStamp();
        blobs = (HashMap<String, String>) blobos.clone();
    }
    /** Initial Commit Creation.
     * @param messager The inital message.*/
    public Commit(String messager) {
        message = messager;
        timestamp = initialtimeStamp();
    }
    /** Merge Commit creation.
     * @param mess The message you want for the commit.
     * @param par1 The original branch of commit merge.
     * @param par2 The merged branch of the commit merge.
     * @param blobos blobs of old commit*/
    public Commit(String mess, String par1, String par2,
                  HashMap<String, String> blobos) {
        merge = true;
        message = mess;
        parent1 = par1;
        parent2 = par2;
        par1 = par1.substring(0, 7);
        par2 = par2.substring(0, 7);
        mergents = par1 + " " + par2;
        timestamp = normaltimeStamp();
        blobs = (HashMap<String, String>) blobos.clone();
    }

    /**Sha1 representation with the file (blob) references of its files
     *  log Message, and commit time.
     *  @return SHA1 of commit.*/
    public String toString() {
        String x;
        if (blobs == null || parent1 == null) {
            x = Utils.sha1(timestamp, message);
        } else {
            x = Utils.sha1(timestamp, message, parent1);
        }
        name = x;
        return x;
    }

    /** How we get the Inital TimeStamp.
     * @return Inital Timestamp.*/
    private String initialtimeStamp() {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("\"EEE MMM d HH:mm:ss yyyy Z\"\t");
        Date x = new Date(0);
        return simpleDateFormat.format(x);
    }

    /** How we get the normal TimeStamp.
     * @return Normal Timestamp. */
    private String normaltimeStamp() {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("\"EEE MMM d HH:mm:ss yyyy Z\"\t");
        Date x = new Date();
        return simpleDateFormat.format(x);
    }
    /** Return Message of commit.
     * @param comm Commit we are duplicating
     * @param messanger Message of the new commit*/
    public Commit duplicate(Commit comm, String messanger) {
        Commit exCommit = new Commit(messanger, comm, this.grabBlobs());
        return exCommit;
    }

    /** Creating new commit that is a merge.
     * @param br1 1st branch.
     * @param br2 2nd branch.
     * @param id id of first string.
     * @param id2 id of second string.
     * @return new Commit.**/
    public Commit mergecommit(String br1, String br2, String id, String id2) {
        br1 = br1.replace("*", "");
        br2 = br2.replace("*", "");
        String newmassage = "Merged " + br1 + " into " + br2 + ".";
        Commit exCommit = new Commit(newmassage, id, id2, this.grabBlobs());
        return exCommit;
    };
    /** Add blob to commit.
     * @param file Name of file ex. Wug.txt.
     * @param namer SHA1 of file, should match up object in OBJECTS.*/
    public void addBlob(String file, String namer) {
        blobs.put(file, namer);
    }

    /** Remove blob from commit. DANGEROUS. Use with caution.
     * @param B  */
    public void removeBlob(String B) {
        blobs.remove(B);
    }
    /** Return Timestamp of commit. */
    public String grapTime() {
        return timestamp.replaceAll("\"", "");
    }

    /** Change parent of commit. Used when creating new commit.
     * @param x Name of parent that is being changed to. */
    public void changeParent(String x) {
        this.parent1 = x;
    }

    /** Return parent of commit. */
    public String grabParent() {
        return this.parent1;
    }

    /** Return globs of commit. */
    public HashMap<String, String> grabBlobs() {
        return this.blobs;
    }

    /** Return Message of commit. */
    public String grabMessage() {
        return message;
    }

    /** Return name of commit. */
    public String grabName() {
        return name;
    }

    /** Return commit status of merge. */
    public Boolean grabMerge() {
        return merge;
    }

    /** Return commit status of merge. */
    public String grabMergents() {
        return mergents;
    }

    /** Return second parent for merges. */
    public String grabParent2() {
        return parent2;
    }
}
