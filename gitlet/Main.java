package gitlet;


import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Hector Ramos
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....*/
    public static void main(String... args) {
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        } else if (args[0].equals("init")) {
            validateNumArgs(args, 1);
            Directory.setupGitlet();
        } else if (args[0].equals("add")) {
            validateNumArgs(args, 2);
            Directory.add(args[1]);
        } else if (args[0].equals("commit")) {
            if (args.length == 1 || args[1].isEmpty()) {
                exitWithError("Please enter a commit message.");
            }
            validateNumArgs(args, 2);
            Directory.commit(args[1]);
        } else if (args[0].equals("rm")) {
            validateNumArgs(args, 2);
            Directory.remove(args[1]);
        } else if (args[0].equals("log")) {
            validateNumArgs(args, 1);
            Logs.displayLog();
        } else if (args[0].equals("global-log")) {
            validateNumArgs(args, 1);
            Logs.displayglobalLog();
        } else if (args[0].equals("find")) {
            validateNumArgs(args, 2);
            Directory.find(args[1]);
        } else if (args[0].equals("status")) {
            validateNumArgs(args, 1);
            Logs.displayStatus();
        } else if (args[0].equals("checkout")) {
            checkout(args);
        } else if (args[0].equals("branch")) {
            validateNumArgs(args, 2);
            Directory.newBranch(args[1], null, false);
        } else if (args[0].equals("rm-branch")) {
            validateNumArgs(args, 2);
            Directory.removeBranch(args[1]);
        } else if (args[0].equals("reset")) {
            validateNumArgs(args, 2);
            Directory.reset(args[1]);
        } else if (args[0].equals("merge")) {
            validateNumArgs(args, 2);
            Directory.merge(args[1]);
        } else {
            exitWithError("No command with that name exists.");
        }
    }

    /** Exiting with error message.
     * @param message message to be exited with.*/
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(0);
    }

    /** Method to ensure right number of arguments and gitlet exists.
     * @param args args of main method.
     * @param n Number of arguements.*/
    public static void validateNumArgs(String[] args, int n) {
        File gitlet = new File(".gitlet");
        if (args.length != n)  {
            exitWithError("Incorrect operands.");
        }
        if (!(args[0].equals("init")) && (!gitlet.exists())) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
    }

    /** Extended method to account for checkout variants.
     * @param args args of main method. */
    public static void checkout(String... args) {
        if (args.length == 3) {
            Directory.checkout(null, args[2], 1);
        } else if (args.length == 4) {
            if (args[2].equals("--")) {
                Directory.checkout(args[1], args[3], 2);
            } else  {
                exitWithError("Incorrect operands.");
            }
        } else if (args.length == 2) {
            Directory.checkout(null, args[1], 3);
        } else {
            exitWithError("Incorrect operands.");
        }
    }
}
