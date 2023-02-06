package tablut;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;

import static tablut.Piece.*;
import static tablut.Square.*;


/**
 * The state of a Tablut Game.
 *
 * @author Osvaldo Valadez
 */
class Board {
    /** Return the 2-d array. */
    private Piece[][] _board = new Piece[SIZE][SIZE];

    /** Return the history of the board. */
    private Stack<String> history;

    /** Return  the move limit. */
    private int moveLimit;

    /**
     * The number of squares on a side of the board.
     */
    static final int SIZE = 9;

    /**
     * The throne (or castle) square and its four surrounding squares..
     */
    static final Square THRONE = sq(4, 4),
            NTHRONE = sq(4, 5),
            STHRONE = sq(4, 3),
            WTHRONE = sq(3, 4),
            ETHRONE = sq(5, 4);

    /**
     * Initial positions of attackers.
     */
    static final Square[] INITIAL_ATTACKERS = {
            sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
            sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
            sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
            sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /**
     * Initial positions of defenders of the king.
     */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** @return if the king is in its territory. */
    boolean isThroneSurrounded() {
        if (_board[4][4] == KING) {
            int count = 0;
            if (_board[4][5] == BLACK) {
                count += 1;
            }
            if (_board[4][3] == BLACK) {
                count += 1;
            }
            if (_board[5][4] == BLACK) {
                count += 1;
            }
            if (_board[3][4] == BLACK) {
                count += 1;
            }
            if (count == 3) {
                return true;
            }
        }
        return false;
    }
    /** @return if the throne is hostile.*/
    boolean hostileThrone() {
        if (_board[4][4] == EMPTY) {
            return true;
        }
        return false;


    }

    /**
     * Initializes a game board with SIZE squares on a side in the
     * initial position.
     */
    Board() {
        history = new Stack<>();
        _winner = null;
        _turn = BLACK;
        init();

    }


    /**
     * Initializes a copy of MODEL.
     */
    Board(Board model) {
        copy(model);
    }

    /**
     * Copies MODEL into me.
     */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        this._board = model._board;
        this.history = model.history;
        this._turn = model._turn;
        this._moveCount = model._moveCount;
        this.moveLimit = model.moveLimit;
    }

