package de.dancefinalmusic;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class TimerRingView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private float progress = 0f;
    private ValueAnimator animator;
    private final float strokeWidth;

    public TimerRingView(Context context) {
        this(context, null);
    }

    public TimerRingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strokeWidth = dp(6);
        bgPaint.setStyle(Paint.Style.FILL);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setColors(int bgColor, int trackColor, int accentColor) {
        bgPaint.setColor(bgColor);
        trackPaint.setColor(trackColor);
        progressPaint.setColor(accentColor);
        invalidate();
    }

    public void setProgressFraction(float fraction) {
        fraction = Math.max(0f, Math.min(1f, fraction));
        if (Math.abs(fraction - progress) < 0.0005f) return;

        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(progress, fraction);
        animator.setDuration(700);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - strokeWidth / 2f;

        canvas.drawCircle(cx, cy, radius, bgPaint);
        canvas.drawCircle(cx, cy, radius, trackPaint);

        float sweep = progress * 360f;
        if (sweep > 0.01f) {
            arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);
            canvas.drawArc(arcRect, 270f, Math.min(sweep, 359.9f), false, progressPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
        animator = null;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
