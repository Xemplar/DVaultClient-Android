package com.xemplarsoft.dv.anim;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation {
    public static final int DIRECTION_HZ = 0x01;
    public static final int DIRECTION_VT = 0x02;
    public static final int DIRECTION_BOTH = 0x03;

    private final int target, direction;
    private View view;
    private int start;
    private boolean dir;

    public ResizeAnimation(View view, int direction, int target, int start) {
        this.view = view;
        this.target = target;
        this.start = start;
        this.direction = direction;
        dir = target > start;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int ret = 0;

        if(dir) ret = (int) (start + target * interpolatedTime);
        else    ret = target + (int)(start - start * interpolatedTime);

        if((direction & 0b01) == DIRECTION_HZ){
            view.getLayoutParams().width = ret;
        }
        if((direction & 0b10) == DIRECTION_VT) view.getLayoutParams().height = ret;

        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}