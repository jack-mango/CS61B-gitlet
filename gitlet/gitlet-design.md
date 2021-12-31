# Gitlet Design Document

**Name**: Jack Mango

For full list of commands and specification, visit
https://inst.eecs.berkeley.edu/~cs61b/fa21/materials/proj/proj3/
## Classes and Data Structures

###Repository
The object that keeps track of the gitlet repository; used for persistance.
####Fields
`private String _active`
\
Name of the currently active branch.


###Commit
This class represents a single gitlet commit. Created each time we want to make a new commit, and stored for later use. 
####Fields
`private String _timestamp`
\
Contains the timestamp when the commit was created.

\
`private String _message`
\
Stores the message associated with this commit.

\
`private TreeMap<String, String> _blobs`
\
The blobs associated with this commit; keys are blob IDs and values
are original filenames. 

\
`private HashSet<String> _versions`
\
Contains the blobIds tracked by this commit. Used to efficiently check if this commit is tracking a certain
version of a file. 

\
`private String _parent`
\
Commit ID of this commit's parent 

###MergeCommit
A special type of commit created when using the merge command. This
type of commit is unique in that it has two parents.
####Fields
`private String _first`
\
The commmit ID corresponding to the commit that was merged into from the merge command
\

`private String _seoncd`
The commit ID corresponding to the commit that was merged with from the merge


###Branch
####Fields
`private String _name`
\
The name of this branch.

\
`private String _head`
\
ID of the commit which is the head of this branch.

###Blob 
####Fields
An object abstraction of the files stored in the git repository
\
`private String _fileanme`
\
The filename this blob corresponds to.

\
`private String _contents`
\
The contents of the file version corresponding to this blob.

\
`private boolean _commited`
\
Stores whether or not this blob is tracked by any commits.

###StagingArea
####Fields
`private TreeMap<String, String> _addition`
\
Tracks files to be added in the next commit; keys are the 
filenames and values are the SHA-1 hash ID of the most recent 
blob staged for addition.

\
`private TreeSet<String> _removal`
\
Stores the filenames of the files to be removed in the next commit.



###Blob
####Fields
`private String _contents`
\
Contents of this blob.

\
`private String _filename`
\
The filename associated with this blob.


