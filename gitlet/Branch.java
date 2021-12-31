package gitlet;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

import static gitlet.Utils.*;

/** Represents branches in gitlet. Serialized for persistance.
 * @author Jack Mango */
public class Branch implements Serializable {

    /** Create a new branch with NAME and HEAD. */
    public Branch(String name, Commit head) {
        _name = name;
        _head = head.getID();
    }

    /** Returns the name of this branch. */
    public String getName() {
        return _name;
    }

    /** Returns this branch's head commit. */
    public String getHead() {
        return _head;
    }

    /** Sets the head of this branch to COMMITID. */
    public void setHead(String commitID) {
        _head = commitID;
    }

    /** Returns the latest common ancestor with the OTHER branch. */
    public String commonAncestor(Branch other) {
        TreeMap<String, Integer> masterTrav = new TreeMap<String, Integer>();
        TreeMap<Integer, String> targTrav = new TreeMap<Integer, String>();
        Queue<String> remaining = new LinkedList<String>(); int distance = 0;
        masterTrav.put(getHead(), distance); remaining.add(getHead());
        while (!remaining.isEmpty()) {
            Commit commit = readObject(join(commitFile(remaining.remove())),
                    Commit.class);
            distance = masterTrav.get(commit.getID()) + 1;
            if (commit instanceof InitialCommit) {
                continue;
            } else if (commit instanceof MergeCommit) {
                MergeCommit mergecom = (MergeCommit) commit;
                if (!masterTrav.containsKey(mergecom.getFirst())
                        || masterTrav.get(mergecom.getFirst()) > distance) {
                    masterTrav.put(mergecom.getFirst(), distance);
                    remaining.add(mergecom.getFirst());
                }
                if (!masterTrav.containsKey(mergecom.getSecond())
                        || masterTrav.get(mergecom.getSecond()) > distance) {
                    masterTrav.put(mergecom.getSecond(), distance);
                    remaining.add(mergecom.getSecond());
                }
            } else {
                if (!masterTrav.containsKey(commit.getParent().getID())
                        || masterTrav.get(commit.getParent().getID())
                        > distance) {
                    masterTrav.put(commit.getParent().getID(), distance);
                    remaining.add(commit.getParent().getID());
                }
            }
        }
        remaining.add(other.getHead());
        while (!remaining.isEmpty()) {
            Commit commit = readObject(join(commitFile(remaining.remove())),
                    Commit.class);
            if (commit instanceof MergeCommit) {
                MergeCommit mergecom = (MergeCommit) commit;
                if (masterTrav.containsKey(mergecom.getFirst())) {
                    targTrav.put(masterTrav.get(mergecom.getFirst()),
                            mergecom.getFirst());
                }
                if (masterTrav.containsKey(mergecom.getSecond())) {
                    targTrav.put(masterTrav.get((mergecom.getSecond())),
                            (mergecom.getSecond()));
                }
                remaining.add(mergecom.getFirst());
                remaining.add(mergecom.getSecond());
            } else {
                if (masterTrav.containsKey(commit.getID())) {
                    targTrav.put(masterTrav.get(commit.getID()),
                            commit.getID());
                }
                if (commit.getParent() != null) {
                    remaining.add(commit.getParent().getID());
                }
            }
        }
        return targTrav.firstEntry().getValue();
    }

    /** The name of this branch. */
    private String _name;

    /** Stores the commit Id corresponding to the head commit. */
    private String _head;

}
