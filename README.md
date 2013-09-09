DmxMediaPicker
==============

DmxMediaPicker is a media picker for Android allowing the selection of multiple images and video. It's developed at [Demotix.com](http://www.demotix.com/ "Demotix.com") by [Alexandru Badiu](http://ctrlz.ro).

It's currently work in progress. Contributions welcome.

Features
========

* Works with the galleries on the phone
* Works with both internal and external storage
* You can specify a maximum number of items to be returned
* You can specify the type of media allowed
* You can specify a blacklist of media items (that are not selectable)

Installation
============

You are going to need the following dependencies:

* Android Support Library
* ActionBar Sherlock

For the time being copy the files and resources in your project and merge the values in strings.xml.


Usage
=====

Copy the code and resources in your project. Use startActivityForResult to show the DmxMediaPickerActivity activity. The selected items are returned in "selectedMedia".

Optionally, add the following options to the intent:

* max - Limits the maximum selected items to this number.
* show - Limits the type of items shown to either images or video.
* blacklist - An array of ids that are not allowed to be selected.

Simple example
==============

    public class MediaActivity extends Activity {
      @Override
      public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.media_layout);    
      
        findViewById(R.id.media_pick_button).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(final View v) {
            Intent intent = new Intent(activity, DmxMediaPickerActivity.class);
            intent.putExtra("max", 4);

            activity.startActivityForResult(intent, 1);
          }
        });

      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
          if (resultCode == RESULT_OK) {
            Log.v("app", "Selected media");
            ArrayList<MediaItem> media = data.getParcelableArrayListExtra("selectedMedia");
            for (MediaList m : media) {
              Log.v("app", "Media id: " + m.id + " of type " + m.type);
            }
          }
        }
      }
    }
