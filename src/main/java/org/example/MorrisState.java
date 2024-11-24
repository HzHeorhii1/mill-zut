package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import sac.State;
import sac.StateFunction;
import sac.game.GameState;
import sac.game.GameStateImpl;

public class MorrisState extends GameStateImpl {
    private char[][] board;
    private int whiteRemaining;
    private int blackRemaining;
    int whitePiecesOnBoard;
    int blackPiecesOnBoard;
    private boolean flyingPhaseWhite;
    private boolean flyingPhaseBlack;
    boolean millFormed;

    private static final int[][][] MILLS = {
            // 0 horizontal
            {{0, 0}, {0, 1}, {0, 2}},
            {{0, 2}, {0, 3}, {0, 4}},
            {{0, 4}, {0, 5}, {0, 6}},
            {{0, 6}, {0, 7}, {0, 0}},
            // 1 horizontal
            {{1, 0}, {1, 1}, {1, 2}},
            {{1, 2}, {1, 3}, {1, 4}},
            {{1, 4}, {1, 5}, {1, 6}},
            {{1, 6}, {1, 7}, {1, 0}},
            // 2 horizontal
            {{2, 0}, {2, 1}, {2, 2}},
            {{2, 2}, {2, 3}, {2, 4}},
            {{2, 4}, {2, 5}, {2, 6}},
            {{2, 6}, {2, 7}, {2, 0}},
            // vertical (0, 1, 2)
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
        IntStream.range(0, 3).forEach(i ->
                IntStream.range(0, 8).forEach(j -> board[i][j] = '.')
        );
        whiteRemaining = 9;
        blackRemaining = 9;
        whitePiecesOnBoard = 0;
        blackPiecesOnBoard = 0;
        flyingPhaseWhite = false;
        flyingPhaseBlack = false;
        millFormed = false;
        setMaximizingTurnNow(true);
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

    private boolean isPartOfMill(int square, int pos, char player) {
        for (int[][] mill : MILLS) {
            boolean inMill = true;
            for (int[] coord : mill) {
                boolean notBelongsToPlayer = board[coord[0]][coord[1]] != player;
                if (notBelongsToPlayer) {
                    inMill = false;
                    break;
                }
            }
            if (inMill) {
                for (int[] coord : mill) {
                    boolean coordsAreInMill = coord[0] == square && coord[1] == pos;
                    if (coordsAreInMill) { return true; }
                }
            }
        }
        return false;
    }

    public void placePiece(int square, int pos) {
        boolean isVoidPlace = board[square][pos] == '.';
        assert(isVoidPlace);
        char currentPlayer = isMaximizingTurnNow() ? 'W' : 'B';

        board[square][pos] = currentPlayer;
        if (currentPlayer == 'W') {
            whiteRemaining--;
            whitePiecesOnBoard++;
        } else {
            blackRemaining--;
            blackPiecesOnBoard++;
        }

        millFormed = isPartOfMill(square, pos, currentPlayer);

        if (millFormed) { setMoveName("mill created at square " + square + ", position " + pos); }
        else { setMaximizingTurnNow(!isMaximizingTurnNow()); }
    }

    public void removePiece(int square, int pos) {
        char opponentPlayer = isMaximizingTurnNow() ? 'B' : 'W';

        boolean isNotOnBoard = square < 0 || square >= 3 || pos < 0 || pos >= 8;
        if (isNotOnBoard) { throw new IllegalArgumentException("Invalid coordinates, you cant remove it"); }

        boolean pieceBelongToOpp = board[square][pos] != opponentPlayer;
        if (pieceBelongToOpp) { throw new IllegalStateException("can not remove a piece that does not belong to the opp, yo"); }

        boolean isPiecePartOfMill = isPartOfMill(square, pos, opponentPlayer);
        boolean hasFreePieces =hasNonMillPieces(opponentPlayer);
        if (isPiecePartOfMill && hasFreePieces) { throw new IllegalStateException("can not remove piece from a mill if other pieces are available"); }

        board[square][pos] = '.';
        if (opponentPlayer == 'W') { whitePiecesOnBoard--; }
        else { blackPiecesOnBoard--; }

        setMaximizingTurnNow(!isMaximizingTurnNow());
        millFormed = false;
    }

    public void removeRandomOpponentPiece() {
        char opponentPlayer = isMaximizingTurnNow() ? 'B' : 'W';
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == opponentPlayer) {
                    boolean isOutsideMill = !isPartOfMill(i, j, opponentPlayer) || !hasNonMillPieces(opponentPlayer);
                    if (isOutsideMill) {
                        board[i][j] = '.';
                        if (opponentPlayer == 'W') { whitePiecesOnBoard--; }
                        else { blackPiecesOnBoard--; }
                        millFormed = false;
                        setMaximizingTurnNow(!isMaximizingTurnNow());
                        return;
                    }
                }
            }
        }
    }

    @Override
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();
        // place phase
        if (whiteRemaining > 0 || blackRemaining > 0) {
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
            // move phase
            char currentPlayer = isMaximizingTurnNow() ? 'W' : 'B';
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == currentPlayer) {
                        List<int[]> neighbors = getNeighbors(i, j);
                        for (int[] neighbor : neighbors) {
                            int ni = neighbor[0], nj = neighbor[1];
                            if (board[ni][nj] == '.') {
                                MorrisState child = new MorrisState(this);
                                child.board[i][j] = '.';
                                child.board[ni][nj] = currentPlayer;
                                child.millFormed = child.isPartOfMill(ni, nj, currentPlayer);
                                child.setMoveName("Move from (" + i + ", " + j + ") to (" + ni + ", " + nj + ")");
                                if (!child.millFormed) { child.setMaximizingTurnNow(!isMaximizingTurnNow()); }
                                children.add(child);
                            }
                        }

                        // flying phase"
                        if ((currentPlayer == 'W' && flyingPhaseWhite) || (currentPlayer == 'B' && flyingPhaseBlack)) {
                            for (int k = 0; k < 3; k++) {
                                for (int l = 0; l < 8; l++) {
                                    if (board[k][l] == '.') {
                                        MorrisState child = new MorrisState(this);
                                        child.board[i][j] = '.';
                                        child.board[k][l] = currentPlayer;
                                        child.millFormed = child.isPartOfMill(k, l, currentPlayer);
                                        child.setMoveName("Fly from " + i + "," + j + " to " + k + "," + l);
                                        if (!child.millFormed) { child.setMaximizingTurnNow(!isMaximizingTurnNow()); }
                                        children.add(child);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return children;
    }


    private List<int[]> getNeighbors(int square, int pos) {
        List<int[]> neighbors = new ArrayList<>();
        // the same circcle
        neighbors.add(new int[]{square, (pos + 1) % 8}); // position before
        neighbors.add(new int[]{square, (pos + 7) % 8}); // position after
        // between circles
        if (true) {
            if (square > 0) neighbors.add(new int[]{square - 1, pos}); // inner
            if (square < 2) neighbors.add(new int[]{square + 1, pos}); // outer
        }

        return neighbors;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(whiteRemaining, blackRemaining, whitePiecesOnBoard, blackPiecesOnBoard, maximizingTurnNow);
        result = 31 * result + Arrays.deepHashCode(board);
        return result;
    }

    public boolean isTerminal() {
        boolean setUpPhaseIsOver = whiteRemaining > 0 || blackRemaining > 0;
        if (setUpPhaseIsOver) { return false; }

        boolean lessThanThreePieces =whitePiecesOnBoard < 3 || blackPiecesOnBoard < 3;
        if (lessThanThreePieces) { return true; }

        boolean impossibleTurn = generateChildren().isEmpty();
        if (impossibleTurn) { return true; }

        return false;
    }

    private boolean hasNonMillPieces(char player) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                boolean isNotPartOfOurMill = !isPartOfMill(i, j, player);
                boolean isPieceInCurrentPosition = board[i][j] == player;
                if (isPieceInCurrentPosition && isNotPartOfOurMill) { return true; }
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
        sb.append("white pieces remaining: ").append(whiteRemaining).append(", Black remaining: ").append(blackRemaining).append("\n");
        sb.append("white pieces on board: ").append(whitePiecesOnBoard).append(", Black on board: ").append(blackPiecesOnBoard).append("\n");
        sb.append("next s: ").append(isMaximizingTurnNow() ? "White" : "Black").append("\n");
        return sb.toString();
    }

    static {
        setHFunction(new StateFunction() {
            @Override
            public double calculate(State state) {
                MorrisState morrisState = (MorrisState) state;
                if (morrisState.isTerminal()) { return morrisState.isMaximizingTurnNow() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY; }
                double score = 0.0;
                score += 10 * (morrisState.whitePiecesOnBoard - morrisState.blackPiecesOnBoard); // pieces on board
                score += 5 * (morrisState.whiteRemaining - morrisState.blackRemaining); // pieces not on board

                // flying phase
                if (morrisState.flyingPhaseWhite) score += 15;
                if (morrisState.flyingPhaseBlack) score -= 15;

                // mill counts
                for (int[][] mill : MorrisState.MILLS) {
                    int whiteCount = 0;
                    int blackCount = 0;
                    for (int[] position : mill) {
                        char piece = morrisState.board[position[0]][position[1]];
                        if (piece == 'W') whiteCount++;
                        else if (piece == 'B') blackCount++;
                    }
                    if (whiteCount == 3) score += 30;
                    if (blackCount == 3) score -= 30;
                }

                if (morrisState.isMaximizingTurnNow()) score += 1.0;
                else score -= 1.0;

                return score;
            }
        });
    }

}