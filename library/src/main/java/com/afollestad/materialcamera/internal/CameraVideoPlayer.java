package com.afollestad.materialcamera.internal;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.AttributeSet;

import com.afollestad.easyvideoplayer.EasyVideoPlayer;

/**
 * Created by diegomedina24 on 18/9/17.
 */
public class CameraVideoPlayer extends EasyVideoPlayer {
  public CameraVideoPlayer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

  }

  @Override
  public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {

  }
}
