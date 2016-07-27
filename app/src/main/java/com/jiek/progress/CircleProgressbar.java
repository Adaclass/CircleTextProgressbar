/*
 * Copyright 2016 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jiek.progress;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.util.AttributeSet;
import android.widget.TextView;

public class CircleProgressbar extends TextView {

    /**
     * max of progress
     */
    private int mMax = 100;
    /**
     * shape stroke color
     */
    private int outLineColor = Color.TRANSPARENT;

    /**
     * shape stroke width
     */
    private int outLineWidth = 2;

    /**
     * solid circle color
     */
    private ColorStateList inCircleColors = ColorStateList.valueOf(Color.TRANSPARENT);
    /**
     * solid circle
     */
    private int circleColor;

    /**
     * progress line color
     */
    private int progressLineColor = Color.BLUE;

    /**
     * progress line width px
     */
    private int progressLineWidth = 8;

    /**
     * paint
     */
    private Paint mPaint = new Paint();

    /**
     *
     */
    private RectF mArcRect = new RectF();

    /**
     * current progress
     */
    private int progress = 0;
    /**
     * progress type
     */
    private ProgressDirection mProgressDirection = ProgressDirection.COUNTERCLOCKWISE;//_BACK;
    /**
     * down time count (millisecond)
     */
    private long counter_millisecond = 5000;

    /**
     * View rect
     */
    final Rect bounds = new Rect();
    /**
     * progress Update Listener
     */
    private ProgressUpdateListener mCountdownProgressListener;
    /**
     * Listener what。
     */
    private int listenerWhat = 0x0f0f;

    /**
     * 0:x-right  -90:y-top  90:y-bottom  180:x-left
     */
    private float startAngle = -90;

    public void setStartAngle(int _startAngle) {
        startAngle = _startAngle;
    }

    /**
     * left:0   top:1  right:2  bottom:3
     */
    public void setStartOrientant(int orientant) {
        switch (orientant) {
            case 0:
                startAngle = 180;
                break;
            case 2:
                startAngle = 0;
                break;
            case 3:
                startAngle = 90;
                break;
            default:
                startAngle = -90;
                break;
        }
    }

    public CircleProgressbar(Context context) {
        this(context, null);
    }

    public CircleProgressbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleProgressbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs);
    }

    /**
     * initialize
     *
     * @param context
     * @param attributeSet
     */
    private void initialize(Context context, AttributeSet attributeSet) {
        mPaint.setAntiAlias(true);
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleProgressbar);
        if (typedArray.hasValue(R.styleable.CircleProgressbar_in_circle_color))
            inCircleColors = typedArray.getColorStateList(R.styleable.CircleProgressbar_in_circle_color);
        else
            inCircleColors = ColorStateList.valueOf(Color.TRANSPARENT);
        circleColor = inCircleColors.getColorForState(getDrawableState(), Color.TRANSPARENT);
        typedArray.recycle();
    }

    /**
     * shape stroke color
     *
     * @param outLineColor @ColorInt
     */
    public void setOutLineColor(@ColorInt int outLineColor) {
        this.outLineColor = outLineColor;
        invalidate();
    }

    /**
     * shape stroke width
     *
     * @param outLineWidth @DimenRes
     */
    public void setOutLineWidth(@DimenRes int outLineWidth) {
        this.outLineWidth = outLineWidth;
        invalidate();
    }

    /**
     * set
     *
     * @param inCircleColor @ColorInt
     */
    public void setInCircleColor(@ColorInt int inCircleColor) {
        this.inCircleColors = ColorStateList.valueOf(inCircleColor);
        invalidate();
    }

    /**
     * validate circle color
     */
    private void validateCircleColor() {
        int circleColorTemp = inCircleColors.getColorForState(getDrawableState(), Color.TRANSPARENT);
        if (circleColor != circleColorTemp) {
            circleColor = circleColorTemp;
            invalidate();
        }
    }

    /**
     * set progress color
     *
     * @param progressLineColor
     */
    public void setProgressColor(@ColorInt int progressLineColor) {
        this.progressLineColor = progressLineColor;
        invalidate();
    }

    /**
     * set progress line width (px)
     *
     * @param progressLineWidth
     */
    public void setProgressLineWidth(int progressLineWidth) {
        this.progressLineWidth = progressLineWidth;
        invalidate();
    }

    /**
     * set progress value
     *
     * @param progress
     */
    public void setProgress(int progress) {
        this.progress = validateProgress(progress);
        invalidate();
    }

    /**
     * validate progress value
     *
     * @param progress
     * @return avaliable progress value.
     */
    private int validateProgress(int progress) {
        if (progress > mMax)
            progress = mMax;
        else if (progress < 0)
            progress = 0;
        return progress;
    }

    /**
     * fetch current progress value
     *
     * @return progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * set initial count timemillis
     *
     * @param counter_millisecond
     */
    public void setCounter_millisecond(long counter_millisecond) {
        this.counter_millisecond = counter_millisecond;
        invalidate();
    }

    /**
     * 拿到进度条计时时间。
     *
     * @return 毫秒。
     */
    public long getCounter_millisecond() {
        return this.counter_millisecond;
    }

    /**
     * 设置进度条类型。
     *
     * @param progressType {@link ProgressDirection}.
     */
    public void setProgressType(ProgressDirection progressType) {
        this.mProgressDirection = progressType;
        resetProgress();
        invalidate();
    }

    /**
     * reset Progress
     */
    private void resetProgress() {
        switch (mProgressDirection) {
            case COUNTERCLOCKWISE:
                progress = 0;
                break;
            case ANTICLOCKWISE:
                progress = mMax;
                break;
        }
    }

    /**
     * fetch current progressType
     *
     * @return
     */
    public ProgressDirection getProgressType() {
        return mProgressDirection;
    }

    /**
     * set ProgressUpdateLisener
     *
     * @param mCountdownProgressListener
     */
    public void setCountdownProgressListener(int what, ProgressUpdateListener mCountdownProgressListener) {
        this.listenerWhat = what;
        this.mCountdownProgressListener = mCountdownProgressListener;
    }

    /**
     * start progress task
     */
    public void start() {
        stop();
        post(progressChangeTask);
    }

    /**
     * restart progress task
     */
    public void reStart() {
        resetProgress();
        start();
    }

    /**
     * stop update task
     */
    public void stop() {
        removeCallbacks(progressChangeTask);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //获取view的边界
        getDrawingRect(bounds);

        int size = bounds.height() > bounds.width() ? bounds.width() : bounds.height();
        float outerRadius = size / 2;

        //画内部背景
        int circleColor = inCircleColors.getColorForState(getDrawableState(), 0);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(circleColor);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), outerRadius - outLineWidth, mPaint);

        //画边框圆
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(outLineWidth);
        mPaint.setColor(outLineColor);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), outerRadius - outLineWidth / 2, mPaint);

        //画字
        Paint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        float textY = bounds.centerY() - (paint.descent() + paint.ascent()) / 2;
        canvas.drawText(getText().toString(), bounds.centerX(), textY, paint);

        //画进度条
        mPaint.setColor(progressLineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(progressLineWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        int deleteWidth = progressLineWidth + outLineWidth;
        mArcRect.set(bounds.left + deleteWidth / 2, bounds.top + deleteWidth / 2, bounds.right - deleteWidth / 2, bounds.bottom - deleteWidth / 2);

        canvas.drawArc(mArcRect, startAngle, 360 * progress / 100, false, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int lineWidth = 4 * (outLineWidth + progressLineWidth);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int size = (width > height ? width : height) + lineWidth;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        validateCircleColor();
    }

    /**
     * update progress task runnable
     */
    private Runnable progressChangeTask = new Runnable() {
        @Override
        public void run() {
            removeCallbacks(this);
            switch (mProgressDirection) {
                case COUNTERCLOCKWISE:
                    progress += 1;
                    break;
                case ANTICLOCKWISE:
                    progress -= 1;
                    break;
            }
            if (progress >= 0 && progress <= 100) {
                if (mCountdownProgressListener != null)
                    mCountdownProgressListener.onProgress(listenerWhat, progress);
                invalidate();
                postDelayed(progressChangeTask, counter_millisecond / 100);
            } else
                progress = validateProgress(progress);
        }
    };

    /**
     * progress type
     */
    public enum ProgressDirection {
        /**
         * counterclockwise，from 0 to 100.
         */
        COUNTERCLOCKWISE,

        /**
         * anti-clockwise，from 100 to 0.
         */
        ANTICLOCKWISE;
    }

    public interface ProgressUpdateListener {
        void onProgress(int what, int progress);
    }
}
