package org.pragyan.dalal18.utils


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.OrdersRecyclerAdapter

class swipeToDeleteCallBack (ct : Context?,adapter: OrdersRecyclerAdapter? ,recycler : RecyclerView){
    private var context :Context?=null
    private var orderRecyclerAdapter :OrdersRecyclerAdapter?=null
    private var mRecycler :RecyclerView?=null

    init {
        this.context=ct
        this.orderRecyclerAdapter=adapter
        this.mRecycler=recycler
    }

    val itemTouchHelperCallback =object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT  or ItemTouchHelper.RIGHT){

        val mBackground : ColorDrawable?=null
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            val itemView =viewHolder.itemView
            val itemHeight =itemView.height
            val deleteDrawable =context!!.resources.getDrawable(R.drawable.delete_order)
            val intrinsicWidth = deleteDrawable.intrinsicWidth
            val intrinsicHeight =deleteDrawable.intrinsicHeight
            var  mClearPaint  = Paint()
            mClearPaint.setXfermode( PorterDuffXfermode(PorterDuff.Mode.CLEAR))

            val isCancelled= dX==0f && ! isCurrentlyActive
            if(isCancelled){
                c.drawRect(itemView.right.toFloat()+dX, itemView.top.toFloat() ,itemView.right.toFloat(),itemView.bottom.toFloat(),mClearPaint)
                return
            }
            mBackground?.color = context!!.resources.getColor(R.color.neon_green)
            mBackground?.setBounds(itemView.right + dX.toInt() ,itemView.top, itemView.right,itemView.bottom)
            mBackground?.draw(c)

            val deleteIconTop =itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconMargin = ((itemHeight - intrinsicHeight) / 2)
            val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
            val deleteIconRight =  itemView.right - deleteIconMargin
            val deleteIconBottom = deleteIconTop + intrinsicHeight
            deleteDrawable.level=0
            deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteDrawable.draw(c)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            return 0.7f
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            val position = viewHolder.adapterPosition
            val orderid = orderRecyclerAdapter!!.getOrderIdFromPosition(position)
            val isbid=orderRecyclerAdapter!!.getTypeFromPosition(position)
            orderRecyclerAdapter!!.swipedata(orderid,isbid)
            orderRecyclerAdapter!!.notifyItemRemoved(position)

        }

    }
    fun attach(){

        val itemTouchHelper =ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(mRecycler)
    }


}

