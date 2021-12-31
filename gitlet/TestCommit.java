package gitlet;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestCommit {

    @Test
    public void testGets() {
        Commit commit1 = new InitialCommit();
        Commit commit2 = new Commit("first commit!", commit1);
        assertEquals("initial commit", commit1.getMessage());
        assertEquals("Thu Jan 1 00:00:00 1970", commit1.getTimestamp());
        assertEquals("first commit!", commit2.getMessage());
        assertTrue(commit1.getTimestamp().contains("Tue"));
    }
}
