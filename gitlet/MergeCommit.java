package gitlet;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.Formatter;

import static gitlet.Utils.*;

/** Commits that are created specifically after merges use this class.
 * Has two parents instead of one and chooses files based on procedure
 * defined in repository.
 * @author Jack Mango */

public class MergeCommit extends Commit {

    /** Creates a new mergecommit with parents CURRENT and TARGET. */
    public MergeCommit(Branch current, Branch target) {
        super(String.format("Merged %s into %s.",
                        target.getName(), current.getName()),
                ZonedDateTime.now().format(getFormat()));
        _first = current.getHead();
        _second = target.getHead();
    }

    /** Updates the blobs this commit is tracking; tracks the exact
     * same blobs as those tracked in OTHER. */
    public void updateTracking(Commit other) {
        for (String hash : other.hashes()) {
            add(readObject(join(BLOBSDIR, hash), Blob.class));
        }
    }


    /** Add the given BLOB to the list of files tracked by commit. */
    @Override
    public void add(Blob blob) {
        File file = new File(blob.getFilename());
        if (file.exists()) {
            Blob currBlob = new Blob(blob.getFilename(),
                    readContentsAsString(file));
            if (!join(BLOBSDIR, currBlob.getID()).exists()) {
                throw error("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }
        super.add(blob);
    }

    /** Adds the blob corresonding to BLOBID to the list of files tracked by
     *  this commit. */
    public void add(String blobId) {
        add(readObject(join(BLOBSDIR, blobId), Blob.class));
    }

    @Override
    /** Almost the same as the regular commit id, except includes both parent
     *  ids. */
    public String getID() {
        Formatter f = new Formatter();
        for (String hash : hashes()) {
            f.format(hash);
        }
        return Utils.sha1(f.toString(), getFirst(), getSecond(), getMessage(),
                getTimestamp());
    }

    /** Returns the first parent. */
    @Override
    public Commit getParent() {
        return readObject(commitFile(getFirst()), Commit.class);
    }

    @Override
    /** Same as commit just added extra line to indicate merge. */
    public String toString() {
        return String.format("===\ncommit %s\nMerge: %s %s\nDate: %s\n%s",
                getID(), _first.substring(0, 7), _second.substring(0, 7),
                getTimestamp(), getMessage());
    }

    /** Returns the first parent ID. */
    public String getFirst() {
        return _first;
    }

    /** Returns the second parent ID. */
    public String getSecond() {
        return _second;
    }

    /** Stores the first parent of this mergecommit; the commit id
     *  corresponding to the
     * head of the branch merged into at the time of merging. */
    private String _first;

    /** Stores the second parent of this mergecommit; the commit id
     *  corresponding to the head of the branch that was merged with
     *  at the time of merging. */
    private String _second;

}
