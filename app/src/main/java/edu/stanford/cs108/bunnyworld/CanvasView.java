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
    static int numShapes;
    int selectedShape;
    Paint blueOutlinePaint;
    static private float xDown, yDown, offsetX = 0, offsetY = 0;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        pageShapes = null;
        numShapes = 1;
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
                ImageView resource = ((GameEditor)getContext()).findViewById(selectedResource);

                if (resource != null && selectedResource != - 1) {

                    createShape(resource, xDown, yDown);

                } else {
                    selectShape(xDown, yDown);
                }

                if (selectedShape != -1) {

                    Shape curr = pageShapes.get(selectedShape);

                    offsetX = xDown - curr.getX();
                    offsetY = yDown - curr.getY();

                }

                invalidate();
            case MotionEvent.ACTION_MOVE:

                if (selectedShape != -1) {

                    Shape curr = pageShapes.get(selectedShape);

                    float mouseX = event.getX();
                    float mouseY = event.getY();

                    curr.setX(mouseX - offsetX);
                    curr.setY(mouseY - offsetY);

                    invalidate();

                }

        }

        return true;
    }

    static public void setSelectedResource(int resourceId) {
        selectedResource = resourceId;
    }

    private void createShape(ImageView resource, float xDown, float yDown) {
        resource.setBackgroundColor(getResources().getColor(R.color.light_grey));

        Shape newShape = new Shape();
        newShape.setName(getResources().getString(R.string.shape) + numShapes);
        numShapes++;

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


            if (xDown >= left && xDown <= right && yDown >= top && yDown <= bottom) {
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


}
