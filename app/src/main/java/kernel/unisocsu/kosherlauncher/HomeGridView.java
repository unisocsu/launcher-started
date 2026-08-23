package kernel.unisocsu.kosherlauncher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.List;

public class HomeGridView extends GridLayout {

    public interface OnItemSelectedListener {
        void onItemSelected(int index);
    }

    public interface OnItemLongPressedListener {
        void onItemLongPressed(int index);
    }

    public interface OnMoveFinishedListener {
        void onMoveFinished(
                int fromIndex,
                int toIndex
        );
    }

    private final List<View> items =
            new ArrayList<View>();

    private int selectedIndex = 0;

    private int columns = 4;

    private boolean moveMode = false;

    private int movingIndex = -1;

    private OnItemSelectedListener
            selectedListener;

    private OnItemLongPressedListener
            longPressedListener;

    private OnMoveFinishedListener
            moveFinishedListener;

    public HomeGridView(Context context) {
        super(context);

        setColumnCount(columns);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setColumns(int columns) {

        if (columns < 1) {
            columns = 1;
        }

        this.columns = columns;

        setColumnCount(columns);

        refreshLayout();
    }

    public void setOnItemSelectedListener(
            OnItemSelectedListener listener) {

        this.selectedListener = listener;
    }

    public void setOnItemLongPressedListener(
            OnItemLongPressedListener listener) {

        this.longPressedListener = listener;
    }

    public void setOnMoveFinishedListener(
            OnMoveFinishedListener listener) {

        this.moveFinishedListener = listener;
    }

    public void addItem(final View view) {

        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        final int index = items.size();

        view.setOnFocusChangeListener(
                new OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(
                            View v,
                            boolean hasFocus) {

                        if (!hasFocus) {
                            return;
                        }

                        selectedIndex = index;

                        updateSelection();

                        if (selectedListener != null) {
                            selectedListener.onItemSelected(
                                    selectedIndex
                            );
                        }
                    }
                }
        );

        view.setOnLongClickListener(
                new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(
                            View v) {

                        if (longPressedListener != null) {

                            longPressedListener
                                    .onItemLongPressed(index);
                        }

                        return true;
                    }
                }
        );

        items.add(view);

        addView(view);

