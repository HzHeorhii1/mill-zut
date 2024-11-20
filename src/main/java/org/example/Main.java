package org.example;

import sac.game.GameSearchAlgorithm;
import sac.game.GameState;
import sac.game.GameStateImpl;
import sac.game.MinMax;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GameState gra = new MlynekState(); // Ініціалізація гри
        GameSearchAlgorithm alg = new MinMax(); // Використання алгоритму мінімаксу
        alg.setInitial(gra);

        Scanner scanner = new Scanner(System.in);
        String ruch; // Змінна для ходу користувача

        while (!gra.isWinTerminal() && !gra.isNonWinTerminal()) {
            System.out.println("Поточний стан гри:");
            System.out.println(gra);

            // Генеруємо всі можливі ходи
            List<GameState> children = gra.generateChildren();

            // Очікуємо хід користувача
            boolean validMove = false;
            while (!validMove) {
                System.out.println("Ваш хід. Введіть назву ходу (наприклад, Place at (0,3)):");
                ruch = scanner.nextLine();

                for (GameState child : children) {
                    if (ruch.equals(child.getMoveName())) {
                        gra = child;
                        validMove = true;
                        break;
                    }
                }

                if (!validMove) {
                    System.out.println("Неправильний хід. Спробуйте ще раз.");
                }
            }

            // Перевіряємо, чи гра завершилася
            if (gra.isWinTerminal() || gra.isNonWinTerminal()) {
                break;
            }

            // Хід комп'ютера
            children = gra.generateChildren();
            alg.setInitial(gra);
            alg.execute(); // Виконуємо обчислення для AI
            ruch = alg.getFirstBestMove(); // Отримуємо найкращий хід

            for (GameState child : children) {
                if (ruch.equals(child.getMoveName())) {
                    gra = child;
                    System.out.println("Хід комп'ютера: " + ruch);
                    break;
                }
            }
        }

        // Кінець гри
        System.out.println("Гра завершена!");
        if (gra.isWinTerminal()) {
            System.out.println("Перемога!");
        } else if (gra.isNonWinTerminal()) {
            System.out.println("Нічия!");
        }
    }
}
