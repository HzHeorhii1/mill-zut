package org.example;

import sac.game.GameStateImpl;
import sac.game.GameState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// блять курва нужно сделать так, чтобы консоль предупреждала о дубриловании ходов, чтобы она записывала буквы в масси и проверяла было ли что-то подобное введено пользователем
public class MlinekState extends GameStateImpl {
    private final int[][] board; // 3 x 8 (3 рівня, по 8 позицій)
    private int whiteToPlace;   // Пішаки, що залишилися для розстановки
    private int blackToPlace;
    private int remainingWhite; // Пішаки, що залишилися на дошці
    private int remainingBlack;

    public MlinekState() {
        board = new int[3][8];
        whiteToPlace = 9;
        blackToPlace = 9;
        remainingWhite = 9;
        remainingBlack = 9;
        setMaximizingTurnNow(true); // Починає гравець
    }

    private MlinekState(MlinekState other) {
        board = new int[3][8];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(other.board[i], 0, this.board[i], 0, 8);
        }
        whiteToPlace = other.whiteToPlace;
        blackToPlace = other.blackToPlace;
        remainingWhite = other.remainingWhite;
        remainingBlack = other.remainingBlack;
        setMaximizingTurnNow(other.isMaximizingTurnNow());
    }

    private boolean isMill(int i, int j) {
        int pos = board[i][j];
        if (pos == 0) return false;

        // Перевірка на горизонтальний і вертикальний "млинок"
        boolean millHorizontal = (board[i][(j + 1) % 8] == pos && board[i][(j + 2) % 8] == pos) ||
                (board[i][(j + 7) % 8] == pos && board[i][(j + 6) % 8] == pos);
        boolean millVertical = (i == 0 && board[1][j] == pos && board[2][j] == pos) ||
                (i == 1 && board[0][j] == pos && board[2][j] == pos) ||
                (i == 2 && board[0][j] == pos && board[1][j] == pos);

        return millHorizontal || millVertical;
    }

    @Override
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();
        if (whiteToPlace > 0 || blackToPlace > 0) {
            // Етап 1: Розстановка фігур
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
                        child.setMoveName(i + "," + j);
                        child.setMaximizingTurnNow(!isMaximizingTurnNow());
                        children.add(child);
                    }
                }
            }
        } else {
            // Етап 2: Рух фігур
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == (isMaximizingTurnNow() ? 1 : -1)) {
                        for (int[] move : getAdjacentPositions(i, j)) {
                            int ni = move[0], nj = move[1];
                            if (board[ni][nj] == 0) {
                                MlinekState child = new MlinekState(this);
                                child.board[i][j] = 0;
                                child.board[ni][nj] = isMaximizingTurnNow() ? 1 : -1;
                                child.setMoveName(i + "," + j + "->" + ni + "," + nj);
                                child.setMaximizingTurnNow(!isMaximizingTurnNow());
                                children.add(child);
                            }
                        }
                    }
                }
            }
        }
        return children;
    }


    private List<int[]> getAdjacentPositions(int i, int j) {
        List<int[]> neighbors = new ArrayList<>();
        neighbors.add(new int[]{i, (j + 1) % 8});
        neighbors.add(new int[]{i, (j + 7) % 8});
        if (j % 2 == 1) { // Можливі вертикальні переходи
            if (i > 0) neighbors.add(new int[]{i - 1, j});
            if (i < 2) neighbors.add(new int[]{i + 1, j});
        }
        return neighbors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String[] labels = {
                "a", "b", "c", "d", "e", "f", "g", "h", // Рівень 0
                "i", "j", "k", "l", "m", "n", "o", "p", // Рівень 1
                "q", "r", "s", "t", "u", "v", "w", "x"  // Рівень 2
        };

        // Відображення позицій рівня 0
        sb.append("g--------------f--------------e\n");
        sb.append("|              |              |\n");
        sb.append("|    o---------n---------m    |\n");
        sb.append("|    |         |         |    |\n");
        sb.append("|    |    w----v----u    |    |\n");
        sb.append("|    |    |         |    |    |\n");
        sb.append("h----p----x         t----l----d\n");
        sb.append("|    |    |         |    |    |\n");
        sb.append("|    |    q----r----s    |    |\n");
        sb.append("|    |         |         |    |\n");
        sb.append("|    i---------j---------k    |\n");
        sb.append("|              |              |\n");
        sb.append("a--------------b--------------c\n");

        // Відображення стану фігур
        for (int layer = 0; layer < 3; layer++) {
            for (int pos = 0; pos < 8; pos++) {
                int index = layer * 8 + pos;
                if (board[layer][pos] == 1) {
                    sb.append(labels[index]).append(": W ");
                } else if (board[layer][pos] == -1) {
                    sb.append(labels[index]).append(": B ");
                }
            }
        }
        return sb.toString();
    }


    public boolean isGameOver() {
        return remainingWhite < 3 || remainingBlack < 3;
    }

    @Override
    public boolean isNonWinTerminal() {
        return isGameOver() || generateChildren().isEmpty();
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
