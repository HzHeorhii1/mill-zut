package org.example;

import sac.game.GameSearchAlgorithm;
import sac.game.GameState;
import sac.game.MinMax;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GameState game = new MorrisState();

//        long[] nodeCounts = new long[6]; // Наприклад, до 10 рівнів
//        expand(game, nodeCounts, 0);
//
//        for (int i = 0; i < nodeCounts.length; i++) {
//            System.out.println("Рівень " + i + ": " + nodeCounts[i] + " вузлів");
//        }

        GameSearchAlgorithm algorithm = new MinMax();
        Scanner scanner = new Scanner(System.in);
        String move;

        while (!game.isWinTerminal() && !game.isNonWinTerminal()) {
            System.out.println(game);
            System.out.println("Ваш хід. Введіть ваш рух (наприклад: 'Place piece at square 0 position 1'):");
            List<GameState> children = game.generateChildren();
            boolean validMove = false;

            // Виведення можливих ходів
            System.out.println("Можливі ходи:");
            for (GameState child : children) {
                System.out.println(child.getMoveName());
            }

            // Очікування правильного вводу від користувача
            do {
                move = scanner.nextLine();
                for (GameState child : children) {
                    if (move.equals(child.getMoveName())) {
                        game = child;
                        validMove = true;
                        break;
                    }
                }
                if (!validMove) {
                    System.out.println("Недійсний хід. Спробуйте ще раз:");
                }
            } while (!validMove);

            // Перевірка завершення гри після ходу користувача
            if (game.isWinTerminal() || game.isNonWinTerminal()) {
                break;
            }

            // Хід комп'ютера
            children = game.generateChildren();
            algorithm.setInitial(game);
            algorithm.execute();
            move = algorithm.getFirstBestMove();

            validMove = false;
            for (GameState child : children) {
                if (move.equals(child.getMoveName())) {
                    game = child;
                    System.out.println("Комп'ютер зробив хід: " + move);
                    validMove = true;
                    break;
                }
            }

            if (!validMove) {
                System.out.println("Комп'ютер не може зробити хід.");
                break;
            }
        }

        // Виведення результату гри
        if (game.isWinTerminal()) {
            System.out.println("Переможець: " + (game.isMaximizingTurnNow() ? "Чорні" : "Білі"));
        } else if (game.isNonWinTerminal()) {
            System.out.println("Гра завершена в нічию.");
        }
    }

    public static void expand(GameState s, long[] v, int d) {
        if (d >= v.length)
            return;
        for (GameState t : s.generateChildren()) {
            v[d]++;
            expand(t, v, d + 1);
        }
    }
}