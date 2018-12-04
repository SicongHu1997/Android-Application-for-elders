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

import android.graphics.Bitmap;

public class BitmapThread implements Runnable {

    private static final int MAX_INSTANCES = 3; //so that it does not crash on most phones

    /**
     * count all instances.
     */
    private static int instanceCounter = 0;

    private int previewWidth;
    private int previewHeight;
    private CamSurface renderer;
    private byte[] yuvDataArray;
    private int[] rgbArray;

    private Bitmap renderedBitmap;

    /**
     * returns an instance of the task
     *
     * @param yuvDataArray
     * @param renderer
     * @return
     */
    public static BitmapThread getInstance(int[] rgb, byte[] yuvDataArray, CamSurface renderer, int previewWidth, int previewHeight) {

        if (instanceCounter >= MAX_INSTANCES) {
            return null;
        }


        BitmapThread instance = new BitmapThread();
        instanceCounter++;
        instance.setYuvDataArray(yuvDataArray);

        instance.setPreviewWidth(previewWidth);
        instance.setPreviewHeight(previewHeight);

        instance.setRenderer(renderer);
        instance.setRgbArray(rgb);

        return instance;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public void setRenderer(CamSurface renderer) {
        this.renderer = renderer;
    }

    public void setYuvDataArray(byte[] yuvDataArray) {
        this.yuvDataArray = yuvDataArray;
    }

    /**
     * the actual hard work.
     * @param yuvData
     */
    protected void createBitmap(byte[] yuvData) {
        // YuvImage yuvImage = new YuvImage(yuvData, ImageFormat.NV21, previewWidth, previewHeight, null);


        if(renderedBitmap == null) {
            renderedBitmap = Bitmap.createBitmap(previewWidth, previewHeight, android.graphics.Bitmap.Config.ARGB_8888);
        }
        renderedBitmap.setPixels(rgbArray, 0, previewWidth, 0, 0, previewWidth, previewHeight);

        // scaling (costs a lot of memory)
        // renderedBitmap = Bitmap.createScaledBitmap(renderedBitmap, targetWidth, targetHeight, true);
    }

    /**
     * do the hard stuff.
     * @param yuvDataArray
     * @return
     */
    protected void doInBackground(byte[] yuvDataArray) {
        this.createBitmap(yuvDataArray);
    }

    /**
     * after the hard stuff is done.
     */
    protected void onPostExecute() {
        renderer.renderBitmap(renderedBitmap);
        instanceCounter--;
    }

    @Override
    public void run() {
        doInBackground(yuvDataArray);
        onPostExecute();
    }

    public void setRgbArray(int[] rgbArray) {
        this.rgbArray = rgbArray;
    }
}
