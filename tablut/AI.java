package tablut;


import static tablut.Board.SIZE;

import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Osvaldo Valadez
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }

        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        int byebye = 0;
        int value = 2 * (int) Math.pow(board.SIZE, 3);
        if (sense == 1) {
            Move currentbest = null;
            int best = -INFTY;
            for (Move i : board.legalMoves(WHITE)) {
                byebye += 1;
                board.makeMove(i);
                int eval = findMove(board, depth - 1, false,
                        -1, alpha, beta);
                board.undo();
                if (eval > best) {
                    currentbest = i;
                    best = eval;
                }
                alpha = Math.max(alpha, best);
                if (beta <= alpha) {
                    break;
                }
                if (saveMove) {
                    _lastFoundMove = currentbest;
                }
            }
            return best;
        } else if (sense == -1) {
            Move currentbest = null;
            int best = INFTY;
            for (Move i : board.legalMoves(BLACK)) {
                byebye += 1;
                board.makeMove(i);
                int eval = findMove(board, depth - 1, false,
                        1, alpha, beta);
                board.undo();
                if (eval > best) {
                    currentbest = i;
                    best = eval;
                }
                beta = Math.min(beta, best);
                if (beta <= alpha) {
                    break;
                }
                if (saveMove) {
                    _lastFoundMove = currentbest;
                }
            }
            return best;
        }
        return 0;

    }




    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        return 1;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Piece[][] brd = board.returnBoard();
        int numWhite = 0;
        int numBlack = 0;
        boolean king = false;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (brd[i][j] == KING) {
                    king = true;
                }
                if (brd[i][j] == WHITE) {
                    numWhite++;
                } else if (brd[i][j] == BLACK) {
                    numBlack--;
                }
                if (king) {
                    return (numWhite + 7) - numBlack;
                }
            }
        }
        return numWhite - numBlack;
    }


}
