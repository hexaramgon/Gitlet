# Gitlet Design Document

**Name**: Hector Ramos

## Classes and Data Structures

### Directory
Meat of the program, most important functions are located within this class.
####init
Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit (just like that, with no punctuation). It will have a single branch: master, which initially points to this initial commit, and master will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates (this is called "The (Unix) Epoch", represented internally by the time 0.) Since the initial commit in all repositories created by Gitlet will have exactly the same content, it follows that all repositories will automatically share this commit (they will all have the same UID) and all commits in all repositories will trace back to it.

####add 
Adds a copy of the file as it currently exists to the staging area (see the description of the commit command). For this reason, adding a file is also called staging the file for addition. Staging an already-staged file overwrites the previous entry in the staging area with the new contents. The staging area should be somewhere in .gitlet. If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back). The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command


####commit 
Saves a snapshot of certain files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be tracking the saved files. By default, each commit's snapshot of files will be exactly the same as its parent commit's snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged for addition but weren't tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the rm command (below).

####rm
Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit).

####find
Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the commit command below.


This is the class responsible for the branches and sequences of commits.

Use an Edge List. So put the array an array 
[commit, parent, children]


### Index 
This class is used during the add portion of git.
It checks what part of the files you need to change and what else you don't.
To track a new file, you have to stage it to index.

###Tree

This is a class structure representing commits and blobs inside them

####Fields:
```` 
<Commit> parent = <Commit> //can only have 1 parent
<Commit> child = <Commit>. // can have many childs
<T> blob = <T> // this is the file itself (Maybe make this arrays)
String Hashcode
Boolean head = false
`````
Have to make sure it is immutable. 

###Blob

This is a class structure representing blobs inside them

Fields:
    //Possilbly an array of different files

Within the commit message 
1. cryptographic hash of commit's contents
1. Information about the author and the commiter
3. A refernce to the paren'ts commit hash
4. A commit message



## Algorithms
####Commands to be Implemented

```
java gitlet.Main init
```
This command will create a new working tree in the same way you create a new linked list. The tree class will have a constructor.

````
java gitlet.Main add [file name]
````
This command will work by creating a temporary tree structure, but not connected to anything.
You add all the files, and save them in their designated spot.
````
java gitlet.Main commit [message]
````
This is where you actually connect the temporary file into  hte main tree, system. 3 Main tasks should be done here.
1. Connect the child and parent to their respective figures.
2. Add the commit to the log.
3. Reassign the head.
````
java gitlet.Main log
````
Go through all the parents itertativly and then add it to linked list to be later printed.
````
java gitlet.Main global-log
````
This gives access to a linked list of all the logs that is tied to the main tree. When running this method, should have 
a set of strings to print.
````
java gitlet.Main find [commit message]
````
Just do an iterative search (or a data structure that makes this fast) and fine one to matches. (Possibly MSD search)
````
java gitlet.Main status
````
Possibly just have a .tostring method that makes displaying all this information super easy.
````
java gitlet.Main checkout -- [file name]
java gitlet.Main checkout [commit id] -- [file name]
java gitlet.Main checkout [branch name]
```` 
This should be very simiplt to do, just deleting a node like on a linked list, but you have to BE VERY CAREFUL.





## Persistence

But essentially how I plan to make sure that no files, get lost or reassigned, is that every time you go through the add function.
You should plan on not only assigning to the parent and child but you should also reassign it a placeholder node. This
Node is going to be privately accessible and so hidden that the user won;t be able to find it, but basically, you have a hash 
set of commits, and every time you add more than it can hold, you double it. If it ever goes hay wire, well you can look back 
through that hash set and hash sort it, (ib order by time, to see what you want to get). Notice this is a lest resort and 
is only if the other safety nets didn't catch the user's mistake 
Going to go more depth into this.

