package org.example;

import sac.game.GameSearchAlgorithm;
import sac.game.GameState;
import sac.game.MinMax;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GameState game = new MorrisState();
        GameSearchAlgorithm algorithm = new MinMax();
        Scanner scanner = new Scanner(System.in);
        String move;

        while (!game.isWinTerminal() && !game.isNonWinTerminal()) {
            System.out.println(game);
            System.out.println("ur turn:");
            List<GameState> children = game.generateChildren();
            boolean validMove = false;

//            System.out.println("Можливі ходи:");
//            for (GameState child : children) {
//                System.out.println(child.getMoveName());
//            }

            do {
                move = scanner.nextLine();
                for (GameState child : children) {
                    if (move.equals(child.getMoveName())) {
                        game = child;
                        validMove = true;
                        break;
                    }
                }
                if (!validMove) { System.out.println("invalid move, try again:"); }
            } while (!validMove);

            //countAndPrintNodeCounts(game);

            if (game.isWinTerminal() || game.isNonWinTerminal()) { break; }

            children = game.generateChildren();
            algorithm.setInitial(game);
            algorithm.execute();
            move = algorithm.getFirstBestMove();

            validMove = false;
            for (GameState child : children) {
                if (move.equals(child.getMoveName())) {
                    game = child;
                    System.out.println("computer's turn: " + move);
                    validMove = true;
                    break;
                }
            }
            if (!validMove) { break; }
            //countAndPrintNodeCounts(game);
        }

        if (game.isWinTerminal()) { System.out.println("winner is: " + (game.isMaximizingTurnNow() ? "white lol" : "black?")); }
        else if (game.isNonWinTerminal()) { System.out.println("friendship won"); }
    }

    public static void countAndPrintNodeCounts(GameState game) {
        long[] nodeCounts = new long[6];
        expand(game, nodeCounts, 0);
        for (int i = 0; i < nodeCounts.length; i++)  System.out.println("level " + i + ": " + nodeCounts[i] + " states");
    }

    public static void expand(GameState s, long[] v, int d) {
        if (d >= v.length)  return;
        for (GameState t : s.generateChildren()) {
            v[d]++;
            expand(t, v, d + 1);
        }
    }
}
