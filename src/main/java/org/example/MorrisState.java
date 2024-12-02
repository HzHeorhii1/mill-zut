package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import sac.State;
import sac.StateFunction;
import sac.game.GameState;
import sac.game.GameStateImpl;

public class MorrisState extends GameStateImpl {
    char[][] board;
    private int whiteRemaining;
    private int blackRemaining;
    int whitePiecesOnBoard;
    int blackPiecesOnBoard;
    private boolean flyingPhaseWhite;
    private boolean flyingPhaseBlack;
    boolean millFormed;

    public MorrisState() {
        board = new char[3][8];
        IntStream.range(0, 3).forEach(i -> IntStream.range(0, 8).forEach(j -> board[i][j] = '.'));
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
        IntStream.range(0, 3).forEach(i -> System.arraycopy(parent.board[i], 0, this.board[i], 0, 8));
        this.whiteRemaining = parent.whiteRemaining;
        this.blackRemaining = parent.blackRemaining;
        this.whitePiecesOnBoard = parent.whitePiecesOnBoard;
        this.blackPiecesOnBoard = parent.blackPiecesOnBoard;
        this.flyingPhaseWhite = parent.flyingPhaseWhite;
        this.flyingPhaseBlack = parent.flyingPhaseBlack;
        this.millFormed = parent.millFormed;
        this.setMaximizingTurnNow(parent.isMaximizingTurnNow());
    }

    boolean isPartOfMill(int i, int j) {
        char currentPlayer = board[i][j];
        if (currentPlayer == '.') { return false; }
        boolean horizontalMill = false;
        boolean verticalMill = false;

        boolean nextPiece1 = board[i][(j + 1) % 8] == currentPlayer;
        boolean nextPiece2 = board[i][(j + 2) % 8] == currentPlayer;
        boolean prevPiece1 = board[i][(j + 7) % 8] == currentPlayer;
        boolean prevPiece2 = board[i][(j + 6) % 8] == currentPlayer;
        boolean nextPieceN = nextPiece1 && nextPiece2;
        boolean prevPieceN = prevPiece1 && prevPiece2;

        boolean chetnij = j % 2 == 0;
        boolean NeChetnij = j % 2 == 1;

        if (chetnij) { horizontalMill = nextPieceN || prevPieceN; }
        else { horizontalMill = nextPiece1 && prevPiece1; }

        if (NeChetnij) {
            boolean upperPiece = board[(i + 1) % 3][j] == currentPlayer;
            boolean lowerPiece = board[(i + 2) % 3][j] == currentPlayer;
            verticalMill = upperPiece && lowerPiece;
        }

        return horizontalMill || verticalMill;
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

        millFormed = isPartOfMill(square, pos);

        setMoveName("Place piece at square " + square + " position " + pos);
        if (!millFormed) { setMaximizingTurnNow(!isMaximizingTurnNow()); }
    }

    public List<GameState> solveMill() {
        List<GameState> children = new ArrayList<>();
        char opponent = isMaximizingTurnNow() ? 'B' : 'W';
        boolean nonMillPieceExists = hasNonMillPieces(opponent);
        IntStream.range(0, 3).forEach(i ->
                IntStream.range(0, 8)
                        .filter(j -> board[i][j] == opponent && (!isPartOfMill(i, j) || !nonMillPieceExists))
                        .forEach(j -> {
                            MorrisState child = new MorrisState(this);
                            child.board[i][j] = '.';
                            if (opponent == 'W') { child.whitePiecesOnBoard--; }
                            else { child.blackPiecesOnBoard--; }
                            child.setMoveName(this.getMoveName() + "; Remove opponent's piece at (" + i + ", " + j + ")");
                            child.setMaximizingTurnNow(!isMaximizingTurnNow());
                            child.millFormed = false;
                            children.add(child);
                        })
        );

        return children;
    }

