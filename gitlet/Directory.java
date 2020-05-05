package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import static gitlet.Main.exitWithError;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/** This is where the main gitlet Directory takes place.
 * This is where you load most of your files in order to maintain persitence.
 * @author Hector Ramos*/

public class Directory implements Serializable {
    /** Current Working Directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));;

    /** Main metadata folder. */
    private static final File GITLET = new File(".gitlet");

    /** Current HEAD Commit. */
    private static final File HEAD = new File(".gitlet/HEAD");

    /** Current HEAD Commit as Text file. */
    public static final File HEADTXT = new File(".gitlet/HEAD/head.txt");

    /** Current HEAD Branch as Text file. */
    public static final File BRANCHTXT = new File(".gitlet/HEAD/branch.txt");

    /** Directory for Branches. */
    private static final File BRANCHES = new File(".gitlet/branches");

    /** Just a place to store all the Blobs.*/
    private static final File OBJECTS = new File(".gitlet/objects");

    /** Just a place to store all the Blobs.*/
    private static final File LOGS = new File(".gitlet/logs");

    /** This is where we do our staging..*/
    private static final File STAGE = new File(".gitlet/stage");

    /** This is where we add files within our stage..*/
    public static final File ADDITION = new File(".gitlet/stage/addition");

    /** This is where we remove files within our stage..*/
    public static final File REMOVAL = new File(".gitlet/stage/removal");

    /** This is where all of our commits are located.*/
    public static final File COMMITS = new File(".gitlet/commits");

    /** Gitlet Error b/c wouldn't pass style check.*/
    public static final String GITERROR = "A Gitlet version-control system "
            + "already exists in the current directory.";

    /** The initial setup of the Gitlet Directory..*/
    public static void setupGitlet() {
        if (GITLET.exists()) {
            exitWithError(GITERROR);
        }
        CWD.mkdir();
        GITLET.mkdir();
        HEAD.mkdir();
        BRANCHES.mkdir();
        OBJECTS.mkdir();
        LOGS.mkdir();
        STAGE.mkdir();
        ADDITION.mkdir();
        REMOVAL.mkdir();
        createFile(HEADTXT);
        createFile(BRANCHTXT);
        Hashtable commitHashie = new Hashtable<String, Commit>();
        Commit initial = new Commit("initial commit");
        commitHashie.put(initial.toString(), initial);
        newBranch("master", initial, true);
        Utils.writeObject(COMMITS, commitHashie);
    }

    /** Adding to the gitLet Directory.
     * @param x name of file you add.*/
    public static void add(String x) {
        File addedfile = new File(x);
        if (!addedfile.exists()) {
            exitWithError("File does not exist.");
        }
        Blob newBlob = new Blob(addedfile);
        String checker = newBlob.toString();
        File checkedfile = new File(".gitlet/objects/" + checker);
        Blob b = null;
        if (checkedfile.exists()) {
            b =  Utils.readObject(checkedfile, Blob.class);
        }
        String a = b.grabBranch();
        String c = newBlob.grabBranch();
        if (!checkedfile.exists() || !b.grabBranch().equals(newBlob.grabBranch())) {
            newBlob.saveBlob();
            File staging = new File(".gitlet/stage/addition/" + x);
            Utils.writeContents(staging, newBlob.contentReturner());
        } else {
            clearstage(x);
        }
    }

    /** Commiting to the gitLet Directory.
     * @param x Message of the commit.*/
    public static void commit(String x) {
        File adddir = new File(".gitlet/stage/addition");
        File[] directoryListing = adddir.listFiles();
        File subdir = new File(".gitlet/stage/removal");
        File[] removalListing = subdir.listFiles();
        if (directoryListing.length == 0 && removalListing.length == 0) {
            exitWithError(" No changes added to the commit.");
        }
        String head = Utils.readContentsAsString(HEADTXT);
        Hashtable<String, Commit> loaded =  loadCommit();
        Commit y = loaded.get(head);
        Commit newCommit = y.duplicate(y, x);
        for (File i : directoryListing) {
            Blob newBlob = new Blob(i);
            newCommit.addBlob(newBlob.grabName(), newBlob.toString());
            i.delete();
        }
        for (File j : removalListing) {
            newCommit.removeBlob(j.getName());
            j.delete();
        }
        newCommit.changeParent(y.toString());
        loaded.put(newCommit.toString(), newCommit);
        updateHead(newCommit);
        Utils.writeObject(COMMITS, loaded);
    }