## Algorithms
###Main
`public void verifyInit()`
\
Checks to see if a repository already exists. If one doesn't already it returns the following output:
`Not in an initialized Gitlet directory.

\
`public void init()`
\
Creates the initial required filestructure for gitlet in a hidden folder.
Then creates a new gitlet repository object inside the folder.
Gives the following error message if a gitlet repository already exists in this directory:
`A Gitlet version-control system already exists in the current directory.`

\
`public void add(String file)`
\
Searches the staging area's `_blobs` to check if the exact same version of the file 
is already there. If not, adds it to the staging area. Also checks the staging area
to see if this file would be removed, which it should change if it is.

###Repository
`public Repository()`
\
Creates a new repository and the file structure for a gitlet repository.
Also calls the initObjects method to create all the necessary objects.

\
`public void initObjects()`
\
Creates all the required objects for the repository to function properly
(initial commit, staging area and master branch).

\
`public Branch getActive()`
\
Returns the branch corresponding to the active branch. 

\
`public Commit lastCommit()`
\
Returns the most recent commit from the active branch.

\
`public void add(String file)`
\
First loads the staging area object. Unstages the file for removal (if it was staged for it) then 
performs appropriate behavior based on whether file is staged or not and modified.
\
*Behavior:*
1. File is already staged and same as currently staged version
\
_Nothing happens_ 
2. File is already staged but contents are modified
\
_Remove older staged version and stage new version of this file_
3. File is not already staged and has same contents as most recent commit
\
_Nothing happens_
4. File is not already staged and contents have been modified since most recent commit
\
_New file is staged for addition_

\
`public void remove(String file)`
\
If the file is being tracked in the staging area it is removed from the list of files
staged for addition.
If the file is tracked in the most recent commit it is added to the list 
of files to be staged for removal (not to be tracked by next commit).
If the file is present in the working directory it is deleted, although only if 
it passes one of the two previous conditions.

\
`public void commit(String message)`
\
Create a new commit object with the given message, and parent commit corresponding
to the most recent in the active branch. Then set up
this commit's tracking by:
1. Iterating over files staged for addition, adding them to my
`_blobs`
2. Iterating over files in my parent's commit, adding all files whose name
don't match any filenames staged for removal and aren't already being tracked from
*step 1* 


After that the staging area is cleared, and the active branch's
`_head` is updated to the new commit ID. 

\
`public void status()`
\
Iterate over branches, adding them to output, and marking the active branch with a "*". Next
iterate over the files staged for addition, including them in the output only if
its SHA-1 hash ID matches the corresponding stored one in our staging area. This way
we know our file is unmodified from the version we staged for addition. After that
simply iterate over files for removal and add them to our output. For the remaining two sections
- Modifications not staged for commit: need some way to iterate over both files
staged for addition and files in the last commit to check if they've been modififed or
deleted, add corresponding message, and keep them alphabetized. 
- Untracked: iterate over files in the current directory and find which are not tracked in last commit
and also not staged for addition. 


\
`public void log()`
\
Starts at the active branch's head commit and iteratively
print the commit's ID, data and message, as long as the commit
has a parent.

\
`public void globalLog()`
\
Prints every commit ever made in this repository. Iterates over every
commit object in the Commits directory and prints out their information.

\
`public void checkoutFile(String file)`
\
Loads the most recent commit object and finds the blob with the correct filename.
Then writes a new file in the current directory with that blob's filename and contents.
If a file with this filename was staged for addition, it now becomes unstaged.

\
`public void checkoutFile(String filename, String commitId)`
\
Loads the commit object file named *commitId* and finds its blob with matching filename.
Then loads that blob and writes a new file with named *filename* with the contents of this blob.

\
`public void checkoutBranch(String branchName)`
\
Calls the restore method on the head commit of the given branch. Also clears the staging
area and sets the active branch to be the given branch.

\
`public void restore(Commit commit)`
\
Clears all files tracked by the most recent commit, using the commit's
clearTracked method, then creates copies of all the files tracked by the given]
commit using the commit's checkoutAll method.

\
`public void branch(String name)`
\
Creates a new branch object in the Branches directory with the given name,
whose head is the most recent commit.

\
`public void rmBranch(String branchName)`
\
Deletes the branch object corresponding to branchName from the Branches directory. 


\
`public void reset(String commitId)`
\
Checks out all the files tracked by the commit with corresponding
COMMITID and removes tracked files that are not present in that commit using the
restore method. Then clears the staging area and sets the current branch's head to the
commitId.

\
`public void merge(String other)`
\
Finds the common ancestor of the last commit and the other commit using
the active branch's commonAncestor method. Creates a mergecommit object according
to the criteria for this project 
(which can be found at https://inst.eecs.berkeley.edu/~cs61b/fa21/materials/proj/proj3/index.html#the-commands). 
Also clears the staging area and updates the head of the current branch to this 
new mergecommit. Employs the use of a helper method to pass CS16B style check method length requirement. 

###Branch
`public Branch(String name, Commit head)`
\
Creates a new Branch named *name* that points to the active branch's head node

\
`public String getName()`
\
Returns the `_name` field.

\
`public String getHead()`
\
Returns the `_head` field.

\
`public void setHead(String commitID)`
\
Changes the `_head` field to the given commit ID.

\
`public String commonAncestor(Branch other)`
\
Performs a breadth first graph traversal of all the nodes reachable by the head
commits from both this branch and the other branch, returning the closest 
commit reachable by both the heads. 

###Commit 
`public Commit(String message, Commit parent)`
\
Constructor for the commit class, storing the message and parent commit by its ID as a String.

\
`public String getMessage()`
\
Returns the _message field.

\
`public String getTimestamp()`
\
Returns the _timestamp field.

\
`public String getID()`
\
Returns the SHA-1 hash for this commit in hexadecimal.

\
`public boolean trackingHash(String hash)`
\
Checks if the given hash is contained in the `_versions` field.

\
`public boolean tracking(String filename)`
\
Checks if the given filename is tracked in the `_blobs` key set.

\
`public boolean modified(Blob blob)`
\
Returns true if the given blob has the same filename but different contents from the one
tracked by this commit.

\
`public Commit getParent()`
\
Returns the commit object corresponding to this commit's parent ID. 

\
`public String hashOf(String filename)`
\
Returns the hash of the filename in the `_blobs` field.

\
`public void add(Blob blob)`
\
Adds the filename and blobId corresponding to the given blob to the `_blobs` and `_versions`
fields.

\
`public void updateTracking()`
\
Handles the files staged for addition to this new commit. For each file
in the staging area directory:
1. add ID and name to `_blobs`
2. add parent's blobs to my `_blobs` if they're not staged for removal and they're not already
being tracked from *step 1.*
3. clear the staging area

\
`public void checkout(String hash)`
\
Writes the contents of the blob object corresponding to hash into a file with that blob's filename
in the current directory.

\
`public void checkoutAll()`
\
Iterates through the blob IDs tracked by this commit and calls the checkout method on each.

\
`public boolean inTheWay(Commit last)`
\
Iterates through files tracked by this commit and checks to see if any 
would be deleted or overwritten by a merge.

\
`public void clearTracked()`
\
Deletes all files with the same filename as those tracked by this commit
from the working directory.

\
`public String toString()`
\
Includes the commit's ID, timestamp, and message

\
`public Collection<String> hashes()`
\
Returns the `_versions` field for use in other class methods.

###MegeCommit
`public MergeCommti(Branch current, Branch target)`
\
Creates a new mergecommit object, giving it the current timestamp
and setting its first and second parents. 

\
`public void updateTracking(Commit other)`
\
Iteratively updates this commit so it tracks the exact same blobs tracked by the other commit.

\
`public void add(Blob blob)`
\
Adds this file to the list of blobs tracked by this commit, throwing an error
if there's an untracked version of the blob in the current directory that would be 
deleted.

\
`public void add(String blobId)`
\
Calls the add method with the blob corresponding to blobId.

\
`public String getId()`
\
Returns an ID essentially the same as that of a regular commit, but uses both parents for generating.

\
`public String toString()`
\
Essentially the same behavior as a regular commit's toString method, only includes
both parents and indicates that this commit was generated from a merge.

\
`public String getFirst()`
\
Returns the `_first` field.

\
`public String getSecond()`
\
Returns the `_second` field.

###InitialCommit
`public InitialCommit()`
Generates an initial commit, calling the parent constructor with the current time and a message reading,
"initial commit."

\
`public String getID()`
\
Generates this commits ID using its message and timestamp only.

\
`public Commit getParent()`
\
Returns null as initial commits don't have any parents.

\
`public boolean inTheWay(Commit last)`
\
Returns false as no files are tracked by the intial commit. 

###Blob
`public Blob(String filename, String contents)`
\
Constructor used for a regular blob object, storing the filename and contents in a blob object

\
`public Blob(String filename, Blob curr, Blob targ)`
\
Constrcutor used for conflicts in a merge, where the contents of this blob
includes the contents of both the curr and targ blobs, with the given filename.

\
`public String getContents()`
\
Returns the contents  of this blob.

\
`public String getFilename()`
\
Returns this blob's filename.

\
`public String getID()`
\
Returns this blob's SHA-1 ID.

\
`public boolean isCommited()`
\
Returns the blob's `_commited` field.

## Persistence
###File Structure
```
.gitlet
    ├──Objects
    │    ├──repository
    │    └──stagingArea
    │
    ├──Branches
    │    └── <branch objects>
    │
    ├──Blobs
    │    └── <blob objects>
    │
    └──Commits
         └── <commit objects>
```

