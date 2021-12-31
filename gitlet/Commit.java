package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Formatter;
import java.util.Collection;
import static gitlet.Utils.*;

/** Handles the commiting objects which track files and previous commits in
 *  gitlet.
 * @author Jack Mango */

public class Commit implements Serializable {


    /** Creates a new gitlet commit with message MESSAGE message,
     *  and parent PARENT. Initially tracks the exact same blobs
     *  as the parent commit. */
    public Commit(String message, Commit parent) {
        _timestamp = ZonedDateTime.now().format(FORMAT);
        _message = message;
        _parent = parent.getID();
        _blobs = new TreeMap<String, String>();
        _versions = new HashSet<String>();
    }

    /** Create a commit with MESSAGE and TIMESTAMP. Used for child classes. */
    public Commit(String message, String timestamp) {
        _timestamp = timestamp;
        _message = message;
        _blobs = new TreeMap<String, String>();
        _versions = new HashSet<String>();
    }

    /** Returns this commit's message. */
    public String getMessage() {
        return _message;
    }

    /** Returns this commit's timestamp. */
    public String getTimestamp() {
        return _timestamp;
    }

    /** Returns the hexadecimal ID string associated with this commit.
     * Generates the ID by calling SHA-1 hash function on this commit's
     * timestamp, blobIDs, and message. */
    public String getID() {
        Formatter f = new Formatter();
        for (String blob : _blobs.values()) {
            f.format(blob);
        }
        return Utils.sha1(f.toString(), _parent, _message, _timestamp);
    }

    /** Returns true if this commit is tracking a file with HASH.
     *  If the _blobs field hasn't been initialized (initial commit),
     *  returns false. */
    public boolean trackingHash(String hash) {
        return _versions.contains(hash);
    }

    /** Returns true if this commit is tracking a file with name FILENAME.
     *  If the _blobs field hasn't been initialized (initial commit),
     *  returns false. */
    public boolean tracking(String filename) {
        return _blobs.containsKey(filename);
    }

    /** Returns true if the file with the corresponding BLOB in the cwd
     *  is tracked by name in this commit however with a different version. */
    public boolean modified(Blob blob) {
        return tracking(blob.getFilename())
                && !trackingHash(blob.getID());
    }

    /** Returns this commmit's parent commit object. */
    public Commit getParent() {
        return readObject(commitFile(_parent), Commit.class);
    }

    /** Updates the blobs this commit is tracking. First iterates over each
     *  of the files in the staging area, STG for addition, including them for
     *  tracking, then filtering parent blobs, adding only if not already
     *  present and not staged for removal. */
    public void updateTracking(StagingArea stg) {
        for (String filename : stg.additionDump()) {
            _blobs.put(filename, stg.additionGetID(filename));
            _versions.add(stg.additionGetID(filename));
        }
        if (!(getParent() instanceof InitialCommit)) {
            Collection<Map.Entry<String, String>> parentBlob =
                    getParent()._blobs.entrySet();
            for (Map.Entry<String, String> blob : parentBlob) {
                if (!_blobs.containsKey(blob.getKey())
                        && !stg.removeContains(blob.getKey())) {
                    _blobs.put(blob.getKey(), blob.getValue());
                    _versions.add(blob.getValue());
                    Blob addedBlob = readObject(join(BLOBSDIR,
                                    blob.getValue()),
                            Blob.class);
                    addedBlob.commit();
                    writeObject(join(BLOBSDIR, blob.getValue()), addedBlob);
                }
            }
        }
    }

    /** Adds the given BLOB to the files tracked by this commit. */
    public void add(Blob blob) {
        _blobs.put(blob.getFilename(), blob.getID());
        _versions.add(blob.getID());
    }

    /** Returns the SHA-1 hash of the file with FILENAME tracked by this
     *  commit. */
    public String hashOf(String filename) {
        return _blobs.get(filename);
    }

    /** Writes a copy of the blob corresponding to HASH tracked by this
     *  commit in the working directory. */
    public void checkout(String hash) {
        Blob blob = readObject(join(BLOBSDIR, hash), Blob.class);
        writeContents(new File(blob.getFilename()), blob.getContents());
    }

    /** Writes copies of all the blobs tracked by this commit to the working
     *  directory. */
    public void checkoutAll() {
        for (String blobId : _blobs.values()) {
            checkout(blobId);
        }
    }

    /** Iterates over all filenames tracked by this commit checking for two
     *  conditions. If the file exists in the cwd and isn't tracked by LAST,
     *  we return true. If the file doesn't exist in the cwd but is tracked
     *  by name in the LAST commit, return true. */
    public boolean inTheWay(Commit last) {
        for (String filename : _blobs.keySet()) {
            File file = new File(filename);
            if (!file.exists() && last.tracking(filename)) {
                return true;
            } else if (file.exists() && !last.trackingHash(sha1(file))) {
                return true;
            }
        }
        return false;
    }

    /** Remove all files tracked by this commit from the cwd. */
    public void clearTracked() {
        for (String filename : _blobs.keySet()) {
            File file = new File(filename);
            restrictedDelete(file);
        }
    }

    /** Returns the format used for timestamps. */
    public static DateTimeFormatter getFormat() {
        return FORMAT;
    }


    /** Returns a string containing my ID, timestamp, and message. */
    @Override
    public String toString() {
        return String.format("===\ncommit %s\nDate: %s\n%s",
                getID(), getTimestamp(), getMessage());
    }

    /** Returns an iterator over the hashes tracked by this class. */
    public Collection<String> hashes() {
        return _versions;
    }

    /** This commit's parents' IDs. */
    private String _parent;

    /** The message associated with this commit. */
    private String _message;

    /** The timestamp associated with this commit. */
    private String _timestamp;

    /** Timestamp formatter used for commits. */
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("EEE LLL d kk:mm:ss uuuu Z");

    /** A map storing the names of the files this commit is tracking;
     *  keys are filenames hashes and values are
     * SHA-1 hashes. */
    private TreeMap<String, String> _blobs;

    /** Stores Ids of blobs stored by this commit. */
    private HashSet<String> _versions;
}