    /** Method to clear stage.
     * @param x if you want to clear stage individual file.*/
    public static void clearstage(String x) {
        if (x == null) {
            File adddir = new File(".gitlet/stage/addition");
            File subdir = new File(".gitlet/stage/removal");
            File[] directoryListing = adddir.listFiles();
            File[] removalListing = subdir.listFiles();
            for (File i : directoryListing) {
                i.delete();
            }
            for (File j : removalListing) {
                j.delete();
            }
        } else {
            File adddirdie = new File(".gitlet/stage/addition/" + x);
            File subdirdie = new File(".gitlet/stage/removal/" + x);
            if (adddirdie.exists()) {
                adddirdie.delete();
            }
            if (subdirdie.exists()) {
                subdirdie.delete();
            }
        }
    }

    /** Method to update head to new commit.
     * @param comm Commit you are updating head to.*/
    public static void updateHead(Commit comm) {
        Utils.writeContents(HEADTXT, comm.toString());
        String txt = Utils.readContentsAsString(BRANCHTXT);
        File x = new File(".gitlet/branches/" + txt + "/branchhead.txt");
        Utils.writeContents(x, comm.toString());
    }


    /**Unstage the file if it is currently staged for addition. If the file
     * is tracked in the current commit,stage it for removal and remove the
     * file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * @param x File you are removing.*/
    public static void remove(String x) {
        Boolean existed = false;
        String head = Utils.readContentsAsString(HEADTXT);
        Hashtable<String, Commit> loaded = loadCommit();
        Commit y = loaded.get(head.toString());
        File checker = new File(".gitlet/stage/addition/" + x);
        if (checker.exists()) {
            checker.delete();
            existed = true;
        }
        HashMap<String, String> checked = y.grabBlobs();
        if (checked.containsKey(x)) {
            File removedfile = new File(x);
            File staging = new File(".gitlet/stage/removal/" + x);
            Utils.writeContents(staging, x);
            existed = true;
            if (removedfile.exists()) {
                removedfile.delete();
            }
        }
        if (!existed) {
            exitWithError("No reason to remove the file.");
        }
    }

    /** Method to find commits..
     * @param x name of Commit you are trying to find*/
    public static void find(String x) {
        Hashtable<String, Commit> loaded = loadCommit();
        Set<String> setter = loaded.keySet();
        String found = "";
        String nofound = "Found no commit with that message.";
        String[] sets = setter.toArray(String[]::new);
        for (String i : sets) {
            Commit y = loaded.get(i);
            if (y.grabMessage().equals(x)) {
                found += y.grabName() + "\n";
            }
        }
        if (found.equals("")) {
            exitWithError(nofound);
        } else {
            found += "\n";
            found = found.replaceAll("\n\n", "");
            System.out.println(found);
        }
    }

    /** Creating a new branch inside Gitlet.
     * @param x name of file you add.
     * @param bool Boolean to check if making inital branch
     * @param comm Commit you are making new branch*/
    public static void newBranch(String x, Commit comm, Boolean bool) {
        if (bool) {
            x = "*" + x;
            Utils.writeContents(BRANCHTXT, x);
        }
        String head = Utils.readContentsAsString(HEADTXT);
        File branch = new File(".gitlet/branches/" + x);
        if (!branch.exists()) {
            branch.mkdir();
            File branchhead = new File(".gitlet/branches/"
                    + x + "/branchhead.txt");
            createFile(branchhead);
            if (comm != null && bool) {
                Utils.writeContents(HEADTXT, comm.toString());
                Utils.writeContents(branchhead, comm.toString());
            } else {
                Utils.writeContents(branchhead, head);
            }
            return;
        }
        exitWithError("A branch with that name already exists.");
    }

    /** Removing branch within Gitlet Directory..
     * @param x name of branch you are removing.*/
    public static void removeBranch(String x) {
        File removed = new File(".gitlet/branches/" + x);
        String head = Utils.readContentsAsString(BRANCHTXT);
        head = head.replace("*", "");
        if (head.equals(x)) {
            exitWithError("Cannot remove the current branch.");
        }
        if (!removed.exists()) {
            exitWithError("A branch with that name does not exist.");
        }
        File removedextra = new File(".gitlet/branches/"
                + x + "/branchhead.txt");
        removedextra.delete();
        removed.delete();
    }

