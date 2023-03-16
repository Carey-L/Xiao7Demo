package com.example.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 自定义日历控件
 */
public class MyCalendar extends View {

    public static final String FORMAT_DATE_DIVIDE = "/";

    /**
     * 小标题颜色、大小、上下距离
     */
    private final int mSubTitleTextColor;
    private final float mSubTitleTextSize;
    private final float mSubTitleVerticalSpac;

    /**
     * 分割线颜色、高度
     */
    private final int mSplitLineColor;
    private final float splitLineWidth;

    /**
     * 未选中日期字体颜色、大小，选中日期字体颜色、背景、背景圆形半径
     */
    private final int mDayTextColor;
    private final float mTextSizeDay;
    private final int mSelectTextColor;
    private final int mSelectBgColor;
    private final float mSelectRadius;

    /**
     * 日期字体行间距
     */
    private final float mLineSpac;

    /**
     * 中间日期区域的上下边距
     */
    private final float mPadTop;
    private final float mPadBottom;

    /**
     * 日期部分整体往上偏移量
     */
    private final float dayPartUpOffset;

    /**
     * 选中日期为今天下方提示字体大小
     */
    private final float todayTipSize;

    /**
     * 文字画笔、背景画笔、选中今天画笔、分割线画笔
     */
    private final Paint mPaint = new Paint();
    private final Paint mBgPaint = new Paint();
    private final Paint mTodayPaint = new Paint();
    private final Paint mLinePaint = new Paint();

    /**
     * 小标题高度、小标题 + 分割线高度
     */
    private float mSubTitleHeight;
    private float mTopPartHeight;

    /**
     * 日期高度
     */
    private float mDayHeight;

    /**
     * 一行日期高度
     */
    private float oneHeight;

    /**
     * 日期每列宽度（7等分）
     */
    private int mColumnWidth;

    /**
     * 当前月份
     */
    private Date mUpdateMonthDate;

    /**
     * 选中的日期、上次选中的日期
     */
    private int mSelectDay;
    private int mLastSelectDay;

    /**
     * 当月第一天位置索引，第一天是不同星期，起点也就不同
     */
    private int mFirstDayIdx;

    /**
     * 第一行、最后一行能展示多少日期
     */
    private int mFirstLineNum;
    private int mLastLineNum;

    /**
     * 日期行数
     */
    private int mLineNum;

    /**
     * 当前日期
     */
    private int mCurrentDay;

    /**
     * 是否为当月
     */
    private boolean isCurrentMonth;

    /**
     * 聊天记录显示日期不超过当前日期，是否已经到绘制到当前日期了
     */
    private boolean haveDrawToday;

    private IClickListener mListener;
    private GestureDetector mGestureDetector;

    public MyCalendar(Context context) {
        this(context, null);
    }

