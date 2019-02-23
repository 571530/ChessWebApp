package no.hvl.chessapp;

import chesspresso.move.Move;

public class ChessGame {
    private ChessEngine engine;


    public ChessGame(String difficulty) {
        int iterations = ChessEngineConfig.MEDIUM_ITERATIONS;

        if ("easy".equals(difficulty)) {
            iterations = ChessEngineConfig.EASY_ITERATIONS;
        }
        else if ("medium".equals(difficulty)) {
            iterations = ChessEngineConfig.MEDIUM_ITERATIONS;
        }
        else if ("hard".equals(difficulty)) {
            iterations = ChessEngineConfig.HARD_ITERATIONS;
        }

        engine = new ChessEngine(iterations, ChessEngineConfig.PICK_DELTA, ChessEngineConfig.TEST_DELTA);
    }

    public String getFEN() {
        return engine.getFEN();
    }

    public Move makeComputerMove() {
        return engine.makeMove();
    }

    public void makePlayerMove(String move) {
        engine.makePlayerMove(move);
    }
}
