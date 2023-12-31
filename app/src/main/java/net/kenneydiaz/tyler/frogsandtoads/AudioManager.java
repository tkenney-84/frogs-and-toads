package net.kenneydiaz.tyler.frogsandtoads;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A class which allows for the easy and dynamic playing of audio files as well as the
 * efficient management of audio resources. Audio for any application can be played,
 * released, suspended, resumed, or any number of other options with this one manager.
 *
 * Author Notes:
 *
 * This is an improved version of the AudioManager I submitted for Homework 2. This
 * manager no longer makes a distinction between sound effects and music (functionally).
 * It now allows for the one time playing of audio, or the infinite playing of audio.
 * It is capable of "pausing" and "resuming" indefinitely looping audio without the
 * need to hold onto any MediaPlayer resource. It is also capable of hosting multiple
 * different or identical sounds, while still maintaining an adjustable limit to
 * prevent the over-use of resources. It still allows for the global muting and
 * un-muting of "music" (which is just how indefinitely looping audio is referred to)
 * and sound effects (which is just how one-time audio is referred to).
 *
 * Known Bugs:
 *
 * - None
 *
 * @author Tyler Kenney
 */
public class AudioManager {

    // The variable which will reference the context this audio manager is tied to.
    private final Context context;

    // The variables which reference the options to have music and/or sound effects
    // muted and/or un-muted.
    private boolean muteMusic;
    private boolean muteSFX;

    // A hash map used to store and manage all active media players.
    private final Map<Integer, MediaPlayer> activePlayers = new HashMap<>();

    // A hash map used to maintain the relation between active media players and the
    // resource ID of the media they are playing. This is used for more efficient
    // suspension and resuming of the AudioManager.
    private final Map<Integer, Integer> musicResources = new HashMap<>();

    // A hash map used to store the resource IDs and the current playback time of all
    // looping audio that was playing when the suspend() method was called.
    private final Map<Integer, Integer> suspendedMusic = new HashMap<>();

    // The maximum number of concurrent media players.
    private final int maxActiveMediaPlayers;

    // Default values for the mute options for both the music and the sound effects.
    private final static boolean DEFAULT_MUTE_MUSIC = true;
    private final static boolean DEFAULT_MUTE_SFX = false;

    // The default number of maximum concurrent media players.
    private final static int DEFAULT_MAX_ACTIVE_MEDIA_PLAYERS = 10;

    // The tag used to identify logs of this class.
    private final static String LOG_TAG = "AudioManager";

    // The designated integer sentinel value which is used to signal an error on method
    // return.
    private final static int ERROR_INDICATOR = -1;

    /**
     *
     *
     * @param context the context in which this instance of the audio manager is
     *                operating.
     */
    public AudioManager(Context context) {
        this(context, DEFAULT_MAX_ACTIVE_MEDIA_PLAYERS);
    }

    /**
     *
     * @param context
     * @param maxActiveMediaPlayers
     */
    public AudioManager(Context context, int maxActiveMediaPlayers) {
        this(context, maxActiveMediaPlayers, DEFAULT_MUTE_MUSIC, DEFAULT_MUTE_SFX);
    }

    /**
     *
     * @param context
     * @param maxActiveMediaPlayers
     * @param muteMusic
     * @param muteSFX
     */
    public AudioManager(Context context, int maxActiveMediaPlayers, boolean muteMusic,
                        boolean muteSFX) {
        this.maxActiveMediaPlayers = maxActiveMediaPlayers;
        this.muteMusic = muteMusic;
        this.muteSFX = muteSFX;
        this.context = context;
    }

    /**
     * Sets the state of the mute option for all music in this instance of audio manager.
     *
     * @param muteMusic the boolean which controls whether all music in this instance
     *                  of audio manager is muted.
     */
    public void muteMusic(boolean muteMusic) {

        this.muteMusic = muteMusic;

        activePlayers.forEach((key, player) -> {
            if (player.isLooping()) {
                if (muteMusic) {
                    player.pause();
                } else {
                    player.start();
                }
            }
        });

    }

