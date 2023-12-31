package net.kenneydiaz.tyler.frogsandtoads;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Kenney
 *
 * This is a backend engine for the game of Frogs and Toads. A board of odd rows and odd
 * columns is created, and populated with frogs, toads, and an empty space. The objective
 * is to move all the frogs to the top left of the board, and all the toads to the bottom
 * right of the board. This can be done through a series of movements into the empty
 * space. Frogs and toads can jump over each other into the empty space or they can jump
 * into the empty space if it is adjacent. The game is in a losing state if no more legal
 * moves can be made, and the frogs and toads are not in their correct positions. The game
 * enters a winning state if the frogs and toads are in their correct positions. Correct
 * positions being toads at the top left of the board and frogs at the bottom right.
 *
 * Known Bugs:
 * - No known bugs. All methods were individually, successfully tested by the developer.
 *   Though, this does not mean that no bugs exist or that improvements cannot be made.
 *
 */
public class FrogsAndToads {

    // Board information.
    private final int[][] board;
    private final List<int[]> positionHistory = new ArrayList<>();
    private int emptyCellRow;
    private int emptyCellColumn;
    private static final int DEFAULT_SIZE = 5;

    // HashMap used as a sort of associative array to associate the integer values
    // of the characters that represent the different game elements with their preferred
    // text color. This spares us writing additional conditional logic when printing the
    // current state of the game. Maybe this is overkill, but it improves the speed of
    // toString().
    private static final Map<Integer,String> COLOR_MAP = new HashMap<>();

    // Characters that represent the different game elements. Their character value will
    // be used when printing the current state of the game. The integer value of the char
    // will be used to represent that element in the 2D int array of the game board. This
    // spares us writing additional conditional logic when printing the current state of
    // the game.
    private static final char EMPTY_SPACE_CHAR = ' ';
    private static final char TOAD_CHAR = 'T';
    private static final char FROG_CHAR = 'F';

    // Text formatting constants.
    private static final String FROG_COLOR = "\u001B[1;31m"; // ANSI Bold Red
    private static final String TOAD_COLOR = "\u001B[1;33m"; // ANSI Bold Yellow
    private static final String EMPTY_SPACE_COLOR = "\u001B[1;30m"; // ANSI Bold Black
    private static final String RESET_TEXT_FORMATTING = "\u001B[0m"; // ANSI Reset

    /**
     * This default constructor passes the one-parameter constructor the DEFAULT_SIZE.
     * Thus, creating a board of DEFAULT_SIZE rows and DEFAULT_SIZE columns.
     */
    public FrogsAndToads() {

        // Use the one-argument constructor that takes a single size integer.
        this(DEFAULT_SIZE);

    }

    /**
     * Accepts one integer as both the number of rows and columns for the game board.
     * Then, initialization is handled by the two parameter constructor.
     *
     * @param size the number of rows and columns to add to the game board.
     */
    public FrogsAndToads(int size) {

        // Use the two-argument constructor that takes a row and column number, and set
        // them both to size.
        this(size, size);

    }

    /**
     * The original/standard constructor for Frogs and Toads prior to the localization
     * of the debug_mode variable. Since it is only used in the constructor, and it
     * could potentially be useful to allow front ends the ability to generate debug
     * games. As such, this standard two parameter constructor is here to accommodate
     * normal circumstances where debug_mode should be initialized to false.
     *
     * @param rows the number of rows to add to the game board.
     * @param columns the number of columns to add to the game board.
     */
    public FrogsAndToads(int rows, int columns) {

        // Use the two-argument constructor that takes a row and column number, and set
        // them both to size.
        this(rows, columns, false);

    }

