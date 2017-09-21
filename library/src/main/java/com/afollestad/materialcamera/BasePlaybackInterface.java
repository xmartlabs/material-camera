package com.afollestad.materialcamera;

import android.support.annotation.Nullable;

/**
 * Created by diegomedina24 on 21/9/17.
 */
public interface BasePlaybackInterface {
  long getRecordingEnd();
  void onRetry(@Nullable String outputUri);
  void useMedia(String uri);
}
