package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class CanvasView extends View {

    ArrayList<Shape> pageShapes;
    static int selectedResource;
    static int selectedShape;
    Paint blueOutlinePaint;
    static private float xDown, yDown, offsetX = 0, offsetY = 0, initialHeight = 0, initialWidth = 0;
    static String corner = "";

    static float CORNER_SIZE = 5;
    static float MINIMUM_SIZE = 30;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        pageShapes = null;
        selectedShape = -1;

        init();
    }

    private void init() {
        blueOutlinePaint = new Paint();
        blueOutlinePaint.setColor(Color.BLUE);
        blueOutlinePaint.setStyle(Paint.Style.STROKE);
        blueOutlinePaint.setStrokeWidth(5.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(pageShapes == null) {
            pageShapes = GameEditor.getCurrPage().getShapes();
        }

        for (int i = 0; i < pageShapes.size(); i++) {
            Shape curr = pageShapes.get(i);
            if (i == selectedShape) {

                canvas.drawRect(curr.getX(), curr.getY(), curr.getX() + curr.getWidth(),
                        curr.getY() + curr.getHeight(), blueOutlinePaint);

                // top left corner
                canvas.drawRect(curr.getX() - CORNER_SIZE, curr.getY() - CORNER_SIZE,
                        curr.getX() + CORNER_SIZE, curr.getY() + CORNER_SIZE, blueOutlinePaint);

                // top right corner
                canvas.drawRect(curr.getX() + curr.getWidth() - CORNER_SIZE, curr.getY() - CORNER_SIZE,
                        curr.getX() + curr.getWidth() + CORNER_SIZE, curr.getY() + CORNER_SIZE, blueOutlinePaint);

                // bottom left corner
                canvas.drawRect(curr.getX() - CORNER_SIZE, curr.getY() + curr.getHeight() - CORNER_SIZE,
                        curr.getX() + CORNER_SIZE, curr.getY() + curr.getHeight() + CORNER_SIZE, blueOutlinePaint);

                // bottom right corner
                canvas.drawRect(curr.getX() + curr.getWidth() - CORNER_SIZE, curr.getY() + curr.getHeight() - CORNER_SIZE,
                        curr.getX() + curr.getWidth() + CORNER_SIZE, curr.getY() + curr.getHeight() + CORNER_SIZE, blueOutlinePaint);


            }
            curr.draw(canvas);
        }

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getX();
                yDown = event.getY();
                System.out.println(selectedResource);
                View resource = ((GameEditor)getContext()).findViewById(selectedResource);

                if (resource != null && selectedResource != - 1 && selectedResource != 0) {

                    System.out.println(resource);

                    createShape(resource, xDown, yDown);

                } else {
                    selectShape(xDown, yDown);
                }

                if (selectedShape != -1) {

                    Shape curr = pageShapes.get(selectedShape);

                    offsetX = xDown - curr.getX();
                    offsetY = yDown - curr.getY();

                    initialHeight = curr.getHeight();
                    initialWidth = curr.getWidth();

                    corner = clickCorner(xDown, yDown, curr);

                }

                invalidate();
            case MotionEvent.ACTION_MOVE:

                if (selectedShape != -1) {

                    Shape curr = pageShapes.get(selectedShape);

                    float mouseX = event.getX();
                    float mouseY = event.getY();


                    if (corner.equals("top left corner")) {

                        float newWidth = initialWidth + xDown - mouseX;
                        float newHeight = initialHeight + yDown - mouseY;


                        if (newWidth < MINIMUM_SIZE) {
                            curr.setWidth(MINIMUM_SIZE);
                        } else {
                            curr.setWidth(newWidth);
                            curr.setX(mouseX);
                        }

                        if (newHeight < MINIMUM_SIZE) {
                            curr.setHeight(MINIMUM_SIZE);
                        } else {
                            curr.setHeight(newHeight);
                            curr.setY(mouseY);
                        }

                    } else if (corner.equals("bottom left corner")) {
                        float newWidth = initialWidth + xDown - mouseX;
                        float newHeight = initialHeight + mouseY - yDown;

                        if (newWidth < MINIMUM_SIZE) {
                            curr.setWidth(MINIMUM_SIZE);
                        } else {
                            curr.setWidth(newWidth);
                            curr.setX(mouseX);
                        }

                        if (newHeight < MINIMUM_SIZE) {
                            curr.setHeight(MINIMUM_SIZE);
                        } else {
                            curr.setHeight(newHeight);
                            curr.setY(mouseY - newHeight);
                        }

                    } else if (corner.equals("top right corner")) {
                        float newWidth = initialWidth + mouseX - xDown;
                        float newHeight = initialHeight + yDown - mouseY;

                        if (newWidth < MINIMUM_SIZE) {
                            curr.setWidth(MINIMUM_SIZE);
                        } else {
                            curr.setWidth(newWidth);
                            curr.setX(mouseX - newWidth);
                        }

                        if (newHeight < MINIMUM_SIZE) {
                            curr.setHeight(MINIMUM_SIZE);
                        } else {
                            curr.setHeight(newHeight);
                            curr.setY(mouseY);
                        }

                    } else if (corner.equals("bottom right corner")) {
                        float newWidth = initialWidth + mouseX - xDown;
                        float newHeight = initialHeight + mouseY - yDown;

                        if (newWidth < MINIMUM_SIZE) {
                            curr.setWidth(MINIMUM_SIZE);
                        } else {
                            curr.setWidth(newWidth);
                            curr.setX(mouseX - newWidth);
                        }

                        if (newHeight < MINIMUM_SIZE) {
                            curr.setHeight(MINIMUM_SIZE);
                        } else {
                            curr.setHeight(newHeight);
                            curr.setY(mouseY - newHeight);
                        }

                    } else {
                        curr.setX(mouseX - offsetX);
                        curr.setY(mouseY - offsetY);
                    }

                    invalidate();

                }

        }

        return true;
    }

    String clickCorner(float x, float y, Shape curr) {

        float shapeX = curr.getX();
        float shapeY = curr.getY();
        float shapeWidth = curr.getWidth();
        float shapeHeight = curr.getHeight();

        // left corners
        if ( x >= shapeX - CORNER_SIZE && x <= shapeX + CORNER_SIZE ) {
            // top and bottom corners
            if (y >= shapeY - CORNER_SIZE && y <= shapeY + CORNER_SIZE) {
                return "top left corner";
            } else if (y >= shapeY + shapeHeight - CORNER_SIZE && y <= shapeY + shapeHeight + CORNER_SIZE) {
                return "bottom left corner";
            }
        }

        // right corners
        if ( x >= shapeX + shapeWidth - CORNER_SIZE && x <= shapeX + shapeWidth + CORNER_SIZE ) {
            // top and bottom corners
            if (y >= shapeY - CORNER_SIZE && y <= shapeY + CORNER_SIZE) {
                return "top right corner";
            } else if (y >= shapeY + shapeHeight - CORNER_SIZE && y <= shapeY + shapeHeight + CORNER_SIZE) {
                return "bottom right corner";
            }
        }

        return "not corner";

    }

    static public void setSelectedResource(int resourceId) {
        selectedResource = resourceId;
    }

    static public void setSelectedShape(int shapeId) {
        selectedShape = shapeId;
    }

    private void createShape(View resource, float xDown, float yDown) {
        resource.setBackgroundColor(getResources().getColor(R.color.light_grey));

        Shape newShape = new Shape();
        newShape.setName(getResources().getString(R.string.shape) + (Shape.getAllShapes().size() + 1));

        int width = resource.getWidth();
        newShape.setX(xDown - width/2);

        int height = resource.getHeight();
        newShape.setY(yDown - height/2);

        newShape.setWidth(width);
        newShape.setHeight(height);

        newShape.setPageID(GameEditor.getCurrPage().getPageID());

        String imgName = resource.getTag().toString();

        if (imgName.equals("texticon")) {
            newShape.setShapeText(getResources().getString(R.string.default_text_message));
            newShape.setHeight(newShape.getShapeText().getFontSize());
            // TO DO: get actual width
            newShape.setWidth(newShape.getText().length() * newShape.getShapeText().getFontSize() / 2);
            newShape.setX(xDown - newShape.getWidth() / 2);
            newShape.setY(yDown - newShape.getHeight() / 2);
        }

        newShape.setImgName(resource.getTag().toString());

        if (imgName.equals("greybox")) {
            newShape.setImgName("");
        }

        pageShapes.add(newShape);

        selectedShape = pageShapes.size() - 1;

        GameEditor.setSelectedShaped(selectedShape);

        TextView objName = ((GameEditor)getContext()).findViewById(R.id.obj_name);
        objName.setText(pageShapes.get(selectedShape).getName());

        LinearLayout objProperties = ((GameEditor)getContext()).findViewById(R.id.obj_properties);
        objProperties.setVisibility(this.VISIBLE);

        TextView clickObj = ((GameEditor)getContext()).findViewById(R.id.click_obj);
        clickObj.setVisibility(this.GONE);

        selectedResource = -1;

    }

    public void selectShape(float xDown, float yDown) {

        for (int i = pageShapes.size() - 1; i >= 0 ; i--) {
            Shape curr = pageShapes.get(i);

            float left = curr.getX();
            float right = left + curr.getWidth();
            float top = curr.getY();
            float bottom = top + curr.getHeight();


            if (xDown >= left - CORNER_SIZE && xDown <= right + CORNER_SIZE && yDown >= top - CORNER_SIZE && yDown <= bottom + CORNER_SIZE) {
                selectedShape = i;

                System.out.println(curr.getImgName());
                System.out.println(curr.getName());
                GameEditor.setSelectedShaped(i);
                TextView objName = ((GameEditor)getContext()).findViewById(R.id.obj_name);
                objName.setText(curr.getName());

                LinearLayout objProperties = ((GameEditor)getContext()).findViewById(R.id.obj_properties);
                objProperties.setVisibility(this.VISIBLE);

                TextView clickObj = ((GameEditor)getContext()).findViewById(R.id.click_obj);
                clickObj.setVisibility(this.GONE);
                return;
            }
        }

        LinearLayout objProperties = ((GameEditor)getContext()).findViewById(R.id.obj_properties);
        objProperties.setVisibility(this.GONE);

        TextView clickObj = ((GameEditor)getContext()).findViewById(R.id.click_obj);
        clickObj.setVisibility(this.VISIBLE);


        selectedShape = -1;

    }



    public void copyShape(View view) {

    }

    public void cutShape(View view) {

    }

    public void pasteShape(View view) {

    }


}
