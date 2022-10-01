package com.auvehassan.swiperecyclerview

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var gestureHelper: ItemTouchHelper

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayMetrics: DisplayMetrics = resources.displayMetrics
//        val height = (displayMetrics.heightPixels / displayMetrics.density).toInt().dp
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt().dp

        val deleteIcon = resources.getDrawable(android.R.drawable.ic_menu_delete, null)
        val archiveIcon = resources.getDrawable(R.drawable.ic_arch, null)
        val recyclerView = findViewById<RecyclerView>(R.id.rvList)

        val deleteColor = resources.getColor(android.R.color.holo_red_light)
        val archiveColor = resources.getColor(android.R.color.holo_green_light)

        // dummy content list
        val items = arrayListOf<String>()
            .apply {
                for (i in 0..50) add("List Item $i")
            }
        // creating list adapter for the items
        val adapter = RecyclerViewItemAdapter(this, items)
            .also {
                recyclerView.adapter = it
            }

        // swipe/drag detection left/right
        gestureHelper = ItemTouchHelper(
            object :
                ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                ) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    //for dragging
                    viewHolder.itemView.elevation = 16F

                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition

                    Collections.swap(items, from, to)
                    adapter.notifyItemMoved(from, to)
                    return true
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)
                    //for dragging
                    viewHolder?.itemView?.elevation = 0F
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // for swapping
                    val pos = viewHolder.adapterPosition
                    items.removeAt(pos)
                    adapter.notifyItemRemoved(pos)

                    Snackbar.make(
                        findViewById(R.id.ll_main),
                        if (direction == ItemTouchHelper.RIGHT) "Deleted" else "Archived",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                override fun onChildDraw(
                    canvas: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    //for swapping
                    //Background color changed according to swipe direction
                    when {
                        abs(dX) < width / 3 -> canvas.drawColor(Color.GRAY)
                        dX > width / 3 -> canvas.drawColor(deleteColor)
                        else -> canvas.drawColor(archiveColor)
                    }

                    //set icons
                    val textMargin = resources.getDimension(R.dimen._16).roundToInt()
                    deleteIcon.bounds = Rect(
                        textMargin,
                        viewHolder.itemView.top + textMargin + 8.dp,
                        textMargin + deleteIcon.intrinsicWidth,
                        viewHolder.itemView.top + deleteIcon.intrinsicHeight
                                + textMargin + 8.dp
                    )

                    archiveIcon.bounds = Rect(
                        width - textMargin - archiveIcon.intrinsicWidth,
                        viewHolder.itemView.top + textMargin + 8.dp,
                        width - textMargin,
                        viewHolder.itemView.top + archiveIcon.intrinsicHeight
                                + textMargin + 8.dp
                    )

                    //Drawing icon according to the swiped direction
                    if (dX > 0) deleteIcon.draw(canvas)
                    else archiveIcon.draw(canvas)

                    super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }


            })

        gestureHelper.attachToRecyclerView(recyclerView)
    }

    fun startDragging(holder: RecyclerView.ViewHolder) = gestureHelper.startDrag(holder)

    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(), resources.displayMetrics
        ).roundToInt()
}