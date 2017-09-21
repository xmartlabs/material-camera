package com.afollestad.materialcamera.internal;

/**
 * Created by diegomedina24 on 27/9/17.
 */
public interface Action<T> {
  void perform(T value);
}
