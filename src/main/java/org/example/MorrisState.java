package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        char player = board[i][j];
        if (player == '.') {
            return false;
        }

        boolean sameCircleMill = false;
        if (j % 2 == 0) {
            sameCircleMill =
                    (board[i][(j + 1) % 8] == player && board[i][(j + 2) % 8] == player) ||
                            (board[i][(j + 7) % 8] == player && board[i][(j + 6) % 8] == player);
        } else {
            sameCircleMill =
                    (board[i][(j + 1) % 8] == player && board[i][(j + 7) % 8] == player);
        }

        boolean betweenCirclesMill = false;
        if (j % 2 == 0) {
            betweenCirclesMill =
                    (board[0][j] == player && board[1][j] == player && board[2][j] == player);
        }

        return sameCircleMill || betweenCirclesMill;
    }

    private List<GameState> solveMill() {
        List<GameState> children = new ArrayList<>();
        char currentPlayer = isMaximizingTurnNow() ? 'W' : 'B'; // Gracz, który stworzył młynek
        char opponent = currentPlayer == 'W' ? 'B' : 'W'; // Przeciwnik

        // Zbieramy wszystkie pionki przeciwnika
        List<int[]> opponentPieces = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == opponent && !isPartOfMill(i, j)) {
                    opponentPieces.add(new int[]{i, j}); // Pionki niebędące częścią młynka
                }
            }
        }

        // Jeśli wszystkie pionki są w młynkach, można usuwać dowolny pionek przeciwnika
        if (opponentPieces.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == opponent) {
                        opponentPieces.add(new int[]{i, j});
                    }
                }
            }
        }

        // Tworzenie stanów potomnych dla każdego możliwego usunięcia pionka przeciwnika
        for (int[] piece : opponentPieces) {
            int x = piece[0];
            int y = piece[1];

            // Tworzenie nowego stanu gry
            MorrisState child = new MorrisState(this);
            child.board[x][y] = '.'; // Usunięcie pionka przeciwnika

            // Aktualizacja liczby pionków przeciwnika na planszy
            if (opponent == 'W') {
                child.whitePiecesOnBoard--;
            } else {
                child.blackPiecesOnBoard--;
            }

            // Aktualizacja tury
            child.millFormed = false; // Młynek już rozwiązany
            child.setMaximizingTurnNow(!isMaximizingTurnNow());

            // Rejestrujemy ruch
            child.setMoveName("Remove opponent's piece at (" + x + "," + y + ")");

            children.add(child);
        }

        return children;
    }


    @Override
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();

        if (whiteRemaining > 0 || blackRemaining > 0) {
            // Phase 1: Placement of pieces
            char currentPlayer = isMaximizingTurnNow() ? 'W' : 'B';

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] == '.') {
                        MorrisState child = new MorrisState(this);

                        // Place the piece on the board
                        child.board[i][j] = currentPlayer;

                        // Update the state
                        if (currentPlayer == 'W') {
                            child.whiteRemaining--;
                            child.whitePiecesOnBoard++;
                        } else {
                            child.blackRemaining--;
                            child.blackPiecesOnBoard++;
                        }

                        child.setMoveName("Place piece at (" + i + "," + j + ")");

                        // Check if mill is formed
                        if (child.isPartOfMill(i, j)) {
                            child.millFormed = true;
                            children.addAll(child.solveMill());
                        } else {
                            child.setMaximizingTurnNow(!isMaximizingTurnNow());
                            children.add(child);
                        }
                    }
                }
            }
        }

        return children;
    }


    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board) + whiteRemaining * 31 + blackRemaining * 17;
    }

    public boolean isTerminal() {
        boolean setUpPhaseIsOver = whiteRemaining == 0 && blackRemaining == 0;
        if (!setUpPhaseIsOver) {
            return false;
        }

        boolean lessThanThreePieces = whitePiecesOnBoard < 3 || blackPiecesOnBoard < 3;
        if (lessThanThreePieces) {
            return true;
        }

        boolean impossibleTurn = generateChildren().isEmpty();
        return impossibleTurn;
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
                "white pieces remaining: " + whiteRemaining + ", Black remaining: " + blackRemaining + "\n" +
                "white pieces on board: " + whitePiecesOnBoard + ", Black on board: " + blackPiecesOnBoard + "\n" +
                "next s: " + (isMaximizingTurnNow() ? "White" : "Black") + "\n";
        return sb;
    }

    static {
        setHFunction(new StateFunction() {
            @Override
            public double calculate(State state) {
                MorrisState nimState = (MorrisState) state;
                if (nimState.isTerminal())
                    return Double.POSITIVE_INFINITY * (nimState.isMaximizingTurnNow() ? -1 : 1);
                return 0.0;
            }
        });
    }

}