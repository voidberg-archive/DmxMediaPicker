package com.voidberg.mediapicker;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Gallery {
  private int type;
  private int location;
  private int num;
  private Bitmap thumbnail;
  private String name;
  private HashMap<Integer, MediaItem> selected;
  private ArrayList<Integer> media;


  public Gallery() {
    this.name = "";
    this.type = 0;
    this.location = 0;
    this.num = 0;
    this.thumbnail = null;
    this.selected = new HashMap<Integer, MediaItem>();
    this.media = new ArrayList<Integer>();
  }

  public Gallery (String name, int type, int location, int num, Bitmap thumbnail) {
    this.name = name;
    this.type = type;
    this.location = location;
    this.num = num;
    this.thumbnail = thumbnail;
    this.selected = new HashMap<Integer, MediaItem>();
    this.media = new ArrayList<Integer>();
  }

  public void addMedia(int mediaId) {
    this.media.add(mediaId);
  }

  public ArrayList<Integer> getMedia() {
    return media;
  }

  public int getSelectedCount() {
    return selected.size();
  }

  public Collection<MediaItem> getSelected() {
    return selected.values();
  }

  public boolean isSelected(int index) {
    if (selected.containsKey(index)) {
      return true;
    }
    return false;
  }

  public void select(int index, int id) {
    MediaItem m = new MediaItem(id, type, location);
    selected.put(index, m);
  }

  public void deselect(int index) {
    selected.remove(index);
  }

  public int getType() {
    return type;
  }

  public int getNum() {
    return media.size();
  }

  public Bitmap getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(Bitmap thumb) {
    this.thumbnail = thumb;
  }

  public String getName() {
    return name;
  }

  public int getLocation() {
    return location;
  }

  public String getLocationInfo() {
    if (location == 0) {
      return "Internal storage";
    }
    else {
      return "External storage";
    }
  }

  public String getNumInfo() {
    if (type == 0) {
      return this.getNum() + " videos";
    }
    else {
      return this.getNum() + " images";
    }
  }

  @Override
  public String toString() {
    StringBuilder info = new StringBuilder();

    info.append(getLocationInfo());
    info.append(" / ");
    info.append(getNumInfo());

    return info.toString();
  }
}
