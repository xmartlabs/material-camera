package com.afollestad.materialcamera;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import com.afollestad.materialcamera.internal.BaseCaptureActivity;
import com.afollestad.materialcamera.internal.CameraFragment;

public class CaptureActivity extends BaseCaptureActivity {
  public static final float OLDER_DEVICES_ASPECT_RATIO = 4f / 3f;

  @Override
  @NonNull
  public Fragment getFragment() {
    return CameraFragment.newInstance();
  }

  @Override
  public float getScreenRatio() {
    return OLDER_DEVICES_ASPECT_RATIO;
  }
}
