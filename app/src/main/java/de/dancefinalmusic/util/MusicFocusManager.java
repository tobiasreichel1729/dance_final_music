package de.dancefinalmusic.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

public class MusicFocusManager {

    public interface FocusListener {
        void onFocusLoss();
        void onFocusGain();
    }

    private final AudioManager audioManager;
    private final FocusListener listener;
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener;
    private AudioFocusRequest focusRequest;
    private boolean hasFocus;

    public MusicFocusManager(Context context, FocusListener listener) {
        this.audioManager = (AudioManager) context.getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
        this.listener = listener;
        this.focusChangeListener = state -> {
            switch (state) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    listener.onFocusLoss();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    listener.onFocusGain();
                    break;
            }
        };
    }

    public boolean request() {
        if (hasFocus) return true;
        AudioFocusRequest.Builder builder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setOnAudioFocusChangeListener(focusChangeListener);
        focusRequest = builder.build();
        int result = audioManager.requestAudioFocus(focusRequest);
        hasFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        return hasFocus;
    }

    public void abandon() {
        if (focusRequest != null && hasFocus) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
        focusRequest = null;
        hasFocus = false;
    }
}
