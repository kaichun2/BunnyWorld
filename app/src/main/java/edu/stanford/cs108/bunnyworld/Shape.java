package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Shape {

    private String name;
    private String imgName;
    private int xLoc, yLoc;
    private int pageID;
    private ShapeText text;
    private boolean isVisible;
    private boolean isMovable;

    // also need something for script text!! TODO



    public Shape(String name, String imgName, int xLoc, int yLoc, int pageID, ShapeText text,
                 boolean isVisible, boolean isMovable) {
        this.name = name;
        this.imgName = imgName;
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.pageID = pageID;
        this.text = text;
        this.isVisible = isVisible;
        this.isMovable = isMovable;
    }

    /* Default values. Do this if using setter functions. */
    public Shape() {
        this("", "", -1, -1, -1, null, true, true);
    }


    /*
    * onDraw function. Given a canvas, will draw
    * itself on that canvas.
    */
    public void onDraw(Canvas canvas) { // TODO: verify that this is how we want this to work

        // TODO: how to differentiate between isMovable and isVisible when calling onDraw?



        /* Also have to redraw ShapeText when you draw/redraw/move Shape. */
        text.onDraw(canvas);
    }



    /* Getters and setters. */

    public String getName() { return name; }

    public String getImgName() { return imgName; }

    public int getPageID() { return pageID; }

    public int getX() { return xLoc; }

    public int getY() { return yLoc; }

    public boolean isVisible() { return isVisible; }

    public boolean isMovable() { return isMovable; }

    public String getText() { return text.getText(); }

    public void setName(String name) { this.name = name; }

    public void setImgName(String name) { imgName = name; }

    public void setPageID(int id) { pageID = id; }

    public void setXLoc(int x) { xLoc = x; }

    public void setYLoc(int y) { yLoc = y; }

    public void setShapeText(ShapeText shapetext) { text = shapetext; }

    public void setMovable(boolean val) { isMovable = val; }

    public void setVisible(boolean val) { isVisible = val; }


    /* ShapeText Inner class. */
    public class ShapeText { // TODO: verify onDraw works, extra: maybe ivar for text color?
        private int x, y;
        private int fontSize; // sp
        private String text;

        public void onDraw(Canvas canvas) {
            Paint paint = new Paint();
            canvas.drawPaint(paint);
            paint.setTextSize(fontSize);
            paint.setColor(Color.BLACK);
            canvas.drawText(text, x, y, paint);
        }

        /* Getters and setters. */

        public int getX() { return x; }

        public int getY() { return y; }

        public int getFontSize() { return fontSize; }

        public String getText() { return text; }

        public void setX(int x) { this.x = x; }

        public void setY(int y) { this.y = y; }

        public void setFontSize(int fontSize) { this.fontSize = fontSize; }

        public void setText(String text) { this.text = text; }

    }
}
