package org.example;

import sac.game.GameSearchAlgorithm;
import sac.game.GameState;
import sac.game.MinMax;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GameState gra = new MlinekState();
        GameSearchAlgorithm alg = new MinMax();
        String ruch;
        while (!gra.isWinTerminal() && !gra.isNonWinTerminal()) {
            List<GameState> children = gra.generateChildren();
            Scanner scanner = new Scanner(System.in);
            for (GameState c : children)
                if (ruch.equals(c.getMoveName())) {
                    gra = c;
                    break;
                }
        }
    }
}