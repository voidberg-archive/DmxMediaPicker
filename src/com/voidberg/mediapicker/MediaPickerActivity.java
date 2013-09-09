package com.voidberg.mediapicker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.voidberg.mediapicker.R;

import java.util.ArrayList;

public class MediaPickerActivity extends SherlockFragmentActivity {
  private GridView gridView;
  private ListView listView;
  private ViewFlipper viewFlipper;
  private GalleryAdapter galleryAdapter;
  private MediaAdapter imagesAdapter;
  private SherlockFragmentActivity activity;
  private Gallery selectedGallery;
  private Gallery[] results;
  private TextView noResultsView;
  private Button cancelButton, cancelmButton;
  private Button selectButton, selectmButton;
  private int maxSelected;
  private int currentScreen;
  private ArrayList<Integer> ignoreIds;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mediaselect);

    Intent intent = getIntent();
    maxSelected = intent.getIntExtra("max", 0);
    ignoreIds = intent.getIntegerArrayListExtra("selected");

    if (ignoreIds == null) {
      ignoreIds = new ArrayList<Integer>();
    }

    currentScreen = 0;

    activity = this;
    selectedGallery = null;
    results = new Gallery[0];

    gridView = (GridView) findViewById(R.id.mediapicker_grid);
    listView = (ListView) findViewById(R.id.mediapicker_list);
    viewFlipper = (ViewFlipper) findViewById(R.id.mediapicker_view);
    noResultsView = (TextView) findViewById(R.id.mediapicker_no_galleries);
    cancelButton = (Button) findViewById(R.id.mediapicker_button_cancel);
    selectButton = (Button) findViewById(R.id.mediapicker_button_select);
    cancelmButton = (Button) findViewById(R.id.mediapicker_buttonm_cancel);
    selectmButton = (Button) findViewById(R.id.mediapicker_buttonm_select);

    listView.setOnItemClickListener(galleryListener);
    gridView.setOnItemClickListener(mediaListener);
    cancelButton.setOnClickListener(cancelListener);
    selectButton.setOnClickListener(selectListener);
    cancelmButton.setOnClickListener(cancelListener);
    selectmButton.setOnClickListener(selectListener);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    new GalleryLoader(this, getString(R.string.mediapicker_loading_galleries)).execute();
  }

  public View.OnClickListener cancelListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      setResult(RESULT_CANCELED);
      finish();
    }
  };

  public View.OnClickListener selectListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Intent intent = new Intent();

      ArrayList<MediaItem> selected = getSelected();
      if (selected.size() == 0) {
        Toast.makeText(activity, getString(R.string.mediapicker_no_images), Toast.LENGTH_LONG).show();
        return;
      }

      intent.putParcelableArrayListExtra("selectedMedia", selected);
      setResult(RESULT_OK, intent);
      finish();
    }
  };

  public AdapterView.OnItemClickListener galleryListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
      selectedGallery = (Gallery) listView.getItemAtPosition(position);

      updateTitle();

      imagesAdapter = new MediaAdapter(activity, R.layout.mediapicker_grid_item, selectedGallery);
      gridView.setAdapter(imagesAdapter);
      imagesAdapter.notifyDataSetChanged();
      viewFlipper.showNext();
      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      currentScreen = 1;
    }
  };

  public AdapterView.OnItemClickListener mediaListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
      if (selectedGallery.isSelected(position)) {
        selectedGallery.deselect(position);
      }
      else {
        int selectedCount = getSelectedCount();
        if (maxSelected != 0 && selectedCount == maxSelected) {
          Toast.makeText(activity, getString(R.string.mediapicker_max_selected, selectedCount), Toast.LENGTH_LONG).show();
        }
        else {
          selectedGallery.select(position, imagesAdapter.getItem(position));
        }
      }
      updateTitle();
      imagesAdapter.notifyDataSetChanged();
    }
  };

  @Override
  public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (currentScreen == 1) {
          viewFlipper.showPrevious();
          activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
          currentScreen = 0;
        }
        break;
    }
    return true;
  }

  public void postGalleriesLoad(Gallery[] results) {
    this.results = results;
    if (results.length > 0) {
      galleryAdapter = new GalleryAdapter(this, R.layout.mediapicker_gallery, results);
      listView.setAdapter(galleryAdapter);
    }
    else {
      noResultsView.setVisibility(View.VISIBLE);

      selectButton.setVisibility(View.INVISIBLE);
      cancelButton.setVisibility(View.INVISIBLE);
    }
  }

  public void updateTitle() {
    String title;
    if (maxSelected == 0) {
      title = getString(R.string.mediapicker_title_num, selectedGallery.getName(), this.getSelectedCount());
    }
    else {
      title = getString(R.string.mediapicker_title_num_max, selectedGallery.getName(), this.getSelectedCount(), maxSelected);
    }
    activity.setTitle(title);
  }

  public void postImagesLoad(ArrayList<Integer> results) {
    updateTitle();
    imagesAdapter = new MediaAdapter(this, R.layout.mediapicker_grid_item, selectedGallery);
    gridView.setAdapter(imagesAdapter);
    imagesAdapter.notifyDataSetChanged();
    viewFlipper.showNext();
    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    currentScreen = 1;
  }

  public int getSelectedCount() {
    int selected = 0;

    for (Gallery g : results) {
      selected += g.getSelectedCount();
    }

    return selected;
  }

  public ArrayList<MediaItem> getSelected() {
    ArrayList<MediaItem> selected = new ArrayList<MediaItem>();

    for (Gallery g : results) {
      selected.addAll(g.getSelected());
    }

    return selected;
  }

  class ImageLoader extends AsyncTask<Void, Void, ArrayList<Integer>> {
    Activity activity;
    ProgressDialogFragment dialog;
    String loadingText;
    int location;
    int type;

    public ImageLoader() {
      super();
    }

    public ImageLoader(Activity activity, String loadingText, int location, int type) {
      super();
      this.activity = activity;
      this.loadingText = loadingText;
      this.location = location;
      this.type = type;
    }

    @Override
    protected ArrayList<Integer> doInBackground(Void... arg) {
      return MediaAdapter.getMedia(activity, selectedGallery.getName(), location, type);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      dialog = new ProgressDialogFragment(loadingText);
      dialog.show(getSupportFragmentManager(), "DIALOG_LOADING_IMAGES");
    }

    @Override
    protected void onPostExecute(ArrayList<Integer> results) {
      super.onPostExecute(results);

      ProgressDialogFragment d = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag("DIALOG_LOADING_IMAGES");
      if (d != null) {
        d.dismiss();
      }

      postImagesLoad(results);
    }
  }


  class GalleryLoader extends AsyncTask<Void, Void, Gallery[]> {
    Activity activity;
    ProgressDialogFragment dialog;
    String loadingText;

    public GalleryLoader() {
      super();
    }

    public GalleryLoader(Activity activity, String loadingText) {
      super();
      this.activity = activity;
      this.loadingText = loadingText;
    }

    @Override
    protected Gallery[] doInBackground(Void... arg) {
      return GalleryAdapter.loadData(activity, ignoreIds);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      dialog = new ProgressDialogFragment(loadingText);
      dialog.show(getSupportFragmentManager(), "DIALOG_LOADING_GALLERY");
    }

    @Override
    protected void onPostExecute(Gallery[] results) {
      super.onPostExecute(results);

      ProgressDialogFragment d = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag("DIALOG_LOADING_GALLERY");
      if (d != null) {
          d.dismiss();
      }
      postGalleriesLoad(results);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)  {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      onBackPressed();
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void onBackPressed() {
    if (currentScreen == 1) {
      viewFlipper.showPrevious();
      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      currentScreen = 0;
    }
    else {
      setResult(RESULT_CANCELED);
      finish();
    }
  }
}

class ProgressDialogFragment extends DialogFragment {
    private String message;

    public ProgressDialogFragment(String string) {
      this.message = string;
    }

    @Override
    public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setMessage(message);
        return dialog;
    }
}