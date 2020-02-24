package org.pragyan.dalal18.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import org.pragyan.dalal18.R
import org.pragyan.dalal18.utils.EndTutorialInterface

class DeleteTutorialView(context: Context, end: EndTutorialInterface) : View(context) {

    var paint: Paint = Paint()
    lateinit var bitmap: Bitmap
    lateinit var scaledBitmap: Bitmap
    var xLocation = 0
    var end: EndTutorialInterface
    var paintText: Paint = Paint()
    var flag = 0
    var isMoving = true

    init {
        paint.color = Color.parseColor("#80161616")
        paintText.color = Color.parseColor("#000000")
        paintText.textSize = 60f
        this.end = end
        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.swipefinger)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        scaledBitmap = Bitmap.createScaledBitmap(bitmap,width/4,height/4,false)

        canvas.drawRect(0f,0f, width.toFloat(), height.toFloat(),paint)
        canvas.drawBitmap(scaledBitmap,width-40f-xLocation,70f,null)
        canvas.drawText("SWIPE LEFT TO DELETE ORDER",80f,height-220f,paintText)

        if (isMoving)
            xLocation += 25

        postInvalidate()

        if (xLocation >= width - 40f) {
            if (flag == 1)
                end.removeTutorial()
            else {
                flag = 1
                xLocation = 0
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event != null) {
            if (event.action == MotionEvent.ACTION_MOVE)
                isMoving = false
            else if (event.action == MotionEvent.ACTION_DOWN)
                isMoving = false
            else if (event.action == MotionEvent.ACTION_UP)
                isMoving = true
        }

        return true
    }
}