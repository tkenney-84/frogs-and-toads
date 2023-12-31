package net.kenneydiaz.tyler.frogsandtoads;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

/**
 * Displays a mobile application GUI for a player to play a simple instance of the game
 * Frogs and Toads. This instance of the front-end boasts a number of features and
 * curiosities. It contains images representing toads and frogs individually. It
 * contains soothing background music which can be muted and un-muted with a button on
 * the interface. It contains a number of sound effects which can be muted and un-muted
 * with a button on the interface. Note as well that the mute configuration is
 * preserved even when the app is paused or stopped, and all sound is muted upon
 * exiting the application. This instance of the game makes use of a custom
 * AudioManager which also more intelligently handles audio related resources. Moves can
 * also be undone with another button on the interface. It contains the option to
 * launch the game one move away from the winning configuration by tapping on the game
 * title six times.
 *
 * To play Frogs & Toads:
 *
 * A board of odd rows and odd
 * columns is created, and populated with frogs, toads, and an empty space. The objective
 * is to move all the frogs to the top left of the board, and all the toads to the bottom
 * right of the board. This can be done through a series of movements into the empty
 * space. Frogs and toads can jump over each other into the empty space or they can jump
 * into the empty space if it is adjacent. The game is in a losing state if no more legal
 * moves can be made, and the frogs and toads are not in their correct positions. The game
 * enters a winning state if the frogs and toads are in their correct positions. Correct
 * positions being toads at the top left of the board and frogs at the bottom right.
 *
 * Author Notes:
 *
 * - Given recent events, time would likely not permit this to be my best work. I am eager
 * to hear feedback on it. I apologize in advance for any mistakes that may have slipped
 * by.
 *
 * Known Bugs:
 *
 * - When a frog shifts onto a row that contained only toads, the row resizes and vice
 * versa. Minor, but annoying.
 *
 * - The game state is not preserved when orientation is changed.
 *
 * @author Tyler Kenney
 */
public class MainActivity extends AppCompatActivity {

    // The target default dimensions. The reason they are only the "target" dimensions
    // is because the game engine is what actually enforces the game board size. So we
    // are essentially asking it politely to make these the game board dimensions.
    private final static int ROWS_WANTED = 5;
    private final static int COLUMNS_WANTED = 5;

    // If the above target dimensions are found disagreeable by the game engine, these
    // are the values which will store the actual number of rows and columns that the game
    // engine has defined. This is not an optimal system, but it follows with what we
    // discussed in terms of how the game engine should handle invalid inputs.
    private int engineRows;
    private int engineColumns;

    // A variable to reference the audio manager for this instance of the game.
    private AudioManager audioManager;

    // The variables which will reference the instance of the game engine and the
    // current front-end game board.
    private ImageButton[][] buttons;
    private FrogsAndToads game = new FrogsAndToads(ROWS_WANTED, COLUMNS_WANTED);

    // The variables which keep track of progress towards and the goal for generating a
    // debug game.
    private int debug_click_count = 0;
    private static final int debug_activation_count = 6;

    private int currentMoves = 0;
    private boolean showValidMoves = false;

