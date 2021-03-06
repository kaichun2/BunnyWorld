package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static edu.stanford.cs108.bunnyworld.GameEditor.undoShapeStack;


/**
 * Canvas View is the canvas representing the current page within GameEditor.
 * This manages all of the associated logic for drawing the shapes and any
 * changes made to them. Supports drag and drop between resources area and
 * canvas, drag and drop within Canvas View itself, resizing shapes by dragging
 * the corner, higlighting shapes appropriately when selected and all the necessary
 * communication between canvas view and game editor for updating the shape dynamically
 * with changes.
 */
public class CanvasView extends View {

    ArrayList<Shape> pageShapes;
    static int selectedResource;
    Paint blueOutlinePaint;
    static private float xDown, yDown, offsetX = 0, offsetY = 0, initialHeight = 0, initialWidth = 0;
    static String corner = "";

    static float CORNER_SIZE = 5;
    static float MINIMUM_SIZE = 30;

    static float RESOURCE_BOUNDARY = 0;
    static float RESOURCE_OFFSET = 30;
    static int actionBarHeight;

    static int windowWidth = 0, windowHeight = 0;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        pageShapes = null;
        GameEditor.selectedShape = -1;

        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);

        init();
    }

    private void init() {
        blueOutlinePaint = new Paint();
        blueOutlinePaint.setColor(getResources().getColor(R.color.alternate_blue));
        blueOutlinePaint.setStyle(Paint.Style.STROKE);
        blueOutlinePaint.setStrokeWidth(5.0f);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        addBackground(canvas);

        if (pageShapes == null) {
            pageShapes = GameEditor.getCurrPage().getShapes();
        }

        if (RESOURCE_BOUNDARY == 0 && windowHeight != 0) {


            RESOURCE_BOUNDARY = windowHeight * 3.0f / 4.0f - actionBarHeight - RESOURCE_OFFSET;
            System.out.println("boundary " + RESOURCE_BOUNDARY);
        }

        for (int i = 0; i < pageShapes.size(); i++) {
            Shape curr = pageShapes.get(i);
            if (i == GameEditor.selectedShape) {

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
                View resource = ((GameEditor) getContext()).findViewById(selectedResource);

                if (resource != null && selectedResource != -1 && selectedResource != 0) {

                    System.out.println(resource);

                    createShape(resource, xDown, yDown);

                } else {
                    selectShape(xDown, yDown);
                }

                if (GameEditor.selectedShape != -1) {

                    Shape curr = pageShapes.get(GameEditor.selectedShape);

                    // undo support
                    GameEditor.ShapeEvent shapeEvent = new GameEditor.ShapeEvent(GameEditor.MISC_SHAPE_CONFIG, (Shape) curr.clone());
                    undoShapeStack.push(shapeEvent);

                    offsetX = xDown - curr.getX();
                    offsetY = yDown - curr.getY();

                    initialHeight = curr.getHeight();
                    initialWidth = curr.getWidth();

                    corner = clickCorner(xDown, yDown, curr);

                }

                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:

                if (GameEditor.selectedShape != -1) {
                    Shape curr = pageShapes.get(GameEditor.selectedShape);

                    float mouseX = event.getX();
                    float mouseY = event.getY();


                    if (corner.equals("top left corner") && !curr.getImgName().equals("texticon")) {

                        float newWidth = initialWidth + xDown - mouseX;
                        float newHeight = initialHeight + yDown - mouseY;

                        if (mouseX > 0 && mouseY > 0) {
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
                        }

                    } else if (corner.equals("bottom left corner") && !curr.getImgName().equals("texticon")) {
                        float newWidth = initialWidth + xDown - mouseX;
                        float newHeight = initialHeight + mouseY - yDown;

                        if (mouseX > 0 && mouseY < RESOURCE_BOUNDARY) {
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
                        }

                    } else if (corner.equals("top right corner") && !curr.getImgName().equals("texticon")) {
                        float newWidth = initialWidth + mouseX - xDown;
                        float newHeight = initialHeight + yDown - mouseY;

                        if (mouseX < windowWidth && mouseY > 0) {
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
                        }

                    } else if (corner.equals("bottom right corner") && !curr.getImgName().equals("texticon")) {
                        float newWidth = initialWidth + mouseX - xDown;
                        float newHeight = initialHeight + mouseY - yDown;

                        if (mouseX < windowWidth && mouseY < RESOURCE_BOUNDARY) {
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
                        }

                    } else {
                        float newX = mouseX - offsetX;
                        float newY = mouseY - offsetY;

                        float currWidth = curr.getWidth();
                        float currHeight = curr.getHeight();

                        if (newX >= 0 && (newX + currWidth) <= windowWidth) {
                            curr.setX(mouseX - offsetX);
                        }

                        if (newY >= 0 && (newY + currHeight) <= RESOURCE_BOUNDARY) {
                            curr.setY(mouseY - offsetY);
                        }

                    }

                    invalidate();
                    break;

                }

            case MotionEvent.ACTION_UP:
                // if the action we added didn't change any properties of the shape
                // we can remove it from undo stack
                // in all the cases where this matters, the shape we are looking at is selected
                if (GameEditor.selectedShape != -1) {
                    Shape curr = pageShapes.get(GameEditor.selectedShape);
                    if (!undoShapeStack.isEmpty()) {
                        GameEditor.ShapeEvent lastEvent = undoShapeStack.peek();
                        Shape affectedShape = lastEvent.getShape();
                        if (lastEvent.getType() == GameEditor.MISC_SHAPE_CONFIG
                                && affectedShape.getWidth() == curr.getWidth()
                                && affectedShape.getHeight() == curr.getHeight()
                                && affectedShape.getX() == curr.getX()
                                && affectedShape.getY() == curr.getY()) {
                            undoShapeStack.pop();
                        }
                    }
                }

                Log.d("what", "STACK STATE:" + undoShapeStack.size());
                for (GameEditor.ShapeEvent ev : undoShapeStack) {
                    Log.d("what", ev.toString());
                }
                break;
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
        GameEditor.selectedShape = shapeId;
    }

    static public void setWindowHeight(int height) {
        windowHeight = height;
    }

    static public void setWindowWidth(int width) {
        windowWidth = width;
    }

    private void createShape(View resource, float xDown, float yDown) {
        resource.setBackgroundColor(getResources().getColor(R.color.light_grey));

        Shape newShape = new Shape();
        newShape.setName(getResources().getString(R.string.shape) + (Shape.getAllShapes().size() + 1));

        int width = resource.getWidth();
        int height = resource.getHeight();

        float newX = xDown - width/2;
        float newY = yDown - height/2;


        if (newX >= 0 && newX + width <= windowWidth) {
            newShape.setX(newX);
        } else {
            if (newX < 0) {
                newShape.setX(0);
            } else {
                newShape.setX(windowWidth - width);
            }

        }

        if (newY >= 0 && newY + height <= RESOURCE_BOUNDARY) {
            newShape.setY(newY);
        } else {
            if (newY < 0) {
                newShape.setY(0);
            } else {
                newShape.setY(RESOURCE_BOUNDARY - height);
            }
        }



        newShape.setWidth(width);
        newShape.setHeight(height);

        newShape.setPageID(GameEditor.getCurrPage().getPageID());

        String imgName = resource.getTag().toString();

        if (imgName.equals("texticon")) {
            newShape.setShapeText(getResources().getString(R.string.default_text_message));
            newShape.setHeight(newShape.getShapeText().getFontSize());
            newShape.setWidth(newShape.getText().length() * newShape.getShapeText().getFontSize() / 2);

            float shapeWidth = newShape.getWidth();
            float shapeHeight = newShape.getHeight();

            float shapeX = xDown - newShape.getWidth() / 2;
            float shapeY = yDown - newShape.getHeight() / 2;

            if (shapeX >= 0 && shapeX + shapeWidth <= windowWidth) {
                newShape.setX(shapeX);
            } else {
                if (newX < 0) {
                    newShape.setX(0);
                } else {
                    newShape.setX(windowWidth - shapeWidth);
                }

            }

            if (shapeY >= 0 && shapeY + shapeHeight <= RESOURCE_BOUNDARY) {
                newShape.setY(shapeY);
            } else {
                if (newY < 0) {
                    newShape.setY(0);
                } else {
                    newShape.setY(RESOURCE_BOUNDARY - shapeHeight);
                }
            }
        }

        newShape.setImgName(resource.getTag().toString());

        if (imgName.equals("greybox")) {
            newShape.setImgName("");
        }

        // undo support
        GameEditor.ShapeEvent event = new GameEditor.ShapeEvent(GameEditor.ADD_SHAPE, newShape);
        undoShapeStack.push(event);

        pageShapes.add(newShape);

        GameEditor.selectedShape = pageShapes.size() - 1;

        GameEditor.setSelectedShaped(GameEditor.selectedShape);

        TextView objName = ((GameEditor)getContext()).findViewById(R.id.obj_name);
        objName.setText(pageShapes.get(GameEditor.selectedShape).getName());

        LinearLayout objProperties = ((GameEditor)getContext()).findViewById(R.id.obj_properties);
        objProperties.setVisibility(this.VISIBLE);

        TextView clickObj = ((GameEditor)getContext()).findViewById(R.id.click_obj);
        clickObj.setVisibility(this.GONE);

        selectedResource = -1;

    }

    public void selectShape(float xDown, float yDown) {
        GameEditor.selectedShape = -1;

        for (int i = pageShapes.size() - 1; i >= 0 ; i--) {
            Shape curr = pageShapes.get(i);

            float left = curr.getX();
            float right = left + curr.getWidth();
            float top = curr.getY();
            float bottom = top + curr.getHeight();


            if (xDown >= left - CORNER_SIZE && xDown <= right + CORNER_SIZE && yDown >= top - CORNER_SIZE && yDown <= bottom + CORNER_SIZE) {
                GameEditor.selectedShape = i;

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
    }

    private void addBackground(Canvas canvas) {
        // the page stores the background image in a string
        String backgroundImage = GameEditor.getCurrPage().getBackgroundImage();
        if (backgroundImage.equals("")) return;
        //System.out.println("Setting background: " + backgroundImage);
        int imageFile = GameEditor.getCurrPage().getImage(backgroundImage);
        if (imageFile != -1) {
            //System.out.println("image file: " + imageFile);
            Drawable draw = getResources().getDrawable(imageFile);
            draw.setBounds(0, 0, getWidth(), getHeight());
            draw.draw(canvas);
        }
    }
}
