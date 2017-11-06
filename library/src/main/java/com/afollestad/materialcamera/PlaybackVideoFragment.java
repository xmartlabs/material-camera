package com.afollestad.materialcamera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcamera.internal.CameraIntentKey;
import com.afollestad.materialdialogs.MaterialDialog;

import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnK4LVideoListener;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;

/** @author Aidan Follestad (afollestad) */
public class PlaybackVideoFragment extends Fragment
    implements OnTrimVideoListener, OnK4LVideoListener {
  public static final int VIDEO_MAX_DURATION = 30;

  private BasePlaybackInterface mInterface;
  private String mOutputUri;

  private K4LVideoTrimmer mVideoTrimmer;
  private ProgressDialog mProgressDialog;

  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mInterface = (BasePlaybackInterface) activity;
  }

  public static PlaybackVideoFragment newInstance(String outputUri, boolean allowRetry) {
    PlaybackVideoFragment fragment = new PlaybackVideoFragment();
    fragment.setRetainInstance(true);
    Bundle args = new Bundle();
    args.putString("output_uri", outputUri);
    args.putBoolean(CameraIntentKey.ALLOW_RETRY, allowRetry);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (getActivity() != null) {
      getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mVideoTrimmer != null) {
      mProgressDialog.cancel();
      mVideoTrimmer.destroy();
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_trimmer, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupPlayer(view);
  }

  private void setupPlayer(@NonNull View view) {
    mOutputUri = getArguments().getString("output_uri");

    //setting progressbar
    mProgressDialog = new ProgressDialog(getContext());
    mProgressDialog.setCancelable(false);
    mProgressDialog.setMessage(getString(R.string.trimming_progress));

    mVideoTrimmer = view.findViewById(R.id.timeLine);
    if (mVideoTrimmer != null) {
      mVideoTrimmer.setMaxDuration(VIDEO_MAX_DURATION);
      mVideoTrimmer.setOnTrimVideoListener(this);
      mVideoTrimmer.setOnK4LVideoListener(this);
      mVideoTrimmer.setVideoURI(Uri.parse(mOutputUri));
    }
  }

  @Override
  public void onVideoPrepared() {

  }

  @Override
  public void onTrimStarted() {
    mProgressDialog.show();
  }

  @Override
  public void getResult(Uri uri) {
    mProgressDialog.cancel();
    mInterface.useMedia(uri.getPath());
  }

  @Override
  public void cancelAction() {
    mProgressDialog.cancel();
    mVideoTrimmer.destroy();
    if (mInterface != null) {
      mInterface.onRetry(mOutputUri);
    }
  }

  @Override
  public void onError(String errorMessage) {
    if (getActivity() != null) {
      mProgressDialog.cancel();
      new MaterialDialog.Builder(getActivity())
          .title(R.string.mcam_error)
          .content(errorMessage)
          .positiveText(android.R.string.ok)
          .show();
    }
  }
}
