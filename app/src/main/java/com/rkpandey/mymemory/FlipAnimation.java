package com.rkpandey.mymemory;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import static java.lang.Math.PI;
import static java.lang.Math.max;
import static java.lang.Math.toDegrees;

public class FlipAnimation extends Animation {

    public static final int DURATION_MS = 200;
    private static final AccelerateDecelerateInterpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private final Drawable target;
    private final ImageView view;
    private final boolean reverse;

    private Camera camera;
    private float centerX;
    private float centerY;
    private boolean flipped;

    public FlipAnimation(ImageView view, Drawable target, boolean reverse) {
        this.view = view;
        this.target = target;
        this.reverse = reverse;
        setDuration(DURATION_MS);
        setFillAfter(false);
        setInterpolator(INTERPOLATOR);
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        centerX = width / 2;
        centerY = height / 2;
        camera = new Camera();
        view.setCameraDistance(1_000 * max(width, height));
        flipped = false;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        double degrees = toDegrees(PI * interpolatedTime);

        if (interpolatedTime >= 0.5f) {
            degrees += 180.f;
            if (!flipped) {
                view.setImageDrawable(target);
                flipped = true;
            }
        }
        if (reverse) {
            degrees = -degrees;
        }

        Matrix matrix = t.getMatrix();
        camera.save();
        camera.rotateY((float) degrees);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
}
