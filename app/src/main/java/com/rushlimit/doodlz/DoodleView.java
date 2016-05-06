package com.rushlimit.doodlz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.provider.MediaStore;
import android.support.v4.print.PrintHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aminm on 5/4/16.
 */
public class DoodleView extends View {
    //Used to determine whether user moved a finger enough to draw again
    private static final float TOUCH_TOLERANCE = 10;

    private Bitmap bitmap; // Drawing area for displaying or saving
    private Canvas bitmapCanvas; // Used to draw on the bitmap
    private final Paint paintScreen; // Used to draw bitmap onto screen
    private final Paint paintLine; // Used to draw lines onto bitmap

    // Maps of the current Paths being drawn and Points in those Paths
    private final Map<Integer, Path> pathMap = new HashMap<>();
    private final Map<Integer, Point> previousPointMap = new HashMap<>();

    // This constructor is called when inflating view from an XML file
    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintScreen = new Paint();

        // Set the initial display settings for the painted line
        paintLine = new Paint();
        paintLine.setAntiAlias(true); // Smooth edges of drawn line
        paintLine.setColor(Color.BLACK); // Default color is black
        paintLine.setStyle(Paint.Style.STROKE); // Solid line
        paintLine.setStrokeWidth(5); // Set the default line width
        paintLine.setStrokeCap(Paint.Cap.ROUND); // Rounded line ends
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the background screen
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        for (Integer key : pathMap.keySet()) {
            canvas.drawPath(pathMap.get(key), paintLine);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked(); // Event type
        int actionIndex = event.getActionIndex(); // Pointer (i.e., finger)

        // Determine whether touch started, ended or is moving
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex));
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchMoved(event);
        }

        invalidate();

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE); // Erase the Bitmap with white
    }

    // Called when the user touches the screen
    private void touchStarted(float x, float y, int lineID) {
        Path path; // Used to store the path for the given touch id
        Point point; // Used to store the last point in path

        // If there is already a path for lineID
        if (pathMap.containsKey(lineID)) {
            path = pathMap.get(lineID); // Get the Path
            path.reset();
            point = previousPointMap.get(lineID); // Get Path's last point
        } else {
            path = new Path();
            pathMap.put(lineID, path); // Add the Path to Map
            point = new Point();
            previousPointMap.put(lineID, point); // Add the Point to the Map
        }

        // Move to the coordinates of the touch
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }

    // Called when the user finishes a touch
    private void touchEnded(int lineID) {
        Path path = pathMap.get(lineID); // Get the corresponding Path
        bitmapCanvas.drawPath(path, paintLine); // Draw to bitmapCanvas
        path.reset(); // Reset the path
    }

    // Called when the user drags along the screen
    private void touchMoved(MotionEvent event) {
        int pointerID;
        int pointerIndex;
        float newX, deltaX;
        float newY, deltaY;
        Path path;
        Point point;
        for (int i = 0; i < event.getPointerCount(); i++) {
            pointerID = event.getPointerId(i);
            pointerIndex = event.findPointerIndex(pointerID);

            // If there is a path associated with the pointer
            if (pathMap.containsKey(pointerID)) {
                // Get the new coordinates for the pointer
                newX = event.getX(pointerIndex);
                newY = event.getY(pointerIndex);

                // Get the path and previous point associated with this pointer
                path = pathMap.get(pointerID);
                point = previousPointMap.get(pointerID);

                // Calculate how far the user moved from the last update
                deltaX = Math.abs(newX - point.x);
                deltaY = Math.abs(newY - point.y);

                // If the distance is significant enough to matter
                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    // Move the path to the new location
                    path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);

                    // Store the new coordinates
                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    public void saveImage() {
        final String name = getContext().getResources().getString(R.string.app_name)
                + System.currentTimeMillis() + ".jpg";

        // Insert the image on the device
        String location = MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(), bitmap, name, "Doodlz Drawing");

        if (location != null) {
            showToastWithStringId(R.string.message_saved);
        } else {
            showToastWithStringId(R.string.message_error_saving);
        }
    }

    public void printImage() {
        if (PrintHelper.systemSupportsPrint()) {
            // Use Android Support Library's PrintHelper to print image
            PrintHelper printHelper = new PrintHelper(getContext());

            // Fit image in page bounds and print the image
            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            printHelper.printBitmap("Doodlz Image", bitmap);
        } else {
            showToastWithStringId(R.string.message_error_printing);
        }
    }

    public void clear() {
        pathMap.clear();
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    public int getDrawingColor() {
        return paintLine.getColor();
    }

    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    private void showToastWithStringId(int id) {
        Toast message = Toast.makeText(getContext(), id, Toast.LENGTH_SHORT);
        message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
        message.show();
    }
}
