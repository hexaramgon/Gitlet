package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import static gitlet.Directory.CWD;

/** Class to start and display all logs.
 * @author Hector Ramos */

public class Logs implements Serializable {

    /** Placeholder for Equals String. */
    static final String EQUALS = "===";

    /** Starting at the current head commit, display information about each
     * commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents
     * found in merge commits. (In regular Git, this is what you get with git
     * log --first-parent). This set of commit nodes is called the commit's
     * history. For every node in this history, the information it should
     * display is the commit id, the time the commit was made, and the commit
     * message. Here is an example of the exact format it should follow:

     ===
     commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
     Date: Thu Nov 9 20:00:05 2017 -0800
     A commit message.

     ===
     commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     Date: Thu Nov 9 17:01:33 2017 -0800
     Another commit message.

     ===
     commit e881c9575d180a215d1a636545b8fd9abfb1d2bb
     Date: Wed Dec 31 16:00:00 1969 -0800
     initial commit. */
    public static void displayLog() {
        String displayed = "";
        String curr = Utils.readContentsAsString(Directory.HEADTXT);
        Hashtable<String, Commit> loaded = Directory.loadCommit();
        while (curr != null) {
            Commit shake = loaded.get(curr);
            if (shake.grabMerge()) {
                displayed += displaymerge(shake, curr);
                curr = shake.grabParent();
            } else {
                displayed += EQUALS + "\n";
                displayed += "commit " + curr + "\n";
                displayed += "Date: " + shake.grapTime() + "\n";
                displayed += shake.grabMessage();
                curr = shake.grabParent();
                displayed += "\n" + "\n";
            }
        }
        displayed += "\n";
        displayed = displayed.replaceAll("\n\n\n", "");
        System.out.println(displayed);
    }

    /**Displaying commit as Merge.
     * @param curr Current name of commit.
     * @param shake Commit we are displaying.
     * @return Returning commit in log form.*/
    public static String displaymerge(Commit shake, String curr)  {
        String displayed = "";
        displayed += EQUALS + "\n";
        displayed += "commit " + curr + "\n";
        displayed += "Merge: " + shake.grabMergents() + "\n";
        displayed += "Date: " + shake.grapTime() + "\n";
        displayed += shake.grabMessage();
        displayed += "\n" + "\n";
        return displayed;
    }

    /**Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.*/
    public static void displayglobalLog() {
        String displayed = "";
        Hashtable<String, Commit> loaded =  Directory.loadCommit();
        Set<String> setOfKeys = loaded.keySet();
        for (String key : setOfKeys) {
            Commit shake = loaded.get(key);
            if (shake.grabMerge()) {
                displayed += displaymerge(shake, key);
                continue;
            }
            displayed += EQUALS + "\n";
            displayed += "commit " + key + "\n";
            displayed += "Date: " + shake.grapTime() + "\n";
            displayed += shake.grabMessage() + "\n" + "\n";
        }
        displayed += "\n";
        displayed = displayed.replaceAll("\n\n\n", "");
        System.out.println(displayed);
    }

    /**LDisplays what branches currently exist, and marks the current branch
     * with a *. Also displays what files have been staged for addition or
     * removal. An example of the exact format it should follow is as follows.

     === Branches ===
     *master
     other-branch

     === Staged Files ===
     wug.txt
     wug2.txt

     === Removed Files ===
     goodbye.txt

     === Modifications Not Staged For Commit ===
     junk.txt (deleted)
     wug3.txt (modified)

     === Untracked Files ===
     random.stuff.*

    /** Displays status of commits.*/
    public static void displayStatus() {
        String print = "=== Branches ===" + "\n";
        File dir = new File(".gitlet/branches");
        print += dirtoString(dir);
        print += "\n";
        print += "=== Staged Files ===" + "\n";
        File add =  new File(".gitlet/stage/addition");
        File sub =  new File(".gitlet/stage/removal");
        print += dirtoString(add);
        print += "\n";
        print += "=== Removed Files ===" + "\n";
        print += dirtoString(sub);
        print += "\n";
        print += "=== Modifications Not Staged For Commit ===" + "\n";
        Hashtable<String, Commit> loaded =  Directory.loadCommit();
        String head = Utils.readContentsAsString(Directory.HEADTXT);
        HashMap<String, String>  blobas = loaded.get(head).grabBlobs();
        print  += unlogHelper(blobas);
        print += "\n";
        print += "=== Untracked Files ===";
        print += untrackedFinder(blobas);
        print += "\n";
        print = print.replaceAll("\n\n\n", "");
        System.out.println(print);
    }

    /** Helper function to find untracked files.
     * @param checked blobs to check if it's untracked or not.
     * @return Untracked files.*/
    public static String untrackedFinder(HashMap<String, String> checked) {
        String returner = "";
        String[] strList = CWD.list();
        for (String i : strList) {
            char[] test = i.toCharArray();
            if (test[0] == '.') {
                continue;
            }
            File f = new File(Directory.ADDITION + "/"  + i);
            if (!checked.containsKey(i)) {
                if (!f.exists()) {
                    returner += "\n" + i;
                }
            }
        }
        return returner;
    }

    /**Helper function for log.
     * @param checked blobs of current commits.
     * @return String to add to commit*/
    public static String unlogHelper(HashMap<String, String> checked) {
        String stringer = "";
        String[] bat = checked.keySet().toArray(String[]::new);
        for (String i : bat) {
            File f = new File(CWD + "/" + i);
            if (f.exists()) {
                Blob checker = new Blob(f);
                String x = checker.toString();
                File orginal = new File(".gitlet/objects/" + x);
                if (!orginal.exists()) {
                    stringer += i + " (modified)" + "\n";
                }
            } else {
                File fil = new File(Directory.REMOVAL + "/" + i);
                if (!fil.exists()) {
                    stringer += i + " (removed)" + "\n";
                }
            }
        }
        return stringer;
    }

    /**Helper function to put list of files in string format.
     * @param file Directory that you want to iterate through.
     * @return String format of files.*/
    public static String dirtoString(File file) {
        String print = "";
        String[] directoryListing = file.list();
        if (directoryListing == null) {
            return "\n";
        }
        String[] test = new String[directoryListing.length];
        int index  = 0;
        for (String x : directoryListing) {
            test[index] = x;
            index++;
        }
        Arrays.sort(test);
        for (String x : test) {
            print += x + "\n";
        }
        return print;
    }
}
