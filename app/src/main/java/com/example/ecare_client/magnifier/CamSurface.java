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
import com.example.ecare_client.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;

/**
 * Used visor-android as reference for autofocus and zooming in
 */
public class CamSurface extends SurfaceView implements SurfaceHolder.Callback {
    /**
     * The maximum of steps until we will reach the maximum zoom level.
     */
    private static final int mCameraZoomSteps = 4;

    /**
     * The jpeg quality which will be rendered for each camera preview image.
     * If the value is too high to performance decreased drastically.
     */
    public static final int JPEG_QUALITY = 90;

    /**
     * Camera state: Device is closed.
     */
    public static final int STATE_CLOSED = -1;

    /**
     * Camera state: Device is opened, but is not capturing.
     */
    public static final int STATE_OPENED = 1;

    /**
     * Camera state: Showing camera preview.
     */
    public static final int STATE_PREVIEW = 2;

    /**
     * Max width for the camera preview to avoid performance and ram/cache issues.
     * TODO should be configurable by a settings-activity! (feature)
     */
    private static final int MAX_CAMERA_PREVIEW_RESOLUTION_WIDTH = 1024;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            keepCameraAspectRatioInView(this, left, top, right, bottom);
        }
    }

    /**
     * method to fix layout aspect ratio to fit camera's.
     *
     * FIXME This only effects to camera preview with no filter!
     *
     * @source https://stackoverflow.com/questions/12751016/android-camera-preview-look-strange
     *
     * @param child the view were the layout should be fixed.
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    private void keepCameraAspectRatioInView(View child, int left, int top, int right, int bottom) {

        final int width = right - left;
        final int height = bottom - top;

        int previewWidth/* = width*/;
        int previewHeight/* = height*/;

        previewWidth = mCameraPreviewWidth;
        if(previewWidth == 0) previewWidth = width;
        previewHeight = mCameraPreviewHeight;
        if(previewHeight == 0) previewHeight = height;

        // Center the child SurfaceView within the parent.
        if (width * previewHeight < height * previewWidth) {

            // abort if height is 0
            if(previewHeight == 0) return;

            final int scaledChildWidth = previewWidth * height / previewHeight;

            left = (width - scaledChildWidth) / 2;
            top = 0;
            right = (width + scaledChildWidth) / 2;
            bottom = height;

            child.layout(left, top, right, bottom);
        } else {

            // abort if width is 0
            if(previewWidth == 0) return;

            final int scaledChildHeight = previewHeight * width / previewWidth;

            left = 0;
            top = (height - scaledChildHeight) / 2;
            right = width;
            bottom = (height + scaledChildHeight) / 2;

            child.layout(left, top, right, bottom);
        }

        // re-scale matrix with given values
        final float tmpScaleX = (right - left) / (float) previewWidth;
        final float tmpScaleY = (bottom - top) / (float) previewHeight;

        if(tmpScaleX != scaleX || tmpScaleY != scaleY) {
            scaleX = tmpScaleX;
            scaleY = tmpScaleY;
            if(scaleMatrix == null) scaleMatrix = new Matrix();
            scaleMatrix.setScale(scaleX, scaleY, 0, 0);
            }

    }

    private SurfaceHolder mHolder;

    /**
     * The camera device reference.
     * An instance will be created if the surface is created.
     * We'll close the camera reference if the surface gets destroyed.
     */
    private Camera mCamera;

    /**
     * defines the current zoom level of the camera.
     */
    private int mCameraCurrentZoomLevel;

    /**
     * if true the flashlight should be on.
     */
    private boolean mCameraFlashMode;
    /**
     * stores the value of the devices max zoom level of the camera.
     */
    private int mCameraMaxZoomLevel;

    /**
     * the width of the view.
     */
    private int width;

    /**
     * the height of the view
     */
    private int height;

    /**
     * the maximum possible width of the camera preview that we'll use.
     */
    private int mCameraPreviewWidth;

    /**
     * the maximum possible height of the camera preview that we'll use.
     */
    private int mCameraPreviewHeight;

    /**
     * the current state of the camera device.
     * i.e. open, closed or preview.
     */
    public int mState;

    /**
     * stores the YUV image (format NV21) when onPreviewFrame was called
     */
    private byte[] mCameraPreviewBufferData;
    /**
     * stores the `mCameraPreviewBufferData` as rgb.
     *
     * @see BitmapThread
     */
    private int[] mCameraPreviewRgb;

    /**
     * after the onPreviewFrame was called we'll generate a
     * bitmap for usage in onDraw.
     */
    private Bitmap mCameraPreviewBitmapBuffer;


    /**
     * callback for camera previews
     */
    protected Camera.PreviewCallback mCameraPreviewCallbackHandler = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {

            // Log.d(TAG, "mCameraPreviewCallbackHandler Camera.PreviewCallback called");

            mCameraPreviewBufferData = data;

            runBitmapCreateThread(false);
        }
    };
    /**
     * reference to the zoom button.
     * <p>
     * We hide the zoom Button if zoom is not supported
     * by the device camera.
     */
    private View zoomButtonView;
    /**
     * reference to the flash button.
     * <p>
     * We hide the flash button if flashlight isn't supported.
     */
    private View flashButtonView;

    /**
     * auto focus mode which was stored in the shared preferences.
     */
    private String storedAutoFocusMode;
    private boolean mPauseOnReady = false;
    private static int mCameraId;

    /**
     * Image Scaling costs much cpu and memory.
     * We store the values here for use in onDraw:
     */
    private float scaleX;
    private float scaleY;
    private Matrix scaleMatrix;

    /**
     * @param context activity
     */
    public CamSurface(Context context) {
        super(context);

        mCameraCurrentZoomLevel = 0;
        mCameraMaxZoomLevel = 0;

        SharedPreferences sharedPreferences = context.getSharedPreferences(String.valueOf(R.string.visor_shared_preference_name), Context.MODE_PRIVATE);
        mCameraCurrentZoomLevel = sharedPreferences.getInt(String.valueOf(R.string.key_preference_zoom_level), mCameraCurrentZoomLevel);
        storedAutoFocusMode = sharedPreferences.getString(String.valueOf(R.string.key_preference_autofocus_mode), FOCUS_MODE_AUTO);

        mCameraFlashMode = false;

        mState = STATE_CLOSED;

        Display mDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();

        Point sizePoint = new Point();

        mDisplay.getSize(sizePoint);
        mDisplay.getRealSize(sizePoint);


        width = sizePoint.x;
        height = sizePoint.y;

        //we have to set this if we're using our own onDraw method
        setWillNotDraw(false);
        setDrawingCacheEnabled(true);

        mCamera = null;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    /**
     * open and return a camera instance.
     *
     * @param cameraId
     * @return
     */
    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;

        final int numOfCameras = Camera.getNumberOfCameras();

        if (!(cameraId < numOfCameras)) {
            return null;
        }else{
            try {
                c = Camera.open(cameraId); // attempt to get a Camera instance
                // stores the used camera id in the static var.
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                // try another one.
                c = getCameraInstance(++cameraId);
            }

            CamSurface.mCameraId = cameraId;
        }


        return c; // returns null if camera is unavailable
    }

    /**
     * return camera with id 0 (default: back camera)
     *
     * @return
     */
    public static Camera getCameraInstance() {
        return getCameraInstance(0);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        enableCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        String currentFocusMode = FOCUS_MODE_AUTO;

        if (mCamera != null) {
            try {
                Camera.Parameters params = mCamera.getParameters();
                currentFocusMode = params.getFocusMode();
            } catch (Exception ex) {
                currentFocusMode = FOCUS_MODE_AUTO;
            }
        }

        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(String.valueOf(R.string.visor_shared_preference_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(String.valueOf(R.string.key_preference_zoom_level), mCameraCurrentZoomLevel);
        editor.putString(String.valueOf(R.string.key_preference_autofocus_mode), currentFocusMode);

        editor.apply();

        releaseCamera();
    }

    /**
     * returns the maximum possible camera preview size which is the same or less than you've
     * specified with the {MAX_CAMERA_PREVIEW_RESOLUTION_WIDTH} const.
     *
     * @param parameters the camera parameters to receive all supported preview sizes.
     * @return Camera.Size or null if the parameters could not be accessed or some other issues occured.
     */
    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        List<Camera.Size> size = parameters.getSupportedPreviewSizes();
        Collections.sort(size, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if (lhs.width < rhs.width) return -1;
                if (lhs.width > rhs.width) return 1;
                return 0;
            }
        });

        if (size.size() <= 0) return null;

        for (int i = (size.size() - 1); i >= 0; i--) {
            final int currentWidth = size.get(i).width;
            if (currentWidth <= MAX_CAMERA_PREVIEW_RESOLUTION_WIDTH) {
                result = size.get(i);
                break;
            }
        }

        // just use the last one, if there are only a few supported sizes.
        if (result == null) return size.get(size.size() - 1);
        return result;
    }

    /**
     * open and enable the camera.
     * If the preview is already running we'll immediately return.
     * If a camera is already open we won't open it again and just use it instead.
     * If it wasn't possible to open the camera we will throw CameraCouldNotOpenedException.
     */
    public void enableCamera() {
        if (mState != STATE_CLOSED) return;
        if (mCamera == null) {
            mCamera = getCameraInstance();
            mState = STATE_OPENED;
        }
        // camera is still null so abort further actions
        // if(mCamera == null) throw new CameraCouldNotOpenedException();
        if (mCamera == null) return;

        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.isZoomSupported()) {
            mCameraMaxZoomLevel = parameters.getMaxZoom();
        } else {
            getZoomButtonView().setVisibility(View.INVISIBLE);
        }
        Camera.Size size = getBestPreviewSize(parameters);

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            getFlashButtonView().setVisibility(View.INVISIBLE);
        }

        int cameraPreviewFormat = parameters.getPreviewFormat();
        if (cameraPreviewFormat != ImageFormat.NV21) parameters.setPreviewFormat(ImageFormat.NV21);

        if (size == null)return;

        mCameraPreviewWidth = size.width;
        mCameraPreviewHeight = size.height;
        parameters.setPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);

        mCameraPreviewBitmapBuffer = Bitmap.createBitmap(mCameraPreviewWidth, mCameraPreviewHeight, android.graphics.Bitmap.Config.ARGB_8888);

        /**
         * this creation is here just to be sure a scaleMatrix is created.
         *
         * The method "onLayout" should be called before this method, so we always should have a valid scaleMatrix!
         * May be these lines can be removed if we're sure that it will never get needed.
         *
         */
        if(scaleMatrix == null) {
            scaleX = width / (float) mCameraPreviewWidth;
            scaleY = height / (float) mCameraPreviewHeight;
            scaleMatrix = new Matrix();
            scaleMatrix.setScale(scaleX, scaleY, 0, 0);
        }

        // Give the camera a hint that we're recording video.  This can have a big
        // impact on frame rate.
        parameters.setRecordingHint(true);

        setCameraDisplayOrientation((Activity) getContext());

        mCamera.setParameters(parameters);

        // pre-define some variables for image processing.
        mCameraPreviewBufferData = new byte[mCameraPreviewWidth * mCameraPreviewHeight * 3 / 2];
        mCameraPreviewRgb = new int[mCameraPreviewWidth * mCameraPreviewHeight];

        // The Surface has been created, now tell the
        // camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        /**/
        mCamera.setPreviewCallback(mCameraPreviewCallbackHandler);

        // NOTE it's possible that an RuntimeException is thrown here.
        // @see https://play.google.com/apps/publish/?#AndroidMetricsErrorsPlace:p=de.visorapp.visor&appVersion=PRODUCTION&lastReportedRange=LAST_60_DAYS&clusterName=apps/de.visorapp.visor/clusters/61aebe9b
        mCamera.startPreview();

        mState = STATE_PREVIEW;

        if (!storedAutoFocusMode.equals(FOCUS_MODE_AUTO)) {
            toggleAutoFocusMode();
        }

        // start with the first zoom level.
        // init zoom level member attr.
        if (mCameraCurrentZoomLevel == 0) {
            mCameraCurrentZoomLevel = mCameraMaxZoomLevel;
            nextZoomLevel();
        } else {
            setCameraZoomLevel(mCameraCurrentZoomLevel);
        }


        if (mPauseOnReady && mCamera != null) {
            toggleCameraPreview();
        }

        autoFocusCamera();
        }

    public void setCameraDisplayOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    /**
     *
     */
    public void releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;

                // stopBackgroundThread();
                mState = STATE_CLOSED;

            }
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        enableCamera();
    }

    /**
     * enables autofocus for the preview.
     * It will autofocus just a single time.
     */
    public void autoFocusCamera() {
        if (mState != STATE_PREVIEW) return;

        try {
            mCamera.cancelAutoFocus();
        } catch (RuntimeException ex) {
        }
        try {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                  @Override
                                  public void onAutoFocus(boolean success, Camera camera) {
                                  }
                              }
            );
        } catch (RuntimeException ex) {
        }
    }

    /**
     * starts or stops the preview mode of the camera to hold still the current
     * picture. We don't need to store it at the moment.
     */
    public void toggleCameraPreview() {
        if (mCamera == null) {
            mState = STATE_CLOSED;
            enableCamera();
            return;
        }

        mState = (mState == STATE_PREVIEW ? STATE_OPENED : STATE_PREVIEW);

        if (mState == STATE_PREVIEW) {

            // FIX: 20160508 On some devices it occured, that the callback handler wasn't called anymore.
            mCamera.setPreviewCallback(mCameraPreviewCallbackHandler);
            mCamera.startPreview();
            return;
        }

        mCamera.stopPreview();

        // run create thread otherwise we could see an old image.
        runBitmapCreateThread(true);
    }

    /**
     * enables or disables the autofocus mode.
     * We use the FOCUS_MODE_CONTINUOUS_PICTURE to enable the autofocus.
     * <p>
     * Your camera has to support this method.
     */
    public void toggleAutoFocusMode() {
        if (mState != STATE_PREVIEW) return;

        Camera.Parameters cameraParameters = mCamera.getParameters();

        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (!focusModes.contains(FOCUS_MODE_AUTO)) {
            return;
        }
        if (!focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            return;
        }

        String currentMode = cameraParameters.getFocusMode();
        if (currentMode.equals(FOCUS_MODE_AUTO)) {
            Toast.makeText(CamSurface.this.getContext(), R.string.text_autofocus_enabled, Toast.LENGTH_SHORT).show();
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else {
            Toast.makeText(CamSurface.this.getContext(), R.string.text_autofocus_disabled, Toast.LENGTH_SHORT).show();
            cameraParameters.setFocusMode(FOCUS_MODE_AUTO);
        }

        mCamera.setParameters(cameraParameters);

    }

    /**
     * toggles flashlight on and off.
     *
     * @param context we need the application context to determine if the users device has flash support or not.
     */
    public void nextFlashlightMode(Context context) {
        if (mState != STATE_PREVIEW) return;

        mCameraFlashMode = !mCameraFlashMode;
        if (mCameraFlashMode == true) {
            turnFlashlightOn();
        } else {
            turnFlashlightOff();
        }
    }

    private void turnFlashlightOff() {
        if (mState != STATE_PREVIEW || !supportsFlashlight()) return;
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(parameters);
    }

    private void turnFlashlightOn() {
        if (mState != STATE_PREVIEW || !supportsFlashlight()) return;
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);
    }


    /**
     * true of the current devices has a flash.
     *
     * @return true if flash is supported
     */
    private boolean supportsFlashlight() {
        boolean hasFlash = getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (hasFlash == false) {
            // Log.e(TAG, "the current device does not have a flashlight!");
            return false;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        // 2015-08-20 Fix: Some devices/android versions return NULL instead of an list object.
        return !(supportedFlashModes == null || !supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH));

    }

    /**
     * triggers the next zoom level for the camera.
     * We use a simple math calculation to calculate each
     * single step until we reach the maximum zoom level.
     * the first step will always be the module of:
     * `mCameraMaxZoomLevel % mCameraZoomSteps` to avoid
     * a fifth baby step for some pixels.
     * <p/>
     * On my Nexus 5 the max level is 99. We have 4 steps, so
     * each step will be 24. The first step will be 3.
     * We'll never reach the 0 zoom level, but that's okay.
     * <p/>
     * If the preview isn't ready it, the values will
     * nevertheless stored in the member variables.
     */
    public void nextZoomLevel() {
        final int steps = (mCameraMaxZoomLevel / (mCameraZoomSteps - 1));
        final int modulo = (mCameraMaxZoomLevel % (mCameraZoomSteps - 1));

        int nextLevel = mCameraCurrentZoomLevel + steps;

        if (mCameraCurrentZoomLevel == mCameraMaxZoomLevel) {
            nextLevel = modulo;
        }

        if (mState == STATE_PREVIEW)
            setCameraZoomLevel(nextLevel);
    }

    /**
     * @see .nextZoomLevel
     */
    public void prevZoomLevel() {
        final int steps = (mCameraMaxZoomLevel / (mCameraZoomSteps - 1));
        final int modulo = (mCameraMaxZoomLevel % (mCameraZoomSteps - 1));

        int prevLevel = mCameraCurrentZoomLevel - steps;

        if (mCameraCurrentZoomLevel <= modulo) {
            prevLevel = mCameraMaxZoomLevel;
        }

        if (mState == STATE_PREVIEW)
            setCameraZoomLevel(prevLevel);
    }
    private void updatePhotoViewBitmap() {
        // FIXME refactor!
        ((MainActivity) getContext()).mPhotoView.setImageBitmap(getBitmap());
    }

    /**
     * Runs a bitmap create thread with the current `mCameraPreviewBufferData`.
     * If finished, the thread calls `renderBitmap` with the final bitmap as the result.
     */
    protected void runBitmapCreateThread(boolean rgb) {
        final BitmapThread bitmapCreateThread = BitmapThread.getInstance(
                mCameraPreviewRgb,
                mCameraPreviewBufferData,
                CamSurface.this,
                mCameraPreviewWidth,
                mCameraPreviewHeight

        );
        if (bitmapCreateThread == null) return;
        new Thread(bitmapCreateThread).start();
    }

    /**
     * sets the bitmap.
     *
     * @param bitmap
     */
    public void renderBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mCameraPreviewBitmapBuffer = bitmap;
        }

        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidate();

                // FIXME refactor
                if(mState != STATE_PREVIEW) {
                    updatePhotoViewBitmap();
                }
            }
        });
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mState == STATE_CLOSED) {
            // Log.d(TAG, "onDraw called but the camera state seems to be closed.");
            return;
        }
        if (mCameraPreviewBitmapBuffer == null || mCameraPreviewBitmapBuffer.isRecycled()) {
            // Log.d(TAG, "onDraw called but the Bitmap is null or recycled. Do nothing here.");
            return;
        }

        /**
         * Description:
         * If the state is opened the preview is probably paused
         */

        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(mCameraPreviewBitmapBuffer, 0, 0, null);

    }


    /**
     * sets the camera level to the specified {zoomLevel}.
     * It dependes on a valid {mCamera} object to receive
     * the parameters and set it as well.
     * @param zoomLevel the integer of the new zoomLevel you want to set. All integers above the maximum possible value will be set to maximum.
     */
    private void setCameraZoomLevel(int zoomLevel) {
        Camera.Parameters parameters = mCamera.getParameters();

        if (parameters.isZoomSupported()) {

        }else{
            return;
        }

        if (zoomLevel > mCameraMaxZoomLevel) {
            zoomLevel = mCameraMaxZoomLevel;
        }
        mCameraCurrentZoomLevel = zoomLevel;

        parameters.setZoom(mCameraCurrentZoomLevel);
        mCamera.setParameters(parameters);
    }

    public View getZoomButtonView() {
        return zoomButtonView;
    }

    public View getFlashButtonView() {
        return flashButtonView;
    }

    public void setZoomButton(View zoomButton) {
        this.zoomButtonView = zoomButton;
    }

    public void setFlashButton(View flashButton) {
        this.flashButtonView = flashButton;
    }

    public Bitmap getBitmap() {
        Bitmap previewCopy = Bitmap.createBitmap(mCameraPreviewBitmapBuffer);
        Canvas canvas = new Canvas();
        canvas.setBitmap(previewCopy);
        canvas.drawBitmap(previewCopy, 0, 0, null);

        return previewCopy;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
