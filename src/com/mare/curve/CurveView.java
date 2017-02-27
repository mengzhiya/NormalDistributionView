package com.mare.curve;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CurveView extends View {
	private int mWavelength;// 波长
	private int mWaveCount;
	private Paint mPaint;
	private int mPeakCircleColor = Color.argb(200, 250, 0, 0),
			mPeakValueColor = Color.BLACK, mWaveFillColor = Color.argb(150, 128, 128, 0);
	private static final float DEFAULT_AMPLITUDE_VALUE = 50.0f;
	private static final int DEFAULT_WAVELENTH = 200;
	private int mWaveColor,mLabelColor;
	private final static int WATER_LEVEL_LINE = 600;
	private int mMarginLeft = 0,mPeakValueMargintTop = 0,mLabelMargintTop = 0;
	private Matrix mMatrix ;
	private Rect mLabelBounds = new  Rect();
	private int mLeftSpace = 0,mTopSpace = 0, mCanvasHeight;
	private int mOffset = (int) (Math.PI / 4);
	private int[] mPeakValues;
	private String[] mLabels;
	private int mSingleWaveWidth;
	private int mTopYScreen;

	public CurveView(Context context) {
		this(context, null);
	}

	public CurveView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CurveView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr) {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		TypedArray attributes = context.obtainStyledAttributes(attrs,
				R.styleable.WaveCurve, defStyleAttr, 0);
		mWaveColor = attributes.getColor(R.styleable.WaveCurve_wave_color, mWaveFillColor);
		mWavelength = attributes.getInt(R.styleable.WaveCurve_wavelenth, DEFAULT_WAVELENTH);
		mWaveCount = attributes.getInt(R.styleable.WaveCurve_wave_count, DEFAULT_WAVELENTH);
		mLabelColor = attributes.getColor(R.styleable.WaveCurve_label_color, mPeakValueColor);
		mMarginLeft = dp2px(10);
		mLabelMargintTop = dp2px(1);
		mPeakValueMargintTop = dp2px(3);
		attributes.recycle();
		mMatrix = new  Matrix();
		mPeakValues= new int[mWaveCount];
		mLabels= new String[mWaveCount];
		setPeakValues(new int[]{12,11,8,18,9});
		setPeakValues(new String[]{"first","second","third","forth","fifth"});
	}

	public void setWavelength(int mWavelength) {
		this.mWavelength = mWavelength;
		invalidate();
	}

	public void setPeakValues(int[] peakvalues){
		this.mPeakValues = peakvalues;
		invalidate();
	}
	
	public void setPeakValues(String[] labels){
		this.mLabels = labels;
		invalidate();
	}

	public CurveView(Context context, int[]  peakValues, int mWavelength,
			String labels[]) {
		this(context);
		this.mPeakValues = peakValues;
		this.mWavelength = mWavelength;
		this.mLabels = labels;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < mPeakValues.length; i++) {
			drawCurve(canvas, i);
		}
	}

	private void drawCurve(Canvas canvas, int index) {  
		// y=Asin(wx+$)+h
		canvas.save();
		if (null == mMatrix) {
			mMatrix = getMatrix();
		}
		int peakValue = mPeakValues[index];
		mMatrix.setTranslate(mWavelength / 4 + mLeftSpace + index * mWavelength, mTopYScreen);
		canvas.setMatrix(mMatrix);
		Path path = new Path();
		float amplitude = peakValue * 10f;
		double defaultAngularFrequency = 2.0f * Math.PI / mWavelength;
		int i = -mWavelength * 1 / 4;
		int end = mWavelength * 3 / 4 ;
		int startX = index * mSingleWaveWidth ;
		int endX = index * mSingleWaveWidth  + mWavelength;
		int centerX = (startX + endX) / 2 ;
		for (; i <= end ; i++) {
			double wx = defaultAngularFrequency * (i - mOffset) ;
			float curY = (float) ( WATER_LEVEL_LINE - amplitude -  amplitude * Math.sin(wx));
			if (path.isEmpty()) {
				path.moveTo(i, curY);
			}
			path.lineTo(i, curY);
		}
		path.close();
		mPaint.setTextSize(10);
		mPaint.setColor(mWaveColor);
		canvas.drawPath(path, mPaint);
		canvas.restore();
		mPaint.setColor(mPeakCircleColor);
		mPaint.setStyle(Style.STROKE);
		mPaint.setTextSize(24);
		canvas.drawCircle((float) (centerX + Math.PI / 4), WATER_LEVEL_LINE - amplitude * 2, 6, mPaint);
		mPaint.setTextSize(24);
		mPaint.setColor(mPeakValueColor);
		mPaint.setStyle(Style.FILL);
		canvas.drawText(peakValue +"", centerX , WATER_LEVEL_LINE - amplitude * 2 - mPeakValueMargintTop, mPaint);
		String label = mLabels[index];
		float labelWidth = mPaint.measureText(label);
		canvas.drawText(label +"", centerX - labelWidth / 2, WATER_LEVEL_LINE  + mLabelMargintTop + (mLabelBounds.bottom - mLabelBounds.top), mPaint);
		canvas.drawLine(startX, WATER_LEVEL_LINE,endX , WATER_LEVEL_LINE, mPaint);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mLeftSpace = getLeft();
		mTopSpace = getTop();
		mPaint.setTextSize(24);
		mPaint.setStyle(Style.FILL);
		mPaint.getTextBounds(mLabels[0], 0, mLabels[0].length() -1, mLabelBounds);
		mCanvasHeight = (int) (mLabelBounds.bottom - mLabelBounds.top + mLabelMargintTop * 2 + WATER_LEVEL_LINE );
		final int[] location = new int[2];
		getLocationOnScreen(location);
		mTopYScreen = location[1];
		Log.i(VIEW_LOG_TAG, "tag : " + getTag() + " ,mLeftSpace : " + mLeftSpace + " ,mTopSpace : " + mTopSpace + ", getX : " + getX() + ", getY : " + getY()+ ", mTopYScreen : " + mTopYScreen );
		mWaveCount = mPeakValues.length ;
		int sw = getResources().getDisplayMetrics().widthPixels;
		int totalW =  mWavelength * mWaveCount >= sw  ? sw: mWavelength * mWaveCount;
		mSingleWaveWidth = totalW / mPeakValues.length ;
		setMeasuredDimension(totalW ,mCanvasHeight );
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.i(VIEW_LOG_TAG, "onSizeChanged");
	}
	
    private int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
