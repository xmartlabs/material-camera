package com.afollestad.materialcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
  private LinearLayout mSelectorView;
  private View mCurrentPositionMarker;
  private long mDuration;

  public PlaybackVideoFrameSelectorView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    View view = LayoutInflater.from(context).inflate(R.layout.mcam_view_playback_video_frame_selector, this, true);
    mLayout = view.findViewById(R.id.container);
    mFrameHolder = mLayout.findViewById(R.id.frameHolder);
    mSelectorView = mLayout.findViewById(R.id.selectorView);
    mCurrentPositionMarker = mLayout.findViewById(R.id.currentPosition);
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