        refreshLayout();
    }

    public void clearItems() {

        items.clear();

        removeAllViews();

        selectedIndex = 0;

        moveMode = false;

        movingIndex = -1;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public boolean isMoveMode() {
        return moveMode;
    }

    public int getMovingIndex() {
        return movingIndex;
    }

    public void startMove() {

        if (items.isEmpty()) {
            return;
        }

        moveMode = true;

        movingIndex = selectedIndex;

        updateSelection();
    }

    public void cancelMove() {

        moveMode = false;

        movingIndex = -1;

        updateSelection();
    }

    public void select(int index) {

        if (index < 0 ||
                index >= items.size()) {
            return;
        }

        selectedIndex = index;

        View view =
                items.get(index);

        view.requestFocus();

        updateSelection();
    }

    private void updateSelection() {

        for (int i = 0;
             i < items.size();
             i++) {

            View view = items.get(i);

            if (i == selectedIndex) {

                if (moveMode &&
                        i == movingIndex) {

                    applyMovingStyle(view);

                } else {

                    applySelectedStyle(view);
                }

            } else {

                applyNormalStyle(view);
            }
        }
    }

    private void applySelectedStyle(
            View view) {

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(220, 235, 255)
        );

        background.setStroke(
                4,
                Color.rgb(40, 100, 220)
        );

        background.setCornerRadius(14);

        view.setBackground(background);
    }

    private void applyMovingStyle(
            View view) {

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(255, 235, 180)
        );

        background.setStroke(
                5,
                Color.rgb(220, 140, 20)
        );

        background.setCornerRadius(14);

        view.setBackground(background);
    }

    private void applyNormalStyle(
            View view) {

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.TRANSPARENT
        );

        background.setCornerRadius(14);

        view.setBackground(background);
    }

    /*
     * API 19 COMPATIBLE LAYOUT
     *
     * Do not use:
     *
     * GridLayout.spec(column, 1, 1f)
     *
     * because the API 19 runtime on the target device
     * fails to resolve that method and throws:
     *
     * java.lang.NoSuchMethodError:
     * android.widget.GridLayout.spec
     *
     * Instead, each item gets an explicit width.
     */
    private void refreshLayout() {

        int screenWidth =
                getResources()
                        .getDisplayMetrics()
                        .widthPixels;

        int margin = 6;

        int totalMargins =
                margin * 2 * columns;

        int itemWidth =
                (screenWidth - totalMargins)
                        / columns;

        if (itemWidth < 1) {
            itemWidth = 1;
        }

        for (int i = 0;
             i < items.size();
             i++) {

            View view = items.get(i);

            LayoutParams params =
                    new LayoutParams();

            /*
             * Explicit width instead of:
             *
             * params.width = 0;
             * GridLayout.spec(..., 1f)
             *
             * This avoids the API 19 crash.
             */
            params.width = itemWidth;
            params.height = 120;

            /*
             * API 19 compatible overload.
             */
            params.columnSpec =
                    GridLayout.spec(
                            i % columns
                    );

            params.rowSpec =
                    GridLayout.spec(
                            i / columns
                    );

            params.setMargins(
                    margin,
                    margin,
                    margin,
                    margin
            );

            view.setLayoutParams(params);
        }

        updateSelection();
    }

    @Override
    public boolean onKeyDown(
            int keyCode,
            KeyEvent event) {

        if (items.isEmpty()) {
            return super.onKeyDown(
                    keyCode,
                    event
            );
        }

        int newIndex = selectedIndex;

        switch (keyCode) {

            case KeyEvent.KEYCODE_DPAD_LEFT:

                newIndex--;

                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:

                newIndex++;

                break;

            case KeyEvent.KEYCODE_DPAD_UP:

                newIndex -= columns;

                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:

                newIndex += columns;

                break;

            case KeyEvent.KEYCODE_DPAD_CENTER:

            case KeyEvent.KEYCODE_ENTER:

                if (moveMode) {

                    finishMove();

                } else {

                    View selected =
                            items.get(
                                    selectedIndex
                            );

                    selected.performClick();
                }

                return true;

            case KeyEvent.KEYCODE_BACK:

                if (moveMode) {

                    cancelMove();

                    return true;
                }

                return false;

            case KeyEvent.KEYCODE_1:

            case KeyEvent.KEYCODE_2:

            case KeyEvent.KEYCODE_3:

            case KeyEvent.KEYCODE_4:

            case KeyEvent.KEYCODE_5:

            case KeyEvent.KEYCODE_6:

            case KeyEvent.KEYCODE_7:

            case KeyEvent.KEYCODE_8:

            case KeyEvent.KEYCODE_9: {

                int digitIndex =
                        keyCode -
                                KeyEvent.KEYCODE_1;

                if (digitIndex >= 0 &&
                        digitIndex < items.size()) {

                    if (selectedIndex ==
                            digitIndex) {

                        items.get(
                                digitIndex
                        ).performClick();

                    } else {

                        select(digitIndex);
                    }
                }

                return true;
            }

            case KeyEvent.KEYCODE_0: {

                int zeroIndex = 9;

                if (zeroIndex >= 0 &&
                        zeroIndex < items.size()) {

                    if (selectedIndex ==
                            zeroIndex) {

                        items.get(
                                zeroIndex
                        ).performClick();

                    } else {

                        select(zeroIndex);
                    }
                }

                return true;
            }

            default:

                return super.onKeyDown(
                        keyCode,
                        event
                );
        }

        if (newIndex >= 0 &&
                newIndex < items.size()) {

            select(newIndex);
        }

        return true;
    }

    private void finishMove() {

        if (!moveMode) {
            return;
        }

        int from = movingIndex;
        int to = selectedIndex;

        moveMode = false;

        movingIndex = -1;

        if (from != to &&
                moveFinishedListener != null) {

            moveFinishedListener.onMoveFinished(
                    from,
                    to
            );
        }

        updateSelection();
    }
}