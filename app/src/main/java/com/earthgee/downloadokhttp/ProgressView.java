package com.earthgee.downloadokhttp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by earthgee on 17/10/11.
 */

public class ProgressView extends View{

    private Paint circlePaint;

    private float progress=0.0f;

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        circlePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.BLACK);
    }

    public void setProgress(float progress){
        this.progress=progress;
        invalidate();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("earthgee","progress:"+progress);
        canvas.drawArc(0,0,200,200,0,360*progress,true,circlePaint);
    }

}