    /**
     * This application is not a music streaming service. Hence, when the app is
     * paused or stopped, all sounds and music are muted and their resources released
     * to be used for whatever apps may need them. Once it is resumed, however,
     * the previous state of the music and sound effect mute options should be restored
     * without the need for user intervention.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Regenerate the audio manager if one is not present. This should never happen
        // but this is here to be extra safe.
        if (audioManager == null) audioManager = new AudioManager(this);
        audioManager.resume();
    }

    /**
     * This application is not a music streaming service. Hence, when the app is
     * paused, all sounds and music should be stopped and their resources released
     * to be used for whatever apps may need them.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Stop and release all audio resources in the audio manager.
        if (audioManager != null) audioManager.suspend();
    }

    /**
     * This application is not a music streaming service. Hence, when the app is
     * paused, all sounds and music should be stopped and their resources released
     * to be used for whatever apps may need them.
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Stop and release all audio resources in the audio manager.
        if (audioManager != null) audioManager.suspend();
    }

    /**
     * Runs the boiler plate code for application creation. Also verifies the rows and
     * columns in terms of the game engine. Initializes background color of the new
     * game button, the size of the front-end game table, audio manager, audio assets,
     * and audio control buttons. Also defines the onclick event handlers for the music
     * toggle button, the sound effects toggle button, the undo button, the new game
     * button, and the game title. Then, fills the front-end game board with the
     * appropriate elements to simulate clickable frogs and toads and an empty space.
     * Finally draws the game board and we are ready to go!
     *
     * @param savedInstanceState a saved instance state if one is used. It is not here.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Boiler Plate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This is to make sure that invalid defaults set here do not mess up the rest
        // of the game because they are invalid defaults to the engine. To prevent it,
        // we simply set the rows and columns used by everything to whatever the engine
        // created.
        engineRows = game.countRows();
        engineColumns = game.countColumns();

        // This is actually an issue with the API version I am using. Buttons cannot
        // have their background colors set in the xml layout, it only works
        // programmatically. Hence, the one Button element (the new game button)s
        // background needs to be set to black here.
        findViewById(R.id.reset_button).setBackgroundColor(
                getResources().getColor(R.color.black, getTheme()));
        findViewById(R.id.show_valid_moves_button).setBackgroundColor(
                getResources().getColor(R.color.black, getTheme()));

        ((Button) findViewById(R.id.show_valid_moves_button)).setText(String.format(
                getResources().getString(R.string.show_valid_moves_button_text),
                ((showValidMoves) ? "HIDE" : "SHOW")));

        // Initializes the front-end game board, which will just be a GUI mirror of the
        // game engines board.
        buttons = new ImageButton[engineRows][engineColumns];

        // Initialize an instance of the audio manager. I played around a lot with the
        // sound here, and the number of functions I was creating got a little large to
        // cram all into here.
        audioManager = new AudioManager(this);
        audioManager.play(R.raw.music, true);

        /* SET DEFAULT AUDIO CONTROL BUTTON IMAGES */

        // Figure out if the audio manager is defaulting to muted or un-muted for music,
        // then properly apply the image to the button that toggles muting the music.
        ((ImageButton) findViewById(R.id.music_toggle_button)).setImageResource(
                audioManager.musicMuted() ?
                        R.drawable.music_muted : R.drawable.music_unmuted);

        // Figure out if the audio manager is defaulting to muted or un-muted for sound
        // effects, then properly apply the image to the button that toggles muting the
        // sound effects.
        ((ImageButton) findViewById(R.id.sfx_toggle_button)).setImageResource(
                audioManager.soundEffectsMuted() ?
                        R.drawable.sfx_muted : R.drawable.sfx_unmuted);

        int numMenuButtons = 5;
        int menuButtonWidth = (int) getEvenWidth(numMenuButtons, 100);
        int menuButtonHeight = (int) getEvenWidth(numMenuButtons, 100);
        Button showValidMovesButton =
                ((Button) findViewById(R.id.show_valid_moves_button));
        Button resetButton = ((Button) findViewById(R.id.reset_button));
        ImageButton soundEffectsToggleButton =
                ((ImageButton) findViewById(R.id.sfx_toggle_button));
        ImageButton musicToggleButton =
                ((ImageButton) findViewById(R.id.music_toggle_button));
        ImageButton undoButton =
                ((ImageButton) findViewById(R.id.undo_button));

        LinearLayout.LayoutParams menuButtonLayoutParams =
                new LinearLayout.LayoutParams(menuButtonWidth, menuButtonHeight);

        showValidMovesButton.setLayoutParams(menuButtonLayoutParams);
        resetButton.setLayoutParams(menuButtonLayoutParams);
        soundEffectsToggleButton.setLayoutParams(menuButtonLayoutParams);
        musicToggleButton.setLayoutParams(menuButtonLayoutParams);
        undoButton.setLayoutParams(menuButtonLayoutParams);

        /* BUTTON EVENT HANDLERS */

        // The reset button is the 'new game' button. Upon clicking it, reset the game
        // board.
        findViewById(R.id.reset_button).setOnClickListener((i) -> resetGameBoard(true));
        findViewById(R.id.show_valid_moves_button).setOnClickListener((i) -> {
            showValidMoves = !showValidMoves;
            drawBoard();
            ((Button) i).setText(String.format(
                    getResources().getString(R.string.show_valid_moves_button_text),
                            ((showValidMoves) ? "HIDE" : "SHOW"))
                    );
        });