    /**
     * Initializes this instance of the game by verifying the given number of rows and
     * columns, defaulting to the DEFAULT_SIZE if any problems are found, then creating
     * a game board of rows rows and cols columns. Then, the first half of rows are filled
     * with frogs, the first half of the middle row is filled with frogs, the middle of
     * the middle row is set to the empty space, the last half of the middle row is set to
     * toads, and the last half of rows are set to toads.
     *
     * If DEBUG_MODE is true, initialize the board to a configuration that is one move
     * away from the winning condition.
     *
     * @param rows the number of rows to add to the game board.
     * @param columns the number of columns to add to the game board.
     * @param debug_mode a boolean which determines whether the game should be
     *                   initialized normally or initialized one move away from a winning
     *                   state.
     */
    public FrogsAndToads(int rows, int columns, boolean debug_mode) {

        // If either the provided number of columns or rows is odd, set it to the
        // default value. Technically, this also prevents inputs of zero and negative
        // inputs. Additional checks are made in other functions to be exceptionally safe.
        int actualRows = (rows % 2 == 1) ? rows : DEFAULT_SIZE;
        int actualCols = (columns % 2 == 1) ? columns : DEFAULT_SIZE;

        // Initialize the game board.
        this.board = new int[actualRows][actualCols];

        // Associate the game elements' characters' integer values with the game elements'
        // preferred colors.
        COLOR_MAP.put((int) TOAD_CHAR,TOAD_COLOR);
        COLOR_MAP.put((int) FROG_CHAR,FROG_COLOR);
        COLOR_MAP.put((int) EMPTY_SPACE_CHAR,EMPTY_SPACE_COLOR);

        emptyCellRow = actualRows / 2;

        // If debugging, the column of the empty space should be one column to the left of
        // center.
        emptyCellColumn = (debug_mode) ? actualCols / 2 - 1 : actualCols / 2;

        // If the engine is not in debugging mode, perform the usual initialization of the
        // game board.
        if (!debug_mode) {
            for (int i = 0; i < actualRows; i++) {
                for (int j = 0; j < actualCols; j++) {
                    if (i < actualRows / 2) {
                        board[i][j] = FROG_CHAR;
                    } else if (i == actualRows / 2) {
                        if (j < actualCols / 2) {
                            board[i][j] = FROG_CHAR;
                        } else if (j == actualCols / 2) {
                            board[i][j] = EMPTY_SPACE_CHAR;
                        } else {
                            board[i][j] = TOAD_CHAR;
                        }
                    } else {
                        board[i][j] = TOAD_CHAR;
                    }
                }
            }

        // If the engine is in debugging mode, initialize the board into the winning
        // configuration, except for one toad which will be placed in the middle of the
        // board, and the empty space which will be placed one column left of the center
        // of the game board.
        } else {
            for (int i = 0; i < actualRows; i++) {
                for (int j = 0; j < actualCols; j++) {
                    if (i < actualRows / 2) {
                        board[i][j] = TOAD_CHAR;
                    } else if (i == actualRows / 2) {
                        if (j == actualCols / 2 - 1) {
                            board[i][j] = EMPTY_SPACE_CHAR;
                        } else if (j == actualCols / 2) {
                            board[i][j] = TOAD_CHAR;
                        } else if (j < actualCols / 2) {
                            board[i][j] = TOAD_CHAR;
                        } else {
                            board[i][j] = FROG_CHAR;
                        }
                    } else {
                        board[i][j] = FROG_CHAR;
                    }
                }
            }
        }
    }

    /**
     * Checks if there are currently any legal moves that can be made by the player.
     *
     * @return true if the list of currently legal moves is not empty. False otherwise.
     */
    public boolean canMove() {
        return !getLegalMoves().isEmpty();
    }

