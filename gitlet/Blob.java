package gitlet;

import java.io.Serializable;
import static gitlet.Utils.*;

/** Represents versions of files tracked in the repository.
 * @author Jack Mango */

public class Blob implements Serializable {

    /** Creates a new blob with CONTENTS and FILENAME. */
    public Blob(String filename, String contents) {
        _filename = filename;
        _contents = contents;
        _commited = false;
    }

    /** Creates a blob from two other blobs; FILENAME is the name of the
     *  filename and CURR and TARG are different versions of the same file
     *  referenced by the current and target commits in a merge. */
    public Blob(String filename, Blob curr, Blob targ) {
        _filename = filename;
        _contents = String.format("<<<<<<< HEAD\n%s=======\n%s>>>>>>>\n",
                curr.getContents(), targ.getContents());
        _commited = true;
    }

    /** Creates a blob from two other blobs; FILENAME is the name of the
     *  filename and CURRHASH and TARGHASH are different versions of the
     *  same file referenced by the current and target commits in a merge. */
    public Blob(String filename, String currHash, String targHash) {
        this(filename, readObject(join(BLOBSDIR, currHash), Blob.class),
                readObject(join(BLOBSDIR, targHash), Blob.class));
    }

    /** Returns this blob's filename. */
    public String getFilename() {
        return _filename;
    }

    /** Returns this blob's contents. */
    public String getContents() {
        return _contents;
    }

    /** Returns this blob's SHA-1 hash ID. */
    public String getID() {
        return sha1(_filename + _contents);
    }

    /** Changes _commited to true. */
    public void commit() {
        _commited = true;
    }

    /** Returns true if this blob is referenced by a commit. */
    public boolean isCommited() {
        return _commited;
    }

    /** Stores this blob's filename. */
    private String _filename;

    /** Stores this blob's contents. */
    private String _contents;

    /** Tells whether or not this blob has already been
     * included in a commit or is just staged. */
    private boolean _commited;

}
