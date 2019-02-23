package no.hvl.chessapp;

import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;
import com.sun.xml.internal.ws.api.pipe.Engine;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static jdk.nashorn.internal.objects.Global.Infinity;

public class ChessEngine {
    private Random random = new Random();
    private Position position = null;
    private ArrayList<EngineMove> moves;
    private int positionsTested = 0;

    private int ITERATIONS = 1000;
    private double PICK_DELTA = 30;
    private double TEST_DELTA = 300;


    public ChessEngine(int iterations, double pick_delta, double test_delta) {
        this.ITERATIONS = iterations;
        this.PICK_DELTA = pick_delta;
        this.TEST_DELTA = test_delta;

        position = Position.createInitialPosition();
    }

    public ChessEngine() {
        position = Position.createInitialPosition();
    }

    public void setFEN(String fen) {
        position = new Position(fen);
    }

    /**
     * @param val, intial value for the moves created
     * @return
     */
    public ArrayList<EngineMove> createMoves(double val) {
        ArrayList<EngineMove> moves = new ArrayList<>();
        for (short move : position.getAllMoves()) {
            moves.add(new EngineMove(move, val));
        }

        return moves;
    }

    /**
     * Creates moves with val 0
     *
     * @return
     */
    public ArrayList<EngineMove> createMoves() {
        return createMoves(0);
    }

    /**
     * Sort a list of moves either descending or ascending based on current turn
     *
     * @param moves to be sorted
     */
    public void sortMoves(ArrayList<EngineMove> moves) {
        if (position.getToPlay() == Chess.WHITE) {
            Collections.sort(moves, (a, b) ->
                    Double.compare(b.getVal(), a.getVal())
            );
        } else {
            Collections.sort(moves, Comparator.comparingDouble(EngineMove::getVal));
        }
    }

    /**
     * Returns the winner of the game, black or white if win, draw if draw and none if the game is not over
     *
     * @return
     */
    public String getWinner() {
        if (position.isTerminal()) {
            if (position.getToPlay() == Chess.WHITE) {
                return "black";
            } else if (position.getToPlay() == Chess.BLACK) {
                return "white";
            } else {
                return "draw";
            }
        }
        return "none";
    }

    /**
     * Picks a move from given list with which can be delta value away from the best
     *
     * @param moves to pick from
     * @param delta to use for picking
     * @return move picked
     */
    public EngineMove pickMove(ArrayList<EngineMove> moves, double delta) {
        boolean whitesTurn = position.getToPlay() == Chess.WHITE;

        double val = moves.get(0).getVal();

        int i = 1;

        for (; i < moves.size(); i++) {
            if (whitesTurn && moves.get(i).getVal() + delta < val) {
                break;
            } else if (!whitesTurn && moves.get(i).getVal() - delta > val) {
                break;
            }
        }

        return moves.get(random.nextInt(i));
    }

    /**
     * Makes a move from the current position
     *
     * @return mode which was made
     */
    public Move makeMove() {
        if (moves == null) {
            moves = createMoves();
        }

        if (this.moves.size() == 0)
            return null;

        this.think();

        EngineMove decision = pickMove(moves, PICK_DELTA);

        try {
            position.doMove(decision.getMove());
            moves = decision.getMoves();
        } catch (IllegalMoveException e) {
            e.printStackTrace();
        }

        return position.getLastMove();
    }

    /**
     *
     */
    private void think() {
        long before = System.currentTimeMillis();
        testAllMoves(moves);
        testAllMoves(moves);

        sortMoves(moves);

        for (int i = 0; i < ITERATIONS; i++) {
            testMove(pickMove(moves, TEST_DELTA));

            sortMoves(moves);
        }
        long after = System.currentTimeMillis();
        System.out.println(positionsTested + " positions tested, best value found: " + moves.get(0).getVal() + " time: " + (after - before) + " ms");
        positionsTested = 0;
        //printMoves(moves, 0);
    }

    private void printMoves(ArrayList<EngineMove> moves, int n) {
        String whitespace = new String(new char[n]).replace("\0", " ");
        for (EngineMove move : moves) {
            System.out.println(whitespace + move.getVal() + " " + moveToText(move.getMove()));
            if (move.getMoves() != null) {
                printMoves(move.getMoves(), n + 4);
            }
        }
    }

    /**
     * Tests all moves in a given list
     *
     * @param moves, list of moves to be tested
     */
    private void testAllMoves(ArrayList<EngineMove> moves) {
        for (EngineMove move : moves) {
            testMove(move);
        }
    }

    /**
     * Tests a given move,
     * if mate, infinity
     * if stalemate, 0
     * if move is already tested, test a variation of the move and update score of current move
     * gives a move a score depending on the material and dominating, TODO: king protection
     *
     * @param move
     */
    private void testMove(EngineMove move) {
        try {
            position.doMove(move.getMove());
        } catch (IllegalMoveException e) {
            e.printStackTrace();
        }

        boolean whitesTurn = position.getToPlay() == Chess.WHITE;

        if (position.isMate()) {
            move.setVal(Infinity * (whitesTurn ? -1 : 1));
            move.setTested(true);
        } else if (position.isStaleMate()) {
            move.setVal(0);
            move.setTested(true);
        } else if (move.isTested()) {
            if (move.getMoves() == null) {
                move.setMoves(createMoves(move.getVal()));
                testAllMoves(move.getMoves());
            } else {
                testMove(pickMove(move.getMoves(), TEST_DELTA));
            }
            sortMoves(move.getMoves());
            move.setVal(move.getMoves().get(0).getVal());
        } else {
            positionsTested++;
            move.setVal((position.getMaterial() + position.getDomination()) * (!whitesTurn ? -1 : 1));
            move.setTested(true);
        }

        position.undoMove();
    }

    /**
     * Return a move as text
     *
     * @param move
     * @return text version of move
     */
    public String moveToText(short move) {
        try {
            position.doMove(move);
        } catch (IllegalMoveException e) {
            e.printStackTrace();
        }

        String moveString = position.getLastMove().toString();

        position.undoMove();

        return moveString;
    }

    public String getFEN() {
        return position.getFEN();
    }

    public void setIterations(int iterations) {
        this.ITERATIONS = iterations;
    }

    public void setTestDelta(double test_delta) {
        this.TEST_DELTA = test_delta;
    }

    public void setPickDelta(double pick_delta) {
        this.PICK_DELTA = pick_delta;
    }

    public void makePlayerMove(String playermove) {
        if (moves == null) {
            moves = createMoves();
        }

        for (EngineMove move : moves) {
            if (moveToText(move.getMove()).equals(playermove)) {
                try {
                    position.doMove(move.getMove());
                    moves = move.getMoves();
                } catch (IllegalMoveException e) {
                    e.printStackTrace();
                }

                break;
            }
        }


    }
}
