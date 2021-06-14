package loa;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;

/** The suite of all JUnit tests for the loa package.
 */
public class UnitTests {

    /** Run the JUnit tests in the loa package. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTests.class);
        textui.runClasses(BoardTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }


}
