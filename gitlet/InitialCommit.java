package gitlet;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** A child class of the Commit class.
 * Used specifically for the first commit created in a repository.
 * @author Jack Mango */

public class InitialCommit extends Commit {

    /** The inital commit constructor. */
    public InitialCommit() {
        super(MESSAGE, TIMESTAMP);
    }

    /** Returns the hexadecimal ID string associated with this commit.
     *  Generates the ID by calling SHA-1 hash function on this commit's
     *  timestamp and message*/
    @Override
    public String getID() {
        return Utils.sha1(getMessage(), getTimestamp());
    }

    @Override
    /** Returns null. */
    public Commit getParent() {
        return null;
    }

    @Override
    /** Since the initial commit tracks no files, none will ever be in the way;
     *  always should be false. */
    public boolean inTheWay(Commit last) {
        return false;
    }

    /** This commit's parents' IDs. */
    private static final Commit PARENT = null;

    /** The message associated with this commit. */
    private static final String MESSAGE = "initial commit";

    /** Timestamp formatter used for commits. */
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("EEE LLL d kk:mm:ss uuuu Z");

    /** The timestamp associated with this commit. */
    private static final String TIMESTAMP = FORMAT.format(ZonedDateTime.of(1970,
            1, 1, 0, 0, 0, 0,
            ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()));

}

