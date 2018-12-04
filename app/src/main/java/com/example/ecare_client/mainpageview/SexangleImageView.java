/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client.mainpageview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class SexangleImageView extends View {
    private int mWidth;
    private int mHeight;

    private int mLenght;
    private Paint paint;
    public SexangleImageView(Context context) {
        super(context);
    }
    public SexangleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        mHeight = getHeight();
        mLenght = mWidth / 2;
        double radian30 = 30 * Math.PI / 180;
        float a = (float) (mLenght * Math.sin(radian30));
        float b = (float) (mLenght * Math.cos(radian30));
        float c = (mHeight - 2 * b) / 2;
        if (null == paint) {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Style.STROKE);//FILL
            paint.setColor(Color.parseColor("#A2A2A2"));
            paint.setAlpha(200);
        }
        Path path = new Path();
        path.moveTo(getWidth(), getHeight() / 2);

        path.lineTo(getWidth() - a, getHeight() - c);
        path.lineTo(getWidth() - a - mLenght, getHeight() - c);
        path.lineTo(0, getHeight() / 2);
        path.lineTo(a, c);
        path.lineTo(getWidth() - a, c);
        path.close();
        canvas.drawPath(path, paint);
    /*

    InputStream is = getResources().openRawResource(R.drawable.sec_10);
    Bitmap mBitmap = BitmapFactory.decodeStream(is);
    Paint mPaint = new Paint();
    canvas.drawBitmap(mBitmap, centreX, centreY, mPaint);
    */
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float edgeLength = ((float) getWidth()) / 2;
                float radiusSquare = edgeLength * edgeLength * 3 / 4;
                float dist = (event.getX() - getWidth() / 2)
                        * (event.getX() - getWidth() / 2)
                        + (event.getY() - getHeight() / 2)
                        * (event.getY() - getHeight() / 2);

                if (dist <= radiusSquare) {
                    paint.setColor(Color.parseColor("#A8A8A8"));
                    paint.setStyle(Style.FILL);
                    paint.setAlpha(100);
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                paint.setColor(Color.parseColor("#A2A2A2"));
                paint.setStyle(Style.STROKE);
                paint.setAlpha(200);

                invalidate();
                CharSequence flagIcons = this.getContentDescription();//Flag_image
                if(flagIcons==null){

                }else{
                    Message msg1=new Message();
                    msg1.what = Integer.parseInt(flagIcons.toString());
                    //MainActivity.setRequestid(msg1.what);
                }
                break;
        }
        return true;
    }
}
