package com.mmgpt.shoplistgpt

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDoubleTapTest {

    @Test
    fun doubleTapOnRowPassesItsAdapterPosition() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var recyclerView: RecyclerView

            scenario.onActivity { activity ->
                recyclerView = createTestRecyclerView(activity)
            }

            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                var tappedPosition: Int? = null

                recyclerView.setOnItemDoubleTapListener { position ->
                    tappedPosition = position
                    true
                }

                dispatchDoubleTap(recyclerView, x = 40f, y = 40f)

                assertEquals(0, tappedPosition)
            }
        }
    }

    @Test
    fun doubleTapOutsideRowsDoesNotInvokeCallback() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var recyclerView: RecyclerView

            scenario.onActivity { activity ->
                recyclerView = createTestRecyclerView(activity)
            }

            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                var tappedPosition: Int? = null

                recyclerView.setOnItemDoubleTapListener { position ->
                    tappedPosition = position
                    true
                }

                // The two test rows are each 100 pixels high; y=350 is empty space.
                dispatchDoubleTap(recyclerView, x = 40f, y = 350f)

                assertNull(tappedPosition)
            }
        }
    }

    private fun createTestRecyclerView(activity: MainActivity): RecyclerView {
        val recyclerView = RecyclerView(activity).apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = TwoRowAdapter()
        }

        val root = android.widget.FrameLayout(activity).apply {
            addView(
                recyclerView,
                android.widget.FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        activity.setContentView(root)
        return recyclerView
    }

    private fun dispatchDoubleTap(view: View, x: Float, y: Float) {
        val startTime = SystemClock.uptimeMillis()

        dispatchTap(view, startTime, x, y)
        dispatchTap(view, startTime + 120, x, y)
    }

    private fun dispatchTap(
        view: View,
        downTime: Long,
        x: Float,
        y: Float
    ) {
        view.dispatchTouchEvent(
            MotionEvent.obtain(
                downTime,
                downTime,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                0
            )
        )
        view.dispatchTouchEvent(
            MotionEvent.obtain(
                downTime,
                downTime + 40,
                MotionEvent.ACTION_UP,
                x,
                y,
                0
            )
        )
    }

    private class TwoRowAdapter :
        RecyclerView.Adapter<TwoRowAdapter.RowViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RowViewHolder {
            val row = TextView(parent.context).apply {
                text = "Test row"
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    100
                )
            }

            return RowViewHolder(row)
        }

        override fun onBindViewHolder(holder: RowViewHolder, position: Int) = Unit

        override fun getItemCount(): Int = 2

        class RowViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}