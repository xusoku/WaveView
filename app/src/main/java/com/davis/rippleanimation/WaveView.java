package com.davis.rippleanimation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by xushengfu on 2017/12/3.
 */

public class WaveView extends View {
    private Path path;
    private Paint paint;

    private int dx;
    private int dy;
    private Bitmap mBitmap;
    private int width, height;
    private Region region;

    private int waveView_boatBitmap;
    private boolean waveView_rise;
    private int duration;
    private int originY;
    private int waveHeight = 80;
    private int waveLength = 400;
    private ValueAnimator valueAnimator;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        waveView_boatBitmap = a.getResourceId(R.styleable.WaveView_boatBitmap, 0);
        waveView_rise = a.getBoolean(R.styleable.WaveView_rise, false);
        duration = (int) a.getDimension(R.styleable.WaveView_duration, 2000);
        originY = (int) a.getDimension(R.styleable.WaveView_originY, 400);
        waveHeight = (int) a.getDimension(R.styleable.WaveView_waveHeight, 200);
        waveLength = (int) a.getDimension(R.styleable.WaveView_waveLength, 400);
        a.recycle();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1; //缩放图片
        if (waveView_boatBitmap > 0) {
            mBitmap = BitmapFactory.decodeResource(getResources(), waveView_boatBitmap, options);
            mBitmap = getCircleBitmap(mBitmap);
        } else {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.photo, options);
        }
        paint = new Paint();
        paint.setColor(Color.parseColor("#ff0000"));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);//填充

        path = new Path();


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightsize = MeasureSpec.getSize(heightMeasureSpec);
        width = widthSize;
        height = heightsize;
        if (originY == 0) {
            originY = height;
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        setPathData();
        canvas.drawPath(path, paint);

        //绘制头像
        Rect bounds = region.getBounds();

        if (bounds.top > 0 || bounds.right > 0) {
            if (bounds.top < (originY - dy)) {//上边 从波峰到基准线
                canvas.drawBitmap(mBitmap, bounds.left - mBitmap.getWidth() / 2, bounds.top - mBitmap.getHeight(), paint);
            } else {//下边
                canvas.drawBitmap(mBitmap, bounds.right - mBitmap.getWidth() / 2, bounds.bottom - mBitmap.getHeight(), paint);
            }
        } else {

            if (bounds.top < (originY - dy)) {//上边 从波峰到基准线
                float x = width / 2 - mBitmap.getWidth();
                canvas.drawBitmap(mBitmap, (float) (x-0.01), originY - dy - mBitmap.getHeight(), paint);
            } else {//下边
                float x = width / 2 - mBitmap.getWidth();
                canvas.drawBitmap(mBitmap, (float) (x-0.01), originY - dy - mBitmap.getHeight(), paint);
            }


        }


    }

    private void setPathData() {
        path.reset();
        int halfWaveLength = waveLength / 2;
        path.moveTo(-waveLength + dx, originY - dy);

        for (int i = -waveLength; i < width + waveLength; i += waveLength) {
            path.rQuadTo(halfWaveLength / 2, -waveHeight, halfWaveLength, 0);//相对坐标
            path.rQuadTo(halfWaveLength / 2, waveHeight, halfWaveLength, 0);
        }

        region = new Region();
        float x = width / 2;
        Region clip = new Region((int) (x - 0.01), 0, (int) (x), height * 2);
        region.setPath(path, clip);


        //曲线封闭
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();
    }

    public void startAnimation() {
        valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(duration);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                dx = (int) (waveLength * fraction);
//                dy += 1;
                postInvalidate();

            }
        });
        valueAnimator.start();
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        try {
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(circleBitmap);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            float roundPx = 0.0f;
            if (bitmap.getWidth() > bitmap.getHeight()) {
                roundPx = bitmap.getHeight() / 2.0f;
            } else {
                roundPx = bitmap.getWidth() / 2.0f;
            }

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return circleBitmap;
        } catch (Exception e) {
            return bitmap;
        }

    }

}
