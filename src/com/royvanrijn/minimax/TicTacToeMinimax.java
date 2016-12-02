/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.royvanrijn.minimax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/****
 * @author Roy Van Rijn 11-nov-2016
 *
 * Needs to be:
 *
 * https://github.com/JavaFXpert/visual-neural-net-server/
 *
 * Empty: 1,0,0
 * X:     0,1,0
 * O:     0,0,1
 */
public class TicTacToeMinimax {

  public static void main(String[] args) {
    new TicTacToeMinimax().run();
  }

  private void run() {
    // Generate the complete tree with all game state for Tic Tac Toe:
    Node gameTree = new TicTacToeEngine().generateTree();

    // Walk the tree and fill the intermediate minimax scores:
    minimax(gameTree, true, 10);

    for(String l:uniqueLines) {
      System.out.println(l);
    }
  }

  int minimax(Node node, boolean maximizingScore, int depth) {
    if(node.isEndNode()) {
      int score = (node.getScore() * depth);
      return score;
    }

    int bestScore = maximizingScore ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    int nextMove = -1;
    for(Node child: node.getChildren()) {
      int score = minimax(child, !maximizingScore, depth-1);
      child.setMinimaxScore(score);
      if(maximizingScore) {
        if(score > bestScore) {
          nextMove = child.getLastPlayedSquare();
        }
        bestScore = Math.max(score, bestScore);
      } else {
        if(score < bestScore) {
          nextMove = child.getLastPlayedSquare();
        }
        bestScore = Math.min(score, bestScore);
      }
    }
    // Print the current board and best deeper move:
    String thisMove = Arrays.stream(node.getState()).map(b -> b==null?"1,0,0":(b?"0,1,0":"0,0,1")).collect(Collectors.joining(", "));
    String dataline = nextMove + ",    " + thisMove + " ";

    uniqueLines.add(dataline);

    return bestScore;
  }

  Set<String> uniqueLines = new HashSet<>();

  /**
   * Dirty little class (tree/nodes) to hold game state.
   */
  public class Node {
    private final Boolean[] state;
    private final int lastPlayedSquare;
    private final List<Node> children = new ArrayList<>();
    private Integer score = null;
    private Integer minimaxScore = null;

    Node(){
      // Empty board:
      lastPlayedSquare = -1;
      state = new Boolean[9];
    }
    Node(Node parent, int filledSquare) {
      // Copy the state:
      this.lastPlayedSquare = filledSquare;
      state = parent.getState().clone();
    }

    // Getters:
    public void setScore(final int score) {
      this.score = score;
    }

    public int getLastPlayedSquare() {
      return lastPlayedSquare;
    }

    boolean isEndNode() {return score != null;}
    int getScore(){return score;}
    public Boolean[] getState() {return state; }
    List<Node> getChildren() {return children;}

    public Integer getMinimaxScore() {
      return minimaxScore;
    }

    public void setMinimaxScore(final Integer minimaxScore) {
      this.minimaxScore = minimaxScore;
    }
  }

  /**
   * Small Tic Tac Toe 'engine' that generates the game tree.
   */
  public class TicTacToeEngine {

    public Node generateTree() {
      Node startNode = new Node();
      fillGameTree(startNode, true);
      return startNode;
    }

    // Recursive method to fill the tree:
    private void fillGameTree(final Node parentNode, final boolean currentPlayer) {

      int amountNull = 0;
      for(int square = 0; square < 9; square++) {
        Boolean ticTacToeSquare = parentNode.getState()[square];
        if(ticTacToeSquare == null) {
          amountNull++;
          //Emoty square, there is a child node here.
          Node childNode = new Node(parentNode, square);
          parentNode.getChildren().add(childNode);
          childNode.getState()[square] = currentPlayer;

          // Did we end the game here?
          if(gameEnded(childNode.getState(), square)) {
            childNode.setScore(currentPlayer ? 1 : -1);
          } else {
            // Recurse:
            fillGameTree(childNode, !currentPlayer);
          }
        }
      }

      if(amountNull == 0) {
        // We have no winner... it is a tie:
        parentNode.setScore(0);
      }
    }

    /**
     * Some ugly Tic Tac Toe logic (checking if we have created a winner
     */
    private boolean gameEnded(Boolean[] state, int placedLocation) {

      Boolean placedPlayer = state[placedLocation];

      int row = placedLocation%3;
      int col = placedLocation/3;

      // Check row and column
      int countRow = 0;
      int countCol = 0;
      for(int steps = 0; steps < 3; steps++) {
        if(placedPlayer.equals(state[row + (3 * steps)])) {
          countCol++;
        }
        if(placedPlayer.equals(state[steps + (3 * col)])) {
          countRow++;
        }
      }

      if(countRow == 3 || countCol == 3) {
        return true;
      }

      // If we are on a diagonal, check those as well:
      if(placedLocation%2==0) {
        int countDiag1 = 0;
        int countDiag2 = 0;

        for(int steps = 0; steps < 3; steps++) {
          if(placedPlayer.equals(state[steps + (3 * steps)])) {
            countDiag1++;
          }
          if(placedPlayer.equals(state[(2-steps) + (3 * steps)])) {
            countDiag2++;
          }
        }

        if(countDiag1 == 3 || countDiag2 == 3) {
          return true;
        }

      }
      return false;
    }
  }

}


