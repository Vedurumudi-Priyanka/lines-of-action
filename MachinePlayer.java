package loa;

import static loa.Piece.*;

import java.util.List;
import java.util.Collections;

/**
 * An automated Player.
 */
class MachinePlayer extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new MachinePlayer with no piece or controller (intended to produce
     * a template).
     */
    MachinePlayer() {
        this(null, null);
    }

    /**
     * A MachinePlayer that plays the SIDE pieces in GAME.
     */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }


    @Override
    String getMove() {
        Move choice;
        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move after searching the game tree to DEPTH>0 moves
     * from the current position. Assumes the game is not over.
     */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {

            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);

        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);

        }

        return _foundMove;
    }

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _foundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _foundMove. If the game is over
     * on BOARD, does not set _foundMove.
     */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        Move tracker = null;
        if (depth == 0 || board.gameOver()) {
            return heuristicFunc(board);
        }
        if (sense == 1) {

            int maxEval = -INFTY;
            for (Move m : board.legalMoves()) {
                board.makeMove(m);
                int eval = findMove(board, depth - 1, false,
                        -1, alpha, beta);
                board.retract();
                if (eval > maxEval) {
                    tracker = m;
                    maxEval = eval;
                }
                alpha = Integer.max(alpha, maxEval);
                if (beta <= alpha) {
                    break;
                }
            }
            assert tracker != null;
            if (saveMove) {
                _foundMove = tracker;
            }
            return maxEval;

        } else {
            int minEval = INFTY;
            for (Move m : board.legalMoves()) {
                board.makeMove(m);
                int eval = findMove(board, depth - 1, false,
                        1, alpha, beta);
                board.retract();
                if (eval < minEval) {
                    tracker = m;
                    minEval = eval;
                }
                beta = Integer.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
                assert tracker != null;
                if (saveMove) {
                    _foundMove = tracker;
                }
            }
            return minEval;
        }
    }

    /**
     * Heuristic function that either returns a
     * the winning value based on the current state
     * or it evaluates the board using the helper
     * function.
     * @param b <Board that it is evaluating at that state>
     * @return Returns a heuristic value based on that Board.
     */
    int heuristicFunc(Board b) {
        if (b.winner() == WP) {
            return  WINNING_VALUE;
        } else if (b.winner() == BP) {
            return  -WINNING_VALUE;
        } else {
            int val = 0;
            val += sizeContig(b);
            return val;
        }
    }


    /**
     * The heuristic helper function checks based on the
     * maximum sizes of the contiguous region and the
     * number of contiguous regions. Because you would
     * the way to win the game is to have a single
     * contiguous region, I have it be favorable for
     * a smaller number of contiguous regions with
     * a larger size of those regions.
     * @param b <Board that it is evaluating at that state>
     * @return Returns the heuristic value on the board.
     */
    int sizeContig(Board b) {

        List<Integer> wp = b.getRegionSizes(WP);
        int wpsize = wp.size();
        List<Integer> bp = b.getRegionSizes(BP);
        int bpsize = bp.size();

        Collections.sort(wp);
        int wpmax = wp.get(wpsize - 1);
        Collections.sort(bp);
        int bpmax = bp.get(bpsize - 1);

        return ((bpsize - wpsize) + (wpmax - bpmax));
    }


    /**
     * Return a search depth for the current position.
     */
    private int chooseDepth() {
        return 3;
    }
  
    /**
     * Used to convey moves discovered by findMove.
     */
    private Move _foundMove;

}
