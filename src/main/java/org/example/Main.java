package org.example;

import org.example.MorrisState;
import sac.game.GameSearchAlgorithm;
import sac.game.GameState;
import sac.game.MinMax;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GameState game = new MorrisState(); // Ініціалізація гри
        GameSearchAlgorithm algorithm = new MinMax(); // Використовуємо Minimax як алгоритм пошуку
        Scanner scanner = new Scanner(System.in); // Для зчитування введення гравця

        while (!game.isWinTerminal() && !game.isNonWinTerminal()) {
            // Виводимо поточний стан гри
            System.out.println(game);

            // Генеруємо можливі дії
            List<GameState> children = game.generateChildren();

            // Введення ходу гравцем
            String move;
            boolean validMove = false;
            do {
                System.out.println("Ваш хід (формат: Place W at (i, j) або Move W from (i, j) to (k, l)):");
                move = scanner.nextLine();
                for (GameState child : children) {
                    if (move.equals(child.getMoveName())) {
                        game = child;
                        validMove = true;
                        break;
                    }
                }
                if (!validMove) {
                    System.out.println("Невірний хід. Спробуйте ще раз.");
                }
            } while (!validMove);

            // Перевірка, чи гра завершена після ходу гравця
            if (game.isWinTerminal() || game.isNonWinTerminal()) {
                break;
            }

            // Хід комп'ютера
            children = game.generateChildren();
            algorithm.setInitial(game);
            algorithm.execute();
            String bestMove = algorithm.getFirstBestMove();

            System.out.println("Хід комп'ютера: " + bestMove);
            for (GameState child : children) {
                if (bestMove.equals(child.getMoveName())) {
                    game = child;
                    break;
                }
            }
        }

        // Виводимо результат гри
        System.out.println("Гра завершена!");
        System.out.println(game);
        if (game.isWinTerminal()) {
            System.out.println("Переможець: " + (game.isMaximizingTurnNow() ? "Чорні" : "Білі"));
        } else {
            System.out.println("Нічия.");
        }
    }
}
