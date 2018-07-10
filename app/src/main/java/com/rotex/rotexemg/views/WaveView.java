package com.rotex.rotexemg.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.rotex.rotexhand.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by QingWuyong on 2016/8/31.
 */

public class WaveView extends View {
    private final static String XmKEY = "Xpos";
    private final static String YmKEY = "Ypos";
    private int mbackLineColor;
    private int mtitleColor;
    private int mpointerLineColor;
    private int mtitleSize;
    private int mXYTextSize;
    // 屏幕上的数量
    private int mPointMaxAmount;
    private float mXUnitLength;
    // 当前加入点
    private int mCurP = 0;
    private int mRemovedPointNum = 0;
    private int mEveryNPointBold = 1;
    // 是否是第第一次加载背景
    private Boolean misfristDrawBackGround = true;
    // 上下左右缩进
    private int mLeftIndent = 100;
    private int mRightIndent = 100;
    private int mBottomIndent = 100;
    private int mTopIndent = 100;
    private float mCurX = mLeftIndent + 4;
    private float mCurY = mTopIndent;


    // 设置每mEveryNPointRefresh个点刷新电图
    private int mEveryNPointRefresh = 1;
    private float mMaxYNumber;
    private int mHeight;
    private int mWidth;
    private float mEffectiveHeight = 1;// 有效高度
    private float mEffectiveWidth = 1;// 有效宽度
    private float mEveryOneValue = 1;// 每个格子的�?
    private int mLatticeWidth = 1;
    private List<Map<String, Float>> mListPoint = new ArrayList<Map<String, Float>>();
    private List<Float> mListVLine = new ArrayList<Float>();
    private List<Float> mListHLine = new ArrayList<Float>();
    private Paint mPaintLine;
    private Paint mPaintLine1;
    private Paint mPaintDataLine;
    private TextPaint mTitleTextPaint;
    private TextPaint mXYTextPaint;
    private String title = " ";
    private int mYSize;
    private int mXSize;
    private Context mcontext;


