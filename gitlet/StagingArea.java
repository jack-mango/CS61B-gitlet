package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Utils.*;

/** Handles file staging for gitlet using object persistance.
 * @author Jack Mango */

public class StagingArea implements Serializable {

    /** Create a new staging area with the absolute PATH of the
     * working directory. */
    public StagingArea() {
        _addition = new HashMap<String, String>();
        _removal = new HashSet<String>();
    }

    /** Returns true if the staging area for addition contains the file
     * with FILENAME. */
    public boolean addContains(String filename) {
        return _addition.containsKey(filename);
    }

    /** Returns true if the staging area for removal contains the file
     * with name FILENAME. */
    public boolean removeContains(String filename) {
        return _removal.contains(filename);
    }

    /** Stages the FILE for removal and unstages for addition. */
    public void stgRemoval(String file) {
        _removal.add(file);
        _addition.remove(file);
    }

    /** Stages the file with corresponding BLOB for addition and unstages
     *  for removal. */
    public void stgAddition(Blob blob) {
        _addition.put(blob.getFilename(), blob.getID());
        _removal.remove(blob.getFilename());
        writeObject(join(BLOBSDIR, blob.getID()), blob);
    }

    /** Unstages the file with NAME for addition. */
    public void unstgRemoval(String name) {
        _removal.remove(name);
    }

    /** Unstages the file with name FILENAME for addition. */
    public void unstgAddition(String filename) {
        if (!readObject(join(BLOBSDIR, additionGetID(filename)),
                Blob.class).isCommited()) {
            join(BLOBSDIR, additionGetID(filename)).delete();
        }
        _addition.remove(filename);
    }

    /** Clear all files staged for addition. Takes O(n) time with respect to
     *  number of staged files*/
    public void clearAddition() {
        for (String blobId: _addition.values()) {
            join(BLOBSDIR, blobId).delete();
        }
        _addition.clear();
    }

    /** Clear all files staged for removal. */
    public void clearRemoval() {
        _removal.clear();
    }

    /** Returns the hash of FILE being tracked for addition. */
    public String additionGetID(String file) {
        return _addition.get(file);
    }

    /** Returns all SHA-1 hash IDs for the files staged for addition. */
    public Collection<String> additionDump() {
        return _addition.keySet();
    }

    /** Returns all filenames for the files staged for removal. */
    public Collection<String> removalDump() {
        return _removal;
    }

    /** Returns a list of filenames that are staged for addition and are
     *  unmodified. */
    public List<String> unmodified() {
        ArrayList<String> result = new ArrayList<String>();
        for (String filename : _addition.keySet()) {
            Blob currblob = new Blob(filename,
                    readContentsAsString(new File(filename)));
            if (currblob.getID().equals(_addition.get(filename))) {
                result.add(filename);
            }
        }
        return result;
    }

    /** Returns true if any files are staged for addition or removal. */
    public boolean anyStaged() {
        return !_addition.isEmpty() || !_removal.isEmpty();
    }

    /** The absolute path of the working directory. */
    private String _dirPath;

    /** The files to be added to the next commit; keys are filenames and values
     *  are SHA-1 hash IDs. */
    private HashMap<String, String> _addition;

    /** The files to be removed from the next commit; filenames. */
    private HashSet<String> _removal;

}