    /** Reseting gitlet directory to Commit.
     * @param x Name of commit you are resetting to.*/
    public static void reset(String x) {
        Hashtable<String, Commit> loaded = loadCommit();
        if (loaded.containsKey(x)) {
            String head = Utils.readContentsAsString(HEADTXT);
            Commit header = loaded.get(head);
            Commit curr = loaded.get(x);
            HashMap<String, String> z = header.grabBlobs();
            HashMap<String, String> y = curr.grabBlobs();
            untrackedFinder(z, y);
            String[] test = curr.grabBlobs().keySet().toArray(String[]::new);
            for (String j : test) {
                checkout(x, j, 2);
            }
            File[] lister = CWD.listFiles();
            for (File i : lister) {
                if (!y.containsKey(i.getName())) {
                    if (header.grabBlobs().containsKey(i.getName())) {
                        i.delete();
                    }
                }
            }
            updateHead(curr);
            clearstage(null);
            return;
        }
        exitWithError("No commit with that id exists.");
    }


    /** Merging method.
     * @param x Branch you are merging.*/
    public static void merge(String x) {
        String ancestorstr = findancestor(x);
        Hashtable<String, Commit> comms = loadCommit();
        Commit ancestor = comms.get(ancestorstr);
        Commit comm1 = comms.get(Utils.readContentsAsString(HEADTXT));
        String headbrch = Utils.readContentsAsString(BRANCHTXT);
        File nf = new File(BRANCHES + "/" + x + "/branchhead.txt");
        String brchcoom = Utils.readContentsAsString(nf);
        Commit comm2 = comms.get(brchcoom);
        HashMap<String, String> current = comm1.grabBlobs();
        HashMap<String, String> given = comm2.grabBlobs();
        HashMap<String, String> ancestorr = ancestor.grabBlobs();
        untrackedFinder(current, given);
        String[] result = mergeArrays(given);
        Boolean conflict = false;
        for (String i : result) {
            String anceblo = ancestorr.get(i);
            String currblo = current.get(i);
            String givenblo = given.get(i);
            if (i.charAt(0) == '.') {
                continue;
            }
            if (current.containsKey(i) && given.containsKey(i)
                    && ancestorr.containsKey(i)) {
                if (!currblo.equals(givenblo)) {
                    if (currblo.equals(anceblo)) {
                        checkout(brchcoom, i, 2); add(i);
                    } else {
                        mergeconflict(currblo, givenblo, new File(i));
                        conflict = true; add(i);
                    }
                }
            } else if (!current.containsKey(i) && given.containsKey(i)
                    && !ancestorr.containsKey(i)) {
                checkout(brchcoom, i, 2); add(i);
            } else if (current.containsKey(i) && !given.containsKey(i)
                    && ancestorr.containsKey(i)) {
                if (anceblo.equals(currblo)) {
                    remove(i);
                } else {
                    mergeconflict(currblo, null, new File(i));
                    conflict = true; add(i);
                }
            } else if (!current.containsKey(i) && given.containsKey(i)
                    && ancestorr.containsKey(i)) {
                if (!anceblo.equals(givenblo)) {
                    mergeconflict(null, givenblo, new File(i));
                    conflict = true; add(i);
                }
            } else if (current.containsKey(i) && given.containsKey(i)
                    && !ancestorr.containsKey(i)) {
                if (!currblo.equals(givenblo)) {
                    mergeconflict(currblo, givenblo, new File(i));
                    conflict = true; add(i);
                }
            }
        }
        mergecommit(conflict, x, headbrch, brchcoom);
    }

    /** Creating Commit of new merge.
     * @param conflict Boolean of there is a conflict or not.
     * @param branch1 Original branch that we are merging into.
     * @param branch2 Branch that is being merged.
     * @param brchcoom Other branch as file.*/
    public static void mergecommit(Boolean conflict, String branch1,
                                   String branch2, String brchcoom) {
        File adddir = new File(".gitlet/stage/addition");
        File[] directoryListing = adddir.listFiles();
        File subdir = new File(".gitlet/stage/removal");
        File[] removalListing = subdir.listFiles();
        String head = Utils.readContentsAsString(HEADTXT);
        Hashtable<String, Commit> loaded =  loadCommit();
        Commit y = loaded.get(head);
        String id1 = Utils.readContentsAsString(HEADTXT);
        Commit newCommit = y.mergecommit(branch1, branch2, id1, brchcoom);
        for (File i : directoryListing) {
            Blob newBlob = new Blob(i);
            newCommit.addBlob(newBlob.grabName(), newBlob.toString());
            i.delete();
        }
        for (File j : removalListing) {
            newCommit.removeBlob(j.getName());
            j.delete();
        }
        loaded.put(newCommit.toString(), newCommit);
        updateHead(newCommit);
        Utils.writeObject(COMMITS, loaded);
        if (conflict) {
            exitWithError("Encountered a merge conflict.");
        }
    }