    /**
     * Sets the state of the boolean which controls whether all sound effects are muted
     * or un-muted. If any sound effects are playing when this is called, they will
     * immediately be released (and subsequently stopped).
     *
     * @param muteSFX the boolean which controls whether all sound effects in this
     *                instance of audio manager are muted.
     */
    public void muteSoundEffects(boolean muteSFX) {
        this.muteSFX = muteSFX;

        // For each sound effect in the sound effect hash map, stop the associated
        // media player if sound effects are now muted.
        if (muteSFX) {
            List<Integer> playersToRelease = new ArrayList<>();
            activePlayers.forEach((key, player) -> {
                if (!player.isLooping()) {
                    playersToRelease.add(key);
                }
            });
            playersToRelease.forEach(this::release);
        }
    }

    /**
     * This function can be used to check if music is currently muted in this instance
     * of audio manager.
     *
     * @return true if all music in this instance of audio manager is muted. False
     *         otherwise.
     */
    public boolean musicMuted() {
        return muteMusic;
    }

    /**
     * This function can be used to check if sound effects are currently muted in this
     * instance of audio manager.
     *
     * @return true if all sound effects in this instance of audio manager are muted.
     *         False otherwise.
     */
    public boolean soundEffectsMuted() {
        return muteSFX;
    }

    /**
     *
     * @param resourceID
     * @return
     */
    public int play(int resourceID) {
        return play(resourceID, false);
    }

    public int play(int resourceID, boolean loopAudioIndefinitely) {

        // If the provided resource ID is valid.
        if (isValidResourceId(resourceID)) {

            // Get an available identifier.
            int identifier = getUnusedUnsignedIdentifier();

            // If sound effects are not muted and the returned identifier does not
            // indicate that we've hit the limit of available media players.
            if (identifier != ERROR_INDICATOR) {

                // Create a media player for this piece of media.
                MediaPlayer player = MediaPlayer.create(context, resourceID);
                player.setOnCompletionListener((listener) -> release(identifier));
                player.setOnErrorListener((object, what, extra) -> {
                    Log.e(LOG_TAG, "Unknown error occurred when trying to play audio!");

                    // A boolean is expected to indicate whether the error was handled.
                    // Since this is false, the on Completion listener will be called
                    // to wrap up. If this was true, the onCompletion listener would
                    // not be called.
                    return false;
                });
                activePlayers.put(identifier, player);
                player.start();
                if (loopAudioIndefinitely) {
                    musicResources.put(identifier, resourceID);
                    player.setLooping(true);
                    if (muteMusic) player.pause();
                } else if (muteSFX) {
                    release(identifier);
                }
                return identifier;
            } else {
                Log.w(LOG_TAG ,"MediaPlayer limit reached!");
            }
        } else {
            Log.e(LOG_TAG, "Resource ID " + resourceID + " not found!");
        }
        return ERROR_INDICATOR;
    }


    public void suspend() {
        activePlayers.forEach((key, player) -> {
            if (player.isLooping()) {
                suspendedMusic.put(musicResources.get(key), player.getCurrentPosition());
            }
        });
        release();
    }

    public void resume() {
        suspendedMusic.forEach((resourceId, pausedAt) -> {
            try {
                if (isValidResourceId(resourceId)) {
                    Objects.requireNonNull(activePlayers.get(play(resourceId, true)))
                            .seekTo(pausedAt);
                } else {
                    throw new NullPointerException();
                }
            } catch (NullPointerException exception) {
                Log.e(LOG_TAG, "Could not find resource ID " + resourceId + " when " +
                        "resuming player!");
            }
        });
        suspendedMusic.clear();
    }

    public void release() {
        for (int i = 0; i < maxActiveMediaPlayers; i++) {
            release(i);
        }
    }

    public void release(int soundIdentifier) {
        if (activePlayers.containsKey(soundIdentifier)) {
            MediaPlayer player = activePlayers.get(soundIdentifier);
            if (player != null) {
                if (player.isLooping()) musicResources.remove(soundIdentifier);
                player.reset();
                player.release();
            }
            activePlayers.remove(soundIdentifier);
        }

    }

    private boolean isValidResourceId(int resourceID) {

        if (resourceID <= 0) {
            return false;
        }

        if (context == null) {
            return false;
        }

        try {
            context.getResources().getResourceName(resourceID);
        } catch (Resources.NotFoundException exception) {
            return false;
        }

        return true;
    }

    private int getUnusedUnsignedIdentifier() {
        for (int i = 0; i < maxActiveMediaPlayers; i++) {
            if (!activePlayers.containsKey(i)) {
                return i;
            }
        }
        return ERROR_INDICATOR;
    }
}
