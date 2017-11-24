package com.afollestad.materialcamera.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Collection;

@SuppressWarnings("deprecation")
@SuppressLint("ViewConstructor")
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

  private static final String TAG = "SF-CameraPreview";

  protected final SurfaceHolder mHolder;
  private final Camera mCamera;
  private final Collection<Camera.Size> mSupportedPreviewSizes;
  private Camera.Size mPreviewSize;

  public Camera.Size getmPreviewSize() {
    return mPreviewSize;
  }

  public CameraPreview(Context context, Camera camera) {
    super(context);
    mCamera = camera;
    mHolder = getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    try {
      mCamera.setPreviewDisplay(holder);
      mCamera.startPreview();
    } catch (Throwable e) {
      Log.d(TAG, "Error setting camera preview: " + e.getMessage());
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    mHolder.removeCallback(this);
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    if (mHolder.getSurface() == null) {
      return;
    }
    try {
      mCamera.stopPreview();
    } catch (Exception ignored) {
    }
    try {
      Camera.Parameters parameters = mCamera.getParameters();
      parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
      mCamera.setParameters(parameters);
      mCamera.startPreview();
    } catch (Exception e) {
      Log.d(TAG, "Error starting camera preview: " + e.getMessage());
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    final int height = MeasureSpec.getSize(heightMeasureSpec);
    setMeasuredDimension(width, height);
    if (mSupportedPreviewSizes != null) {
      mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
    }
  }

  private Camera.Size getOptimalPreviewSize(Collection<Camera.Size> sizes, int width, int height) {
    final double ASPECT_TOLERANCE = 0.1;
    double targetRatio = (double) height / width;

    if (sizes == null) {
      return null;
    }

    Camera.Size optimalSize = null;
    double minDiff = Double.MAX_VALUE;

    for (Camera.Size size : sizes) {
      double ratio = (double) size.width / size.height;
      if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
        continue;
      }
      if (Math.abs(size.height - height) < minDiff) {
        optimalSize = size;
        minDiff = Math.abs(size.height - height);
      }
    }

    if (optimalSize == null) {
      minDiff = Double.MAX_VALUE;
      for (Camera.Size size : sizes) {
        if (Math.abs(size.height - height) < minDiff) {
          optimalSize = size;
          minDiff = Math.abs(size.height - height);
        }
      }
    }
    return optimalSize;
  }
}