    @Override
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();
        // Place phase
        if ((isMaximizingTurnNow() && whiteRemaining > 0) || (!isMaximizingTurnNow() && blackRemaining > 0)) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == '.') {
                        MorrisState child = new MorrisState(this);
                        child.placePiece(i, j);
                        if (child.millFormed) {
                            List<GameState> millChildren = child.solveMill();
                            children.addAll(millChildren);
                        } else {
                            child.setMaximizingTurnNow(!isMaximizingTurnNow());
                            children.add(child);
                        }
                    }
                }
            }
        } else {
            // Move phase
            char currentPlayer = isMaximizingTurnNow() ? 'W' : 'B';
            boolean flyingPhase = (currentPlayer == 'W' && flyingPhaseWhite) || (currentPlayer == 'B' && flyingPhaseBlack);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == currentPlayer) {
                        List<int[]> targets;
                        if (flyingPhase) { targets = getAllVoidPositions(); }
                        else { targets = getNeighbors(i, j); }
                        for (int[] target : targets) {
                            int ni = target[0], nj = target[1];
                            if (board[ni][nj] == '.') {
                                MorrisState child = new MorrisState(this);
                                child.board[i][j] = '.';
                                child.board[ni][nj] = currentPlayer;
                                child.millFormed = child.isPartOfMill(ni, nj);
                                child.setMoveName("Move from (" + i + ", " + j + ") to (" + ni + ", " + nj + ")");
                                if (child.millFormed) {
                                    List<GameState> millChildren = child.solveMill();
                                    children.addAll(millChildren);
                                } else {
                                    child.setMaximizingTurnNow(!isMaximizingTurnNow());
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

    private List<int[]> getAllVoidPositions() {
        return java.util.stream.IntStream.range(0, 3)
                .boxed()
                .flatMap(i ->
                        java.util.stream.IntStream.range(0, 8)
                                .filter(j -> board[i][j] == '.')
                                .mapToObj(j -> new int[]{i, j})
                )
                .collect(Collectors.toList());
    }

    private List<int[]> getNeighbors(int square, int pos) {
        List<int[]> neighbors = new ArrayList<>();
        // the same circle
        neighbors.add(new int[]{square, (pos + 1) % 8}); // next position
        neighbors.add(new int[]{square, (pos + 7) % 8}); // previous position
        // between circles
        if (pos % 2 == 0) {
            if (square > 0) neighbors.add(new int[]{square - 1, pos}); // inner
            if (square < 2) neighbors.add(new int[]{square + 1, pos}); // outer
        }
        return neighbors;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    public boolean isTerminal() {
        boolean setUpPhaseIsOver = whiteRemaining == 0 && blackRemaining == 0;
        if (!setUpPhaseIsOver) { return false; }

        boolean lessThanThreePieces = whitePiecesOnBoard < 3 || blackPiecesOnBoard < 3;
        if (lessThanThreePieces) { return true; }

        boolean impossibleTurn = generateChildren().isEmpty();
        return impossibleTurn;
    }

    private boolean hasNonMillPieces(char player) {
        return IntStream.range(0, 3)
                .anyMatch(i -> IntStream.range(0, 8)
                        .anyMatch(j -> board[i][j] == player && !isPartOfMill(i, j)));
    }

    @Override
    public String toString() {
        String sb = "\n" +
                board[0][6] + "--------------" + board[0][5] + "--------------" + board[0][4] + "  7\n" +
                "|              |              |\n" +
                "|    " + board[1][6] + "---------" + board[1][5] + "---------" + board[1][4] + "    |  6\n" +
                "|    |         |         |    |\n" +
                "|    |    " + board[2][6] + "----" + board[2][5] + "----" + board[2][4] + "    |    |  5\n" +
                "|    |    |         |    |    |\n" +
                board[0][7] + "----" + board[1][7] + "----" + board[2][7] + "         " + board[2][3] + "----" + board[1][3] + "----" + board[0][3] + "  4\n" +
                "|    |    |         |    |    |\n" +
                "|    |    " + board[2][0] + "----" + board[2][1] + "----" + board[2][2] + "    |    |  3\n" +
                "|    |         |         |    |\n" +
                "|    " + board[1][0] + "---------" + board[1][1] + "---------" + board[1][2] + "    |  2\n" +
                "|              |              |\n" +
                board[0][0] + "--------------" + board[0][1] + "--------------" + board[0][2] + "  1\n" +
                "a    b    c    d    e    f    g\n" +
                "\n" +
                "white pieces remaining: " + whiteRemaining + ", black remaining: " + blackRemaining + "\n" +
                "white pieces on board: " + whitePiecesOnBoard + ", met... on board: " + blackPiecesOnBoard + "\n" +
                "next turn: " + (isMaximizingTurnNow() ? "white" : "black") + "\n";
        return sb;
    }

    static {
        setHFunction(new StateFunction() {
            @Override
            public double calculate(State state) {
                MorrisState morrisState = (MorrisState) state;
                if (morrisState.isTerminal()) {
                    return Double.POSITIVE_INFINITY * (morrisState.isMaximizingTurnNow() ? -1 : 1);
                }
                return 0.0;
            }
        });
    }
}