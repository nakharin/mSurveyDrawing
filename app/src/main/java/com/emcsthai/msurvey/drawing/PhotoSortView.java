package com.emcsthai.msurvey.drawing;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import org.metalev.multitouch.controller.MultiTouchController;
import org.metalev.multitouch.controller.MultiTouchController.MultiTouchObjectCanvas;
import org.metalev.multitouch.controller.MultiTouchController.PointInfo;
import org.metalev.multitouch.controller.MultiTouchController.PositionAndScale;

import java.util.ArrayList;

public class PhotoSortView extends View implements MultiTouchObjectCanvas<PhotoSortView.Img> {

    private ArrayList<Img> mImages = new ArrayList<>();

    private MultiTouchController<Img> multiTouchController = new MultiTouchController<>(this);

    private PointInfo currTouchPoint = new PointInfo();

    protected boolean mShowDebugInfo = true;

    private static final int UI_MODE_ROTATE = 1;
    private static final int UI_MODE_ANISOTROPIC_SCALE = 2;

    private int mUIMode = UI_MODE_ROTATE;

    protected Context mContext;

    private Resources mRes;

    private Paint mLinePaintTouchPointCircle = new Paint();

    public PhotoSortView(Context context) {
        this(context, null);
    }

    public PhotoSortView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoSortView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Method from this class
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mRes = mContext.getResources();
        mLinePaintTouchPointCircle.setColor(Color.YELLOW);
        mLinePaintTouchPointCircle.setStrokeWidth(5);
        mLinePaintTouchPointCircle.setStyle(Style.STROKE);
        mLinePaintTouchPointCircle.setAntiAlias(true);
        mLinePaintTouchPointCircle.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    public void addImages(int drawable) {
        Img img = new Img(drawable, mRes);
        mImages.add(img);

        // Method from this class
        loadImages();

        /** Method from View Class **/
        invalidate();
    }

    public void removeImages() {
        if (mImages.size() > 0) {
            mImages.remove(mImages.size() - 1);
            /** Method from View Class **/
            invalidate();
        }
    }

    public void clearImages() {
        if (mImages.size() > 0) {
            mImages.clear();
            /** Method from View Class **/
            invalidate();
        }
    }

    /**
     * Called by activity's onResume() method to load the images
     */
    public void loadImages() {
        for (int i = 0; i < mImages.size(); i++) {
            mImages.get(i).load(mRes);
        }
    }