    /**
     * Checks if the current state of the game is the winning configuration, as described
     * below.
     *
     * @return true if the top half, and first half of the middle row are toads, the
     *         middle space of the middle row is the empty space, and the remaining cells
     *         of the game board are frogs. Otherwise, returns false.
     */
    public boolean over() {
        int rows = board.length;
        int columns = (rows > 0) ? board[0].length : 0;

        // For every row...
        for (int i = 0; i < rows; i++) {

            // For every column of that row...
            for (int j = 0; j < columns; j++) {

                // If we are on the first half of rows, check for all toads.
                // If we are on the first half of the middle row, check for all toads.
                // If we are in the middle of the middle row, check for the empty space.
                // If we are on the second half of the middle row, check for all frogs.
                // If we are on the last half of rows, check for all frogs.
                // If any above checks fail, immediately return false.
                if (i < rows / 2 && !toadAt(i, j)) {
                    return false;
                } else if (i == rows / 2) {
                    if (j < columns / 2 && !toadAt(i, j)) {
                        return false;
                    } else if (j == columns / 2 && !emptyAt(i, j)) {
                        return false;
                    } else if (j > columns / 2 && !frogAt(i, j)) {
                        return false;
                    }
                } else if (i > rows / 2 && !frogAt(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * References the list of moves made up to the current state of the board. Looking at
     * the coordinates of the very last move, this function replaces the content of the
     * cell at the coordinates of that last move with the empty space, and the content of
     * the cell at the current coordinates of the empty space with the previous contents
     * of the cell at the coordinates of the last move. The last move is then wiped from
     * the list of recorded moves.
     *
     * In short, undoes the last player move by undoing the swap performed when the last
     * move was made.
     *
     */
    public void undo() {

        // Make sure there are prior moves to undo.
        if (positionHistory.size() > 0) {

            // Get the last position.
            int[] lastPosition = positionHistory.get(positionHistory.size() - 1);

            // Remove the last position from position history.
            positionHistory.remove(positionHistory.size() - 1);

            // I cannot remember the method to perform this swap more efficiently, but
            // this swaps the content of the empty cell with the content of the cell
            // located at the most recent set of coordinates in lastPosition[].
            int temporaryHolderVariable = board[emptyCellRow][emptyCellColumn];
            board[emptyCellRow][emptyCellColumn] = board[lastPosition[0]][lastPosition[1]];
            board[lastPosition[0]][lastPosition[1]] = temporaryHolderVariable;
            emptyCellRow = lastPosition[0];
            emptyCellColumn = lastPosition[1];
        }
    }

    /**
     * Verifies that the provided coordinates are within the list of currently legal moves
     * and that the player can currently move, then "Moves" the selected toad or frog into
     * the cell located at the coordinates of the empty space. Then "moves" the empty cell
     * into the cell located at the given coordinates.
     *
     * @param i the row coordinate of the frog or toad the player would like to move.
     * @param j the column coordinate of the frog or toad the player would like to move.
     */
    public void move(int i, int j) {
        List<int[]> legalMoves = getLegalMoves();

        // If the player can move and the selected move is legal, swap the contents of the
        // selected cell with the contents of the current empty cell.
        if (canMove() && moveIsValid(i, j)) {

            positionHistory.add(new int[] {emptyCellRow, emptyCellColumn});
            int temporaryHolderVariable = board[emptyCellRow][emptyCellColumn];
            board[emptyCellRow][emptyCellColumn] = board[i][j];
            board[i][j] = temporaryHolderVariable;
            emptyCellRow = i;
            emptyCellColumn = j;
        }
    }

    /**
     * Prints the current state of the board. That being a row of numbers to represent
     * each individual column; then, a series of rows containing the row number and the
     * current contents of each cell of the game board, represented as toads, frogs, or
     * the empty space.
     *
     * @return a formatted string which visually represents the current state of the game
     * board.
     */
    @NonNull
    @Override
    public String toString() {

        StringBuilder outputString = new StringBuilder();

        // Define the number of rows and columns. If there are no rows to count the
        // number of columns in, then we can treat columns as if it is 0 for the
        // purposes of this method. (As 0 rows means there is no board to display.)
        //
        // Add or set columns to 1 to ensure minimum spacing between game elements.
        int rows = board.length;
        int columns = (rows > 0) ? board[0].length : 0; // Number of columns in first row.
        int charLengthOfLongestRowNumber = String.valueOf(board.length).length();
        int charLengthOfLongestColumnNumber =
                (rows > 0) ? 1 + String.valueOf(board[0].length).length() : 1;

        // Prepend with an appropriate number of spaces.
        for (int i = 0; i < charLengthOfLongestRowNumber; i++) {
            outputString.append(" ");
        }

        // Add the header column, containing column numbers.
        for (int i = 0; i < columns; i++) {
            outputString.append(String.format(
                    "%" + charLengthOfLongestColumnNumber + "s",
                    i));
        }
        outputString.append("\n");

        // Add the row numbers and the following content of the game board.
        for (int i = 0; i < rows; i++) {
            outputString.append(String.format(
                    "%" + charLengthOfLongestRowNumber + "s",
                    i));
            for (int j = 0; j < columns; j++) {
                outputString.append(COLOR_MAP.get(board[i][j]));
                outputString.append(String.format(
                        "%" + charLengthOfLongestColumnNumber + "s",
                        (char) board[i][j]));
                outputString.append(RESET_TEXT_FORMATTING);
            }
            outputString.append("\n");
        }
        return outputString.toString();
    }

    /**
     * Checks if the empty space is located at the given set of coordinates on the board.
     *
     * @param i the row in which to look for the empty space.
     * @param j the column in which to look for the empty space.
     * @return true if the empty space is located in the cell at the given coordinates;
     *         false otherwise.
     */
    public boolean emptyAt(int i, int j) {
        return board.length != 0 &&
                board[0].length != 0 &&
                i < board.length &&
                j < board[0].length &&
                i >= 0 &&
                j >= 0 &&
                board[i][j] == EMPTY_SPACE_CHAR;
    }

    /**
     * Checks if a frog is located at the given set of coordinates on the board.
     *
     * @param i the row in which to look for a frog.
     * @param j the column in which to look for a frog.
     * @return true if a frog is located in the cell at the given coordinates; false
     *         otherwise.
     */
    public boolean frogAt(int i, int j) {
        return  board.length != 0 &&
                board[1].length != 0 &&
                i < board.length &&
                j < board[0].length &&
                i >= 0 &&
                j >= 0 &&
                board[i][j] == FROG_CHAR;
    }

    /**
     * Checks if a toad is located at the given set of coordinates on the board.
     *
     * @param i the row in which to look for a toad.
     * @param j the column in which to look for a toad.
     * @return true if a toad is located in the cell at the given coordinates; false
     *         otherwise.
     */
    public boolean toadAt(int i, int j) {
        return  board.length != 0 &&
                board[0].length != 0 &&
                i < board.length &&
                j < board[0].length &&
                i >= 0 &&
                j >= 0 &&
                board[i][j] == TOAD_CHAR;
    }

    /**
     * Provides a list of the coordinates of all legal moves, respective of this instance
     * and state of the game board.
     *
     * To be legal, movements must abide by the following rules:
     * 1) Frogs can go down and to the right.
     * 2) Toads can go up and to the left.
     * 3) Frogs can jump over one toad.
     * 4) Toads can jump over one frog.
     * 5) All movements must place the animal in the empty space.
     *
     * @return a list of arrays who contain two integer values each. The first integer
     *         of each array representing one legal move's row coordinate, and the second
     *         integer of each array representing one legal move's column coordinate.
     */
    public List<int[]> getLegalMoves() {
        // Frogs can go down and right.
        // Toads can go up and left.
        // Frogs can jump over one toad.
        // Toads can jump over one frog.
        // All movements must put the animal in the empty space.
        List<int[]> legalMoves = new ArrayList<>();

        // Hop over from top.
        if (    emptyCellRow - 2 >= 0 &&
                toadAt(emptyCellRow - 1, emptyCellColumn) &&
                frogAt(emptyCellRow - 2, emptyCellColumn)) {
            legalMoves.add(new int[] {emptyCellRow - 2, emptyCellColumn});
        }

        // Hop from top.
        if (    emptyCellRow - 1 >= 0 &&
                frogAt(emptyCellRow - 1, emptyCellColumn)) {
            legalMoves.add(new int[] {emptyCellRow - 1, emptyCellColumn});
        }

        // Hop over from bottom.
        if (    emptyCellRow + 2 < board.length &&
                frogAt(emptyCellRow + 1, emptyCellColumn) &&
                toadAt(emptyCellRow + 2, emptyCellColumn)) {
            legalMoves.add(new int[] {emptyCellRow + 2, emptyCellColumn});
        }

        // Hop from bottom.
        if (    emptyCellRow + 1 < board.length &&
                toadAt(emptyCellRow + 1, emptyCellColumn)) {
            legalMoves.add(new int[] {emptyCellRow + 1, emptyCellColumn});
        }

        // Hop over from left.
        if (    emptyCellColumn - 2 >= 0 &&
                toadAt(emptyCellRow, emptyCellColumn - 1) &&
                frogAt(emptyCellRow, emptyCellColumn - 2)) {
            legalMoves.add(new int[] {emptyCellRow, emptyCellColumn - 2});
        }

        // Hop from left.
        if (    emptyCellColumn - 1 >= 0 &&
                frogAt(emptyCellRow, emptyCellColumn - 1)) {
            legalMoves.add(new int[] {emptyCellRow, emptyCellColumn - 1});
        }

        // Hop over from right.
        if (    board.length > 0 &&
                emptyCellColumn + 2 < board[0].length &&
                frogAt(emptyCellRow, emptyCellColumn + 1) &&
                toadAt(emptyCellRow, emptyCellColumn + 2)) {
            legalMoves.add(new int[] {emptyCellRow, emptyCellColumn + 2});
        }

        // Hop from right.
        if (    board.length > 0 &&
                emptyCellColumn + 1 < board[0].length &&
                toadAt(emptyCellRow, emptyCellColumn + 1)) {
            legalMoves.add(new int[] {emptyCellRow, emptyCellColumn + 1});
        }

        return legalMoves;
    }

    /**
     * A public function which can be used to determine if undoing a move is possible or
     * not. It returns an indication of whether this instance of the game engine has
     * any recorded moves in its positionHistory list.
     *
     * @return true if there are any recorded previous moves in the positionHistory
     *         list. False otherwise.
     */
    public boolean hasPreviousMove() {
        return !positionHistory.isEmpty();
    }

    /**
     * A function which can be used to find the current number of rows in this instance
     * of the game engines game board.
     *
     * @return the number of rows on the game board currently.
     */
    public int countRows() {
        return board.length;
    }

    /**
     * A function which can be used to find the current number of columns in this instance
     * of the game engines game board.
     *
     * @return the number of columns on the game board currently.
     */
    public int countColumns() {

        // If there are no rows in the first column, just return 0 to avoid an error.
        return (countRows() > 0) ? board[0].length : 0;
    }

    public boolean moveIsValid(int row, int column) {
        List<int[]> legalMoves = getLegalMoves();

        boolean movementIsLegal = false;

        // For all possible legal moves, if the provided coordinates match even one,
        // mark the move as legal.
        for (int[] legalMove : legalMoves) {
            if (legalMove[0] == row && legalMove[1] == column) {
                movementIsLegal = true;
                break;
            }
        }
        return movementIsLegal;
    }


    public List<int[]> getOptimalPath() {

        List<int[]> optimalPath = new ArrayList<>();



        return optimalPath;

    }

    private int calculateHeuristic() {

        int rows = countRows();
        int columns = countColumns();
        int numberOfIncorrectCells = 0;

        // For every row...
        for (int i = 0; i < rows; i++) {

            // For every column of that row...
            for (int j = 0; j < columns; j++) {

                // If we are on the first half of rows, check for all toads.
                // If we are on the first half of the middle row, check for all toads.
                // If we are in the middle of the middle row, check for the empty space.
                // If we are on the second half of the middle row, check for all frogs.
                // If we are on the last half of rows, check for all frogs.
                // If any above checks fail, immediately return false.
                if (i < rows / 2 && !toadAt(i, j)) {
                    numberOfIncorrectCells++;
                } else if (i == rows / 2) {
                    if (j < columns / 2 && !toadAt(i, j)) {
                        numberOfIncorrectCells++;
                    } else if (j == columns / 2 && !emptyAt(i, j)) {
                        numberOfIncorrectCells++;
                    } else if (j > columns / 2 && !frogAt(i, j)) {
                        numberOfIncorrectCells++;
                    }
                } else if (i > rows / 2 && !frogAt(i, j)) {
                    numberOfIncorrectCells++;
                }
            }
        }
        return 0;
    }
}
