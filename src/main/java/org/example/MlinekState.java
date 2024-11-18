package org.example;

import sac.game.GameState;

import java.util.List;

public class MlinekState extends GameState {
    private static final int PLACE_NUMBER = 18;
    private final int[][] board;
    private int remainingWhite;
    private int remainingBlack;
    private int whiteToPlace;
    private int blackToPlace;
    public MlinekState() {
        board = new int[3][8];
        whiteToPlace = 9;
        blackToPlace = 9;
        remainingWhite = 9;
        remainingBlack = 9;
    }
    private MlinekState(MlinekState other) {
        this.board = new int[3][8];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(other.board[i], 0, this.board[i], 0, 8);

        }
        this.whiteToPlace = other.whiteToPlace;
        this.blackToPlace = other.blackToPlace;
        this.remainingWhite = other.remainingWhite;
        this.remainingBlack = other.remainingBlack;
        this.setMaximizingTurnNow(other.isMaximizingTurnNow());
    }

    private boolean isMlinek(int i, int j) {
        int pos = board[i][j];
        if (pos == 0) return false;

        boolean millHorizontal = (board[i][(j + 1) % 8] == pos && board[i][(j + 2) % 8] == pos) ||
                (board[i][(j + 7) % 8] == pos && board[i][(j + 6) % 8] == pos);

        boolean millVertical = (i == 0 && board[1][j] == pos && board[2][j] == pos) ||
                (i == 1 && board[0][j] == pos && board[2][j] == pos) ||
                (i == 2 && board[0][j] == pos && board[1][j] == pos);

        return millHorizontal || millVertical;
    }

    private List<GameState> resolveIdk() {

    }

//    @Override
//    public List<GameState> generateChildren() {
//
//    }

    @Override
    public boolean isNonWinTerminal() {
        return generateChildren().isEmpty();
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                sb.append(board[i][j] == 1 ? "W" : board[i][j] == -1 ? "B" : ".");
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