    /**
     * Called by activity's onPause() method to free memory used for loading the
     * images
     */
    public void unloadImages() {
        for (int i = 0; i < mImages.size(); i++) {
            mImages.get(i).unload();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            for (int i = 0; i < mImages.size(); i++) {
                if (mImages.get(i) != null) {
                    mImages.get(i).draw(canvas);
                }
            }

            if (mShowDebugInfo) {
                drawMultitouchDebugMarks(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void trackballClicked() {
        mUIMode = (mUIMode + 1) % 3;
        /** Method from View Class **/
        invalidate();
    }

    private void drawMultitouchDebugMarks(Canvas canvas) {
        if (currTouchPoint.isDown()) {

            float[] xs = currTouchPoint.getXs();
            float[] ys = currTouchPoint.getYs();
            float[] pressures = currTouchPoint.getPressures();
            int numPoints = Math.min(currTouchPoint.getNumTouchPoints(), 2);

            for (int i = 0; i < numPoints; i++) {
                canvas.drawCircle(xs[i], ys[i], 50 + pressures[i] * 80, mLinePaintTouchPointCircle);
            }

            if (numPoints == 2) {
                canvas.drawLine(xs[0], ys[0], xs[1], ys[1], mLinePaintTouchPointCircle);
            }
        }
    }

    /**
     * Pass touch events to the MT controller
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return multiTouchController.onTouchEvent(event);
    }

    /**
     * Get the image that is under the single-touch point, or return null
     * (canceling the drag op) if none
     */
    @Override
    public Img getDraggableObjectAtPoint(PointInfo pt) {

        float x = pt.getX();
        float y = pt.getY();

        for (int i = mImages.size() - 1; i >= 0; i--) {
            Img im = mImages.get(i);
            if (im.containsPoint(x, y)) {
                return im;
            }
        }
        return null;
    }

    /**
     * Select an object for dragging. Called whenever an object is found to be
     * under the point (non-null is returned by getDraggableObjectAtPoint()) and
     * a drag operation is starting. Called with null when drag op ends.
     */
    @Override
    public void selectObject(Img img, PointInfo touchPoint) {
        currTouchPoint.set(touchPoint);
        if (img != null) {
            // Move image to the top of the stack when selected
            mImages.remove(img);
            mImages.add(img);
        }
        // else your want Called with img == null when drag stops.

        /** Method from View Class **/
        invalidate();
    }

    /**
     * Get the current position and scale of the selected image. Called whenever
     * a drag starts or is reset.
     */
    @Override
    public void getPositionAndScale(Img img, PositionAndScale objPosAndScaleOut) {
        // requires averaging the two scale factors)
        objPosAndScaleOut.set(img.getCenterX(),
                img.getCenterY(),
                (mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0,
                (img.getScaleX() + img.getScaleY()) / 2,
                (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0,
                img.getScaleX(),
                img.getScaleY(),
                (mUIMode & UI_MODE_ROTATE) != 0,
                img.getAngle());
    }

    /**
     * Set the position and scale of the dragged/stretched image.
     */
    @Override
    public boolean setPositionAndScale(Img img, PositionAndScale newImgPosAndScale, PointInfo touchPoint) {
        currTouchPoint.set(touchPoint);
        boolean ok = img.setPos(newImgPosAndScale);
        if (ok) {
            /** Method from View Class **/
            invalidate();
        }
        return ok;
    }

    public class Img {

        private static final String TAG = "Img";

        private int resId;

        private Drawable mDrawable;

        private boolean firstLoad;

        private int width;
        private int height;
        private int displayWidth;
        private int displayHeight;

        private float centerX;
        private float centerY;
        private float scaleX;
        private float scaleY;
        private float angle;
        private float scaleFixed;
        private float minX;
        private float maxX;
        private float minY;
        private float maxY;

        private static final float SCREEN_MARGIN = 100;

        private DisplayMetrics metrics;

        public Img(int resId, Resources res) {
            this.resId = resId;
            this.firstLoad = true;
            // Method from this class
            getMetrics(res);
        }

        private void getMetrics(Resources res) {
            metrics = res.getDisplayMetrics();
            this.displayWidth = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math.max(metrics.widthPixels, metrics.heightPixels) : Math.min(metrics.widthPixels, metrics.heightPixels);
            this.displayHeight = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math.min(metrics.widthPixels, metrics.heightPixels) : Math.max(metrics.widthPixels, metrics.heightPixels);
        }

        /**
         * Called by activity's onResume() method to load the images
         */
        public void load(Resources res) {

            try {
                // Method from this class
                getMetrics(res);
                mDrawable = res.getDrawable(resId);
                if (mDrawable != null) {
                    this.width = mDrawable.getIntrinsicWidth();
                    this.height = mDrawable.getIntrinsicHeight();
                    float cx, cy, sx, sy;
                    int widthPixels = metrics.widthPixels;
                    int heightPixels = metrics.heightPixels;

                    float scaleFactor = metrics.density;

                    float widthDp = widthPixels / scaleFactor;
                    float heightDp = heightPixels / scaleFactor;

                    float smallestWidth = Math.min(widthDp, heightDp);
                    scaleFixed = (float) ((0.0016) * smallestWidth);

                    if (firstLoad) {
                        sx = sy = scaleFixed;
                        setPos(150, 150, sx, sy, 0.0f);
                        firstLoad = false;
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        /**
         * Called by activity's onPause() method to free memory used for loading
         * the images
         */
        public void unload() {
            mDrawable = null;
        }

        public boolean setPos(PositionAndScale newImgPosAndScale) {
            return setPos(newImgPosAndScale.getXOff(), newImgPosAndScale.getYOff(),
                    (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale.getScaleX() : newImgPosAndScale.getScale(),
                    (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale.getScaleY() : newImgPosAndScale.getScale(),
                    newImgPosAndScale.getAngle());
        }

        /**
         * Set the position and scale of an image in screen coordinates
         */
        private boolean setPos(float centerX, float centerY, float scaleX, float scaleY, float angle) {
            float ws = (width / 2) * scaleX, hs = (height / 2) * scaleY;
            float newMinX = centerX - ws, newMinY = centerY - hs, newMaxX = centerX + ws, newMaxY = centerY + hs;

            if (newMinX > displayWidth - SCREEN_MARGIN
                    || newMaxX < SCREEN_MARGIN
                    || newMinY > displayHeight - SCREEN_MARGIN
                    || newMaxY < SCREEN_MARGIN) {
                return false;
            }

            this.centerX = centerX;
            this.centerY = centerY;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.angle = angle;
            this.minX = newMinX;
            this.minY = newMinY;
            this.maxX = newMaxX;
            this.maxY = newMaxY;

            return true;
        }

        /**
         * Return whether or not the given screen coords are inside this image
         */
        public boolean containsPoint(float scrX, float scrY) {
            return (scrX >= minX && scrX <= maxX && scrY >= minY && scrY <= maxY);
        }

        public void draw(Canvas canvas) {
            canvas.save();
            try {
                float dx = (maxX + minX) / 2;
                float dy = (maxY + minY) / 2;
                mDrawable.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
                canvas.translate(dx, dy);
                canvas.rotate(angle * 180.0f / (float) Math.PI);
                canvas.translate(-dx, -dy);
                mDrawable.draw(canvas);
                canvas.restore();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Drawable getDrawable() {
            return mDrawable;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public float getCenterX() {
            return centerX;
        }

        public float getCenterY() {
            return centerY;
        }

        public float getScaleX() {
            return scaleX;
        }

        public float getScaleY() {
            return scaleY;
        }

        public float getAngle() {
            return angle;
        }

        public float getMinX() {
            return minX;
        }

        public float getMaxX() {
            return maxX;
        }

        public float getMinY() {
            return minY;
        }

        public float getMaxY() {
            return maxY;
        }
    }
}
