package com.afollestad.materialcamera;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.afollestad.materialcamera.internal.BaseCaptureActivity;
import com.afollestad.materialcamera.internal.Camera2Fragment;

public class CaptureActivity2 extends BaseCaptureActivity {

  @Override
  @NonNull
  public Fragment getFragment() {
    return Camera2Fragment.newInstance();
  }
}
