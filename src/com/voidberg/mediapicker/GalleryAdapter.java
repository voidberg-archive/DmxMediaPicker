package com.voidberg.mediapicker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.voidberg.mediapicker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: andu
 * Date: 10/9/12
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class GalleryAdapter extends ArrayAdapter<Gallery> {
  ArrayList<Gallery> galleries;
  Context context;
  int layoutItem;

  public GalleryAdapter(Context context, int textViewResourceId, Gallery[] objects) {
    super(context, textViewResourceId, objects);
    this.galleries = new ArrayList<Gallery>(Arrays.asList(objects));
    this.context = context;
    this.layoutItem = textViewResourceId;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

    View row = inflater.inflate(layoutItem, parent, false);

    TextView nameLabel = (TextView)row.findViewById(R.id.mediapicker_gallery_name);
    TextView numLabel = (TextView)row.findViewById(R.id.mediapicker_gallery_num);
    TextView locationLabel = (TextView)row.findViewById(R.id.mediapicker_gallery_location);
    ImageView thumbImage = (ImageView)row.findViewById(R.id.mediapicker_gallery_image);

    Gallery g = galleries.get(position);

    nameLabel.setText(g.getName());
    numLabel.setText(g.getNumInfo());
    locationLabel.setText(g.getLocationInfo());
    thumbImage.setImageBitmap(g.getThumbnail());

    return row;
  }

  public static Gallery[] loadData(Context c, ArrayList<Integer> ignoreIds) {
    ArrayList<Gallery> galleries = new ArrayList<Gallery>();

    galleries.addAll(getGalleries(c, 0, 0, ignoreIds));
    galleries.addAll(getGalleries(c, 0, 1, ignoreIds));
    galleries.addAll(getGalleries(c, 1, 0, ignoreIds));
    galleries.addAll(getGalleries(c, 1, 1, ignoreIds));

    return galleries.toArray(new Gallery[galleries.size()]);
  }

  private static ArrayList<Gallery> getGalleries(Context c, int location, int type, ArrayList<Integer> ignoreIds) {
    String[] projection;
    String sort;

    Uri loc;
    HashMap<String, Gallery> found;

    found = new HashMap<String, Gallery>();

    if (type == 0) {
      projection = new String[]{
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.DATE_ADDED
      };

      sort = MediaStore.Video.Media._ID + " DESC";

      if (location == 0) {
        loc = MediaStore.Video.Media.INTERNAL_CONTENT_URI;
      }
      else {
        loc = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
      }
    }
    else {
      projection = new String[]{
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED
      };

      sort = MediaStore.Images.Media._ID + " DESC";

      if (location == 0) {
        loc = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
      }
      else {
        loc = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
      }
    }

    Cursor cur = c.getContentResolver().query(loc, projection, null, null, sort);
    if (cur.moveToFirst()) {
      int id;
      String bucket;
      int idColumn, bucketColumn, dateColumn;

      if (type == 0) {
        idColumn = cur.getColumnIndex(MediaStore.Video.Media._ID);
        bucketColumn = cur.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        dateColumn = cur.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);
      }
      else {
        idColumn = cur.getColumnIndex(MediaStore.Images.Media._ID);
        bucketColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        dateColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
      }

      do {
        id = cur.getInt(idColumn);
        bucket = cur.getString(bucketColumn);

        if (ignoreIds.contains(id)) {
          continue;
        }

        Gallery g = null;

        if (found.containsKey(bucket)) {
          g = found.get(bucket);
          g.addMedia(id);
        }
        else {
          g = new Gallery(bucket, type, location, 1, null);

          Bitmap thumb;
          if (type == 0) {
            thumb = MediaStore.Video.Thumbnails.getThumbnail(c.getContentResolver(), id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
          }
          else {
            thumb = MediaStore.Images.Thumbnails.getThumbnail(c.getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
          }

          if (thumb == null) {
            g = null;
          }
          else {
            g.addMedia(id);
            g.setThumbnail(thumb);
          }
        }

        if (g != null) {
          found.put(bucket, g);
        }
      } while (cur.moveToNext());
      cur.close();
    }

    return new ArrayList<Gallery>(found.values());
  }
}
