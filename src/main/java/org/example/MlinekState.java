package org.example;

import sac.game.GameState;
import sac.game.GameStateImpl;

import java.util.ArrayList;
import java.util.List;

public class MlinekState extends GameStateImpl {
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
        List<GameState> children = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == (isMaximizingTurnNow() ? -1 : 1) && !isMlinek(i, j)) {
                    MlinekState child = new MlinekState(this);
                    child.board[i][j] = 0;
                    children.add(child);
                }
            }
        }
        return children;
    }

    private List<GameState> generateInitialPhaseChildren() {
        List<GameState> children = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == 0) {
                    MlinekState child = new MlinekState(this);
                    child.board[i][j] = isMaximizingTurnNow() ? 1 : -1;
                    if (isMaximizingTurnNow()) {
                        child.whiteToPlace--;
                    } else {
                        child.blackToPlace--;
                    }
                    children.add(child);
                }
            }
        }
        return children;
    }

    private List<GameState> generateMidEndPhaseChildren() {
        List<GameState> children = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == (isMaximizingTurnNow() ? 1 : -1)) {
                    for (int di = -1; di <= 1; di++) {
                        for (int dj = -1; dj <= 1; dj++) {
                            if (Math.abs(di) + Math.abs(dj) == 1) {
                                int ni = (i + di + 3) % 3;
                                int nj = (j + dj + 8) % 8;
                                if (board[ni][nj] == 0) {
                                    MlinekState child = new MlinekState(this);
                                    child.board[i][j] = 0;
                                    child.board[ni][nj] = isMaximizingTurnNow() ? 1 : -1;
                                    children.add(child);
                                }
                            }
                        }
                    }
                }
            }
        }
        return children;
    }

    @Override
    public List<GameState> generateChildren() {
        if (whiteToPlace > 0 || blackToPlace > 0) {
            return generateInitialPhaseChildren();
        } else {
            return generateMidEndPhaseChildren();
        }
    }

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
        sb.append("+--------------+--------------+\n");
        sb.append("|              |              |\n");
        sb.append("|    +---------+---------+    |\n");
        sb.append("|    |         |         |    |\n");
        sb.append("|    |    +----+----+    |    |\n");
        sb.append("|    |    |         |    |    |\n");
        sb.append("+----+----+         +----+----+\n");
        sb.append("|    |    |         |    |    |\n");
        sb.append("|    |    +----+----+    |    |\n");
        sb.append("|    |         |         |    |\n");
        sb.append("|    +---------+---------+    |\n");
        sb.append("|              |              |\n");
        sb.append("+--------------+--------------+\n");
        //save "+" indexes, add thiss shit to file and read it from file

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (j == 0) {
                    sb.append("Layer ").append(i).append(":  ");
                }
                sb.append(board[i][j] == 1 ? "W" : board[i][j] == -1 ? "B" : ".");
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
