package jp.shts.android.keyakifeed.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class FloatingActionMenuBehavior extends CoordinatorLayout.Behavior<FloatingActionsMenu> {

    private static final String TAG = FloatingActionMenuBehavior.class.getSimpleName();

    public FloatingActionMenuBehavior(Context context, AttributeSet attrs) {
        // コンストラクタを追加しないとクラスを見つけることができず強制終了する
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionsMenu child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionsMenu child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}