        // This is a little secret listener. If you click on the title of the game,
        // this activates.
        findViewById(R.id.game_title).setOnClickListener((i) -> {

            // Add one to the number of times the title was clicked.
            debug_click_count++;

            // If the title was clicked enough times...
            if (debug_click_count >= debug_activation_count) {

                // Start a debug game and draw the board.
                game = new FrogsAndToads(engineRows, engineColumns, true);
                currentMoves = 0;
                drawBoard();

                // Set the number of recorded title clicks to 0 and notify the use that
                // a debug game was created.
                debug_click_count = 0;
                Snackbar.make(findViewById(R.id.table_layout),
                        getResources().getString(R.string.debug_activated),
                        Snackbar.LENGTH_LONG).show();
            } else if (debug_click_count >= debug_activation_count / 2) {

                // Notify the user that they are approaching a debug game if they have
                // done at least half the required clicks.
                Snackbar.make(findViewById(R.id.table_layout),
                        String.format(getResources()
                                        .getString(R.string.debug_approaching),
                                (debug_activation_count - debug_click_count)),
                        Snackbar.LENGTH_LONG).show();
            }
        });

        // The music toggle button's event handler.
        findViewById(R.id.music_toggle_button).setOnClickListener((view) -> {

            // Find out if the music is currently muted.
            boolean musicMuted = audioManager.musicMuted();

            // Toggle the image indicating the mute status of the music.
            ((ImageButton) view).setImageResource(musicMuted ?
                    R.drawable.music_unmuted : R.drawable.music_muted);

            // Toggle the music mute option in the audio manager.
            audioManager.muteMusic(!musicMuted);

            // Let the user know that the music has been muted or un-muted.
            Snackbar.make(findViewById(R.id.table_layout), ((musicMuted) ?
                            R.string.music_unmuted : R.string.music_muted),
                    Snackbar.LENGTH_LONG).show();
        });

        // Toggle sound effects button event handler.
        findViewById(R.id.sfx_toggle_button).setOnClickListener((view) -> {

            // Check to see if sound effects are muted in the current audio manager.
            boolean sfxMuted = audioManager.soundEffectsMuted();

            // Toggle the image indicating the mute status of sound effects.
            ((ImageButton) view).setImageResource(sfxMuted ?
                    R.drawable.sfx_unmuted : R.drawable.sfx_muted);

            // Toggle the sound effects mute option in the audio manager.
            audioManager.muteSoundEffects(!sfxMuted);

            // Let the user know that the sound effects have been muted or un-muted.
            Snackbar.make(findViewById(R.id.table_layout), ((sfxMuted) ?
                            R.string.sfx_unmuted : R.string.sfx_muted),
                    Snackbar.LENGTH_LONG).show();
        });

        // Undo button event handler.
        findViewById(R.id.undo_button).setOnClickListener((i) -> {

            // If an undo can be performed...
            if (game.hasPreviousMove()) {
                game.undo();
                currentMoves--;
                drawBoard();
                Snackbar.make(findViewById(R.id.table_layout), R.string.move_undone,
                        Snackbar.LENGTH_LONG).show();
                audioManager.play(R.raw.undo);
            } else {

                // Indicate that an undo cannot be performed if there are no recorded
                // prior moves left.
                audioManager.play(R.raw.invalid);
                Snackbar.make(findViewById(R.id.table_layout), R.string.illegal_undo_text,
                        Snackbar.LENGTH_LONG).show();
            }
        });

