package apps.apidez.com.mycustomviewproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

// this represent the paint view
public class PaintView extends View {
    public static final int MAX_FINGERS = 5; // max finger count
    private Path[] mFingerPaths = new Path[MAX_FINGERS]; // represent path of five finger
    private Paint mFingerPaint; // painting color
    private Paint mClearPaint; // clear paint
    private ArrayList<Path> mCompletedPaths; // all path
    private RectF mPathBounds = new RectF(); // hold the rect that view need to update
    private Rect mBound = new Rect();
    public boolean isClear = false;
    private int index = -1; // current final path
    private DrawingListener mListener;

    // constructor
    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // listener
        if (getContext() instanceof TestActivity) {
            mListener = (TestActivity) getContext();
        }

        // setup the paint and others in this section
        mCompletedPaths = new ArrayList<>();

        // color to draw
        mFingerPaint = new Paint();
        mFingerPaint.setAntiAlias(true);
        mFingerPaint.setColor(Color.BLACK);
        mFingerPaint.setStrokeWidth(6);
        mFingerPaint.setStyle(Paint.Style.STROKE);
        mFingerPaint.setStrokeCap(Paint.Cap.BUTT);

        // color to clear
        mClearPaint = new Paint();
        mClearPaint.setAntiAlias(true);
        mClearPaint.setColor(Color.WHITE);
    }

    // can undo
    public boolean canUndo() {
        return index >= 0;
    }

    // can redo
    public boolean canRedo() {
        return index < mCompletedPaths.size() - 1;
    }

    // undo method
    public void undoDrawing() {
        if (canUndo()) {
            index--;
            debug();
            invalidate();
            mListener.onDrawSetting(canUndo(), canRedo());
        }
    }

    // redo method
    public void redoDrawing() {
        if (canRedo()) {
            index++;
            debug();
            invalidate();
            mListener.onDrawSetting(canUndo(), canRedo());
        }
    }

    // debug
    private void debug() {
        Log.d("App", mCompletedPaths.size() + " vs " + index);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isClear) {
            // redraw the screen every time the view call redraw, invalidate
            for (int i = 0; i <= index; i++) {
                canvas.drawPath(mCompletedPaths.get(i), mFingerPaint);
            }
            for (Path path : mFingerPaths) {
                if (path != null)
                    canvas.drawPath(path, mFingerPaint);
            }
        } else {
            mBound.set(getLeft(), getTop(), getRight(), getBottom());
            canvas.drawRect(mBound, mClearPaint);
            mCompletedPaths.clear();
            isClear = false;
            index = -1;
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // update new path went finger moving
        int pointerCount = event.getPointerCount();
        int cappedPointerCount = pointerCount > MAX_FINGERS ? MAX_FINGERS : pointerCount;
        int actionIndex = event.getActionIndex();
        int action = event.getActionMasked();
        int id = event.getPointerId(actionIndex);

        if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) // finger down
                && id < MAX_FINGERS) {
            // setup path
            mFingerPaths[id] = new Path();

            // init point down
            mFingerPaths[id].moveTo(event.getX(actionIndex), event.getY(actionIndex));
        } else if ((action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_UP) // release the finger
                && id < MAX_FINGERS) {
            // end of path
            mFingerPaths[id].setLastPoint(event.getX(actionIndex), event.getY(actionIndex));

            // remove all previous path before index
            for (int i = mCompletedPaths.size() - 1; i >= index + 1; i--) {
                mCompletedPaths.remove(i);
            }

            // add new path to the complete path
            mCompletedPaths.add(mFingerPaths[id]);
            index++;
            debug();

            // update UI
            mListener.onDrawSetting(canUndo(), canRedo());

            // compute the bounded rect
            mFingerPaths[id].computeBounds(mPathBounds, true);

            // call onDraw to update this bounded rectangle
            // invalidate only the part of the view that will be update --> better performance
            // rather than calling invalidate()
            invalidate((int) mPathBounds.left, (int) mPathBounds.top,
                    (int) mPathBounds.right, (int) mPathBounds.bottom);

            // release this path for memory
            mFingerPaths[id] = null;
        }

        // moving the finger
        for(int i = 0; i < cappedPointerCount; i++) {
            if(mFingerPaths[i] != null) {
                int index = event.findPointerIndex(i);

                // moving the line
                mFingerPaths[i].lineTo(event.getX(index), event.getY(index));

                // compute bounded rect
                mFingerPaths[i].computeBounds(mPathBounds, true);

                // update bound
                // invalidate only the part of the view that will be update --> better performance
                // rather than calling invalidate()
                invalidate((int) mPathBounds.left, (int) mPathBounds.top,
                        (int) mPathBounds.right, (int) mPathBounds.bottom);
            }
        }

        return true;
    }

}