    /** Handling of file during a merge conflict.
     * @param cublo Head commit from original branch.
     * @param givblo Head commit from merging branch.
     * @param repl File you are replacing*/
    public static void mergeconflict(String cublo, String givblo, File repl) {
        String cuR = "";
        String giV = "";
        if (cublo != null) {
            File cur = new File(OBJECTS + "/" + cublo);
            Blob curr =  Utils.readObject(cur, Blob.class);
            cuR = new String(curr.contentReturner(), StandardCharsets.UTF_8);
        }
        if (givblo != null) {
            File giv = new File(OBJECTS + "/" + givblo);
            Blob given =  Utils.readObject(giv, Blob.class);
            giV = new String(given.contentReturner(), StandardCharsets.UTF_8);
        }
        String replace = "<<<<<<< HEAD\n" + cuR + "=======\n"
                + giV + ">>>>>>>\n";
        Utils.writeContents(repl, replace);
    }

    /** Finding ancestor of commit branches.
     * @param x branch we are merging into.
     * @return commit Id of ancestor.*/
    public static String findancestor(String x) {
        if (ADDITION.list().length > 0 || REMOVAL.list().length > 0) {
            exitWithError("You have uncommitted changes");
        }
        Hashtable<String, Commit> comms = loadCommit();
        Commit comm1 = comms.get(Utils.readContentsAsString(HEADTXT));
        String headbrch = Utils.readContentsAsString(BRANCHTXT);
        headbrch = headbrch.replace("*", "");
        if (x.equals(headbrch)) {
            exitWithError("Cannot merge a branch with itself.");
        }
        File f = new File(BRANCHES + "/" + x);
        if (!f.exists()) {
            exitWithError("A branch with that name does not exist.");
        }
        File nf = new File(BRANCHES + "/" + x + "/branchhead.txt");
        String brchcoom = Utils.readContentsAsString(nf);
        Commit comm2 = comms.get(brchcoom);
        HashSet stringer = new HashSet<String>();
        Commit pointer = comm1;
        while (pointer.grabParent() != null) {
            if (pointer.grabName().equals(comm2.grabName())) {
                exitWithError("Given branch is an ancestor "
                        + "of the current branch.");
            }
            pointer = comms.get(pointer.grabParent());
        }
        while (comm2.grabParent() != null) {
            if (comm2.grabMerge()) {
                comm2 = comms.get(comm2.grabParent2());
            } else {
                comm2 = comms.get(comm2.grabParent());
            }
            if (comm2.equals(comm1)) {
                checkout(null, x, 3);
                exitWithError("Current branch fast-forwarded.");
            }
            stringer.add(comm2);
        }
        String ancestor = null;
        while (comm1.grabParent() != null) {
            if (comm1.grabMerge()) {
                comm1 = comms.get(comm1.grabParent2());
            } else  {
                comm1 = comms.get(comm1.grabParent());
            }
            if (stringer.contains(comm1)) {
                ancestor = comm1.grabName();
                break;
            }
        }
        return ancestor;
    }

    /** Checking out file from gitlet Directory.
     * @param commitId name of commit you want to checkout to.
     * @param file file you are checking out.
     * @param route how you want to check out.*/
    public static void checkout(String commitId, String file, int route) {
        if (route == 1) {
            File replace = new File(file);
            String head = Utils.readContentsAsString(HEADTXT);
            Hashtable<String, Commit> loaded = loadCommit();
            Commit y = loaded.get(head);
            HashMap<String, String> checked = y.grabBlobs();
            checkouthelper(checked, file, replace);
            return;
        } else if (route == 2) {
            Hashtable<String, Commit> loaded = loadCommit();
            String id = finderhelp(loaded, commitId);
            if (id != null) {
                File replace = new File(file);
                Commit getted = loaded.get(id);
                HashMap<String, String> checked = getted.grabBlobs();
                checkouthelper(checked, file, replace);
                return;
            }
        } else if (route == 3) {
            String head = Utils.readContentsAsString(BRANCHTXT);
            head = head.replace("*", "");
            if (head.equals(file)) {
                exitWithError("No need to checkout the current branch.");
            }
            String curr = Utils.readContentsAsString(HEADTXT);
            File branch = new File(".gitlet/branches/" + file);
            if (!branch.exists()) {
                exitWithError("No such branch exists.");
            }
            File otherbranch = new File(".gitlet/branches/"
                    + file + "/branchhead.txt");
            String branchhead = Utils.readContentsAsString(otherbranch);
            Hashtable<String, Commit> loaded = loadCommit();
            Commit curry = loaded.get(curr);
            Commit newcom = loaded.get(branchhead);
            deletionhelper(curry, newcom);
            clearstage(null);
            changehead(file, branchhead);
        }
    }

