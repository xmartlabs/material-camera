package com.afollestad.materialcamera.internal;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.afollestad.easyvideoplayer.EasyVideoPlayer;

/**
 * Created by diegomedina24 on 18/9/17.
 */
public class CameraVideoPlayer extends EasyVideoPlayer {
  private Action<Integer> mOnProgressChanged;

  public CameraVideoPlayer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setOnProgressChanged(@NonNull Action<Integer> onProgressChanged) {
    mOnProgressChanged = onProgressChanged;
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

  }

  @Override
  public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {

  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
    super.onProgressChanged(seekBar, value, fromUser);

    if (mOnProgressChanged != null) {
      mOnProgressChanged.perform(value);
    }
  }
}
