package com.mmgpt.shoplistgpt

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

data class ShoppingItem(
    val id: Long,
    val name: String,
    var isChecked: Boolean = false,
    var isFlagged: Boolean = false,
    val isHeading: Boolean = false,
    val isStruck: Boolean = false,
    /** Indicates this entry is a UI placeholder (e.g. the "Add..." row). */
    val isPlaceholder: Boolean = false
)

/**
 * Detects double taps on RecyclerView rows and lets the caller map each row
 * to a page. MotionEvent coordinates are already relative to the RecyclerView.
 */
internal fun RecyclerView.setOnItemDoubleTapListener(
    onItemDoubleTap: (adapterPosition: Int) -> Boolean
) {
    val detector = GestureDetectorCompat(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(event: MotionEvent): Boolean {
                val child = findChildViewUnder(event.x, event.y)
                    ?: return super.onDoubleTap(event)

                val position = getChildAdapterPosition(child)
                return if (position != RecyclerView.NO_POSITION) {
                    onItemDoubleTap(position)
                } else {
                    super.onDoubleTap(event)
                }
            }
        }
    )

    setOnTouchListener { _, event ->
        detector.onTouchEvent(event)
        false
    }
}