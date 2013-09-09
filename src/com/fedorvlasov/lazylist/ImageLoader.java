package com.fedorvlasov.lazylist;


import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.widget.ImageView;
import com.voidberg.mediapicker.R;

public class ImageLoader {

  MemoryCache memoryCache = new MemoryCache();
  FileCache fileCache;
  private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
  ExecutorService executorService;

  public ImageLoader(Context context) {
    fileCache=new FileCache(context);
    executorService = Executors.newFixedThreadPool(5);
  }

  final int stub_id = R.drawable.stub;

  public void DisplayImage(int id, int type, ImageView imageView) {
    imageViews.put(imageView, id + "-" + type);
    Bitmap bitmap = memoryCache.get(id + "-" + type);
    if (bitmap != null) {
      imageView.setImageBitmap(bitmap);
    }
    else {
      queuePhoto(id, type, imageView);
      imageView.setImageResource(stub_id);
    }
  }

  private void queuePhoto(int id, int type, ImageView imageView) {
    PhotoToLoad p = new PhotoToLoad(id, type, imageView);
    executorService.submit(new PhotosLoader(p));
  }

  private Bitmap getBitmap(int id, int type, Context context) {
    Bitmap thumb = null;

    File f = fileCache.getFile(id + "-" + type);

    FileInputStream bStream = null;
    try {
      bStream = new FileInputStream(f);
      thumb = BitmapFactory.decodeStream(bStream);
      bStream.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (thumb != null) {
      return thumb;
    }

    try {
      if (type == 0) {
        thumb = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
      }
      else {
        thumb = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
      }

      OutputStream oStream = new FileOutputStream(f);
      thumb.compress(Bitmap.CompressFormat.PNG, 100, oStream);
      oStream.flush();
      oStream.close();
    } catch (Throwable ex) {
      ex.printStackTrace();
      if (ex instanceof OutOfMemoryError)
        memoryCache.clear();
    }
    return thumb;
  }

  //Task for the queue
  private class PhotoToLoad {
    public int id;
    public int type;
    public ImageView imageView;

    public PhotoToLoad(int id, int type, ImageView i) {
      this.id = id;
      this.type = type;
      this.imageView = i;
    }
  }

  class PhotosLoader implements Runnable {
    PhotoToLoad photoToLoad;

    PhotosLoader(PhotoToLoad photoToLoad) {
      this.photoToLoad = photoToLoad;
    }

    @Override
    public void run() {
      if (imageViewReused(photoToLoad)) {
        return;
      }

      Bitmap bmp = getBitmap(photoToLoad.id, photoToLoad.type, photoToLoad.imageView.getContext());
      memoryCache.put(photoToLoad.id + "-" + photoToLoad.type, bmp);
      if (imageViewReused(photoToLoad)) {
        return;
      }
      BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
      Activity a = (Activity) photoToLoad.imageView.getContext();
      a.runOnUiThread(bd);
    }
  }

  boolean imageViewReused(PhotoToLoad photoToLoad) {
    String tag = imageViews.get(photoToLoad.imageView);
    if (tag == null || !tag.equals(photoToLoad.id + "-" + photoToLoad.type)) {
      return true;
    }
    return false;
  }

  class BitmapDisplayer implements Runnable {
    Bitmap bitmap;
    PhotoToLoad photoToLoad;

    public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
      bitmap = b;
      photoToLoad = p;
    }

    public void run() {
      if (imageViewReused(photoToLoad))
        return;
      if (bitmap != null)
        photoToLoad.imageView.setImageBitmap(bitmap);
      else
        photoToLoad.imageView.setImageResource(stub_id);
    }
  }

  public void clearCache() {
    memoryCache.clear();
    fileCache.clear();
  }

}
