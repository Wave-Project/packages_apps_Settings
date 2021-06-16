/*
 * Copyright (C) 2021 Wave-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.wave.edgeeffect

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.AbsListView.OnScrollListener.*
import android.widget.EdgeEffect
import androidx.core.widget.NestedScrollView
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.abs

class SpringNestScrollView : NestedScrollView {
    private lateinit var scrollConsumed: IntArray
    private lateinit var scrollStepConsumed: IntArray
    private lateinit var scrollOffsets: IntArray
    private lateinit var nestedOffsets: IntArray
    private var overScrollNested = false
    private var pullGrowTop = 0.1f
    private var pullGrowBottom = 0.9f
    private var dampedScrollShift = 0.0f
    private var distance = 0.0f
    private var edgeEffectFactory: SEdgeEffectFactory? = null
    private var glowingTop = false
    private var glowingBottom = false
    private var lastX = 0f
    private var lastY = 0f
    private var lastYVel = 0f
    private var lastTouchY = 0
    private var maxFlingVelocity = 0
    private var pullCount = 0
    private var scrollPointerId = 0
    private var scrollState = SCROLL_STATE_IDLE
    private var springAnimation: SpringAnimation? = null
    private var topGlow: EdgeEffect? = null
    private var bottomGlow: EdgeEffect? = null
    private var touchSlop = 0
    private var velocityTracker: VelocityTracker? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
        maxFlingVelocity = configuration.scaledMaximumFlingVelocity
        scrollStepConsumed = IntArray(2)
        scrollOffsets = IntArray(2)
        nestedOffsets = IntArray(2)
        scrollConsumed = IntArray(2)
        edgeEffectFactory = createViewEdgeEffectFactory()
        setEdgeEffectFactory(edgeEffectFactory)
        springAnimation = SpringAnimation(this, DAMPED_SCROLL, 0.0f)
        val force = SpringForce(0.0f)
        force.stiffness = 590.0f
        force.dampingRatio = 0.5f
        springAnimation!!.spring = force
    }

    private fun setEdgeEffectFactory(sEdgeEffectFactory: SEdgeEffectFactory?) {
        edgeEffectFactory = sEdgeEffectFactory
        invalidateGlows()
    }

    private fun invalidateGlows() {
        bottomGlow = null
        topGlow = null
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
        val masked = event.actionMasked
        val index = event.actionIndex
        val obtain = MotionEvent.obtain(event)
        when (masked) {
            MotionEvent.ACTION_DOWN -> {
                scrollPointerId = event.getPointerId(0)
                lastTouchY = (event.y + 0.5f).toInt()
                if (scrollState == SCROLL_STATE_FLING) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    setScrollState(SCROLL_STATE_TOUCH_SCROLL)
                }
                nestedOffsets[1] = 0
                nestedOffsets[0] = 0
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker!!.addMovement(obtain)
                velocityTracker!!.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                val yVelocity = -velocityTracker!!.getYVelocity(scrollPointerId)
                if (yVelocity == 0.0f) {
                    setScrollState(SCROLL_STATE_IDLE)
                } else {
                    lastYVel = yVelocity
                    lastX = event.x
                    lastY = event.y
                }
                resetTouch()
                stopNestedScroll()
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(scrollPointerId)
                if (pointerIndex < 0) {
                    Log.e(
                        "SpringScrollView",
                        "Error processing scroll; pointer index for id "
                                + scrollPointerId + " not found. Did any MotionEvents get skipped?"
                    )
                    return false
                }
                event.getX(pointerIndex)
                val touchY = (event.getY(pointerIndex) + 0.5f).toInt()
                var consumed = lastTouchY - touchY
                if (dispatchNestedPreScroll(0, consumed, scrollConsumed, scrollOffsets)) {
                    consumed -= scrollConsumed[1]
                    obtain.offsetLocation(
                        scrollOffsets[0].toFloat(),
                        scrollOffsets[1].toFloat()
                    )
                    nestedOffsets[0] += scrollOffsets[0]
                    nestedOffsets[1] += scrollOffsets[1]
                }
                val hasConsumedLot: Boolean
                if (scrollState != SCROLL_STATE_TOUCH_SCROLL) {
                    if (abs(consumed) > touchSlop) {
                        consumed = if (consumed > 0) consumed - touchSlop else consumed + touchSlop
                        hasConsumedLot = true
                    } else {
                        hasConsumedLot = false
                    }
                    if (hasConsumedLot) setScrollState(SCROLL_STATE_TOUCH_SCROLL)
                }
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    lastTouchY = touchY - scrollOffsets[1]
                    if (scrollByInternal(
                            consumed,
                            obtain
                        )
                    ) parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_CANCEL -> cancelTouch()
            MotionEvent.ACTION_POINTER_DOWN -> {
                scrollPointerId = event.getPointerId(index)
                lastTouchY = (event.getY(index) + 0.5f).toInt()
            }
            MotionEvent.ACTION_POINTER_UP -> onPointerUp(event)
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        val obtain = MotionEvent.obtain(event)
        val masked = event.actionMasked
        val index = event.actionIndex
        var isActionUp = false
        if (masked == 0) {
            nestedOffsets[1] = 0
            nestedOffsets[0] = 0
        }
        obtain.offsetLocation(nestedOffsets[0].toFloat(), nestedOffsets[1].toFloat())
        when (masked) {
            MotionEvent.ACTION_DOWN -> {
                scrollPointerId = event.getPointerId(0)
                lastTouchY = (event.y + 0.5f).toInt()
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker!!.addMovement(obtain)
                velocityTracker!!.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                val yVelocity = -velocityTracker!!.getYVelocity(scrollPointerId)
                if (yVelocity == 0.0f) {
                    setScrollState(SCROLL_STATE_IDLE)
                } else {
                    lastYVel = yVelocity
                    lastX = event.x
                    lastY = event.y
                }
                resetTouch()
                isActionUp = true
            }
            MotionEvent.ACTION_MOVE -> {
                val findPointerIndex = event.findPointerIndex(scrollPointerId)
                if (findPointerIndex < 0) {
                    Log.e(
                        "SpringScrollView",
                        "Error processing scroll; pointer index for id "
                                + scrollPointerId + " not found. Did any MotionEvents get skipped?"
                    )
                    return false
                }
                event.getX(findPointerIndex)
                val touchY = (event.getY(findPointerIndex) + 0.5f).toInt()
                var consumed = lastTouchY - touchY
                if (dispatchNestedPreScroll(0, consumed, scrollConsumed, scrollOffsets)) {
                    consumed -= scrollConsumed[1]
                    obtain.offsetLocation(
                        scrollOffsets[0].toFloat(),
                        scrollOffsets[1].toFloat()
                    )
                    nestedOffsets[0] += scrollOffsets[0]
                    nestedOffsets[1] += scrollOffsets[1]
                }
                val hasConsumedLot: Boolean
                if (scrollState != SCROLL_STATE_TOUCH_SCROLL) {
                    if (abs(consumed) > touchSlop) {
                        consumed = if (consumed > 0) consumed - touchSlop else consumed + touchSlop
                        hasConsumedLot = true
                    } else {
                        hasConsumedLot = false
                    }
                    if (hasConsumedLot) setScrollState(SCROLL_STATE_TOUCH_SCROLL)
                }
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    lastTouchY = touchY - scrollOffsets[1]
                    if (scrollByInternal(
                            consumed,
                            obtain
                        )
                    ) parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_CANCEL -> cancelTouch()
            MotionEvent.ACTION_POINTER_DOWN -> {
                scrollPointerId = event.getPointerId(index)
                lastTouchY = (event.getY(index) + 0.5f).toInt()
            }
            MotionEvent.ACTION_POINTER_UP -> onPointerUp(event)
        }
        if (!isActionUp) velocityTracker!!.addMovement(obtain)
        obtain.recycle()
        return super.onTouchEvent(event)
    }

    private fun ensureTopGlow() {
        checkNotNull(edgeEffectFactory) { "setEdgeEffectFactory first, please!" }
        if (topGlow != null) return
        topGlow = edgeEffectFactory!!.createEdgeEffect(this, 1)
        if (clipToPadding) {
            topGlow!!.setSize(
                (measuredWidth - paddingLeft) - paddingRight,
                (measuredHeight - paddingTop) - paddingBottom
            )
        } else topGlow!!.setSize(measuredWidth, measuredHeight)
    }

    private fun ensureBottomGlow() {
        checkNotNull(edgeEffectFactory) { "setEdgeEffectFactory first, please!" }
        if (bottomGlow != null) return
        bottomGlow = edgeEffectFactory!!.createEdgeEffect(this, 3)
        if (clipToPadding) {
            bottomGlow!!.setSize(
                (measuredWidth - paddingLeft) - paddingRight,
                (measuredHeight - paddingTop) - paddingBottom
            )
        } else bottomGlow!!.setSize(measuredWidth, measuredHeight)
    }

    private fun pullGlows(x: Float, xUnconsumed: Float, y: Float, yUnconsumed: Float) {
        if (y <= height.toFloat() && y >= 0.0f) {
            val height = y / (height.toFloat())
            var consumedLess = true
            if (yUnconsumed < 0.0f && height < pullGrowBottom && height > pullGrowTop) {
                ensureTopGlow()
                topGlow!!.onPull(
                    (-yUnconsumed) / getHeight().toFloat(),
                    x / width.toFloat()
                )
                glowingTop = true
            } else if (yUnconsumed <= 0.0f || height <= pullGrowTop || height >= pullGrowBottom) {
                consumedLess = false
            } else {
                ensureBottomGlow()
                bottomGlow!!.onPull(
                    yUnconsumed / getHeight().toFloat(),
                    1.0f - (x / width.toFloat())
                )
                glowingBottom = true
            }
            if (consumedLess || xUnconsumed != 0.0f || yUnconsumed != 0.0f) postInvalidateOnAnimation()
        }
    }

    private fun setScrollState(state: Int) {
        if (scrollState != state) {
            scrollState = state
        }
    }

    private fun resetTouch() {
        if (velocityTracker != null) {
            velocityTracker!!.clear()
        }
        releaseGlows()
    }

    private fun releaseGlows() {
        var isFinished: Boolean
        if (topGlow != null) {
            topGlow!!.onRelease()
            glowingTop = false
            isFinished = topGlow!!.isFinished
        } else isFinished = false
        if (bottomGlow != null) {
            bottomGlow!!.onRelease()
            glowingBottom = false
            isFinished = isFinished or bottomGlow!!.isFinished
        }
        if (isFinished) postInvalidateOnAnimation()
    }

    private fun cancelTouch() {
        resetTouch()
        setScrollState(SCROLL_STATE_IDLE)
    }

    private fun onPointerUp(motionEvent: MotionEvent) {
        val actionIndex = motionEvent.actionIndex
        if (motionEvent.getPointerId(actionIndex) == scrollPointerId) {
            val id = if (actionIndex == 0) 1 else 0
            scrollPointerId = motionEvent.getPointerId(id)
            lastTouchY = (motionEvent.getY(id) + 0.5f).toInt()
        }
    }

    private fun dispatchOnScrolled() {
        onScrollChanged(scrollX, scrollY, scrollX, scrollY)
    }

    private fun scrollByInternal(yConsumed: Int, event: MotionEvent?): Boolean {
        if (!isReadyToOverScroll(yConsumed < 0)) return false
        if (childCount >= 0) scrollStep(scrollStepConsumed)
        val dxConsumed: Int = if (childCount >= 0) scrollStepConsumed[0] else 0
        val dyConsumed: Int = if (childCount >= 0) scrollStepConsumed[1] else 0
        val dxUnconsumed: Int = if (childCount >= 0) -dxConsumed else 0
        val dyUnconsumed: Int = if (childCount >= 0) yConsumed - dyConsumed else 0
        invalidate()
        val nestedScroll = dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            scrollOffsets
        )
        if (nestedScroll) {
            lastTouchY -= scrollOffsets[1]
            event?.offsetLocation(scrollOffsets[0].toFloat(), scrollOffsets[1].toFloat())
            nestedOffsets[0] += scrollOffsets[0]
            nestedOffsets[1] += scrollOffsets[1]
        }
        if ((!nestedScroll || overScrollNested) && overScrollMode != OVER_SCROLL_NEVER) {
            if (event != null && !event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                pullGlows(
                    event.x, dxUnconsumed.toFloat(), event.y,
                    dyUnconsumed.toFloat()
                )
            }
            considerReleasingGlowsOnScroll(yConsumed)
        }
        if (dxConsumed != 0 || dyConsumed != 0) dispatchOnScrolled()
        if (!awakenScrollBars()) invalidate()
        return dxConsumed != 0 || dyConsumed != 0
    }

    private fun scrollStep(steps: IntArray?) {
        if (steps != null) {
            steps[1] = 0
        }
    }

    private fun isReadyToOverScroll(hasConsumedLess: Boolean): Boolean {
        return if (childCount <= 0) false else !canScrollVertically(
            if (hasConsumedLess) -1 else 1
        )
    }

    private fun considerReleasingGlowsOnScroll(yConsumed: Int) {
        var isFinished = false
        if (topGlow != null && !topGlow!!.isFinished && yConsumed > 0) {
            topGlow!!.onRelease()
            isFinished = topGlow!!.isFinished
        }
        if (bottomGlow != null && !bottomGlow!!.isFinished && yConsumed < 0) {
            bottomGlow!!.onRelease()
            isFinished = isFinished or bottomGlow!!.isFinished
        }
        if (isFinished) postInvalidateOnAnimation()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (glowingTop && canScrollVertically(-1) && t > oldt) onRecyclerViewScrolled()
        if (glowingBottom && canScrollVertically(1) && t < oldt) onRecyclerViewScrolled()
        if (!glowingTop && !canScrollVertically(-1) && t < oldt) {
            pullGlows(lastX, 0.0f, lastY, lastYVel / 20.0f)
            if (topGlow != null) {
                topGlow!!.onAbsorb((lastYVel / 20.0f).toInt())
            }
        }
        if (!glowingBottom && !canScrollVertically(1) && (t > oldt)) {
            pullGlows(lastX, 0.0f, lastY, lastYVel / 20.0f)
            if (bottomGlow == null) return
            bottomGlow!!.onAbsorb((lastYVel / 20.0f).toInt())
        }
    }

    private fun createViewEdgeEffectFactory(): ViewEdgeEffectFactory {
        return ViewEdgeEffectFactory()
    }

    private fun setDampedScrollShift(shift: Float) {
        if (dampedScrollShift != shift) {
            dampedScrollShift = shift
            invalidate()
        }
    }

    private fun finishScrollWithVelocity(f: Float) {
        springAnimation!!.setStartVelocity(f)
        springAnimation!!.setStartValue(dampedScrollShift)
        springAnimation!!.start()
    }

    private fun onRecyclerViewScrolled() {
        if (pullCount == 1) return
        distance = 0.0f
        pullCount = 0
        finishScrollWithVelocity(0.0f)
    }

    override fun draw(canvas: Canvas) {
        if (dampedScrollShift == 0.0f) {
            super.draw(canvas)
            return
        }
        canvas.translate(0.0f, dampedScrollShift)
        super.draw(canvas)
        canvas.restoreToCount(canvas.save())
    }

    open class SEdgeEffectFactory {
        open fun createEdgeEffect(view: View, direction: Int): EdgeEffect {
            return EdgeEffect(view.context)
        }

        companion object {
            const val DIRECTION_LEFT = 0
            const val DIRECTION_TOP = 1
            const val DIRECTION_RIGHT = 2
            const val DIRECTION_BOTTOM = 3
        }
    }

    inner class ViewEdgeEffectFactory : SEdgeEffectFactory() {
        override fun createEdgeEffect(view: View, direction: Int): EdgeEffect {
            return when (direction) {
                DIRECTION_LEFT, DIRECTION_TOP -> {
                    SpringEdgeEffect(context, 0.3f)
                }
                DIRECTION_RIGHT, DIRECTION_BOTTOM -> {
                    SpringEdgeEffect(context, -0.3f)
                }
                else -> {
                    super.createEdgeEffect(view, direction)
                }
            }
        }
    }

    private inner class SpringEdgeEffect(
        context: Context?,
        private val velocityMultiplier: Float
    ) :
        EdgeEffect(context) {
        override fun draw(canvas: Canvas): Boolean {
            return false
        }

        override fun onAbsorb(velocity: Int) {
            finishScrollWithVelocity(velocity.toFloat() * velocityMultiplier)
            distance = 0.0f
        }

        override fun onPull(deltaDistance: Float, displacement: Float) {
            if (springAnimation!!.isRunning) {
                springAnimation!!.cancel()
            }
            pullCount++
            distance += deltaDistance * (velocityMultiplier / 3.0f)
            setDampedScrollShift(distance * height.toFloat())
        }

        override fun onRelease() {
            distance = 0.0f
            pullCount = 0
            finishScrollWithVelocity(0.0f)
        }
    }

    companion object {
        private val DAMPED_SCROLL: FloatPropertyCompat<SpringNestScrollView> =
            object : FloatPropertyCompat<SpringNestScrollView>("value") {
                override fun getValue(springNestScrollView: SpringNestScrollView): Float {
                    return springNestScrollView.dampedScrollShift
                }

                override fun setValue(springNestScrollView: SpringNestScrollView, f: Float) {
                    springNestScrollView.setDampedScrollShift(f)
                }
            }
    }
}
