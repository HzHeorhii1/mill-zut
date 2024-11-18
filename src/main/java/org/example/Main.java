package org.example;

import sac.game.GameSearchAlgorithm;
import sac.game.GameState;
import sac.game.MinMax;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static final Set<String> previousMoves = new HashSet<>(); // Колекція для зберігання попередніх ходів

    public static void main(String[] args) {
        GameState currentState = new MlinekState();
        GameSearchAlgorithm algorithm = new MinMax();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Вітаємо у грі 'Млинок'!");

        while (!currentState.isWinTerminal() && !currentState.isNonWinTerminal()) {
            System.out.println(currentState);

            // Хід гравця
            currentState = playerMove(scanner, currentState);

            if (currentState.isWinTerminal() || currentState.isNonWinTerminal()) break;

            // Хід комп'ютера
            currentState = computerMove(algorithm, currentState);
        }

        System.out.println("Гра завершена. Результат:");
        System.out.println(currentState);

        // Визначення переможця
        if (currentState.isWinTerminal()) {
            if (currentState.isMaximizingTurnNow()) {
                System.out.println("Гравець переміг!");
            } else {
                System.out.println("Комп'ютер переміг!");
            }
        } else {
            System.out.println("Гра завершена нічиєю.");
        }
    }

    private static GameState playerMove(Scanner scanner, GameState state) {
        while (true) {
            System.out.println("Ваш хід (вкажіть літеру позиції): ");
            String input = scanner.nextLine().trim().toLowerCase();

            // Перевірка на повтор ходу
            if (previousMoves.contains(input)) {
                System.out.println("Цей хід вже було зроблено. Спробуйте ще раз.");
                continue;
            }

            try {
                int i = input.charAt(0) - 'a';
                int layer = i / 8, position = i % 8;

                for (GameState child : state.generateChildren()) {
                    if (child.getMoveName().equals(layer + "," + position)) {
                        previousMoves.add(input); // Додаємо хід до списку попередніх
                        return child;
                    }
                }
            } catch (Exception e) {
                System.out.println("Некоректний хід. Спробуйте ще раз.");
            }
        }
    }

    private static GameState computerMove(GameSearchAlgorithm algorithm, GameState state) {
        algorithm.setInitial(state);
        algorithm.execute();
        String bestMove = algorithm.getFirstBestMove();

        for (GameState child : state.generateChildren()) {
            if (child.getMoveName().equals(bestMove)) {
                System.out.println("Хід комп'ютера: " + bestMove);
                previousMoves.add(bestMove); // Додаємо хід комп'ютера до списку попередніх
                return child;
            }
        }
        throw new IllegalStateException("Хід комп'ютера не знайдено.");
    }
}
