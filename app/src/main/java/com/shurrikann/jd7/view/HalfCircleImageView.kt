package com.shurrikann.jd7.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap

@SuppressLint("AppCompatCustomView")
class HalfCircleImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var rotationAngle = 0f  // 旋转角度

    init {
        scaleType = ScaleType.MATRIX // 允许对图像进行矩阵操作
    }

    // 允许外部设置旋转角度
    fun setRotationAngle(angle: Float) {
        rotationAngle = angle
        invalidate()  // 强制重绘
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // 创建一个矩阵来进行旋转
        val matrix = Matrix()

        // 在圆心位置进行旋转
        matrix.postRotate(rotationAngle, width / 2, height)

        // 应用旋转
        val bitmap = drawable?.toBitmap()
        bitmap?.let {
            // 创建圆形裁剪路径
            val path = Path()
            path.addCircle(width / 2, height, width / 2, Path.Direction.CW)

            // 创建一个裁剪区域
            canvas?.clipPath(path)

            // 绘制裁剪后的图片
            canvas?.drawBitmap(it, matrix, paint)
        }
    }
}