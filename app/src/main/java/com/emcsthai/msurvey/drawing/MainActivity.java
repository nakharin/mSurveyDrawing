package com.emcsthai.msurvey.drawing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PhotoSortView photoSorter;

    private FrameLayout frame_layout;

    private FloatingActionButton fab_road;
    private FloatingActionButton fab_draw;
    private FloatingActionButton fab_object;
    private FloatingActionButton fab_vehicle;
    private FloatingActionMenu fab_menu_list;

    private Button btn_remove;
    private Button btn_clear;
    private Button btn_save;

    private List<FloatingActionMenu> menus = new ArrayList<>();

    private Handler mUiHandler = new Handler();

    private int DRAWING_ROAD_IN_YOURSELF = 0;

    private ArrayList<String> data = new ArrayList<>();

    private int imgRoadSmall[];
    private int imgRoadLarge[];
    private int imgVehicles[];
    private int imgObject[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data.add("ภาพใช้บ่อย");
        data.add("ภาพมาตรฐาน");

        // Method from this class
        initWidgets();

        photoSorter = new PhotoSortView(this);

        frame_layout.addView(photoSorter);

        fab_road.setOnClickListener(onClickListener);
        fab_draw.setOnClickListener(onClickListener);
        fab_object.setOnClickListener(onClickListener);
        fab_vehicle.setOnClickListener(onClickListener);
        btn_remove.setOnClickListener(onClickListener);
        btn_clear.setOnClickListener(onClickListener);
        btn_save.setOnClickListener(onClickListener);

        // Method from this class
        checkForUpdates();
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }

    private void checkForCrashes() {
        CrashManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoSorter.loadImages();
        // Method from this class
        checkForCrashes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        photoSorter.unloadImages();
        unregisterManagers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }

    private void initWidgets() {

        imgRoadSmall = Utils.getDrawableArray(this, R.array.road_small);
        imgRoadLarge = Utils.getDrawableArray(this, R.array.road_large);
        imgVehicles = Utils.getDrawableArray(this, R.array.vehicles);
        imgObject = Utils.getDrawableArray(this, R.array.objects);

        // AbsoluteLayout
        frame_layout = (FrameLayout) this.findViewById(R.id.frame_layout);

        // FloatingActionMenu
        fab_menu_list = (FloatingActionMenu) this.findViewById(R.id.fab_menu_list);

        // FloatingActionButton
        fab_road = (FloatingActionButton) this.findViewById(R.id.fab_road);
        fab_draw = (FloatingActionButton) this.findViewById(R.id.fab_draw);
        fab_object = (FloatingActionButton) this.findViewById(R.id.fab_object);
        fab_vehicle = (FloatingActionButton) this.findViewById(R.id.fab_vehicle);

        // Button
        btn_remove = (Button) this.findViewById(R.id.btn_remove);
        btn_clear = (Button) this.findViewById(R.id.btn_clear);
        btn_save = (Button) this.findViewById(R.id.btn_save);

        menus.add(fab_menu_list);

        fab_menu_list.hideMenuButton(false);

        int delay = 400;
        for (final FloatingActionMenu menu : menus) {
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    menu.showMenuButton(true);
                }
            }, delay);
            delay += 150;
        }

        // Hide after some seconds
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                fab_menu_list.open(true);
            }
        };

        handler.postDelayed(runnable, 500);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            photoSorter.trackballClicked();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DRAWING_ROAD_IN_YOURSELF && resultCode == RESULT_OK) {
            byte[] bytes = data.getByteArrayExtra("DRAWING_ROAD_IN_YOURSELF");
            Bitmap bitmapDrawingInYourSelf = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Drawable drawable = new BitmapDrawable(getResources(), bitmapDrawingInYourSelf);
            frame_layout.setBackground(drawable);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.fab_road:

                    AlertDialog.Builder roadBuilder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater roadInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View roadView = roadInflater.inflate(R.layout.dialog_category,
                            (ViewGroup) findViewById(R.id.layout_root), false);
                    ImageView roadImage = (ImageView) roadView.findViewById(R.id.img_close);
                    GridView roadGridView = (GridView) roadView.findViewById(R.id.gridview);
                    roadGridView.setAdapter(new DialogScenarioCustomAdapter(MainActivity.this, imgRoadSmall));
                    roadBuilder.setView(roadView);
                    final Dialog roadDialog = roadBuilder.create();

                    roadImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            roadDialog.dismiss();
                        }
                    });

                    roadGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

                            if (position < imgRoadLarge.length) {
                                photoSorter.addImages(imgRoadLarge[position]);
                            }

                            roadDialog.dismiss();
                        }
                    });

                    roadDialog.show();

                    break;
                case R.id.fab_draw:
                    Intent intent = new Intent(MainActivity.this, DrawingActivity.class);
                    intent.putExtra("titlebar", "วาดภาพถนนเอง");
                    intent.putExtra("ButtonSingalId", -1);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivityForResult(intent, DRAWING_ROAD_IN_YOURSELF);

                    break;
                case R.id.fab_object:

                    AlertDialog.Builder objBuilder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater objInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View objView = objInflater.inflate(R.layout.dialog_category,
                            (ViewGroup) findViewById(R.id.layout_root), false);
                    ImageView objImage = (ImageView) objView.findViewById(R.id.img_close);
                    GridView objGridView = (GridView) objView.findViewById(R.id.gridview);
                    objGridView.setAdapter(new DialogScenarioCustomAdapter(MainActivity.this, imgObject));
                    objBuilder.setView(objView);
                    final Dialog objDialog = objBuilder.create();

                    objImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            objDialog.dismiss();
                        }
                    });

                    objGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            if (position < imgObject.length) {
                                photoSorter.addImages(imgObject[position]);
                            }
                        }
                    });

                    objDialog.show();

                    break;
                case R.id.fab_vehicle:

                    AlertDialog.Builder vehBuilder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater vehInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View vehView = vehInflater.inflate(R.layout.dialog_category,
                            (ViewGroup) findViewById(R.id.layout_root), false);
                    ImageView vehImage = (ImageView) vehView.findViewById(R.id.img_close);
                    GridView vehGridView = (GridView) vehView.findViewById(R.id.gridview);
                    vehGridView.setAdapter(new DialogScenarioCustomAdapter(MainActivity.this, imgVehicles));
                    vehBuilder.setView(vehView);
                    final Dialog vehDialog = vehBuilder.create();

                    vehImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            vehDialog.dismiss();
                        }
                    });

                    vehGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            if (position < imgVehicles.length) {
                                photoSorter.addImages(imgVehicles[position]);
                            }
                        }
                    });

                    vehDialog.show();

                    break;
                case R.id.btn_remove:
                    photoSorter.removeImages();
                    break;
                case R.id.btn_clear:
                    photoSorter.clearImages();
                    break;
                case R.id.btn_save:
                    // Create a Bitmap with the same dimensions
                    Bitmap image = Bitmap.createBitmap(frame_layout.getWidth(), frame_layout.getHeight(),
                            Bitmap.Config.RGB_565);
                    // Draw the view inside the Bitmap
                    frame_layout.draw(new Canvas(image));

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Save successful");

                    final AlertDialog alert = builder.create();
                    alert.show();

                    // Hide after some seconds
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (alert.isShowing()) {
                                alert.dismiss();
                            }
                        }
                    };

                    alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            handler.removeCallbacks(runnable);
                        }
                    });

                    handler.postDelayed(runnable, 1000);

                    break;
            }
        }
    };
}
