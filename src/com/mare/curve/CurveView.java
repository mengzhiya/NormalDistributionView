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
	private float mAmplitude;// 振幅
	private int mWavelength;// 波长
	private String mLabel;// 标签
	private Paint mPaint;
	private int mPeakCircleColor = Color.argb(200, 250, 0, 0),
			mPeakValueColor = Color.BLACK, mWaveFillColor = Color.argb(150, 128, 128, 0);
	private static final float DEFAULT_AMPLITUDE_VALUE = 50.0f;
	private static final int DEFAULT_WAVELENTH = 200;
	private int mWaveColor,mLabelColor;
	private final static int WATER_LEVEL_LINE = 300;
	private int mPeakValue;
	private int mMarginLeft = 0,mPeakValueMargintTop = 0,mLabelMargintTop = 0;
	private Matrix mMatrix ;
	private Rect mLabelBounds = new  Rect();
	private int mLeftSpace = 0, mCanvasHeight;
	private int mOffset = (int) (Math.PI / 4);
	private int mCenterX;

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
		mAmplitude = attributes.getFloat(R.styleable.WaveCurve_amplitude, DEFAULT_AMPLITUDE_VALUE);
		mWavelength = attributes.getInt(R.styleable.WaveCurve_wavelenth, DEFAULT_WAVELENTH);
		mPeakValue = attributes.getInt(R.styleable.WaveCurve_peakvalue, 0);
		mLabel = attributes.getString(R.styleable.WaveCurve_label);
		mLabelColor = attributes.getColor(R.styleable.WaveCurve_label_color, mPeakValueColor);
		Log.i(VIEW_LOG_TAG, "mAmplitude : " + mAmplitude + ", mWavelength : " + mWavelength);
		mMarginLeft = dp2px(10);
		mLabelMargintTop = dp2px(1);
		mPeakValueMargintTop = dp2px(3);
		attributes.recycle();
		mMatrix = new  Matrix();
	}

	public void setAmplitude(float amplitude) {
		this.mAmplitude = amplitude;
	}

	public void setmWavelength(int mWavelength) {
		this.mWavelength = mWavelength;
	}

	public void setmLabel(String mLabel) {
		this.mLabel = mLabel;
	}

	public CurveView(Context context, float mAmplitude, int mWavelength,
			String mLabel,int peakValue) {
		this(context);
		this.mAmplitude = mAmplitude;
		this.mWavelength = mWavelength;
		this.mLabel = mLabel;
		this.mPeakValue = peakValue;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawCurve(canvas);
	}


	private void drawCurve(Canvas canvas) {  
		// y=Asin(wx+$)+h
		canvas.save();
		if (null == mMatrix) {
			mMatrix = getMatrix();
			
		}
		mMatrix.setTranslate(mWavelength / 4 + mLeftSpace, 0);
		canvas.setMatrix(mMatrix);
		drawWave(canvas);
		canvas.restore();
		mPaint.setColor(mPeakCircleColor);
		mPaint.setStyle(Style.STROKE);
		mPaint.setTextSize(24);
		canvas.drawCircle((float) (mCenterX + Math.PI / 4), WATER_LEVEL_LINE - mAmplitude * 2, 6, mPaint);
		mPaint.setTextSize(24);
		mPaint.setColor(mPeakValueColor);
		mPaint.setStyle(Style.FILL);
		canvas.drawText(mPeakValue +"", mCenterX , WATER_LEVEL_LINE - mAmplitude * 2 - mPeakValueMargintTop, mPaint);
		float labelWidth = mPaint.measureText(mLabel);
		canvas.drawText(mLabel +"", mCenterX - labelWidth / 2, WATER_LEVEL_LINE  + mLabelMargintTop + (mLabelBounds.bottom - mLabelBounds.top), mPaint);
		canvas.drawLine(0, WATER_LEVEL_LINE,mWavelength , WATER_LEVEL_LINE, mPaint);
	}
	
	private void drawWave(Canvas canvas) {
		Path path = new Path();
		double defaultAngularFrequency = 2.0f * Math.PI / mWavelength;
		int i = -mWavelength * 1 / 4;
		int end = mWavelength * 3 / 4 ;
		mCenterX = /*(i + end) /2*/mWavelength /2  ;
		for (; i <= end ; i++) {
			double wx = defaultAngularFrequency * (i - mOffset) ;
			float curY = (float) ( WATER_LEVEL_LINE - mAmplitude -  mAmplitude * Math.sin(wx));
			if (path.isEmpty()) {
				path.moveTo(i, curY);
			}
			path.lineTo(i, curY);
		}
		path.close();
		mPaint.setTextSize(10);
		mPaint.setColor(mWaveColor);
		canvas.drawPath(path, mPaint);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mLeftSpace != getLeft()) {
			mLeftSpace = getLeft();
		};
		mPaint.setTextSize(24);
		mPaint.setStyle(Style.FILL);
		mPaint.getTextBounds(mLabel, 0, mLabel.length() -1, mLabelBounds);
		mCanvasHeight = (int) (mLabelBounds.bottom - mLabelBounds.top + mLabelMargintTop * 2 + WATER_LEVEL_LINE );
		Log.i(VIEW_LOG_TAG, "tag : " + getTag() + " ,mLeftSpace : " + mLeftSpace + ", getX : " + getX() + ", mlabelbound :" + mLabelBounds.toString());
		setMeasuredDimension(mWavelength /* + mMarginLeft * 2*/ ,mCanvasHeight );
	}
	
    private int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