        // Identify the existing table layout and specify the layout parameters for all of
        // our table rows.
        TableLayout tableLayout = findViewById(R.id.table_layout);
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT
        );

        // For every row on the game board...
        for (int i = 0; i < engineRows; i++) {

            // Create a new table row for this game row and provide it with the previously
            // defined parameters.
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(tableRowParams);
            tableRow.setZ(0);

            // For every column of the current row...
            for (int j = 0; j < engineColumns; j++) {

                // Create a new image button in the context of this instance of the game.
                buttons[i][j] = new ImageButton(this);

                // Provide the button a tag which will be used to identify it later.
                buttons[i][j].setTag(new int[]{i, j});

                // Confine the button to the defined button size.
                buttons[i][j].setMaxWidth((int) getEvenWidth(game.countColumns(), 80));
                buttons[i][j].setMaxHeight((int) getEvenHeight(game.countColumns(), 80));
                buttons[i][j].setMinimumWidth((int) getEvenWidth(game.countColumns(),
                        80));
                buttons[i][j].setMinimumHeight((int) getEvenHeight(game.countColumns(),
                        80));

                // Some scaling and styling attributes.
                buttons[i][j].setAdjustViewBounds(true);
                buttons[i][j].setScaleType(ImageView.ScaleType.FIT_CENTER);
                buttons[i][j].setBackgroundColor(getResources()
                        .getColor(R.color.transparent, getTheme()));

                // On clicking the button, run this class' makeMove() method.
                buttons[i][j].setOnClickListener(this::makeMove);

                // Finally add the button to the table row.
                tableRow.addView(buttons[i][j]);
            }

            // Ad the table row to the table layout.
            tableLayout.addView(tableRow);
        }

        // All done! Update the GUI with new front-end mirror of the game engine board.
        drawBoard();
    }

    /**
     * Uses the game engine to attempt to move one of the frogs, and mirrors the result
     * in the front-end GUI. Specifically: this method first checks if the player has
     * already won or lost. If they have, and are attempting to move again, ask them to
     * undo moves or start a new game.
     *
     * @param view the view in which the moving element is present.
     */
    private void makeMove(View view) {

        // Check if the player won or lost the game. If they did, asks them to undo a
        // move or start a new game.
        boolean playerWon = game.over();
        if (playerWon || !game.canMove()) {
            audioManager.play(R.raw.invalid);
            Snackbar.make(findViewById(R.id.table_layout), R.string.waiting_on_reset_text,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        // Get the row and column selected for the move.
        int[] eventTag = (int[]) view.getTag();
        int row = eventTag[0];
        int column = eventTag[1];

        boolean movementIsLegal = false;

        // For all possible legal moves, if the provided coordinates match even one,
        // mark the move as legal.
        for (int[] legalMove : game.getLegalMoves()) {
            if (legalMove[0] == row && legalMove[1] == column) {
                movementIsLegal = true;
                break;
            }
        }

        // If the movement is not in the list of legal movements, indicate an invalid
        // move with a noise and a message to the user.
        if (!movementIsLegal) {
            audioManager.play(R.raw.invalid);
            Snackbar.make(
                    findViewById(R.id.table_layout),
                    String.format(getResources().getString(R.string.illegal_move_text),
                            (game.toadAt(row, column) ?
                                    getResources().getString(R.string.toad_text) :
                                    getResources().getString(R.string.frog_text))),
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        // If the move was valid, play the correct jump noise.
        audioManager.play(game.frogAt(row, column) ?
                R.raw.frog_jump : R.raw.toad_jump);

        // Run the animation for the selected frog or toad.
        animateElement(view, new int[]{row, column});

        // Make the move in the game engine. Assume it was valid.
        game.move(row, column);

        currentMoves++;

        // If the player won or lost, display the game over alert.
        playerWon = game.over();

        if (playerWon || !game.canMove()) {
            showGameOverAlert(playerWon);
        }
    }

    /**
     * Reloads the GUI so it accurately reflects the current state of the game board as
     * according to the game engine. Iterates through every button, checks what it is
     * in the game engine, and updates its image and alt text.
     */
    private void drawBoard() {
        if (currentMoves < 0) currentMoves = 0;
        ((TextView) findViewById(R.id.move_count)).setText(
            String.format(
                    getResources().getString(R.string.move_count), currentMoves)
        );
        for (int i = 0; i < engineRows; i++) {
            for (int j = 0; j < engineColumns; j++) {
                if (showValidMoves && game.moveIsValid(i, j)) {
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.dark_green,
                            getTheme()));
                } else {
                    buttons[i][j].setBackgroundColor(getResources().getColor(R.color.transparent, getTheme()));
                }
                if (game.toadAt(i, j)) {
                    buttons[i][j].setZ(2);
                    buttons[i][j].setImageResource(R.drawable.toad);
                    buttons[i][j].setContentDescription(getResources().getString(R.string.toad_text).toUpperCase());

                } else if (game.frogAt(i, j)) {
                    buttons[i][j].setZ(2);
                    buttons[i][j].setImageResource(R.drawable.frog);
                    buttons[i][j].setContentDescription(getResources().getString(R.string.frog_text).toUpperCase());

                } else {
                    buttons[i][j].setZ(1);
                    buttons[i][j].setImageResource(R.drawable.empty);
                    buttons[i][j].setContentDescription(getResources().getString(R.string.empty_alt));
                }
            }
        }
    }

    /**
     * Generates a fresh instance of the game engine, then updates the GUI to reflect
     * the new board. Allows for the option of asking the user to confirm before a new
     * game is generated, to avoid frustration and lost progress from accidental button
     * presses.
     *
     * @param requireConfirmation a boolean which indicates whether the user should be
     *                            asked to confirm their desire to create a new game or
     *                            not.
     */
    private void resetGameBoard(boolean requireConfirmation) {

        // If we should ask the user to confirm the creation of a new game, display an
        // alert and ask them. If they indicate no, do nothing. If they indicate yes,
        // replace the current instance of the game engine with a new instance and draw
        // the board once more.
        if (requireConfirmation) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.game_over_title).setMessage(R.string.confirm_reset_text);
            builder.setPositiveButton(R.string.confirm_reset_affirm, (dialogInterface, i) -> {
                currentMoves = 0;
                game = new FrogsAndToads(engineRows, engineColumns);
                drawBoard();
            });
            builder.setNegativeButton(R.string.confirm_reset_negative, null);
            builder.create().show();
        } else {

            // If we do not need to ask for confirmation, simply replace the current
            // instance of the game engine with a new one and draw the board so the GUI
            // reflects the new board.
            currentMoves = 0;
            game = new FrogsAndToads(engineRows, engineColumns);
            drawBoard();
        }
    }

    /**
     * A method whose purpose is the displaying of a game over alert. It dynamically
     * displays a win or lose message depending on the provided boolean. A victory or
     * loss sound accompanies the alert, and the player is presented the option to
     * observe the game over state of the game board or immediate generate a new game.
     *
     * @param isWon a boolean that indicates whether the player has won or lost.
     */
    private void showGameOverAlert(boolean isWon) {

        // Play the audio which accompanies the loss or victory.
        int playerID = audioManager.play((isWon) ? R.raw.win : R.raw.lose);

        // Display an alert informing the player that they won or lost. Once any button
        // is clicked, stop the victory/loss audio.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.game_over_title).setMessage((isWon) ?
                R.string.game_over_victory : R.string.game_over_failure);
        builder.setPositiveButton(R.string.affirm_button_text,
                (dialogInterface, i) -> audioManager.release(playerID));
        builder.setNegativeButton(R.string.reset_button_text, (dialogInterface, i) -> {
                    audioManager.release(playerID);
                    resetGameBoard(false);
                });
        builder.setOnDismissListener(
                (onDismissListener) -> audioManager.release(playerID));
        builder.create().show();
    }

    private float getEvenWidth(int numItems, int maxPercentageOfScreen) {
//        float padding = getResources().getDimension(R.dimen.standard_gap);
        float screenWidth = getResources().getDisplayMetrics().widthPixels;

        return ((screenWidth * ((float) maxPercentageOfScreen / 100))) / numItems;
    }

    private float getEvenHeight(int numItems, int maxPercentageOfScreen) {
//        float padding = getResources().getDimension(R.dimen.standard_gap);
        float screenHeight = getResources().getDisplayMetrics().heightPixels;

        return ((screenHeight * ((float) maxPercentageOfScreen / 100))) / numItems;
    }

    /**
     * Determines which direction and how significant the jump being made by the
     * element is. Once this is determined, the animation is run on the element at the
     * provided move location.
     *
     * @param view the view in which the moving element is present.
     * @param moveLocation an integer array whose first element is the row of the
     *                     selected move and second element is the y of the selected move.
     */
    private void animateElement(View view, int[] moveLocation) {

        // Identifies the move row and column and the animation that needs to be run.
        int row = moveLocation[0];
        int column = moveLocation[1];
        int animationID;
        if (game.emptyAt(row - 1, column)) {
            animationID = R.anim.jump_up_one_tile;
        } else if (game.emptyAt(row, column - 1)) {
            animationID = R.anim.jump_left_one_tile;
        } else if (game.emptyAt(row, column + 1)) {
            animationID = R.anim.jump_right_one_tile;
        } else if (game.emptyAt(row + 1, column)) {
            animationID = R.anim.jump_down_one_tile;
        } else if (game.emptyAt(row - 2, column)) {
            animationID = R.anim.jump_up_three_tiles;
        } else if (game.emptyAt(row, column - 2)) {
            animationID = R.anim.jump_left_three_tiles;
        } else if (game.emptyAt(row, column + 2)) {
            animationID = R.anim.jump_right_three_tiles;
        } else if (game.emptyAt(row + 2, column)) {
            animationID = R.anim.jump_down_three_tiles;
        } else {
            animationID = R.anim.jump_up_one_tile;
        }

        // Load the animation.
        Animation animation = AnimationUtils.loadAnimation(this, animationID);

        // Set the animation listener to remove the view after the animation ends
        animation.setAnimationListener(new Animation.AnimationListener() {

            /**
             * Runs on the start of the animation. This is never run in this code.
             * @param animation the animation being started.
             */
            @Override
            public void onAnimationStart(Animation animation) {
            }

            /**
             * Updates the GUI board on the end of the animation.
             * @param animation the animation that is ending.
             */
            @Override
            public void onAnimationEnd(Animation animation) {
                drawBoard();
            }

            /**
             * Runs when the animation is set to repeat. It never is in this code.
             * @param animation the animation being repeated.
             */
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // Start the animation.
        view.startAnimation(animation);
    }

}