    public MyCalendar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyCalendar, defStyleAttr, 0);
        mPadTop = a.getDimension(R.styleable.MyCalendar_padTop, 0);
        mPadBottom = a.getDimension(R.styleable.MyCalendar_padBottom, 0);
        mSubTitleTextSize = a.getDimension(R.styleable.MyCalendar_subTitleTextSize, 51);
        mSubTitleTextColor = a.getColor(R.styleable.MyCalendar_subTitleTextColor, Color.BLACK);
        mSubTitleVerticalSpac = a.getDimension(R.styleable.MyCalendar_subTitleVerticalSpace, 15);
        mSplitLineColor = a.getColor(R.styleable.MyCalendar_splitLineColor, Color.parseColor("#f9f9f9"));
        splitLineWidth = a.getDimension(R.styleable.MyCalendar_splitLineWidth, 3);
        mTextSizeDay = a.getDimension(R.styleable.MyCalendar_dayTextSize, 51);
        mDayTextColor = a.getColor(R.styleable.MyCalendar_dayTextColor, Color.GRAY);
        mLineSpac = a.getDimension(R.styleable.MyCalendar_daylineSpace, 60);
        mSelectTextColor = a.getColor(R.styleable.MyCalendar_selectTextColor, Color.parseColor("#ffffff"));
        mSelectBgColor = a.getColor(R.styleable.MyCalendar_selectBgColor, Color.parseColor("#12cdb0"));
        mSelectRadius = a.getDimension(R.styleable.MyCalendar_selectRadius, 45);
        dayPartUpOffset = a.getDimension(R.styleable.MyCalendar_dayPartUpOffset, 0);
        todayTipSize = a.getDimension(R.styleable.MyCalendar_todayTipSize, 33);
        a.recycle();
        init();
    }

    /**
     * 计算相关常量，构造方法中调用
     */
    private void init() {
        mGestureDetector = new GestureDetector(getContext(), new GestureCallback());
        mBgPaint.setAntiAlias(true);
        mPaint.setAntiAlias(true);
        mTodayPaint.setAntiAlias(true);
        mLinePaint.setAntiAlias(true);

        // 选中今天的提示画笔初始化
        mTodayPaint.setColor(mSelectBgColor);
        mTodayPaint.setTextSize(todayTipSize);

        // 小标题高度计算
        mPaint.setTextSize(mSubTitleTextSize);
        mSubTitleHeight = getFontHeight(mPaint) + 2 * mSubTitleVerticalSpac;
        // 日期上半部分高度 = 小标题高度 + 分割线高度
        mTopPartHeight = mSubTitleHeight + splitLineWidth;
        // 日期高度计算
        mPaint.setTextSize(mTextSizeDay);
        mDayHeight = getFontHeight(mPaint);
        // 每行高度 = 行间距 + 日期字体高度
        oneHeight = mLineSpac + mDayHeight;
    }

    /**
     * @param calendar 传入日历，计算该月日期位置
     */
    private void computeRowsAndColumns(Calendar calendar) {
        // 当月天数
        int mDayOfMonth = isCurrentMonth ? Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                : calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 当月 1 号星期几
        mFirstDayIdx = getDayOfWeekByDate(calendar.getTime());
        mLineNum = 1;
        // 第一行能展示的天数
        mFirstLineNum = 7 - mFirstDayIdx;
        // 最后一行能展示的天数
        mLastLineNum = 0;
        int remainDays = mDayOfMonth - mFirstLineNum;
        while (remainDays > 7) {
            mLineNum++;
            remainDays -= 7;
        }
        if (remainDays > 0) {
            mLineNum++;
            mLastLineNum = remainDays;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取宽的尺寸，宽度 = 填充父窗体
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        mColumnWidth = widthSize / 7;
        int height = (int) (mLineNum * oneHeight + mPadTop + mPadBottom);
        height = (int) computeY(height);
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawSubTitle(canvas);
        drawSplitLine(canvas);
        drawDay(canvas);
    }

    /**
     * 画年月小标题
     */
    private void drawSubTitle(Canvas canvas) {
        mPaint.setTextSize(mSubTitleTextSize);
        mPaint.setColor(mSubTitleTextColor);
        float start = (float) getWidth() / 28;
        float y = mSubTitleVerticalSpac + getFontLeading(mPaint);
        String subTitle = getMonthYear(mUpdateMonthDate);
        canvas.drawText(subTitle, start, y, mPaint);
    }

    /**
     * 画小标题和日期间的分割线
     */
    private void drawSplitLine(Canvas canvas) {
        mLinePaint.setStrokeWidth(splitLineWidth);
        mLinePaint.setColor(mSplitLineColor);
        float startX = (float) getWidth() / 28;
        float endX = (float) 27 * getWidth() / 28;
        canvas.drawLine(startX, mTopPartHeight, endX, mTopPartHeight, mLinePaint);
    }

    /**
     * 绘制日期
     */
    private void drawDay(Canvas canvas) {
        // 某行开始绘制的 Y 坐标，第一行开始的 y 坐标为日期上半部分高度 - 日期整体向上偏移量
        float lineTop = computeY(mPadTop);
        for (int line = 0; line < mLineNum; line++) {
            if (haveDrawToday) {
                break;
            }
            int overDay = mFirstLineNum + (line - 1) * 7;
            if (line == 0) {
                drawDay(canvas, lineTop, mFirstLineNum, 0, mFirstDayIdx);
            } else if (line == mLineNum - 1) {
                lineTop += oneHeight;
                drawDay(canvas, lineTop, mLastLineNum, overDay, 0);
            } else {
                lineTop += oneHeight;
                drawDay(canvas, lineTop, 7, overDay, 0);
            }
        }
    }

    private float computeY(float y) {
        return y + mTopPartHeight - dayPartUpOffset;
    }

    /**
     * 绘制某一行的日期
     *
     * @param top        顶部坐标
     * @param count      此行需要绘制的日期数量
     * @param overDay    已经绘制过的日期，从 overDay+1 开始绘制
     * @param startIndex 此行第一个日期的星期索引
     */
    private void drawDay(Canvas canvas, float top, int count, int overDay, int startIndex) {
        mPaint.setTextSize(mTextSizeDay);
        float dayTextLeading = getFontLeading(mPaint);
        for (int i = 0; i < count; i++) {
            int left = (startIndex + i) * mColumnWidth;
            int day = (overDay + i + 1);
            // 大于今天结束绘制x
            if (isCurrentMonth && mCurrentDay < day) {
                haveDrawToday = true;
                break;
            }
            String dayStr = day + "";
            int dayTxtLen = (int) getFontLength(mPaint, dayStr);
            int x = left + (mColumnWidth - dayTxtLen) / 2;
            drawSelectRange(canvas, left, top, day);
            canvas.drawText(dayStr, x, top + mLineSpac + dayTextLeading, mPaint);
        }
    }

    /**
     * 画选中状态
     */
    private void drawSelectRange(Canvas canvas, int left, float top, int day) {
        mPaint.setColor(mDayTextColor);
        //绘制选中的日期
        if (mSelectDay == day) {
            if (isCurrentMonth && day == mCurrentDay) {
                int dayTxtLen = (int) getFontLength(mTodayPaint, "今天");
                int x = left + (mColumnWidth - dayTxtLen) / 2;
                float dayTextLeading = getFontLeading(mTodayPaint);
                float offsetY = mSelectRadius + mDayHeight / 2;
                canvas.drawText("今天", x, top + mLineSpac + dayTextLeading + offsetY, mTodayPaint);
            }
            mPaint.setColor(mSelectTextColor);
            mBgPaint.setColor(mSelectBgColor);
            float centerX = left + (float) mColumnWidth / 2;
            float centerY = top + mLineSpac + mDayHeight / 2;
            canvas.drawCircle(centerX, centerY, mSelectRadius, mBgPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 焦点滑动
     */
    private void touchFocusMove(final PointF point) {
        //触摸的是日期
        if (isTouchDay(point)) {
            touchDay(point);
        }
    }

    /**
     * 是否触摸的是日期
     */
    private boolean isTouchDay(final PointF point) {
        return point.y > computeY(mPadTop);
    }

    /**
     * 事件点在 日期区域 范围内
     */
    private void touchDay(final PointF point) {
        float top = computeY(mPadTop);
        float diffY = point.y - top;
        int focusLine = (int) (diffY / oneHeight);
        if (diffY % oneHeight > 0) {
            focusLine++;
        }
        // 触摸是否在日期范围内
        boolean availability = focusLine <= mLineNum;
        if (availability) {
            // 根据 X 坐标找到具体的焦点日期
            int xIdx = (int) Math.ceil(point.x / mColumnWidth);
            if (xIdx <= 0) {
                xIdx = 1;
            }
            if (xIdx > 7) {
                xIdx = 7;
            }
            if (focusLine == 1) {
                // 第一行
                if (xIdx <= mFirstDayIdx) {
                    // 到开始空位
                    updateSelectedDay(mSelectDay);
                } else {
                    updateSelectedDay(xIdx - mFirstDayIdx);
                }
            } else if (focusLine == mLineNum) {
                // 最后一行
                if (xIdx > mLastLineNum) {
                    // 到结尾空位
                    updateSelectedDay(mSelectDay);
                } else {
                    updateSelectedDay(mFirstLineNum + (focusLine - 2) * 7 + xIdx);
                }
            } else {
                updateSelectedDay(mFirstLineNum + (focusLine - 2) * 7 + xIdx);
            }
        } else {
            //超出日期区域后，视为事件结束，响应最后一个选择日期的回调
            updateSelectedDay(mSelectDay);
        }
    }

    /**
     * 设置选中的日期
     */
    private void updateSelectedDay(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mUpdateMonthDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        Date date = dayStr2Date(year + FORMAT_DATE_DIVIDE + month + FORMAT_DATE_DIVIDE + day);
        if (date == null) {
            return;
        }
        mSelectDay = day;
        invalidate();
        if (mListener != null && mLastSelectDay != mSelectDay) {
            mLastSelectDay = mSelectDay;
            mListener.onDayClick(date.getTime());
        }
    }

    /**
     * 设置月份
     */
    public void updateMonth(Date updateMonthDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        // 当前日期
        mCurrentDay = calendar.get(Calendar.DAY_OF_MONTH);
        // 当前月份
        Date currentMonthDate = dayStr2Date(day2Str(new Date()));
        mUpdateMonthDate = dayStr2Date(day2Str(updateMonthDate));
        if (currentMonthDate != null && getMonthMiddle(currentMonthDate) == (getMonthMiddle(mUpdateMonthDate))) {
            isCurrentMonth = true;
            mSelectDay = mCurrentDay;
        } else {
            isCurrentMonth = false;
            mSelectDay = 0;
        }
        calendar.setTime(mUpdateMonthDate);
        computeRowsAndColumns(calendar);
    }

    /**
     * 月份增减
     */
    public void monthChange(int change) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mUpdateMonthDate);
        calendar.add(Calendar.MONTH, change);
        haveDrawToday = false;
        Date currentMonthDate = dayStr2Date(day2Str(new Date()));
        Date updateMonthDate = dayStr2Date(day2Str(calendar.getTime()));
        if (currentMonthDate != null && updateMonthDate != null && currentMonthDate.getTime() < updateMonthDate.getTime()) {
            Toast.makeText(getContext(), "没有下个月的记录", Toast.LENGTH_SHORT).show();
            return;
        }
        updateMonth(calendar.getTime());
        requestLayout();
        invalidate();
    }

    /**
     * 返回指定画笔绘制文本长度
     */
    private float getFontLength(Paint paint, String str) {
        return paint.measureText(str);
    }

    /**
     * 返回指定笔的文字高度
     */
    private float getFontHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    /**
     * 返回指定笔离文字顶部的基准距离
     */
    private float getFontLeading(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.leading - fm.ascent;
    }

    public static Date dayStr2Date(String str) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            return df.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String day2Str(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return format.format(date);
    }

    public static String getMonthYear(Date date) {
        if (date == null) {
            date = new Date();
        }
        DateFormat df = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
        return df.format(date);
    }

    public static String getMonthDayYear(Date date) {
        if (date == null) {
            date = new Date();
        }
        DateFormat df = new SimpleDateFormat("MM dd,yyyy", Locale.getDefault());
        return df.format(date);
    }

    /**
     * 获取指定日期所在月份 15 号的时间戳/秒
     * @param date 指定日期
     */
    public static long getMonthMiddle(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 15);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND,59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis() / 1000;
    }

    /**
     * 判断当月 1 号是星期几
     */
    public static int getDayOfWeekByDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * 日期点击回调
     */
    public interface IClickListener {
        void onDayClick(long mills);
    }

    public void setOnIClickListener(IClickListener listener) {
        this.mListener = listener;
    }

    public class GestureCallback extends GestureDetector.SimpleOnGestureListener {

        // 焦点坐标
        private final PointF mFocusPoint = new PointF();

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mFocusPoint.set(e.getX(), e.getY());
            touchFocusMove(mFocusPoint);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // true，否则无法触发 onSingleTap、onFling 等方法
            return true;
        }
    }
}
