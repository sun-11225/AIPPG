package com.viatom.bloodoxygendemo.views;

import static android.animation.ValueAnimator.INFINITE;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.viatom.bloodoxygendemo.R;
import com.viatom.bloodoxygendemo.ext.ContextExtKt;


public class BatteryView extends View {
    private int power = 100;
    private boolean mIsCharging;
    private int state = 0;
    private int color = Color.parseColor("#36D8C0");
    private int colorRed = Color.rgb(255, 0, 0);
    private ObjectAnimator objectAnimator;


    public BatteryView(Context context) {
        super(context);
    }

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int battery_left = 0;
        int battery_top = 0;
        int battery_width = (int) ContextExtKt.convertDpToPixel(getContext(),32);
        int battery_height =(int) ContextExtKt.convertDpToPixel(getContext(),16);

        int battery_head_width = (int) ContextExtKt.convertDpToPixel(getContext(),4);
        int battery_head_height = (int) ContextExtKt.convertDpToPixel(getContext(),7);
        int battery_round_x = (int) ContextExtKt.convertDpToPixel(getContext(),2);
        int battery_round_y = (int) ContextExtKt.convertDpToPixel(getContext(),2);

        int battery_inside_margin = (int) ContextExtKt.convertDpToPixel(getContext(),1);

        //先画外框
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);

        RectF rectF = new RectF(battery_left, battery_top,
                battery_left + battery_width, battery_top + battery_height);

        canvas.drawRoundRect(rectF,battery_round_x,battery_round_y, paint);

        float power_percent = power / 100.0f;

        Paint paint2 = new Paint(paint);
        paint2.setStyle(Paint.Style.FILL);
        Paint paint3 = new Paint(paint);
        paint3.setStyle(Paint.Style.FILL);
        paint3.setColor(color);
        //画电量
        if(power_percent != 0) {
            int p_left = battery_left + battery_inside_margin;
            int p_top = battery_top + battery_inside_margin;
            int p_right = p_left - battery_inside_margin + (int)((battery_width - battery_inside_margin) * power_percent);
            int p_bottom = p_top + battery_height - battery_inside_margin * 2;
            RectF rect2 = new RectF(p_left, p_top, p_right , p_bottom);
            if(isLowPower() && !isCharging()) {
                paint3.setColor(colorRed);
            }
            canvas.drawRoundRect(rect2,battery_round_x,battery_round_y, paint3);

        }

        //画电量文字
       if(!isCharging()) {
           Paint mPaint = new Paint();
           mPaint.setStyle(Paint.Style.FILL);
           mPaint.setTextSize((int) ContextExtKt.convertDpToPixel(getContext(),12));
           mPaint.setColor(Color.WHITE);
           Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
           float bottomLineY = rectF.centerY() - (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.top;
           String bat = power + "%";
           float txtWidth = mPaint.measureText(bat);
           canvas.drawText(bat, rectF.centerX() - txtWidth/2, bottomLineY, mPaint);
       }


        //画电池头
        int h_left = battery_left + battery_width;
        int h_top = battery_top + battery_height / 2 - battery_head_height / 2;
        int h_right = h_left + battery_head_width;
        int h_bottom = h_top + battery_head_height;
        Rect rect3 = new Rect(h_left, h_top, h_right, h_bottom);
        canvas.drawRect(rect3, paint2);

        //charging
        if (isCharging()) {
            RectF rectF1 = new RectF(battery_left + battery_round_x, battery_top + battery_round_x,
                    battery_left + battery_width - battery_round_x, battery_top + battery_height - battery_round_x);
            Drawable drawable = getResources().getDrawable(R.mipmap.charged);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            canvas.drawBitmap(bitmap, null, rectF1, paint);
        }
    }

    public void setPower(int p) {
        if(state != 2) {
            power = p;
            if(power < 0) {
                power = 0;
            } else if (power > 100) {
                power = 100;
            }
        } else {
            power = 100;
        }
        invalidate();
    }

    public int getPower() {
        return power;
    }

    public boolean isLowPower() {
        return state == 3 || power < 10;
    }

    public void setState(int state) {
        this.state = state;
        this.mIsCharging = (state == 1 || state == 2);
        setCharging();
    }

    public boolean isCharging() {
        return mIsCharging;
    }

    public void setCharging() {
        if(mIsCharging) {
            if(state == 1) {
                if(objectAnimator == null || !objectAnimator.isRunning()) {
                    objectAnimator = ObjectAnimator.ofInt(this, "power", 0, power, 100);
                    objectAnimator.setDuration(2000);
                    objectAnimator.setRepeatCount(INFINITE);
                    objectAnimator.start();
                }
            } else {
                if(objectAnimator != null && objectAnimator.isRunning()) {
                    objectAnimator.cancel();
                }
                power = 100;
            }
        } else {
            if(objectAnimator != null && objectAnimator.isRunning()) {
                objectAnimator.cancel();
            }
        }
        invalidate();
    }
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }
}
