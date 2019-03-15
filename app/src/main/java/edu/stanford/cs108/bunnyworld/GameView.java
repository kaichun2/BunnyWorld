package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GameView extends View {

    private int currentPageIndex;
    private boolean openNewPage;
    private float yBoundary;
    private Shape selectedShape;
    private int bottomShapeIndex;

    private Paint blackPaint;
    private Paint greenOutlinePaint;
    private Paint redOutlinePaint;

    // for drag and dropping
    private static float initX = 0, initY = 0, offsetX = 0, offsetY = 0;

    private static final float JUMP_OFFSET = 12; // when transitioning between game and possessions

    // when reset is true, will load the default properties
    // since this signifies the user having clicked the
    // restart button in GameActivity
    public static boolean reset = false;

    // getting instance of gameactivity class
    // necessary if we want to update pagename in toolbar
    // https://stackoverflow.com/questions/9723106/get-activity-instance
    private static WeakReference<Activity> gameActivityInstance = null;
    public static void updateActivity(Activity activity) {
        gameActivityInstance = new WeakReference<>(activity);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaultProperties();
        init();
    }

    private void setDefaultProperties() {
        currentPageIndex = 0;
        openNewPage = true;
        bottomShapeIndex = -1;
        selectedShape = null;
    }

    private void init() {
        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        blackPaint.setStrokeWidth(5.0f);

        greenOutlinePaint = new Paint();
        greenOutlinePaint.setColor(Color.GREEN);
        greenOutlinePaint.setStyle(Paint.Style.STROKE);
        greenOutlinePaint.setStrokeWidth(5.0f);

        redOutlinePaint = new Paint();
        redOutlinePaint.setColor(Color.RED);
        redOutlinePaint.setStyle(Paint.Style.STROKE);
        redOutlinePaint.setStrokeWidth(5.0f);
    }

    private void drawBoundaryLine(Canvas canvas) {
        yBoundary = (0.75f)*getHeight();
        canvas.drawLine(0, yBoundary, getWidth(), yBoundary, blackPaint);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (reset) {
            reset = false;
            setDefaultProperties();
        }

        Page currPage = GameActivity.pages.get(currentPageIndex);

        // draw on the background
        String backgroundImage = currPage.getBackgroundImage();
        if (!backgroundImage.equals("")) {
            int imageFile = currPage.getImage(backgroundImage);
            if (imageFile != -1) {
                Drawable draw = getResources().getDrawable(imageFile);
                draw.setBounds(0, 0, getWidth(), getHeight());
                draw.draw(canvas);
            }
        }

        drawBoundaryLine(canvas); // draw the boundary line for possessions always

        // handle logic for opening a new page (calling onEnter scripts)
        Page newPage = null;
        if (openNewPage) {
            // update toolbar
            if (gameActivityInstance != null) {
                String currPageName = currPage.getPageName();
                // Action Bar can't be used in static contexts. So we need to keep a weak reference
                // to the game activity and update the action bar through the reference like so (sad)
                android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) ((GameActivity)gameActivityInstance.get()).findViewById(R.id.game_activity_toolbar);
                ((GameActivity) gameActivityInstance.get()).setSupportActionBar(toolbar);
                ((GameActivity) gameActivityInstance.get()).getSupportActionBar().setTitle(currPageName);
            }

            // call on Enter's for each shape
            for (Shape shape : currPage.getShapes()) {
                Page tempPage = shape.onEnter(getContext());
                if (tempPage != null && newPage == null) newPage = tempPage;
            }
            openNewPage = newPage != null; // won't open another page if the onEnter's didn't define so

        }

        // draw all the shapes regardless of whether we are opening a new page
        ArrayList<Shape> shapes = currPage.getShapes();
        Shape bottomShape = null;
        if (bottomShapeIndex >= 0) {
            bottomShape = shapes.get(bottomShapeIndex);
            bottomShapeIndex = -1;
        }
        for (int i = 0; i < shapes.size(); i++) {
            Shape currShape = shapes.get(i);
            if (bottomShape != null) { // determine whether we need a box, depends on valid/invalid on drop command
                if (bottomShape.getCommands().containsKey("on drop " + currShape.getName())) {
                    canvas.drawRect(bottomShape.getX(), bottomShape.getY(), bottomShape.getX() + bottomShape.getWidth(),
                            bottomShape.getY() + bottomShape.getHeight(), greenOutlinePaint);
                } else {
                    canvas.drawRect(bottomShape.getX(), bottomShape.getY(), bottomShape.getX() + bottomShape.getWidth(),
                            bottomShape.getY() + bottomShape.getHeight(), redOutlinePaint);
                }
            }
            if (selectedShape != currShape) currShape.draw(canvas);
        }

        // always draw possessions in
        ArrayList<Shape> possessions = GameActivity.possessions;
        for (Shape shape : possessions) {
            // we are allowing possessions to be drawn on top of shapes in page
            if (bottomShape != null) { // determine whether we need a box, depends on valid/invalid on drop command
                if (bottomShape.getCommands().containsKey("on drop " + shape.getName())) {
                    canvas.drawRect(bottomShape.getX(), bottomShape.getY(), bottomShape.getX() + bottomShape.getWidth(),
                            bottomShape.getY() + bottomShape.getHeight(), greenOutlinePaint);
                } else {
                    canvas.drawRect(bottomShape.getX(), bottomShape.getY(), bottomShape.getX() + bottomShape.getWidth(),
                            bottomShape.getY() + bottomShape.getHeight(), redOutlinePaint);
                }
            }
            if (shape != selectedShape) shape.draw(canvas);
        }

        if (selectedShape != null) selectedShape.draw(canvas); // selected shape is drawn last since it's on top!
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Page currPage = GameActivity.pages.get(currentPageIndex);
        Page newPage = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // set selected shape, search depending on whether on page or in possessions
                if (event.getY() > yBoundary) {
                    selectedShape = getPossessionsShape(event.getX(), event.getY());
                } else {
                    selectedShape = currPage.getShape(event.getX(), event.getY(), 1); // remains null if no shape
                }

                // first, do any on clicks if there's a shape here, handle script's change page possibility
                newPage = currPage.onMousePageEvent(getContext(), event.getX(), event.getY(), "on click");
                openNewPage = newPage != null; // case for snap back falls under
                if (newPage != null) {
                    currentPageIndex = newPage.getPageID() - 1; // pageID's are 1-indexed

                    // since we're changing pages, we have to de-select our current shape,
                    // otherwise, due to how on-draw is written, we will accidentally draw selected in next page
                    selectedShape = null;
                }

                if (selectedShape != null && selectedShape.isMovable()) {
                    // get offset for dragging
                    offsetX = event.getX() - selectedShape.getX();
                    offsetY = event.getY() - selectedShape.getY();

                    // store initial position in case we need to snap back for invalid drop
                    initX = selectedShape.getX();
                    initY = selectedShape.getY();
                }


                // handles green/red borders when hovering
                handleHovering(currPage, event.getX(), event.getY());

                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                // drag
                if (selectedShape != null && selectedShape.isMovable()) {
                    float mouseX = event.getX();
                    float mouseY = event.getY();

                    // update drawn image based on where user clicked initially
                    selectedShape.setX(mouseX - offsetX);
                    selectedShape.setY(mouseY - offsetY);
                }

                // logic for highlighting if we're above another shape and dragging
                handleHovering(currPage, event.getX(), event.getY());

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                // first, do any on-drops if there's a shape here
                // If a shape is dropped onto a shape that does not have a matching on-drop clause,
                // the dropped shape should "snap back" to where it started— either on the page or
                // in the possession area.

                if (selectedShape != null) {
                    if (currPage.getShapes().contains(selectedShape)) {
                        newPage = currPage.onMousePageEvent(getContext(), event.getX(), event.getY(), "on drop");
                    } else {
                        newPage = possessionDropOnPageShape(currPage, event.getX(), event.getY());
                    }

                    if (newPage != null) {
                        if (newPage.getPageID() < 0) {
                            selectedShape.setX(initX);
                            selectedShape.setY(initY);
                        } else {
                            openNewPage = true;
                            currentPageIndex = newPage.getPageID() - 1; // pageID's are 1-indexed
                        }
                    } else {
                        openNewPage = false;
                    }
                } else {
                    openNewPage = false; // can't open a new page if nothing was selected
                }


                // handle transitioning between page view and possessions inventory
                if (selectedShape != null && selectedShape.isMovable()) {
                    // if at least half of shape body is above boundary, we will consider it to
                    // be in the page's shapes, and we'll snap it upwards if necessary so that
                    // it doesn't overlap with possessions. vice versa for possessions.
                    if (selectedShape.getY() + (selectedShape.getHeight()/2)  <= yBoundary) {
                        makeSureShapeIsInPage(currPage);
                    } else {
                        makeSureShapeIsInPossessions(currPage);
                    }
                }

                selectedShape = null;

                invalidate();
                break;
        }

        return true;
    }

    private void makeSureShapeIsInPage(Page currPage) {
        // snap into place if necessary
        if (selectedShape.getY() + selectedShape.getHeight() >= yBoundary) {
            selectedShape.setY(yBoundary - selectedShape.getHeight() - JUMP_OFFSET);
        }


        // the rest of this code makes sure the selected shape is in the correct array
        ArrayList<Shape> shapes = currPage.getShapes();

        // is selected shape in page shapes? if so, return
        if (shapes.contains(selectedShape)) return;

        // if not, add to shapes array and remove from possessions
        shapes.add(selectedShape);
        GameActivity.possessions.remove(selectedShape);
    }

    private void makeSureShapeIsInPossessions(Page currPage) {
        // snap into place if necessary
        if (selectedShape.getY() <= yBoundary) { // if top of shape is in page
            selectedShape.setY(yBoundary + JUMP_OFFSET);
        }

        // the rest of this code makes sure the selected shape is in the correct array
        ArrayList<Shape> possessions = GameActivity.possessions;

        // is selected shape in possessions? if so, return
        if (possessions.contains(selectedShape)) return;

        // if not, add to possessions and remove from shapes
        possessions.add(selectedShape);
        currPage.getShapes().remove(selectedShape);
    }


    private void handleHovering(Page currPage, float x, float y) {
        // slightly different depending on whether the selected shape
        // is in the page's shapes or is a possession
        // note that we aren't allowing the client to drag possessions on each other within
        // the possessions inventory
        if (selectedShape != null) {
            Shape bottomShape;
            if (currPage.getShapes().contains(selectedShape)) { // both are shapes
                bottomShape = currPage.getShape(x, y, 2);
            } else {
                // selected shape is a possession
                // there's only one shape in page class in that location, other is a possession
                bottomShape = currPage.getShape(x, y, 1);
            }

            if (bottomShape != null) {
                bottomShapeIndex = getShapeIndex(currPage, bottomShape);
            } else {
                bottomShapeIndex = -1;
            }
        }
    }

    private int getShapeIndex(Page currPage, Shape bottomShape) {
        return currPage.getShapes().indexOf(bottomShape);
    }

    private Shape getPossessionsShape(float x, float y) {
        Shape shapeInPossessions = null;
        ArrayList<Shape> possessions = GameActivity.possessions;

        // items on top are near end of possessions array
        for (int i = possessions.size() -1; i >= 0; i--) {
            Shape shape = possessions.get(i);
            if (shape.isVisible()) {
                if ((shape.getX() <= x && x <= shape.getX() + shape.getWidth()) && // x is contained in shape
                        (shape.getY() <= y && y <= shape.getY() + shape.getHeight())) { // y is also contained in shape
                    shapeInPossessions = shape;
                    break;
                }
            }
        }

        return shapeInPossessions;
    }

    private Page possessionDropOnPageShape(Page currPage, float x, float y) {
        Page newPage;
        Shape topShape = selectedShape;
        if (topShape == null) return null; // no shape, no action

        Shape secondTopShape = currPage.getShape(x, y, 1); // only one page shape at x, y (other is possession)
        if (secondTopShape == null) return null; // don't do anything if there wasn't a second top shape!!!

        // if there isn't an "on drop topShape" for secondTopShape, then we'll return
        // a new, empty page with pageID = -1 to denote the "snap back to place" case in gameeditor
        if (!secondTopShape.getCommands().containsKey("on drop " + topShape.getName())) {
            return new Page();
        }

        newPage = secondTopShape.onDrop(getContext(), topShape); // execute "on drop topShape" script for secondTopShape
        return newPage;
    }

}