    /** helper function for short uids.
     * @param lo Loaded Commits.
     * @param x Commit id you are trying to find.
     * @return returns id's if found.**/
    public static String finderhelp(Hashtable<String, Commit> lo, String x) {
        if (lo.containsKey(x)) {
            return x;
        }
        String[] checked = lo.keySet().toArray(String[]::new);
        for (String i : checked) {
            if (i.contains(x)) {
                return i;
            }
        }
        exitWithError("No commit with that id exists.");
        return null;
    }
    /** Helper method to delete when checking out.
     * @param curry Current commit.
     * @param newcom Commit we are checking out.*/
    public static void deletionhelper(Commit curry, Commit newcom) {
        HashMap<String, String> orig = curry.grabBlobs();
        HashMap<String, String> change = newcom.grabBlobs();
        untrackedFinder(orig, change);
        String[] lister = change.keySet().toArray(String[]::new);
        File[] strList = CWD.listFiles();
        for (String i : lister) {
            checkout(newcom.grabName(), i, 2);
        }
        for (File j : strList) {
            if (orig.containsKey(j.getName())) {
                if (!change.containsKey(j.getName())) {
                    j.delete();
                }
            }
        }
    }

    /** Changing main branch head.
     * @param x Branch name you are making new head.
     * @param oldcomm Old head branch name.*/
    public static void changehead(String x, String oldcomm) {
        Utils.writeContents(HEADTXT, oldcomm);
        File[] listy = BRANCHES.listFiles();
        for (File j : listy) {
            if (j.getName().contains("*")) {
                String newie = j.getName().replace("*", "");
                File tmp = new File(BRANCHES.toString() + "/" + newie);
                j.renameTo(tmp);
            }
            if (x.equals(j.getName())) {
                File f = new File(BRANCHES + "/*" + x);
                j.renameTo(f);
                Utils.writeContents(BRANCHTXT, "*" + x);
            }
        }
    }

    /** Helper method for checking out.
     * @param file name of file you are checking out.
     * @param checked loaded commit blobs.
     * @param replace file you are checking out*/
    public static void checkouthelper(HashMap<String, String> checked,
                                      String file, File replace) {
        if (checked.containsKey(file)) {
            String x = checked.get(file);
            File replacer = new File(".gitlet/objects/" + x);
            Blob z =  Utils.readObject(replacer, Blob.class);
            String s = new String(z.contentReturner(), StandardCharsets.UTF_8);
            Utils.writeContents(replace, s);
            return;
        }
        exitWithError("File does not exist in that commit.");
    }

    /** Finding files that are untracked.
     * @param checked loaded current blobs.
     * @param other loads other commits blobs.*/
    public static void untrackedFinder(HashMap<String, String> checked,
                                       HashMap<String, String> other) {
        String[] strList = CWD.list();
        for (String i : strList) {
            char[] test = i.toCharArray();
            if (test[0] == '.') {
                continue;
            }
            File f = new File(ADDITION + "/"  + i);
            if (!checked.containsKey(i)) {
                if (!f.exists() && other.containsKey(i)) {
                    exitWithError("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                }
            }
        }
    }

    /** Creating new file.
     * @param file name of file you add.*/
    public static void createFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** Loading hashmap of Commits.
     * @return loaded commits*/
    public static Hashtable loadCommit() {
        return Utils.readObject(COMMITS, Hashtable.class);
    }

    /** merges arrays.
     * @param given hashset of blobs.
     * @return merged array*/
    public static String[] mergeArrays(HashMap<String, String> given) {
        String[] v1 = CWD.list();
        String[] v2 = given.keySet().toArray(String[]::new);
        int leg1 = v1.length;
        int leg2 = v2.length;
        String[] result = new String[leg1 + leg2];
        System.arraycopy(v1, 0, result, 0, leg1);
        System.arraycopy(v2, 0, result, leg1, leg2);
        return result;
    }

}