    /**
     * Clears the board to the initial position.
     */
    void init() {
        history = new Stack<>();
        _turn = BLACK;
        _winner = null;
        _moveCount = 0;
        Piece king = KING;
        _board[4][4] = king;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                Piece empty = EMPTY;
                _board[x][y] = empty;

            }
        }
        _board[4][4] = king;
        for (Square i : INITIAL_ATTACKERS) {
            int xCoord = i.col();
            int yCoord = i.row();
            Piece black = BLACK;
            _board[xCoord][yCoord] = black;

        }
        for (Square i : INITIAL_DEFENDERS) {
            int xCoord = i.col();
            int yCoord = i.row();
            Piece white = WHITE;
            _board[xCoord][yCoord] = white;
        }
        clearUndo();
    }


    /**
     * Set the move limit toLIM. @param n Itis an error if 2*LIM <= moveCount().
     */
    void setMoveLimit(int n) {
        if ((2 * n) <= moveCount()) {
            throw new AssertionError("Limit * 2 cannot be < movecount()");
        }
        moveLimit = n;

    }

    /**
     * Return a Piece representing whose move it is (WHITE or BLACK).
     */
    Piece turn() {
        return _turn;
    }

    /**
     * Return the winner in the current position, or null if there is no winner
     * yet.
     */
    Piece winner() {
        return _winner;
    }

    /**
     * Returns true iff this is a win due to a repeated position.
     */
    boolean repeatedPosition() {
        return _repeated;
    }

    /**
     * Record current position and set winner() next mover if the current
     * position is a repeat.
     */
    private void checkRepeated() {
        String current = encodedBoard();
        if (history.empty()) {
            history.push(current);
        } else if (history.contains(current)) {
            _winner = _turn;
        } else {
            history.push(encodedBoard());
        }

    }

    /**
     * Return the number of moves since the initial position that have not been
     * undone.
     */
    int moveCount() {
        return _moveCount;
    }

    /**
     * Return location of the king.
     */
    Square kingPosition() {
        Square returnSq = null;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (_board[x][y] == KING) {
                    returnSq = sq(x, y);
                }
            }
        }
        return returnSq;
    }

    /**
     * Return the contents the square at S.
     */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /**
     * Return the contents of the square at (COL, ROW), where
     * 0 <= COL, ROW <= 9.
     */
    final Piece get(int col, int row) {
        return _board[col][row];
    }

    /**
     * Return the contents of the square at COL ROW.
     */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /**
     * Set square S to P.
     */
    final void put(Piece p, Square s) {
        _board[s.col()][s.row()] = p;
    }

    /**
     * Set square S to P and record for undoing.
     */
    final void revPut(Piece p, Square s) {
        put(p, s);

    }

    /**
     * Set square COL ROW to P.
     */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /**
     * Return true iff FROM - TO is an unblocked rook move on the current
     * board.  For this to be true, FROM-TO must be a rook move and the
     * squares along it, other than FROM, must be empty.
     */
    boolean isUnblockedMove(Square from, Square to) {
        boolean legal = true;
        int sep = distanceFromTo(from, to);
        int direction = from.direction(to);
        if (direction == 0) {
            for (int i = 1; i <= sep; i++) {
                if (_board[from.col()][from.row() + i] != EMPTY) {
                    legal = false;
                    break;
                }
            }
        } else if (direction == 1) {
            for (int i = 1; i <= sep; i++) {
                if (_board[from.col() + i][from.row()] != EMPTY) {
                    legal = false;
                    break;
                }
            }
        } else if (direction == 2) {
            for (int i = 1; i <= sep; i++) {
                if (_board[from.col()][from.row() - i] != EMPTY) {
                    legal = false;
                    break;
                }
            }
        } else if (direction == 3) {
            for (int i = 1; i <= sep; i++) {
                if (_board[from.col() - i][from.row()] != EMPTY) {
                    legal = false;
                    break;
                }
            }
        }
        return legal;
    }

    /**
     * Return true iff FROM is a valid starting square for a move.
     */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /**
     * Return true iff FROM-TO is a valid move.
     */
    boolean isLegal(Square from, Square to) {
        boolean legal = true;
        if (_board[from.col()][from.row()] == EMPTY) {
            legal = false;
        }
        if (_board[from.col()][from.row()] != KING && (to == THRONE)) {
            legal = false;
        }
        if (!isUnblockedMove(from, to)) {
            legal = false;
        }
        return legal;
    }
    /** Given @param from to hi @param to @return int.
     * @param to bye. */
    int distanceFromTo(Square from, Square to) {
        int distance = 0;
        if (from.col() != to.col() && from.row() != to.row()) {
            throw new AssertionError("Not valid move");
        } else if (from.col() == to.col() && from.row() != to.row()) {
            distance = Math.abs(from.row() - to.row());
        } else if (from.row() == to.row() && from.col() != to.col()) {
            distance = Math.abs(from.col() - to.col());
        }
        return distance;
    }

    /**
     * Return true iff MOVE is a legal move in the current
     * position.
     */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /**
     * Move FROM-TO, assuming this is a legal move.
     */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        if (_turn == _board[from.col()][from.row()]
                || _turn == _board[from.col()][from.row()].ally()) {
            if (_winner == null) {
                put(_board[from.col()][from.row()], to);
                _board[from.col()][from.row()] = EMPTY;
                _turn = _board[to.col()][to.row()].opponent();
                captureUp(to);
                captureDown(to);
                captureLeft(to);
                captureRight(to);
                checkRepeated();
                _moveCount += 1;
                if (_moveCount > moveLimit && moveLimit != 0) {
                    _winner = _turn;
                }
            }

        }


    }

    /** @param up captures up */
    void captureUp(Square up) {
        if (SIZE - up.row() > 2) {
            if (_board[up.col()][up.row()] == BLACK
                    && _board[up.col()][up.row() + 1] == KING
                    && checkFourWayCapture()) {
                kingFourWayCapture();
            }
            if (_board[up.col()][up.row()] == BLACK
                    && _board[up.col()][up.row() + 1] == WHITE && up.col() == 4
                    && up.row() + 2 == 4 && isThroneSurrounded()) {
                capture(up, sq(4, 4));
            }
            if (_board[up.col()][up.row() + 1]
                    == _board[up.col()][up.row()].opponent()
                    || _board[up.col()][up.row() + 1]
                    == _board[up.col()][up.row()].opponent().ally()) {
                if (_board[up.col()][up.row() + 2]
                        == _board[up.col()][up.row()]
                        || (_board[up.col()][up.row() + 2]
                        == _board[up.col()][up.row()].ally())
                        || (up.col() == 4 && up.row() + 2
                        == 4 && hostileThrone())) {
                    if (_board[up.col()][up.row() + 1] == KING) {
                        if (!checkFourWayCapture()) {
                            capture(up, sq(up.col(), up.row() + 2));
                            _winner = BLACK;
                        }
                    } else {
                        capture(up, sq(up.col(), up.row() + 2));
                    }
                }
            }
        }
    }
    /** @param down captures down. */
    void captureDown(Square down) {
        if (down.row() >= 2) {
            if (_board[down.col()][down.row()] == BLACK
                    && _board[down.col()][down.row() - 1] == KING
                    && checkFourWayCapture()) {
                kingFourWayCapture();
            }
            if (_board[down.col()][down.row()] == BLACK
                    && _board[down.col()][down.row() - 1] == WHITE
                    && down.col() == 4
                    && down.row() - 2 == 4 && isThroneSurrounded()) {
                capture(down, sq(4, 4));
            }
            if (_board[down.col()][down.row() - 1]
                    == _board[down.col()][down.row()].opponent()
                    || _board[down.col()][down.row() - 1]
                    == _board[down.col()][down.row()].opponent().ally()) {
                if (_board[down.col()][down.row() - 2]
                        == _board[down.col()][down.row()]
                        || (_board[down.col()][down.row() - 2]
                        == _board[down.col()][down.row()].ally())
                        || (down.col() == 4 && down.row() - 2
                        == 4 && hostileThrone())) {
                    if (_board[down.col()][down.row() - 1] == KING) {
                        if (!checkFourWayCapture()) {
                            capture(down, sq(down.col(), down.row() - 2));

                        }
                    } else {
                        capture(down, sq(down.col(), down.row() - 2));
                    }
                }
            }
        }
    }
    /** @param right captures up. */
    void captureRight(Square right) {
        if (SIZE - right.col() > 2) {
            if (_board[right.col()][right.row()]
                    == BLACK && _board[right.col() + 1][right.row()]
                    == KING && checkFourWayCapture()) {
                kingFourWayCapture();
            }
            if (_board[right.col()][right.row()] == BLACK
                    && _board[right.col() + 1][right.row()] == WHITE
                    && right.col() + 2 == 4
                    && right.row() == 4 && isThroneSurrounded()) {
                capture(right, sq(4, 4));
            }
            if (_board[right.col() + 1][right.row()]
                    == _board[right.col()][right.row()].opponent()
                    || _board[right.col() + 1][right.row()]
                    == _board[right.col()][right.row()].opponent().ally()) {
                if (_board[right.col() + 2][right.row()]
                        == _board[right.col()][right.row()]
                        || (_board[right.col() + 2][right.row()]
                        == _board[right.col()][right.row()].ally())
                        || (right.col() + 2 == 4 && right.row()
                        == 4 && hostileThrone())) {
                    if (_board[right.col() + 1][right.row()] == KING) {
                        if (!checkFourWayCapture()) {
                            capture(right, sq(right.col() + 2, right.row()));
                            _winner = BLACK;
                        }
                    } else {
                        capture(right, sq(right.col() + 2, right.row()));
                    }
                }
            }
        }
    }
    /** @param left captures left.  */
    void captureLeft(Square left) {
        if (left.col() >= 2) {
            if (_board[left.col()][left.row()] == BLACK
                    && _board[left.col() - 1][left.row()] == KING
                    && checkFourWayCapture()) {
                kingFourWayCapture();
            } else if (_board[left.col()][left.row()] == BLACK
                    && _board[left.col() - 1][left.row()] == WHITE
                    && left.col() - 2 == 4
                    && left.row() == 4 && isThroneSurrounded()) {
                capture(left, sq(4, 4));
            }
            if (_board[left.col() - 1][left.row()]
                    == _board[left.col()][left.row()].opponent()
                    || _board[left.col() - 1][left.row()]
                    == _board[left.col()][left.row()].opponent().ally()) {
                if (_board[left.col() - 2][left.row()]
                        == _board[left.col()][left.row()]
                        || (_board[left.col() - 2][left.row()]
                        == _board[left.col()][left.row()].ally())
                        || (left.col() - 2 == 4 && left.row() == 4
                        && hostileThrone())) {
                    if (_board[left.col() - 1][left.row()] == KING) {
                        if (!checkFourWayCapture()) {
                            capture(left, sq(left.col() - 2, left.row()));
                            _winner = BLACK;

                        }
                    }
                    capture(left, sq(left.col() - 2, left.row()));
                }
            }
        }
    }

    /** @return boolean if king should be captured. */
    boolean checkFourWayCapture() {
        Square kingSquare = kingPosition();
        if ((kingSquare.col() == 4 && (kingSquare.row() == 3
                || kingSquare.row() == 4 || kingSquare.row() == 5))
                || (kingSquare.row() == 4 && (kingSquare.col() == 3
                || kingSquare.col() == 4 || kingSquare.col() == 5))) {
            return true;
        }
        return false;

    }

    /** @return boolean king is alive. */
    boolean kingAlive() {
        boolean found = false;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (_board[i][j] == KING) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }
    /** Capture the king. */
    void kingFourWayCapture() {
        Square kingSquare = kingPosition();
        if ((_board[kingSquare.col()][kingSquare.row() + 1] == BLACK
                || (kingSquare.col() == 4 && kingSquare.row() + 1 == 4))
                && (_board[kingSquare.col()][kingSquare.row() - 1] == BLACK
                || (kingSquare.col() == 4 && kingSquare.row() - 1 == 4))
                && (_board[kingSquare.col() + 1][kingSquare.row()] == BLACK
                || (kingSquare.col() + 1 == 4 && kingSquare.row() == 4))
                && ((_board[kingSquare.col() - 1][kingSquare.row()] == BLACK)
                || (kingSquare.col() - 1 == 4 && kingSquare.row() == 4))) {
            _board[kingSquare.col()][kingSquare.row()] = EMPTY;
            _winner = BLACK;
        }
    }

    /**
     * Move according to MOVE, assuming it is a legal move.
     */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /**
     * Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     * SQ0 and the necessary conditions are satisfied.
     */
    private void capture(Square sq0, Square sq2) {
        Square middle = sq0.between(sq2);
        _board[middle.col()][middle.row()] = EMPTY;
    }

    /**
     * Undo one move.  Has no effect on the initial board.
     */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            String code = history.pop();
            uncodeBoard(code);
            _moveCount--;
        }
    }

    /**
     * Remove record of current position in the set of positions encountered,
     * unless it is a repeated position or we are at the first move.
     */
    private void undoPosition() {
        _repeated = false;
    }

    /**
     * Clear the undo stack and board-position counts. Does not modify the
     * current position or win status.
     */
    void clearUndo() {
        history.clear();
    }

    /**
     * Return a new mutable list of all legal moves on the current board for
     * SIDE (ignoring whose turn it is at the moment).
     */
    List<Move> legalMoves(Piece side) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (get(i, j) == side || get(i, j) == side.ally()) {
                    int index = 1;
                    while ((j + index < SIZE) && get(i, j + index) == EMPTY) {
                        Move up = new Move(sq(i, j), sq(i, j + index));
                        moves.add(up);
                        index++;
                    }
                    index = 1;
                    while ((j - index >= 0) && get(i, j - index) == EMPTY) {
                        Move down = new Move(sq(i, j), sq(i, j - index));
                        moves.add(down);
                        index++;
                    }
                    index = 1;
                    while ((i + index < SIZE) && get(i + index, j) == EMPTY) {
                        Move right = new Move(sq(i, j), sq(i + index, j));
                        moves.add(right);
                        index++;
                    }
                    index = 1;
                    while ((i - index >= 0) && get(i - index, j) == EMPTY) {
                        Move left = new Move(sq(i, j), sq(i - index, j));
                        moves.add(left);
                        index++;
                    }
                }
            }
        }

        return moves;
    }

    /**
     * Return true iff SIDE has a legal move.
     */
    boolean hasMove(Piece side) {
        List listOfMoves = legalMoves(side);
        if (listOfMoves.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Return a text representation of this Board.  If COORDINATES, then row
     * and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /**
     * Return the locations of all pieces on SIDE.
     */
    private HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> locations = new HashSet<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (get(i, j) == side) {
                    locations.add(sq(i, j));
                }
            }
        }
        return locations;
    }

    /**
     * Return the contents of _board in the order of SQUARE_LIST as a sequence
     * of characters: the toString values of the current turn and Pieces.
     */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Given @param code uncode the board. */
    void uncodeBoard(String code) {
        int index = 1;
        char please = code.charAt(0);
        if (please == 'W') {
            _turn = WHITE;
        }
        if (please == 'B') {
            _turn = BLACK;
        }
        for (Square sq : SQUARE_LIST) {
            char bye = code.charAt(index);
            if (bye == 'W') {
                _board[sq.col()][sq.row()] = WHITE;
                index += 1;
            } else if (bye == 'B') {
                _board[sq.col()][sq.row()] = BLACK;
                index += 1;
            } else if (bye == 'K') {
                _board[sq.col()][sq.row()] = KING;
                index += 1;
            } else {
                _board[sq.col()][sq.row()] = EMPTY;
                index += 1;
            }
        }
    }


    /**
     * Piece whose turn it is (WHITE or BLACK).
     */
    private Piece _turn;
    /**
     * Cached value of winner on this board, or EMPTY if it has not been
     * computed.
     */
    private Piece _winner;

    /**
     * Number of (still undone) moves since initial position.
     */
    private int _moveCount;
    /**
     * True when current board is a repeated position (ending the game).
     */
    private boolean _repeated;

    /** @return Piece[][] return the board. */
    public Piece[][] returnBoard() {
        return _board;
    }
}
