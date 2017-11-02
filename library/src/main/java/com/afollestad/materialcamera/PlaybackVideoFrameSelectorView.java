package com.afollestad.materialcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diegomedina24 on 27/9/17.
 */
public class PlaybackVideoFrameSelectorView extends LinearLayout {
  private static final int STEP_COUNT = 14;
  private static final int MILLI_TO_MICRO_RATIO = 1000;

  private RelativeLayout mLayout;
  private LinearLayout mFrameHolder;
  private RelativeLayout mSelectorView;
  private View mCurrentPositionMarker;
  private View mStartMarker;
  private View mEndMarker;
  private long mDuration;

  public PlaybackVideoFrameSelectorView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    View view = LayoutInflater.from(context).inflate(R.layout.mcam_view_playback_video_frame_selector, this, true);
    mLayout = view.findViewById(R.id.container);
    mFrameHolder = mLayout.findViewById(R.id.frameHolder);
    mSelectorView = mLayout.findViewById(R.id.selectorView);
    mCurrentPositionMarker = mLayout.findViewById(R.id.currentPosition);
    mStartMarker = mLayout.findViewById(R.id.startPosition);
    mEndMarker = mLayout.findViewById(R.id.endPosition);
  }

  public void setupFrames(final String filePath) {
    post(new Runnable() {
      @Override
      public void run() {
        addFrames(filePath);
      }
    });
  }

  private void addFrames(final String filePath) {
    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

    try {
      metadataRetriever.setDataSource(Uri.parse(filePath).getPath());
      mDuration = Long.valueOf(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

      long step = convertToMicro(mDuration) / STEP_COUNT;
      int width = mSelectorView.getWidth() / STEP_COUNT;

      FrameCreationAsyncTask task = new FrameCreationAsyncTask(metadataRetriever, filePath, step,
          width, mSelectorView.getHeight());
      task.execute();
    } catch (Exception e) {
      Log.e("ERROR_SELECTING_FRAME", e.getMessage());
    }
  }

  private boolean mIsScrolling;
  ViewConfiguration vc = ViewConfiguration.get(getContext());
  private int mTouchSlop = vc.getScaledTouchSlop();
  private float startOfTouchScroll = 0f;
  private float startXOffset = 0f;
  private float endXOffset = 0f;
  private boolean movingStart;
  private boolean movingEnd;

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    final int action = MotionEventCompat.getActionMasked(ev);

    if (action == MotionEvent.ACTION_DOWN) {
      startOfTouchScroll = ev.getX();
      if (Math.abs(startOfTouchScroll - endXOffset) < 30) {
        movingEnd = true;
      } else if (Math.abs(startOfTouchScroll - startXOffset) < 30) {
        movingStart = true;
      }
    }

    // Always handle the case of the touch gesture being complete.
    if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
      // Release the scroll.
      mIsScrolling = false;
      movingEnd = false;
      movingStart = false;
      return false; // Do not intercept touch event, let the child handle it
    }

    switch (action) {
      case MotionEvent.ACTION_MOVE: {
        // left as an exercise for the reader
        int xDiff = (int) Math.abs(ev.getX() - startOfTouchScroll);

        // Touch slop should be calculated using ViewConfiguration
        // constants.
        if (xDiff > mTouchSlop) {
          // Start scrolling!
          mIsScrolling = true;
          if (movingStart) {
            startXOffset = ev.getX();
            mStartMarker.setX(startXOffset);
            movingStart = false;
          } else if (movingEnd){
            endXOffset = ev.getX();
            mEndMarker.setX(endXOffset);
            movingEnd = false;
          }

          return true;
        }
        break;
      }
    }

    // In general, we don't want to intercept touch events. They should be
    // handled by the child view.
    return false;
  }

  private long convertToMicro(long durationInMilli) {
    return durationInMilli * MILLI_TO_MICRO_RATIO;
  }

  public void setMarkerAtTime(long timeInMillis) {
    long width = mSelectorView.getWidth() - mCurrentPositionMarker.getWidth();
    long x = timeInMillis * width / mDuration;
    mCurrentPositionMarker.setX(x);
    mCurrentPositionMarker.setVisibility(VISIBLE);
  }

  private class FrameCreationAsyncTask extends AsyncTask<Void, Void, List<Bitmap>> {
    private MediaMetadataRetriever metadataRetriever;
    private String filePath;
    private long step;
    private int width;
    private int height;

    FrameCreationAsyncTask(MediaMetadataRetriever metadataRetriever, String filePath, long step, int width, int height) {
      this.metadataRetriever = metadataRetriever;
      this.filePath = filePath;
      this.step = step;
      this.width = width;
      this.height = height;
    }

    @Override
    protected List<Bitmap> doInBackground(Void... params) {
      List<Bitmap> bitmaps = new ArrayList<>();

      try {
        metadataRetriever.setDataSource(Uri.parse(filePath).getPath());
        mDuration = Long.valueOf(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        for (int i = 0; i < STEP_COUNT; i++) {
          Bitmap bitmap = metadataRetriever.getFrameAtTime(step * i);
          bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
          bitmaps.add(bitmap);
        }
      } catch (Exception e) {
        Log.e("ERROR_SELECTING_FRAME", e.getMessage());
      }

      return bitmaps;
    }

    @Override
    protected void onPostExecute(List<Bitmap> bitmaps) {
      try {
        for (int i = 0; i < STEP_COUNT; i++) {
          ImageView view =  new ImageView(mLayout.getContext());
          view.setBackgroundColor(ContextCompat.getColor(mLayout.getContext(), R.color.white));
          view.setLayoutParams(new LinearLayout.LayoutParams(width, mLayout.getHeight()));
          view.setScaleType(ImageView.ScaleType.FIT_XY);
          view.setImageBitmap(bitmaps.get(i));
          mFrameHolder.addView(view);
        }
      } catch (Exception e) {
        Log.e("ERROR_SELECTING_FRAME", e.getMessage());
      }
    }
  }
}
