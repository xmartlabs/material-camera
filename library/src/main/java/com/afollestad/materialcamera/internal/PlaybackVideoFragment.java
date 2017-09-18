package com.afollestad.materialcamera.internal;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.materialcamera.R;
import com.afollestad.materialcamera.util.CameraUtil;
import com.afollestad.materialdialogs.MaterialDialog;

/** @author Aidan Follestad (afollestad) */
public class PlaybackVideoFragment extends Fragment
    implements CameraUriInterface, EasyVideoCallback {

  private CameraVideoPlayer mPlayer;
  private ImageButton mPlayButton;
  private Button mRetryButton;
  private Button mUseVideoButton;
  private String mOutputUri;
  private BaseCaptureInterface mInterface;

  private Handler mCountdownHandler;
  private final Runnable mCountdownRunnable =
      new Runnable() {
        @Override
        public void run() {
          if (mPlayer != null) {
            long diff = mInterface.getRecordingEnd() - System.currentTimeMillis();
            if (diff <= 0) {
              useVideo();
              return;
            }
            mPlayer.setBottomLabelText(String.format("-%s", CameraUtil.getDurationString(diff)));
            if (mCountdownHandler != null) mCountdownHandler.postDelayed(mCountdownRunnable, 200);
          }
        }
      };

  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mInterface = (BaseCaptureInterface) activity;
  }

  public static PlaybackVideoFragment newInstance(
      String outputUri, boolean allowRetry, int primaryColor) {
    PlaybackVideoFragment fragment = new PlaybackVideoFragment();
    fragment.setRetainInstance(true);
    Bundle args = new Bundle();
    args.putString("output_uri", outputUri);
    args.putBoolean(CameraIntentKey.ALLOW_RETRY, allowRetry);
    args.putInt(CameraIntentKey.PRIMARY_COLOR, primaryColor);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (getActivity() != null)
      getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mPlayer != null) {
      mPlayer.release();
      mPlayer.reset();
      mPlayer = null;
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.mcam_fragment_videoplayback, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setupPlayer(view);
    setupButtons(view);
  }

  private void setupButtons(@NonNull View view) {
    mRetryButton = view.findViewById(R.id.retryButton);
    mUseVideoButton = view.findViewById(R.id.useVideoButton);
    mPlayButton = view.findViewById(R.id.playButton);

    setupRetryButton();
    setupUseVideoButton();
    setupPlayButton();
  }

  private void setupRetryButton() {
    mRetryButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        retryVideo();
      }
    });
  }

  private void setupUseVideoButton() {
    mUseVideoButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        useVideo();
      }
    });
  }

  private void setupPlayButton() {
    mPlayButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        @DrawableRes int buttonRes = mPlayer.isPlaying()
            ? R.drawable.ic_icon_preview_play
            : R.drawable.ic_icon_preview_pause;
        mPlayButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), buttonRes));

        if (mPlayer.isPlaying()) {
          mPlayer.pause();
        } else {
          mPlayer.start();
        }
      }
    });
  }

  private void setupPlayer(@NonNull View view) {
    mPlayer = view.findViewById(R.id.playbackView);
    mPlayer.setCallback(this);
    mPlayer.hideControls();
    mPlayer.setLoop(false);
    mPlayer.disableControls();

    mOutputUri = getArguments().getString("output_uri");

    mPlayer.setSource(Uri.parse(mOutputUri));
  }

  private void startCountdownTimer() {
    if (mCountdownHandler == null) mCountdownHandler = new Handler();
    else mCountdownHandler.removeCallbacks(mCountdownRunnable);
    mCountdownHandler.post(mCountdownRunnable);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (mCountdownHandler != null) {
      mCountdownHandler.removeCallbacks(mCountdownRunnable);
      mCountdownHandler = null;
    }
    if (mPlayer != null) {
      mPlayer.release();
      mPlayer = null;
    }
  }

  private void useVideo() {
    if (mPlayer != null) {
      mPlayer.release();
      mPlayer = null;
    }
    if (mInterface != null) mInterface.useMedia(mOutputUri);
  }

  @Override
  public String getOutputUri() {
    return getArguments().getString("output_uri");
  }

  @Override
  public void onStarted(EasyVideoPlayer player) {}

  @Override
  public void onPaused(EasyVideoPlayer player) {}

  @Override
  public void onPreparing(EasyVideoPlayer player) {}

  @Override
  public void onPrepared(EasyVideoPlayer player) {}

  @Override
  public void onBuffering(int percent) {}

  @Override
  public void onError(EasyVideoPlayer player, Exception e) {
    new MaterialDialog.Builder(getActivity())
        .title(R.string.mcam_error)
        .content(e.getMessage())
        .positiveText(android.R.string.ok)
        .show();
  }

  @Override
  public void onCompletion(EasyVideoPlayer player) {
    mPlayButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_icon_preview_play));
  }

  @Override
  public void onRetry(EasyVideoPlayer player, Uri source) {
    retryVideo();
  }

  private void retryVideo() {
    if (mInterface != null) mInterface.onRetry(mOutputUri);
  }

  @Override
  public void onSubmit(EasyVideoPlayer player, Uri source) {
    useVideo();
  }
}
