package loa;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import static loa.Piece.*;
import static loa.Square.sq;
import static loa.Move.mv;

/**
 * Tests of the Board class API.
 *
 * @author
 */
public class BoardTest {

    /**
     * A "general" position.
     */
    static final Piece[][] BOARD1 = {
            {EMP, BP, EMP, BP, BP, EMP, EMP, EMP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, BP, BP, EMP, WP},
            {WP, EMP, BP, EMP, EMP, WP, EMP, EMP},
            {WP, EMP, WP, WP, EMP, WP, EMP, EMP},
            {WP, EMP, EMP, EMP, BP, EMP, EMP, WP},
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {EMP, BP, BP, BP, EMP, BP, BP, EMP}
    };

    /**
     * A position in which black, but not white, pieces are contiguous.
     */
    static final Piece[][] BOARD2 = {
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {EMP, BP, WP, BP, BP, BP, EMP, EMP},
            {EMP, WP, BP, WP, WP, EMP, EMP, EMP},
            {EMP, EMP, BP, BP, WP, WP, EMP, WP},
            {EMP, WP, WP, BP, EMP, EMP, EMP, EMP},
            {EMP, EMP, EMP, BP, EMP, EMP, EMP, EMP},
    };

    /**
     * A position in which black, but not white, pieces are contiguous.
     */
    static final Piece[][] BOARD3 = {
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
            {EMP, BP, WP, BP, WP, EMP, EMP, EMP},
            {EMP, WP, BP, WP, WP, EMP, EMP, EMP},
            {EMP, EMP, BP, BP, WP, WP, WP, EMP},
            {EMP, WP, WP, WP, EMP, EMP, EMP, EMP},
            {EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP},
    };


    static final String BOARD1_STRING =
            "===\n"
                    + "    - b b b - b b - \n"
                    + "    - - - - - - - - \n"
                    + "    w - - - b - - w \n"
                    + "    w - w w - w - - \n"
                    + "    w - b - - w - - \n"
                    + "    w - - - b b - w \n"
                    + "    w - - - - - - w \n"
                    + "    - b - b b - - - \n"
                    + "Next move: black\n"
                    + "===";

    /**
     * Test display
     */
    @Test
    public void toStringTest() {
        assertEquals(BOARD1_STRING, new Board(BOARD1, BP).toString());
    }

    /**
     * Test legal moves.
     */
    @Test
    public void testLegality1() {
        Board b = new Board(BOARD1, BP);
        assertTrue("f3-d5", b.isLegal(mv("f3-d5")));
        assertTrue("f3-h5", b.isLegal(mv("f3-h5")));
        assertTrue("f3-h1", b.isLegal(mv("f3-h1")));
        assertTrue("f3-b3", b.isLegal(mv("f3-b3")));
        assertFalse("f3-d1", b.isLegal(mv("f3-d1")));
        assertFalse("f3-h3", b.isLegal(mv("f3-h3")));
        assertFalse("f3-e4", b.isLegal(mv("f3-e4")));
        assertFalse("c4-c7", b.isLegal(mv("c4-c7")));
        assertFalse("b1-b4", b.isLegal(mv("b1-b4")));
    }

    /**
     * Test contiguity.
     */
    @Test
    public void testContiguous1() {
        Board b1 = new Board(BOARD1, BP);
        assertFalse("Board 1 black contiguous?", b1.piecesContiguous(BP));
        assertFalse("Board 1 white contiguous?", b1.piecesContiguous(WP));
        assertFalse("Board 1 game over?", b1.gameOver());
        Board b2 = new Board(BOARD2, BP);
        assertTrue("Board 2 black contiguous?", b2.piecesContiguous(BP));
        assertFalse("Board 2 white contiguous?", b2.piecesContiguous(WP));
        assertTrue("Board 2 game over", b2.gameOver());
        Board b3 = new Board(BOARD3, BP);
        assertTrue("Board 3 white contiguous?", b3.piecesContiguous(WP));
        assertTrue("Board 3 black contiguous?", b3.piecesContiguous(WP));
        assertTrue("Board 3 game over", b2.gameOver());
    }

    @Test
    public void testEquals1() {
        Board b1 = new Board(BOARD1, BP);
        Board b2 = new Board(BOARD1, BP);

        assertEquals("Board 1 equals Board 1", b1, b2);
    }

    @Test
    public void testMove1() {
        Board b0 = new Board(BOARD1, BP);
        Board b1 = new Board(BOARD1, BP);
        b1.makeMove(mv("f3-d5"));
        assertEquals("square d5 after f3-d5", BP, b1.get(sq(3, 4)));
        assertEquals("square f3 after f3-d5", EMP, b1.get(sq(5, 2)));
        assertEquals("Check move count for board 1 after one move",
                1, b1.movesMade());
        b1.retract();
        assertEquals("Check for board 1 restored after retraction", b0, b1);
        assertEquals("Check move count for board 1 after move + retraction",
                0, b1.movesMade());
    }

    @Test
    public void testregionSizes() {
        Board b1 = new Board(BOARD1, BP);
        List<Integer> blackRegions = b1.getRegionSizes(BP);
        assertEquals(7, blackRegions.size());
        List<Integer> corrBlack = new ArrayList<>();
        corrBlack.add(3);
        corrBlack.add(2);
        corrBlack.add(1);
        corrBlack.add(1);
        corrBlack.add(2);
        corrBlack.add(1);
        corrBlack.add(2);
        assertTrue(blackRegions.containsAll(corrBlack));

        List<Integer> whiteRegions = b1.getRegionSizes(WP);
        assertEquals(5, whiteRegions.size());
        List<Integer> corrWhite = new ArrayList<>();
        corrWhite.add(5);
        corrWhite.add(2);
        corrWhite.add(2);
        corrWhite.add(1);
        corrWhite.add(1);
        assertTrue(whiteRegions.containsAll(corrWhite));
    }

}
