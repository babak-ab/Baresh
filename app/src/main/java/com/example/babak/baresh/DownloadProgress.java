package com.example.babak.baresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */

public class DownloadProgress extends View{

    public DownloadProgress(Context context) {
        super(context);
        init(null);
    }

    public DownloadProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DownloadProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DownloadProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet set){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        Rect rect = new Rect();
        rect.left = 0;
        rect.right = getWidth() / 4;
        rect.top = 0;
        rect.bottom = getHeight();

        Rect rect1 = new Rect();
        rect1.left = getWidth() / 2;
        rect1.right = 3 * getWidth() / 4 ;
        rect1.top = 0;
        rect1.bottom = getHeight();

        canvas.drawRect(rect, paint);
        canvas.drawRect(rect1, paint);
    }
}