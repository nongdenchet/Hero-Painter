package apps.apidez.com.mycustomviewproject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestActivity extends ActionBarActivity implements DrawingListener {
    private PaintView myDrawingArea;
    private TextView undoBtn, redoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.paint_activity);

        // init views
        initView();
    }

    private void initView() {
        // draw area
        myDrawingArea = (PaintView) findViewById(R.id.paint_area);

        // undo button
        undoBtn = (TextView) findViewById(R.id.undo);
        undoBtn.setClickable(false);
        undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDrawingArea.undoDrawing();
            }
        });

        // redo button
        redoBtn = (TextView) findViewById(R.id.redo);
        redoBtn.setClickable(false);
        redoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDrawingArea.redoDrawing();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                myDrawingArea.isClear = true;
                update(false, false);
                myDrawingArea.invalidate();
                break;
            case R.id.action_save:
                saveToFile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // save tp file
    private void saveToFile() {
        // image naming and path  to include sd card  appending name you choose for file
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat ft =
                new SimpleDateFormat("dd/MM/yyyy/hh:mm:ss");
        String mPath = ft.format(date) + ".png";

        // create bitmap screen capture
        Bitmap bitmap;
        View v1 = myDrawingArea.getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        // store
        String url = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, mPath, "Hero Painter");
        Toast.makeText(getApplicationContext(), "File has been saved as "
                + url, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDrawSetting(boolean canUndo, boolean canRedo) {
        update(canUndo, canRedo);
    }

    // update UI
    private void update(boolean canUndo, boolean canRedo) {
        undoBtn.setClickable(canUndo);
        redoBtn.setClickable(canRedo);

        // update UI
        if (canUndo) {
            undoBtn.setBackgroundResource(R.drawable.button_action);
        } else {
            undoBtn.setBackgroundResource(R.drawable.button_un_active);
        }

        // update UI
        if (canRedo) {
            redoBtn.setBackgroundResource(R.drawable.button_action);
        } else {
            redoBtn.setBackgroundResource(R.drawable.button_un_active);
        }
    }
}
