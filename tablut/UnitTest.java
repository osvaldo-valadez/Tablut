package tablut;

import org.junit.Test;
import static org.junit.Assert.*;


import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author Osvaldo Valadez
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test as a placeholder for real ones. */
    @Test
    public void checkKingFinder() {
        Board testBoard = new Board();
        testBoard.init();
        Square check = Square.sq(4, 4);
        assertEquals(testBoard.kingPosition(), check);
    }
    @Test
    public void checkDistanceFromTo1() {
        Board testBoard = new Board();
        testBoard.init();
        Square s1 = Square.sq(4, 5);
        Square s2 = Square.sq(4, 8);

        assertEquals(testBoard.distanceFromTo(s1, s2), 3);
    }
    @Test
    public void checkDistanceFromTo2() {
        Board testBoard = new Board();
        testBoard.init();
        Square s1 = Square.sq(1, 5);
        Square s2 = Square.sq(5, 5);
        assertEquals(testBoard.distanceFromTo(s1, s2), 4);
    }

}


