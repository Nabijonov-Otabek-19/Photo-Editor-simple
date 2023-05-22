package uz.gita.myphotoeditor_bek.utils

import android.app.Activity
import android.content.res.Resources
import android.graphics.PointF
import android.util.Log
import android.widget.Toast
import kotlin.math.sqrt

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun lineLength(firstPoint: PointF, secondPoint: PointF): Double {
    return sqrt(((firstPoint.x - secondPoint.x) * (firstPoint.x - secondPoint.x) + (firstPoint.y - secondPoint.y) * (firstPoint.y - secondPoint.y)).toDouble())
}

fun logger(message: String) {
    Log.d("AAA", message)
}

fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}