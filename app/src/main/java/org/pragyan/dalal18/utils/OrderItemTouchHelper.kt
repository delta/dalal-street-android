package org.pragyan.dalal18.utils

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.pragyan.dalal18.adapter.OrdersRecyclerAdapter

class OrderItemTouchHelper(
        dragDirs: Int,
        swipeDirs: Int,
        private val listener: BusItemTouchHelperListener
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val foregroundView = (viewHolder as OrdersRecyclerAdapter.OrderViewHolder).orderViewForeground
            ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(foregroundView)
        }
    }

    override fun onChildDrawOver(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
    ) {

        val foregroundView = (viewHolder as OrdersRecyclerAdapter.OrderViewHolder).orderViewForeground
        ItemTouchHelper.Callback.getDefaultUIUtil()
                .onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val foregroundView = (viewHolder as OrdersRecyclerAdapter.OrderViewHolder).orderViewForeground
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(foregroundView)
    }

    override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
    ) {
        val foregroundView = (viewHolder as OrdersRecyclerAdapter.OrderViewHolder).orderViewForeground

        ItemTouchHelper.Callback.getDefaultUIUtil()
                .onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    interface BusItemTouchHelperListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int)
    }
}
