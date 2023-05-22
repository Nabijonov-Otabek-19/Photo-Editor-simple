package uz.gita.musicplayer.utils

import android.graphics.PointF
import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.sqrt

class MyRotationGestureDetector(private val mListener: MyRotationGestureDetector.OnRotationGestureListener?) {

    // We want rotation only if user puts two fingers
    private var ptrID1: Int
    private var ptrID2: Int

    init {
        ptrID1 = INVALID_POINTER_ID
        ptrID2 = INVALID_POINTER_ID
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }

    private val fPoint = PointF() // first finger point
    private val sPoint = PointF() // second finger point

    var fPointAfterMove = PointF()
    var sPointAfterMove = PointF()
    var oldDistance: Float? = null
    private val oldDelta = PointF()

    private var angle1 = 0.0f
    private var angle2 = 0.0f

    var angle: Float = 0f
        private set

    var scale: Float = 0f
            private set

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> ptrID1 = event.getPointerId(event.actionIndex)
            MotionEvent.ACTION_POINTER_DOWN -> {
                ptrID2 = event.getPointerId(event.actionIndex)

                val sX = event.getX(event.findPointerIndex(ptrID1))
                val sY = event.getY(event.findPointerIndex(ptrID1))
                val fX = event.getX(event.findPointerIndex(ptrID2))
                val fY = event.getY(event.findPointerIndex(ptrID2))

                fPoint.set(fX, fY)
                sPoint.set(sX, sY)

                // tanAlpha = (m1 - m2) / (1 + m1*m2)

                oldDelta.x = sX - fX // m1
                oldDelta.y = sY - fY // m2

                angle1 = atan2(oldDelta.y.toDouble(), oldDelta.x.toDouble()).toFloat()

                angle = (angle1 + angle2) * 180

                oldDistance = sPoint.distance(fPoint)
            }

            MotionEvent.ACTION_MOVE -> if (ptrID1 != INVALID_POINTER_ID && ptrID2 != INVALID_POINTER_ID) {
                val nsX: Float = event.getX(event.findPointerIndex(ptrID1))
                val nsY: Float = event.getY(event.findPointerIndex(ptrID1))
                val nfX: Float = event.getX(event.findPointerIndex(ptrID2))
                val nfY: Float = event.getY(event.findPointerIndex(ptrID2))

                val newDx2 = nsX - nfX
                val newDy2 = nsY - nfY

                angle2 = atan2(newDy2.toDouble(), newDx2.toDouble()).toFloat()

                angle = ( (angle1 + angle2) * 180 / Math.PI).toFloat()

                scale = PointF(nfX, nfY).distance(PointF(nsX,nsY)) / oldDistance!!

                mListener?.onRotation(this)
            }

            MotionEvent.ACTION_UP -> ptrID1 = INVALID_POINTER_ID
            MotionEvent.ACTION_POINTER_UP -> ptrID2 = INVALID_POINTER_ID
            MotionEvent.ACTION_CANCEL -> {
                ptrID1 = INVALID_POINTER_ID
                ptrID2 = INVALID_POINTER_ID
            }
        }
        return true
    }

    interface OnRotationGestureListener {
        fun onRotation(rotationDetector: MyRotationGestureDetector?)
    }

}


// Extensions
private fun sqr(a: Float) = a * a
fun PointF.distance(other: PointF): Float = sqrt(sqr(x - other.x) + sqr(y - other.y))