    public WaveView(Context context, AttributeSet attrs,
                    int defStyle) {
        super(context, attrs, defStyle);
        mcontext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.elg);
        mbackLineColor = typedArray.getColor(R.styleable.elg_BackLineColor,
                Color.GREEN);
        mtitleColor = typedArray
                .getColor(R.styleable.elg_TitleColor, Color.RED);
        mpointerLineColor = typedArray.getColor(
                R.styleable.elg_PointerLineColor, Color.WHITE);
        mtitleSize = typedArray.getDimensionPixelSize(
                R.styleable.elg_TitleSize, 30);
        mXYTextSize = typedArray.getDimensionPixelSize(
                R.styleable.elg_XYTextSize, 20);
        typedArray.recycle();
        initView();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mcontext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.elg);
        mbackLineColor = typedArray.getColor(R.styleable.elg_BackLineColor,
                Color.GREEN);
        mtitleColor = typedArray
                .getColor(R.styleable.elg_TitleColor, Color.RED);
        mpointerLineColor = typedArray.getColor(
                R.styleable.elg_PointerLineColor, Color.WHITE);
        mtitleSize = typedArray.getDimensionPixelSize(
                R.styleable.elg_TitleSize, 30);
        mXYTextSize = typedArray.getDimensionPixelSize(
                R.styleable.elg_XYTextSize, 20);
        typedArray.recycle();
        initView();
    }

    public WaveView(Context context) {
        this(context, null);
        mcontext = context;
        initView();
    }

    private void initView() {
        mPaintLine = new Paint();
        mPaintLine.setStrokeWidth(2.5f);
        mPaintLine.setColor(mpointerLineColor);
        mPaintLine.setAntiAlias(true);

        mPaintDataLine = new Paint();
        mPaintDataLine.setColor(mbackLineColor);
        mPaintDataLine.setAntiAlias(true);
        mPaintDataLine.setStrokeWidth(10);
        mXYTextPaint = new TextPaint();
        mXYTextPaint.setColor(mtitleColor);
        mXYTextPaint.setTextSize(mXYTextSize);
        mTitleTextPaint = new TextPaint();
        mTitleTextPaint.setColor(mtitleColor);
        mTitleTextPaint.setTextSize(mtitleSize);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (misfristDrawBackGround) {
            mHeight = getHeight();
            mWidth = getWidth();
           // mCurX=mWidth-mLeftIndent - 4;
         //   Log.e("gggg", "init mCurX=" + mCurX);
        }
        mEffectiveHeight = mHeight - mTopIndent - mBottomIndent;
        mEffectiveWidth = mWidth - mRightIndent - mLeftIndent;

        mXUnitLength = (mEffectiveWidth) / (mPointMaxAmount - 1);// 两条线之间的间距等于宽减出左右缩进除以点�?1
        drawBackground(canvas);
        drawWave(canvas);
    }

    // 画背景图以及格子
    public void drawBackground(Canvas canvas) {
        if (misfristDrawBackGround) {
            mYSize = (int) (mMaxYNumber / mEveryOneValue);// 垂直格子数量
            mLatticeWidth = (int) (mEffectiveHeight / mYSize);
            mXSize = (mWidth - mRightIndent - mLeftIndent) / mLatticeWidth;// 水平格子数量
            float curX = 0;
            if (mEveryNPointBold > mYSize || mEveryNPointBold > mXSize) {
                mEveryNPointBold = Math.min(mYSize, mXSize) / 2 + 1;
            }
            for (int i = 0; i < mXSize; i++) {
                mListVLine.add(curX);
                curX += mLatticeWidth;
            }
            float curY = 0;
            for (int j = 0; j < mYSize; j++) {
                mListHLine.add(curY);
                curY += mLatticeWidth;
            }
            misfristDrawBackGround = false;
        }
        mPaintDataLine.setStrokeWidth(1);
        int sText = 5;
        for (int i = 0; i < mListVLine.size(); i++) {
            sText = 5 * i;
            canvas.drawText(sText + "", mListVLine.get(i) + mTopIndent, mHeight
                    - mTopIndent + mXYTextSize, mXYTextPaint);
            if (i == 0) {
                mPaintDataLine.setStrokeWidth(8);
                canvas.drawLine(mListVLine.get(i) + mLeftIndent,
                        0 + mTopIndent, mListVLine.get(i) + mLeftIndent,
                        mHeight - mBottomIndent, mPaintDataLine);
                mPaintDataLine.setStrokeWidth(1);
            } else {
                if (i % mEveryNPointBold == 0) {
                    mPaintDataLine.setStrokeWidth(3);
                    canvas.drawLine(mListVLine.get(i) + mLeftIndent,
                            0 + mTopIndent, mListVLine.get(i) + mLeftIndent,
                            mHeight - mBottomIndent, mPaintDataLine);
                    mPaintDataLine.setStrokeWidth(1);
                } else {
                    canvas.drawLine(mListVLine.get(i) + mLeftIndent,
                            0 + mTopIndent, mListVLine.get(i) + mLeftIndent,
                            mHeight - mBottomIndent, mPaintDataLine);
                }
            }
        }
        mPaintDataLine.setStrokeWidth(6);
        canvas.drawLine(0 + mLeftIndent, mHeight - mTopIndent, mWidth
                - mRightIndent, mHeight - mBottomIndent, mPaintDataLine);
        mPaintDataLine.setStrokeWidth(1);
        String sYText = "";
        for (int i = 0; i < mListHLine.size(); i++) {
            if (i == 0) {
                sYText = (int) mEveryOneValue * (mYSize - i) + "";
                canvas.drawText(sYText, mLeftIndent - mXYTextSize * 3,
                        mListHLine.get(i) + mTopIndent, mXYTextPaint);
                mPaintDataLine.setStrokeWidth(8);
                canvas.drawLine(0 + mLeftIndent,
                        mListHLine.get(i) + mTopIndent, mWidth - mRightIndent,
                        mListHLine.get(i) + mBottomIndent, mPaintDataLine);
                mPaintDataLine.setStrokeWidth(1);
            } else {
                if (i % mEveryNPointBold == 0) {
                    sYText = (int) mEveryOneValue * (mYSize - i) + "";
                    canvas.drawText(sYText, mLeftIndent - mXYTextSize * 3,
                            mListHLine.get(i) + mTopIndent, mXYTextPaint);
                    mPaintDataLine.setStrokeWidth(3);
                    canvas.drawLine(0 + mLeftIndent, mListHLine.get(i)
                                    + mTopIndent, mWidth - mRightIndent,
                            mListHLine.get(i) + mBottomIndent, mPaintDataLine);
                    mPaintDataLine.setStrokeWidth(1);
                } else {
                    canvas.drawLine(0 + mLeftIndent, mListHLine.get(i)
                                    + mTopIndent, mWidth - mRightIndent,
                            mListHLine.get(i) + mBottomIndent, mPaintDataLine);
                }
            }
        }
        canvas.drawText(title, mWidth / 2 - 100, mTopIndent / 2,
                mTitleTextPaint);
        mPaintDataLine.setStrokeWidth(8);
        canvas.drawLine(mWidth - mRightIndent, 0 + mTopIndent, mWidth
                - mRightIndent, mHeight - mBottomIndent, mPaintDataLine);
    }

    // 画点
    public void drawWave(Canvas canvas) {
        for (int index = 0; index < mListPoint.size(); index++) {
            if (mListPoint.size() == mPointMaxAmount
                    && (index >= mCurP && index < mCurP + mRemovedPointNum)) {
                continue;
            }
            if (index > 0) {
                if (mListPoint.get(index).get(YmKEY) < 0
                        || mListPoint.get(index).get(YmKEY) < mTopIndent) {
                    continue;
                }
                canvas.drawLine(mListPoint.get(index - 1).get(XmKEY),
                        mListPoint.get(index - 1).get(YmKEY),
                        mListPoint.get(index).get(XmKEY), mListPoint.get(index)
                                .get(YmKEY), mPaintLine);

                canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                        Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            }
        }
    }

    // 设置 心电图的�?
    public void setLinePoint(float curY) {
        Map<String, Float> temp = new HashMap<String, Float>();
        temp.put(XmKEY, mCurX);
     //   Log.e("Tagggg", "mCurX=" + mCurX);
        mCurX = mCurX+mXUnitLength;
        // 计算y真实�?���?
        float number = curY / mEveryOneValue;// 这个数应该占的格子数
        if (mHeight != 0) {
            mCurY = mHeight - (mBottomIndent + number * mLatticeWidth);
        }
        if (mCurY < mTopIndent) {
            mCurY = mTopIndent + 10;
        }
        temp.put(YmKEY, mCurY);
        // 判断当前点是否大于最大点�?
        if (mCurP < mPointMaxAmount) {
            try {
                if (mListPoint.size() == mPointMaxAmount
                        && mListPoint.get(mCurP) != null) {
                    mListPoint.remove(mCurP);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mListPoint.add(mCurP, temp);
            mCurP++;
        } else {
            mCurP = 0;
            mCurX = mRightIndent;
        }

        if (mCurP % mEveryNPointRefresh == 0) {
            invalidate();
        }
    }

    public void setRemovedPointNum(int removedPointNum) {
        mRemovedPointNum = removedPointNum;
    }

    // 设置每N个点刷新�?��
    public void setEveryNPointRefresh(int num) {
        mEveryNPointRefresh = num;
    }

    public float getCurrentPointX() {
        return mCurX;
    }

    public float getCurrentPointY() {
        return mCurY;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // 设置�?��屏幕有多少个�?
    public void setMaxPointAmount(int i) {
        mPointMaxAmount = i;
    }

    // 设置几个格子画一条粗�?
    public void setEveryNPoint(int everyNPointBold) {
        if (everyNPointBold < 0) {
            return;
        }
        mEveryNPointBold = everyNPointBold;
    }

    // 设置Y轴最大�?
    public void setMaxYNumber(float maxYNumber) {
        this.mMaxYNumber = maxYNumber;
    }

    // 设置心电图标�?
    public void setTitle(String title) {
        this.title = title;
    }

    // 设置格子的单�?
    public void setEffticeValue(int value) {
        mEveryOneValue = value;
    }


}
