package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import sac.State;
import sac.StateFunction;
import sac.game.GameState;
import sac.game.GameStateImpl;

public class MorrisState extends GameStateImpl {
    private char[][] board; // Дошка 3x8: [3][8]
    private int whiteRemaining; // Кількість фішок білих, які ще потрібно поставити
    private int blackRemaining; // Кількість фішок чорних, які ще потрібно поставити
    private int whitePiecesOnBoard; // Фішки білих на дошці
    private int blackPiecesOnBoard; // Фішки чорних на дошці
    private boolean flyingPhaseWhite; // Чи можуть білі стрибати
    private boolean flyingPhaseBlack; // Чи можуть чорні стрибати
    private boolean millFormed; // Чи утворено млин у поточному ході

    private static final int[][][] MILLS = {
            // Зовнішній квадрат (горизонтальні)
            {{0, 0}, {0, 1}, {0, 2}},
            {{0, 2}, {0, 3}, {0, 4}},
            {{0, 4}, {0, 5}, {0, 6}},
            {{0, 6}, {0, 7}, {0, 0}},

            // Середній квадрат (горизонтальні)
            {{1, 0}, {1, 1}, {1, 2}},
            {{1, 2}, {1, 3}, {1, 4}},
            {{1, 4}, {1, 5}, {1, 6}},
            {{1, 6}, {1, 7}, {1, 0}},

            // Внутрішній квадрат (горизонтальні)
            {{2, 0}, {2, 1}, {2, 2}},
            {{2, 2}, {2, 3}, {2, 4}},
            {{2, 4}, {2, 5}, {2, 6}},
            {{2, 6}, {2, 7}, {2, 0}},

            // Вертикальні лінії (зовнішній -> середній -> внутрішній)
            {{0, 0}, {1, 0}, {2, 0}},
            {{0, 1}, {1, 1}, {2, 1}},
            {{0, 2}, {1, 2}, {2, 2}},
            {{0, 3}, {1, 3}, {2, 3}},
            {{0, 4}, {1, 4}, {2, 4}},
            {{0, 5}, {1, 5}, {2, 5}},
            {{0, 6}, {1, 6}, {2, 6}},
            {{0, 7}, {1, 7}, {2, 7}}
    };

    public MorrisState() {
        board = new char[3][8];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = '.'; // Порожні клітинки
            }
        }
        whiteRemaining = 9;
        blackRemaining = 9;
        whitePiecesOnBoard = 0;
        blackPiecesOnBoard = 0;
        flyingPhaseWhite = false;
        flyingPhaseBlack = false;
        millFormed = false;
        setMaximizingTurnNow(true); // Білі починають
    }

    public MorrisState(MorrisState parent) {
        this.board = new char[3][8];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(parent.board[i], 0, this.board[i], 0, 8);
        }
        this.whiteRemaining = parent.whiteRemaining;
        this.blackRemaining = parent.blackRemaining;
        this.whitePiecesOnBoard = parent.whitePiecesOnBoard;
        this.blackPiecesOnBoard = parent.blackPiecesOnBoard;
        this.flyingPhaseWhite = parent.flyingPhaseWhite;
        this.flyingPhaseBlack = parent.flyingPhaseBlack;
        this.millFormed = parent.millFormed;
        this.setMaximizingTurnNow(parent.isMaximizingTurnNow());
    }

    // Перевірка, чи належить позиція до млина
    private boolean isPartOfMill(int square, int pos, char player) {
        for (int[][] mill : MILLS) {
            boolean inMill = true;
            for (int[] coord : mill) {
                int sq = coord[0];
                int p = coord[1];
                if (board[sq][p] != player) {
                    inMill = false;
                    break;
                }
            }
            if (inMill) {
                // Якщо вся лінія належить одному гравцеві
                for (int[] coord : mill) {
                    if (coord[0] == square && coord[1] == pos) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Встановити фішку на дошку
    public void placePiece(int square, int pos) {
        assert (board[square][pos] == '.');
        char currentPlayer = isMaximizingTurnNow() ? 'W' : 'B';

        board[square][pos] = currentPlayer;
        if (currentPlayer == 'W') {
            whiteRemaining--;
            whitePiecesOnBoard++;
        } else {
            blackRemaining--;
            blackPiecesOnBoard++;
        }

        // Перевірка на утворення млина
        millFormed = isPartOfMill(square, pos, currentPlayer);

        if (millFormed) {
            setMoveName("Mill created at square " + square + ", position " + pos);
        } else {
            // Передача ходу іншому гравцеві, якщо млин не утворено
            setMaximizingTurnNow(!isMaximizingTurnNow());
        }
    }


    // Видалення фішки опонента
    public void removePiece(int square, int pos) {
        char opponentPlayer = isMaximizingTurnNow() ? 'B' : 'W';

        // Якщо можна видаляти тільки фішки поза млинами, перевіряємо цю умову
        if (isPartOfMill(square, pos, opponentPlayer) && hasNonMillPieces(opponentPlayer)) {
            throw new IllegalStateException("Cannot remove piece from a mill if other pieces are available.");
        }

        board[square][pos] = '.';
        if (opponentPlayer == 'W') {
            whitePiecesOnBoard--;
        } else {
            blackPiecesOnBoard--;
        }

        millFormed = false; // Скидаємо стан млина
    }


    // Метод для генерації нащадків
    @Override
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();

        if (millFormed) {
            // Видалення фішки опонента
            char opponentPlayer = isMaximizingTurnNow() ? 'B' : 'W';
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == opponentPlayer) {
                        if (!isPartOfMill(i, j, opponentPlayer) || !hasNonMillPieces(opponentPlayer)) {
                            MorrisState child = new MorrisState(this);
                            child.removePiece(i, j);
                            child.setMoveName("Remove piece at square " + i + " position " + j);
                            children.add(child);
                        }
                    }
                }
            }
        } else if (whiteRemaining > 0 || blackRemaining > 0) {
            // Стадія виставлення фішок
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == '.') {
                        MorrisState child = new MorrisState(this);
                        child.placePiece(i, j);
                        child.setMoveName("Place piece at square " + i + " position " + j);
                        children.add(child);
                    }
                }
            }
        } else {
            // Стадія руху
            char currentPlayer = isMaximizingTurnNow() ? 'W' : 'B';
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == currentPlayer) {
                        int[] neighbors = { (j + 1) % 8, (j + 7) % 8 };
                        for (int neighbor : neighbors) {
                            if (board[i][neighbor] == '.') {
                                MorrisState child = new MorrisState(this);
                                child.board[i][j] = '.';
                                child.board[i][neighbor] = currentPlayer;
                                child.setMoveName("Move from " + i + "," + j + " to " + i + "," + neighbor);
                                children.add(child);
                            }
                        }
                    }
                }
            }
        }
        return children;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(whiteRemaining, blackRemaining, whitePiecesOnBoard, blackPiecesOnBoard, maximizingTurnNow);
        result = 31 * result + Arrays.deepHashCode(board);
        return result;
    }

    public boolean isTerminal() {
        return whitePiecesOnBoard < 3 || blackPiecesOnBoard < 3;
    }

    private boolean hasNonMillPieces(char player) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == player && !isPartOfMill(i, j, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

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
        sb.append("Next move: ").append(isMaximizingTurnNow() ? "White" : "Black").append("\n");

        return sb.toString();
    }
}
