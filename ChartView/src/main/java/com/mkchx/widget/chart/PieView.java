package com.mkchx.widget.chart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author Efthimis Charitonidis
 */
public class PieView extends View {

    private final float D_TO_R = 0.0174532925f;

    private List<String> mPieTexts = new ArrayList<>();
    private List<Paint> mPiePaint = new ArrayList<>();

    private List<Map<Float, Float>> mPieRange = new ArrayList<>();

    private RectF mRectF;
    private Paint mTextPaint, mBlankPaint, mSeparatorPaint;
    private int mProgressTextColor;
    private float mTextSize, mStartAngle = -90f, mLastAngle = 0f;
    private boolean mHideProgressValue;
    private int mWidth, mHeight;

    private onSliceClickListener pieInterface;

    public PieView(Context context) {
        super(context);

        if (mProgressTextColor == 0)
            mProgressTextColor = Color.WHITE;

        if (mTextSize == 0)
            mTextSize = getResources().getDimensionPixelSize(R.dimen.default_textSize);

        init();
    }

    public PieView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PieView, 0, 0);
        try {
            mProgressTextColor = a.getColor(R.styleable.PieView_progressTextColor, Color.WHITE);
            mTextSize = a.getDimensionPixelSize(R.styleable.PieView_progressTextSize, R.dimen.default_textSize);
            mHideProgressValue = a.getBoolean(R.styleable.PieView_hideProgressValue, false);
        } finally {
            a.recycle();
        }

        init();
    }

    public PieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        if (!mHideProgressValue) {
            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setStyle(Paint.Style.FILL);
            mTextPaint.setColor(mProgressTextColor);
            mTextPaint.setTextSize(mTextSize);
        }

        if (mBlankPaint == null) {
            mBlankPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBlankPaint.setStyle(Paint.Style.FILL);
            mBlankPaint.setColor(Color.LTGRAY);
        }

        if (mSeparatorPaint == null) {
            mSeparatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSeparatorPaint.setStyle(Paint.Style.STROKE);
            mSeparatorPaint.setColor(Color.WHITE);
        }
    }

    /**
     * Register a callback to be invoked when the slice is clicked.
     *
     * @param l The callback that will run
     */
    public void setOnSliceClickListener(onSliceClickListener l) {
        pieInterface = l;
    }

    /**
     * add title, value percentage to the chart
     *
     * @param title      The title of the slice
     * @param percentage The percentage of the slice
     */
    public void addSlice(String title, float percentage) {
        Random random = new Random();
        int randomColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));

        this.addSlice(title, percentage, randomColor);
    }

    /**
     * add title, value percentage to the chart
     *
     * @param title      The title of the slice
     * @param percentage The percentage of the slice
     * @param color      The resource color
     */
    public void addSlice(String title, float percentage, int color) {

        Paint paintProgress = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintProgress.setStyle(Paint.Style.FILL);
        paintProgress.setColor(color);

        mPiePaint.add(paintProgress);
        mPieTexts.add(title);

        float angle = calculateAngle(percentage);

        Map<Float, Float> temp = new TreeMap<>();

        if (mPieRange.size() == 0) {

            temp.put(mStartAngle, angle);
            mLastAngle += angle + mStartAngle;

        } else {

            temp.put(mLastAngle, angle);
            mLastAngle += angle;
        }

        mPieRange.add(temp);
    }

    /**
     * start drawing the pie
     */
    public void draw() {
        init();
        invalidate();
    }

    /**
     * set progress text color
     *
     * @param res
     */
    public void setTextColor(int res) {
        mProgressTextColor = ContextCompat.getColor(getContext(), res);
    }

    /**
     * set progress text size
     *
     * @param dimen
     */
    public void setTextSize(int dimen) {
        mTextSize = getResources().getDimensionPixelSize(dimen);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth();
        mHeight = getHeight();

        if (mRectF == null) {
            float mRadius = calculateRadius(mWidth, mHeight);
            mRectF = new RectF(mWidth / 2 - mRadius, mHeight / 2 - mRadius, mWidth / 2 + mRadius, mHeight / 2 + mRadius);
        }

        drawThePiece(canvas);
    }

    private void drawThePiece(Canvas canvas) {

        for (int i = 0; i < mPieRange.size(); i++) {

            Paint paint = mPiePaint.get(i);

            if (paint == null) {
                paint = mBlankPaint;
            }

            Map.Entry<Float, Float> entry = mPieRange.get(i).entrySet().iterator().next();

            // Draw the slice
            canvas.drawArc(mRectF, entry.getKey(), entry.getValue(), true, paint);

            // Draw the white separator
            canvas.drawArc(mRectF, entry.getKey(), entry.getValue(), true, mSeparatorPaint);

            // Draw the text if possible
            if (!mHideProgressValue) {

                String text = mPieTexts.get(i);

                float angle = (entry.getKey() + (entry.getValue() / 2)) * D_TO_R;
                float labelWidth = paint.measureText(text);

                float xText = mRectF.centerX() + (float) Math.cos(angle) * (mRectF.width() / 3 + labelWidth / 2);
                float yText = mRectF.centerY() + (float) Math.sin(angle) * (mRectF.height() / 3 + labelWidth / 2);

                canvas.drawText(text, xText, yText, mTextPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (pieInterface != null) {

                    float relX = event.getX() - (mRectF.right - mRectF.left) * 0.5f;
                    float relY = event.getY() - (mRectF.bottom - mRectF.top) * 0.5f;

                    double angleInRad = getAngleRad(relX, relY);
                    int degrees = (int) getAngleRange(angleInRad);

                    degrees -= (relX < 0 && relY < 0 ? -180 : 180);

                    for (int i = 0; i < mPieRange.size(); i++) {
                        Map.Entry<Float, Float> entry = mPieRange.get(i).entrySet().iterator().next();

                        if (degrees >= entry.getKey() && degrees <= Math.abs(entry.getKey() + entry.getValue())) {
                            pieInterface.onSliceClick(i, calculatePercentage(entry.getValue()));
                        }
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    public interface onSliceClickListener {
        void onSliceClick(int position, float percentage);
    }

    private float calculateRadius(int width, int height) {
        return (((width > height) ? height : width) / 2);
    }

    private double getAngleRad(float x, float y) {
        return Math.atan2(y, x);
    }

    private double getAngleRange(double angleRad) {
        return (angleRad + Math.PI) * 180 / Math.PI;
    }

    private float calculateAngle(float percent) {
        return percent * 360f / 100;
    }

    private float calculatePercentage(float angle) {
        return (angle / 360) * 100;
    }
}