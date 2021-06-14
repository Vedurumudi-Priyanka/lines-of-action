package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/**
 * Represents the state of a game of Lines of Action.
 
 */
class Board {

    /**
     * Default number of moves for each side that results in a draw.
     */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /**
     * Pattern describing a valid square designator (cr).
     */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");


    /**
     * A Board whose initial contents are taken from INITIALCONTENTS
     * and in which the player playing TURN is to move. The resulting
     * Board has
     * get(col, row) == INITIALCONTENTS[row][col]
     * Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     * <p>
     * CAUTION: The natural written notation for arrays initializers puts
     * the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /**
     * A new board in the standard initial position.
     */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /**
     * A Board whose initial contents and state are copied from
     * BOARD.
     */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /**
     * Set my state to CONTENTS with SIDE to move.
     */
    void initialize(Piece[][] contents, Piece side) {
        for (int i = 0; i < contents.length; i++) {
            for (int j = 0; j < contents[i].length; j++) {
                Piece piece = contents[i][j];
                Square sq = sq(j, i);
                set(sq, piece);
            }
        }
        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
        _subsetsInitialized = false;
    }

    /**
     * Set me to the initial configuration.
     */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /**
     * Set my state to a copy of BOARD.
     */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _winner = board._winner;
        _winnerKnown = board._winnerKnown;
        _moveLimit = board._moveLimit;
        _turn = board._turn;
        _subsetsInitialized = board._subsetsInitialized;
        _moves.addAll(board._moves);
        _blackRegionSizes.addAll(board._blackRegionSizes);
        _whiteRegionSizes.addAll(board._whiteRegionSizes);
        System.arraycopy(board._board, 0, _board, 0, _board.length);

    }

    /**
     * Return the contents of the square at SQ.
     */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /**
     * Set the square at SQ to V and set the side that is to move next
     * to NEXT, if NEXT is not null.
     */
    void set(Square sq, Piece v, Piece next) {
        int ind = sq.index();
        _board[ind] = v;
        if (next != null) {
            _turn = next;
        }
        _subsetsInitialized = false;
        _winnerKnown = false;
    }

    /**
     * Set the square at SQ to V, without modifying the side that
     * moves next.
     */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /**
     * Set limit on number of moves by each side that results in a tie to
     * LIMIT, where 2 * LIMIT > movesMade().
     */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /**
     * Assuming isLegal(MOVE), make MOVE. Assumes MOVE.isCapture()
     * is false. If it saves the move for later retraction,
     * makeMove itself uses MOVE.captureMove() to produce
     * the capturing move.
     */
    void makeMove(Move move) {
        if (!isLegal(move)) {
            return;
        }
        Square from = move.getFrom();
        Piece pFrom = _board[from.index()];
        set(from, EMP);

        Square to = move.getTo();
        Piece pTo = _board[to.index()];
        if (pTo == _turn.opposite()) {
            move = move.captureMove();
        }

        set(to, pFrom);
        _moves.add(move);

        _turn = _turn.opposite();

    }


    /**
     * Retract (unmake) one move, returning to the state immediately before
     * that move.  Requires that movesMade () > 0.
     */
    void retract() {
        if (movesMade() <= 0) {
            return;
        }

        Move lastMove = _moves.get(_moves.size() - 1);
        Piece pTo = _board[lastMove.getTo().index()];
        Square sqTo = lastMove.getTo();
        Square sqFrom = lastMove.getFrom();
        if (lastMove.isCapture() && sqTo != null && pTo.opposite() != null) {
            set(sqTo, pTo.opposite());
        } else {
            set(sqTo, EMP);
        }
        set(sqFrom, pTo);

        _moves.remove(_moves.size() - 1);
        _turn = turn().opposite();
    }

    /**
     * Return the Piece representing who is next to move.
     */
    Piece turn() {
        return _turn;
    }

    /**
     * Finding a certain number of pieces in a direction
     * in order to calculate the number of legal steps
     * a piece can take.
     * @param from <from the square to number of steps>
     * @param dir <direction being passed to use for moveDest>
     * @return Number of pieces in a certain direction.
     */
    int piecesDir(Square from, int dir) {
        ArrayList<Square> visitedsq = new ArrayList<>();
        int steps = 1;
        int numPieces = 0;
        Square smoveDest = from.moveDest(dir, steps);
        while (smoveDest != null) {
            if (_board[smoveDest.index()] != EMP
                    && !visitedsq.contains(smoveDest)) {
                numPieces++;
            }
            visitedsq.add(smoveDest);
            smoveDest = from.moveDest(dir, steps++);

        }
        return numPieces;
    }

    /**
     * A function that returns the opposite direction.
     * Helper function for makeMove to account
     * @param dir <direction that I want to find opposite of>
     * @return Opposite direction of DIR parameter.
     */
    int oppdirection(int dir) {
        int ans = 8;
        if (dir <= 3 && dir >= 0) {
            ans = dir + 4;
        } else if (dir >= 4 && dir <= 7) {
            ans = dir - 4;
        }
        return ans;
    }

    /**
     * Return true iff FROM - TO is a legal move for the player currently on
     * move.
     */
    boolean isLegal(Square from, Square to) {
        boolean legalmovebool = false;
        int dir = from.direction(to);
        int numPieces = piecesDir(from, dir)
                + piecesDir(from, oppdirection(dir)) + 1;
        Move mToFrom = Move.mv(from, to);
        int steps = mToFrom.length();
        if (steps == numPieces && !blocked(from, to)) {
            legalmovebool = true;
        }
        return legalmovebool;
    }

    /**
     * Return true iff MOVE is legal for the player currently on move.
     * The isCapture() property is ignored.
     */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /**
     * Return a sequence of all legal moves from this position.
     */
    List<Move> legalMoves() {
        ArrayList<Move> alllegal = new ArrayList<>();
        ArrayList<Square> turnSquares = new ArrayList<>();
        for (int i = 0; i < ALL_SQUARES.length; i++) {
            if (_board[i] == _turn) {
                turnSquares.add(ALL_SQUARES[i]);
            }
        }

        for (Square sFrom : turnSquares) {
            for (int dir = 0; dir <= 3; dir++) {
                int steps = piecesDir(sFrom, dir)
                        + piecesDir(sFrom, oppdirection(dir)) + 1;
                Square frontsq = sFrom.moveDest(dir, steps);
                if (frontsq != null) {
                    if (isLegal(sFrom, frontsq)) {
                        Move movefront = Move.mv(sFrom, frontsq);
                        alllegal.add(movefront);
                    }
                }
                Square backsq = sFrom.moveDest(oppdirection(dir), steps);
                if (backsq != null && steps != 1) {
                    if (isLegal(sFrom, backsq)) {
                        Move moveback = Move.mv(sFrom, backsq);
                        alllegal.add(moveback);
                    }

                }
            }
        }
        return alllegal;
    }

    /**
     * Return true iff the game is over (either player has all his
     * pieces continguous or there is a tie).
     */
    boolean gameOver() {
        return winner() != null;
    }

    /**
     * Return true iff SIDE's pieces are continguous.
     */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }


    /**
     * Return the winning side, if any.  If the game is not over, result is
     * null.  If the game has ended in a tie, returns EMP.
     */
    Piece winner() {
        if (!_winnerKnown) {
            if (piecesContiguous(turn())
                    && piecesContiguous(turn().opposite())) {
                _winner = turn().opposite();
                _winnerKnown = true;
            } else if (piecesContiguous(turn())) {
                _winner = turn();
                _winnerKnown = true;
            } else if (piecesContiguous(turn().opposite())) {
                _winner = turn().opposite();
                _winnerKnown = true;
            } else if (_moveLimit <= movesMade()) {
                _winner = EMP;
                _winnerKnown = true;
            } else {
                _winner = null;
                _winnerKnown = false;
            }
        }
        return _winner;
    }

    /**
     * Return the total number of moves that have been made (and not
     * retracted).  Each valid call to makeMove with a normal move increases
     * this number by 1.
     */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /**
     * Return true if a move from FROM to TO is blocked by an opposing
     * piece or by a friendly piece on the target square.
     */
    private boolean blocked(Square from, Square to) {

        Move blockmv = Move.mv(from, to);
        int numsteps = blockmv.length();
        int dir = from.direction(to);
        for (int step = 0; step < numsteps; step++) {
            Square sqmv = from.moveDest(dir, step);
            if (sqmv != null && _board[sqmv.index()] == _turn.opposite()) {
                return true;
            }
        }
        return _board[to.index()] == _turn;
    }

    /**
     * Return the size of the as-yet unvisited cluster of squares
     * containing P at and adjacent to SQ.  VISITED indicates squares that
     * have already been processed or are in different clusters.  Update
     * VISITED to reflect squares counted.
     */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        if (p == EMP) {
            return 0;
        }
        if (_board[sq.index()] != p) {
            return 0;
        }
        if (visited[sq.col()][sq.row()]) {
            return 0;
        }
        int counter = 1;
        visited[sq.col()][sq.row()] = true;
        for (Square adjSq : sq.adjacent()) {
            counter += numContig(adjSq, visited, p);
        }
        return counter;
    }

    /**
     * Set the values of _whiteRegionSizes and _blackRegionSizes.
     */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (boolean[] booleans : visited) {
            Arrays.fill(booleans, false);
        }
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece contigP = _board[sq(i, j).index()];
                int numContigfunc = numContig(sq(i, j), visited, contigP);

                if (contigP == BP && numContigfunc != 0) {
                    _blackRegionSizes.add(numContigfunc);

                }

                if (contigP == WP && numContigfunc != 0) {
                    _whiteRegionSizes.add(numContigfunc);
                }
            }
        }
        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /**
     * Return the sizes of all the regions in the current union-find
     * structure for side S.
     */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }


    /**
     * The standard initial configuration for Lines of Action (bottom row
     * first).
     */
    static final Piece[][] INITIAL_PIECES = {
            {EMP, BP, BP, BP, BP, BP, BP, EMP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {WP, EMP, EMP, EMP, EMP, EMP, EMP, WP},
            {EMP, BP, BP, BP, BP, BP, BP, EMP}
    };

    /**
     * Current contents of the board.  Square S is at _board[S.index()].
     */
    private final Piece[] _board = new Piece[BOARD_SIZE * BOARD_SIZE];

    /**
     * List of all unretracted moves on this board, in order.
     */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /**
     * Current side on move.
     */
    private Piece _turn;
    /**
     * Limit on number of moves before tie is declared.
     */
    private int _moveLimit;
    /**
     * True iff the value of _winner is known to be valid.
     */
    private boolean _winnerKnown;
    /**
     * Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     * in progress).  Use only if _winnerKnown.
     */
    private Piece _winner;

    /**
     * True iff subsets computation is up-to-date.
     */
    private boolean _subsetsInitialized;

    /**
     * List of the sizes of continguous clusters of pieces, by color.
     */
    private final ArrayList<Integer> _whiteRegionSizes = new ArrayList<>(),
            _blackRegionSizes = new ArrayList<>();
}
