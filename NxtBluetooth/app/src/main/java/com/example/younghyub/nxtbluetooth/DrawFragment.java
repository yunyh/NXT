package com.example.younghyub.nxtbluetooth;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import static android.view.MotionEvent.*;

/**
 * A simple {@link Fragment} subclass.
 */
public class DrawFragment extends Fragment {

    private DrawView mDrawView;
    private View rootView;
    ArrayList<Vertex> vertexArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_draw, container, false);
        mDrawView = new DrawView(rootView.getContext());
        vertexArrayList = new ArrayList<>();
        rootView = mDrawView;
        return rootView;
    }

    public class Vertex{
        float x;
        float y;
        boolean isDrawable;
        public Vertex(float x, float y, boolean isDrawable){
            this.x = x;
            this.y = y;
            this.isDrawable = isDrawable;
        }
    }
    protected class DrawView extends View{
        Paint mPaint;
        public DrawView(Context context) {
            super(context);
            mPaint = new Paint();
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(3);
            mPaint.setAntiAlias(true);// antialiasing
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    vertexArrayList.add(new Vertex(event.getX(), event.getY(), false));
                    break;
                case MotionEvent.ACTION_MOVE:
                    vertexArrayList.add(new Vertex(event.getX(), event.getY(), true));
            }
            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);

            canvas.drawColor(Color.WHITE);
            for(int i = 0; i < vertexArrayList.size(); i++){
                if(vertexArrayList.get(i).isDrawable){
                    canvas.drawLine(vertexArrayList.get(i-1).x, vertexArrayList.get(i-1).y,
                            vertexArrayList.get(i).x, vertexArrayList.get(i).y, mPaint);
                }else{
                    canvas.drawPoint(vertexArrayList.get(i).x, vertexArrayList.get(i).y, mPaint);
                }
            }
        }
    }

}
