package org.example;

import sac.game.GameSearchAlgorithm;
import sac.game.GameState;
import sac.game.MinMax;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MorrisState game = new MorrisState();
        GameSearchAlgorithm algorithm = new MinMax();
        Scanner scanner = new Scanner(System.in);
        boolean gameCanNotBeContinued = !game.isTerminal();
        while (gameCanNotBeContinued) {
            System.out.println(game);
            List<GameState> children = game.generateChildren();
            if (children.isEmpty()) {
                System.out.println("no possible moves");
                break;
            }

            String move;
            boolean validMove = false;
            do {
                System.out.println("your moe (format: Place W at (i, j) або Move W from (i, j) to (k, l)):");
                move = scanner.nextLine();
                for (GameState child : children) {
                    if (move.equals(child.getMoveName())) {
                        game = (MorrisState) child;
                        validMove = true;
                        break;
                    }
                }
                if (!validMove) { System.out.println("lets try aggain"); }
            } while (!validMove);

            if (game.millFormed) {
                boolean validRemove = false;
                do {
                    System.out.println("you made mill, pick up coords to remove (format: i, j):");
                    String[] coordinates = scanner.nextLine().split(",");
                    if (coordinates.length == 2) {
                        try {
                            int square = Integer.parseInt(coordinates[0].trim());
                            int pos = Integer.parseInt(coordinates[1].trim());
                            try {
                                game.removePiece(square, pos);
                                validRemove = true;
                            } catch (IllegalStateException e) {
                                System.out.println("err: " + e.getMessage());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("bad format");
                        }
                    } else {
                        System.out.println("bad format");
                    }
                } while (!validRemove);
            }

            if (game.isTerminal()) { break; }

            children = game.generateChildren();
            algorithm.setInitial(game);
            algorithm.execute();
            System.out.println("closed-states: " + algorithm.getClosedStatesCount());
            System.out.println("time-duration[ms]: " + algorithm.getDurationTime());
            String bestMove = algorithm.getFirstBestMove();

            System.out.println("computers turn: " + bestMove);
            for (GameState child : children) {
                boolean isMoveTheBEst = bestMove.equals(child.getMoveName());
                if (isMoveTheBEst) {
                    game = (MorrisState) child;
                    if (game.millFormed) { game.removeRandomOpponentPiece(); }
                    break;
                }
            }
        }

        System.out.println("game over");
        System.out.println(game);

        if (game.whitePiecesOnBoard < 3) { System.out.println("black won lol"); }
        else if (game.blackPiecesOnBoard < 3) { System.out.println("white won"); }
        else { System.out.println("friendship won"); }
    }
}
