package org.example;

import sac.game.GameState;
import sac.game.GameStateImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MlynekState extends GameStateImpl {
    private static final int TOTAL_PIECES = 9; // Кількість фішок кожного гравця
    private static final int BOARD_SIZE = 3; // Три квадрати по 8 полів
    private static final int FIELDS_PER_SQUARE = 8;

    private char[][] board = new char[BOARD_SIZE][FIELDS_PER_SQUARE];
    private int whiteRemaining = TOTAL_PIECES;
    private int blackRemaining = TOTAL_PIECES;
    private int whitePiecesOnBoard = 0;
    private int blackPiecesOnBoard = 0;
    private boolean maximizingTurnNow;

    public MlynekState() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < FIELDS_PER_SQUARE; j++) {
                board[i][j] = '+';
            }
        }
        maximizingTurnNow = true; // Починають білі
    }

    public MlynekState(MlynekState other) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(other.board[i], 0, this.board[i], 0, FIELDS_PER_SQUARE);
        }
        this.whiteRemaining = other.whiteRemaining;
        this.blackRemaining = other.blackRemaining;
        this.whitePiecesOnBoard = other.whitePiecesOnBoard;
        this.blackPiecesOnBoard = other.blackPiecesOnBoard;
        this.maximizingTurnNow = other.maximizingTurnNow;
    }

    private boolean isMill(int square, int field) {
        char player = board[square][field];
        if (player == '+') return false;

        if (field % 2 == 0) {
            int prev1 = (field + FIELDS_PER_SQUARE - 1) % FIELDS_PER_SQUARE;
            int prev2 = (field + FIELDS_PER_SQUARE - 2) % FIELDS_PER_SQUARE;
            int next1 = (field + 1) % FIELDS_PER_SQUARE;
            int next2 = (field + 2) % FIELDS_PER_SQUARE;

            return (board[square][prev1] == player && board[square][prev2] == player) ||
                    (board[square][next1] == player && board[square][next2] == player);
        } else {
            int nextSquare = (square + 1) % BOARD_SIZE;
            int prevSquare = (square + BOARD_SIZE - 1) % BOARD_SIZE;

            return board[nextSquare][field] == player && board[prevSquare][field] == player;
        }
    }

    @Override
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();
        if (whiteRemaining > 0 || blackRemaining > 0) {
            generateChildrenPhaseOne(children);
        } else {
            generateChildrenPhaseTwoOrThree(children);
        }
        return children;
    }

    private void generateChildrenPhaseOne(List<GameState> children) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < FIELDS_PER_SQUARE; j++) {
                if (board[i][j] == '+') {
                    MlynekState child = new MlynekState(this);
                    child.board[i][j] = maximizingTurnNow ? 'W' : 'B';
                    if (maximizingTurnNow) {
                        child.whiteRemaining--;
                        child.whitePiecesOnBoard++;
                    } else {
                        child.blackRemaining--;
                        child.blackPiecesOnBoard++;
                    }
                    child.maximizingTurnNow = !this.maximizingTurnNow;
                    child.setMoveName("Place at (" + i + "," + j + ")");
                    children.add(child);
                }
            }
        }
    }

    private void generateChildrenPhaseTwoOrThree(List<GameState> children) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < FIELDS_PER_SQUARE; j++) {
                if (board[i][j] == (maximizingTurnNow ? 'W' : 'B')) {
                    for (int[] move : getValidMoves(i, j)) {
                        int newSquare = move[0];
                        int newField = move[1];
                        MlynekState child = new MlynekState(this);
                        child.board[i][j] = '+';
                        child.board[newSquare][newField] = maximizingTurnNow ? 'W' : 'B';
                        child.maximizingTurnNow = !this.maximizingTurnNow;
                        child.setMoveName("Move from (" + i + "," + j + ") to (" + newSquare + "," + newField + ")");
                        children.add(child);
                    }
                }
            }
        }
    }

    private List<int[]> getValidMoves(int square, int field) {
        List<int[]> moves = new ArrayList<>();
        int[] neighbors = {1, -1};
        for (int neighbor : neighbors) {
            int newField = (field + neighbor + FIELDS_PER_SQUARE) % FIELDS_PER_SQUARE;
            if (board[square][newField] == '+') {
                moves.add(new int[]{square, newField});
            }
        }
        if (field % 2 != 0) {
            int nextSquare = (square + 1) % BOARD_SIZE;
            int prevSquare = (square + BOARD_SIZE - 1) % BOARD_SIZE;
            if (board[nextSquare][field] == '+') moves.add(new int[]{nextSquare, field});
            if (board[prevSquare][field] == '+') moves.add(new int[]{prevSquare, field});
        }
        return moves;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Верхній рядок (зовнішній квадрат)
        sb.append("\n");

        sb.append(board[0][6]).append("--------------").append(board[0][5]).append("--------------").append(board[0][4]).append("  7\n");
        sb.append("|              |              |\n");
        sb.append("|    ").append(board[1][6]).append("---------").append(board[1][5]).append("---------").append(board[1][4]).append("    |  6\n");
        sb.append("|    |         |         |    |\n");
        sb.append("|    |    ").append(board[2][6]).append("----").append(board[2][5]).append("----").append(board[2][4]).append("    |    |  5\n");
        sb.append("|    |    |         |    |    |\n");
        sb.append(board[0][7]).append("----").append(board[1][7]).append("----").append(board[2][7]).append("         ").append(board[2][3]).append("----").append(board[1][3]).append("----").append(board[0][3]).append("  4\n");
        sb.append("|    |    |         |    |    |\n");
        sb.append("|    |    ").append(board[2][0]).append("----").append(board[2][1]).append("----").append(board[2][2]).append("    |    |  3\n");
        sb.append("|    |         |         |    |\n");
        sb.append("|    ").append(board[1][0]).append("---------").append(board[1][1]).append("---------").append(board[1][2]).append("    |  2\n");
        sb.append("|              |              |\n");
        sb.append(board[0][0]).append("--------------").append(board[0][1]).append("--------------").append(board[0][2]).append("  1\n");
                sb.append("a    b    c    d    e    f    g\n");

        sb.append("\n");
        sb.append("White remaining: ").append(whiteRemaining).append(", Black remaining: ").append(blackRemaining).append("\n");
        sb.append("White on board: ").append(whitePiecesOnBoard).append(", Black on board: ").append(blackPiecesOnBoard).append("\n");
        sb.append("Next move: ").append(maximizingTurnNow ? "White" : "Black").append("\n");

        return sb.toString();
    }



    @Override
    public int hashCode() {
        int result = Objects.hash(whiteRemaining, blackRemaining, whitePiecesOnBoard, blackPiecesOnBoard, maximizingTurnNow);
        result = 31 * result + Arrays.deepHashCode(board);
        return result;
    }
}
