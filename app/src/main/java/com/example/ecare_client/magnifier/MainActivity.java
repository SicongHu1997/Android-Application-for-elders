/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client.magnifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ecare_client.R;
import com.example.ecare_client.TitleLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 */
public class MainActivity extends Activity {


    /**
     * contains the camera preview image.
     */
    private CamSurface mVisorView;


    private boolean cameraPreviewState;

    public uk.co.senab.photoview.PhotoView mPhotoView;


    private View.OnClickListener autoFocusClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mVisorView.autoFocusCamera();
        }
    };


    private void cameraPreviewIsActive(ImageButton playOrPauseButton) {
        playOrPauseButton.setImageResource(R.drawable.ic_pause);
        mZoomButton.setImageResource(R.drawable.ic_zoom);
        mFlashButton.setAlpha(255);
        mFlashButton.getBackground().setAlpha(255);

        // previewLayout.removeView(mPhotoView);
        mVisorView.setAlpha(1.0f);
        // mVisorView.setVisibility(View.VISIBLE); // the change of visiblity would cause a surfaceDestroy!

        mPhotoView.setVisibility(View.GONE);
        mPhotoView.setAlpha(0);
    }

    private View.OnClickListener flashLightClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.startAnimation(animScale);
            mVisorView.nextFlashlightMode(getApplicationContext());
        }
    };
    private View.OnClickListener zoomClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.startAnimation(animScale);

            if (cameraPreviewState) {
                mVisorView.nextZoomLevel();
                return;
            }

            takeScreenshot();
        }
    };
    private View.OnLongClickListener zoomOutClickHandler = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            v.startAnimation(animScaleLongPress);

            if (cameraPreviewState) {
                mVisorView.prevZoomLevel();
                return true;
            }
            return true;
        }
    };
    private View.OnLongClickListener tapAndHoldListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            mVisorView.toggleAutoFocusMode();
            return true;
        }
    };

    /**
     * Store the reference to swap the icon on it if we pause the preview.
     */
    private ImageButton mZoomButton;
    private ImageButton mFlashButton;
    private Animation animScale;
    private Animation animScaleLongPress;

    /**
     * When you use the SYSTEM_UI_FLAG_IMMERSIVE_STICKY flag, an inward swipe in the system bars
     * areas causes the bars to temporarily appear in a semi-transparent state, but no flags are
     * cleared, and your system UI visibility change listeners are not triggered. The bars
     * automatically hide again after a short delay, or if the user interacts with the
     * middle of the screen.
     *
     * Below is a simple approach to using this flag. Any time the window receives focus, simply set the IMMERSIVE_STICKY flag,
     * along with the other flags discussed in Use IMMERSIVE.
     *
     * @note https://developer.android.com/training/system-ui/immersive.html
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();

            // Api level 1
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

                uiOptions = uiOptions
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;


            // Kitkat to Oreo
            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                uiOptions = uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // 19
            }

            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.magnifier);
        TitleLayout titleLayout = (TitleLayout) findViewById(R.id.magnifier_title);
        titleLayout.setTitleText("Magnifier");
        animScale = AnimationUtils.loadAnimation(this, R.anim.scale);
        animScaleLongPress = AnimationUtils.loadAnimation(this, R.anim.longpress);

        mVisorView = new CamSurface(this);
        mPhotoView = new uk.co.senab.photoview.PhotoView(this);



        FrameLayout previewLayout = getCameraPreviewFrame();
        previewLayout.setBackgroundColor(Color.BLACK);
        previewLayout.addView(mVisorView);
        previewLayout.addView(mPhotoView);

        mPhotoView.setAlpha(0);

        setButtonListeners();

        // Add a listener to the Preview button
        mVisorView.setOnClickListener(autoFocusClickHandler);/**/
        mVisorView.setOnLongClickListener(tapAndHoldListener);

        // set proper display orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private FrameLayout getCameraPreviewFrame() {
        return (FrameLayout) findViewById(R.id.camera_preview);
    }

    private void setButtonListeners() {
        // Add a listener to the Zoom button
        ImageButton zoomButton = (ImageButton) findViewById(R.id.button_zoom);
        zoomButton.setOnClickListener(zoomClickHandler);
        zoomButton.setOnLongClickListener(zoomOutClickHandler);

        // Add a listener to the Flash button
        ImageButton flashButton = (ImageButton) findViewById(R.id.button_flash);
        flashButton.setOnClickListener(flashLightClickHandler);

        mVisorView.setZoomButton(zoomButton);
        mVisorView.setFlashButton(flashButton);

        mZoomButton = zoomButton;
        mFlashButton = flashButton;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!cameraPreviewState) {
            cameraPreviewState = true;
        }
    }

    /**
     * @source https://stackoverflow.com/questions/2661536/how-to-programmatically-take-a-screenshot-in-android#5651242
     */
    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/Pictures/visor-os-android.app_" + now + ".jpg";

            Bitmap bitmap = mVisorView.getBitmap();
            File imageFile = new File(mPath);

            //mVisorView.playActionSoundShutter();

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            final int quality = CamSurface.JPEG_QUALITY;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            int duration = Toast.LENGTH_SHORT;
            Toast toasty = Toast.makeText(this, R.string.text_image_stored + mPath, duration);
            toasty.show();

            mVisorView.mState = CamSurface.STATE_CLOSED;
            cameraPreviewIsActive(mZoomButton);

            openScreenshot(imageFile);

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    /**
     * @source https://stackoverflow.com/questions/2661536/how-to-programmatically-take-a-screenshot-in-android#5651242
     */
    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }
}
