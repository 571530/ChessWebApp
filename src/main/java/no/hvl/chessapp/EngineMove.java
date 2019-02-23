package no.hvl.chessapp;

import chesspresso.move.Move;

import java.util.ArrayList;

public class EngineMove {
    private short move;
    private double val = 0;
    private ArrayList<EngineMove> moves = null;
    private boolean tested = false;

    public EngineMove(short move, double val, ArrayList<EngineMove> moves) {
        this.move = move;
        this.val = val;
        this.moves = moves;
    }

    public EngineMove(short move, double val) {
        this.move = move;
        this.val = val;
    }

    public short getMove() {
        return move;
    }

    public void setMove(short move) {
        this.move = move;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public ArrayList<EngineMove> getMoves() {
        return moves;
    }

    public void setMoves(ArrayList<EngineMove> moves) {
        this.moves = moves;
    }

    public boolean isTested() {
        return tested;
    }

    public void setTested(boolean tested) {
        this.tested = tested;
    }

}
