package com.emcsthai.msurvey.drawing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.simplify.ink.InkView;

import java.io.ByteArrayOutputStream;

public class DrawingActivity extends AppCompatActivity implements OnClickListener {

    private Button btn_save;
    private Button btn_edit;

    private InkView ink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drwaing);

        // Method from this class
        initWidgets();

        // OnClickListener
        btn_save.setOnClickListener(this);
        btn_edit.setOnClickListener(this);
    }

    private void initWidgets() {

        // InkView
        ink = (InkView) this.findViewById(R.id.ink);
        ink.setMinStrokeWidth(InkView.DEFAULT_MAX_STROKE_WIDTH);
        ink.setMaxStrokeWidth(InkView.DEFAULT_MAX_STROKE_WIDTH);

        // Button
        btn_save = (Button) this.findViewById(R.id.btn_save);
        btn_edit = (Button) this.findViewById(R.id.btn_edit);
    }

    private byte[] getDrawingInYourSelf() {

        Bitmap bitmap = ink.getBitmap(getResources().getColor(R.color.colorText));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return bytes;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_save:
                // Drawing Road in yourself
                Intent intent = new Intent();
                intent.putExtra("DRAWING_ROAD_IN_YOURSELF", getDrawingInYourSelf());
                setResult(RESULT_OK, intent);
                Utils.ToastSaveComplete(getApplicationContext());
                finish();
                break;
            case R.id.btn_edit:
                try {
                    ink.clear();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}