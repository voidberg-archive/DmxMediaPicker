package com.voidberg.mediapicker;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaItem implements Parcelable {
  public int id;
  public int type;
  public int location;

  public MediaItem(int id, int type, int location) {
    this.id = id;
    this.type = type;
    this.location = location;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(id);
    parcel.writeInt(type);
    parcel.writeInt(location);
  }

  public static final Parcelable.Creator<MediaItem> CREATOR = new Parcelable.Creator<MediaItem>() {
    public MediaItem createFromParcel(Parcel in) {
      return new MediaItem(in);
    }

    public MediaItem[] newArray(int size) {
      return new MediaItem[size];
    }
  };

  private MediaItem(Parcel in) {
    id = in.readInt();
    type = in.readInt();
    location = in.readInt();
  }
}
