package com.smart.meeting.view.comment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import android.util.AttributeSet
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import java.io.ByteArrayOutputStream
import kotlin.math.abs

class PaintView(context: Context, attrs: AttributeSet?) : View(context, attrs), PaintViewInterface {
    private var penPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private var eraserPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private var mainPath: Path = Path()
    private var pathMeasure = PathMeasure()
    private var lastSegmentEnd = 0f
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private var mX: Float = 0f
    private var mY: Float = 0f
    private val touchTolerance = 4f
    private var commentTypeEnum: CommentTypeEnum = CommentTypeEnum.DRAW_PEN_PATH
    private var bitmapWidth: Int = 0
    private var bitmapHeight: Int = 0
    private var maxStrokeWidth = 8f
    private var maxEraserWidth = 200f
    private var pressure = 0f
    private var strokeWidth = 5f
    private var paintColor: Int = Color.parseColor("#fc4e50")
    private var lastPressure = 0f

    init {
        penPaint.isAntiAlias = true
        penPaint.color = paintColor
        penPaint.style = Paint.Style.STROKE
        penPaint.strokeJoin = Paint.Join.ROUND
        penPaint.strokeWidth = strokeWidth
        penPaint.strokeCap = Paint.Cap.ROUND
        eraserPaint.isAntiAlias = true
        eraserPaint.color = 0x00
        eraserPaint.style = Paint.Style.STROKE
        eraserPaint.strokeJoin = Paint.Join.ROUND
        eraserPaint.strokeWidth = maxEraserWidth
        eraserPaint.strokeCap = Paint.Cap.ROUND
        eraserPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        // 如果必要，禁用硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (bitmapWidth == 0 && bitmapHeight == 0) {
            bitmapWidth = w
            bitmapHeight = h
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, 0f, 0f, penPaint)
    }


    override fun setDrawType(commentTypeEnum: CommentTypeEnum) {
        this.commentTypeEnum = commentTypeEnum
    }

    /**
     * Set paint color
     * 设置画笔颜色
     * @param paintColor
     */
    fun setPaintColor(paintColor: Int) {
        this.paintColor = paintColor
        penPaint.color = paintColor
    }

    /**
     * Set stroke width
     * 设置画笔最大粗细
     * @param strokeWidth
     */
    fun setStrokeWidth(strokeWidth: Float) {
        this.maxStrokeWidth = strokeWidth
        penPaint.strokeWidth = strokeWidth
    }

    override fun onTouchEvent(x: Float, y: Float, event: MotionEvent) {
        pressure = event.pressure
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart(x, y)
            MotionEvent.ACTION_MOVE -> touchMove(x, y)
            MotionEvent.ACTION_UP -> touchUp()
        }
        invalidate()
    }

    // 平滑压感变化并计算笔触宽度
    private fun calculateStrokeWidth(pressure: Float, maxStrokeWidth: Float): Float {
        // 平滑压感变化
        lastPressure += (pressure - lastPressure) * 0.3f // 0.3f 是敏感度参数
        // 计算笔触宽度（不超过最大宽度）
        return (lastPressure * maxStrokeWidth).coerceAtMost(maxStrokeWidth)
    }

    private fun touchStart(x: Float, y: Float) {
        penPaint.strokeWidth = calculateStrokeWidth(pressure, maxStrokeWidth)
        eraserPaint.strokeWidth = calculateStrokeWidth(pressure, maxEraserWidth)
        mainPath.moveTo(x, y)
        mX = x
        mY = y
        pathMeasure.setPath(mainPath, false)
        canvas.drawPath(
            mainPath,
            if (commentTypeEnum == CommentTypeEnum.DRAW_PEN_PATH) penPaint else eraserPaint
        )
    }

    private fun touchMove(x: Float, y: Float) {
        penPaint.strokeWidth = calculateStrokeWidth(pressure, maxStrokeWidth)
        eraserPaint.strokeWidth = calculateStrokeWidth(pressure, maxEraserWidth)
        val dx = abs(x - mX)
        val dy = abs(y - mY)
        val newPath = Path()
        if (dx >= touchTolerance || dy >= touchTolerance) {
            mainPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            pathMeasure.setPath(mainPath, false)
            val length = pathMeasure.length
            // 获取 mainPath 上相应的段落
            pathMeasure.getSegment(
                lastSegmentEnd,
                length,
                newPath,
                true
            )
            lastSegmentEnd = length
            mX = x
            mY = y
        }
        canvas.drawPath(
            newPath,
            if (commentTypeEnum == CommentTypeEnum.DRAW_PEN_PATH) penPaint else eraserPaint
        )
    }

    private fun touchUp() {
        penPaint.strokeWidth = calculateStrokeWidth(pressure, maxStrokeWidth)
        eraserPaint.strokeWidth = calculateStrokeWidth(pressure, maxEraserWidth)
        mainPath.lineTo(mX, mY)
        pathMeasure.setPath(mainPath, false)
        val length = pathMeasure.length
        val newPath = Path()
        pathMeasure.getSegment(
            lastSegmentEnd,
            length,
            newPath,
            true
        )
        lastSegmentEnd = length
        canvas.drawPath(
            newPath,
            if (commentTypeEnum == CommentTypeEnum.DRAW_PEN_PATH) penPaint else eraserPaint
        )
        mainPath.reset()
        lastSegmentEnd = 0f
    }

    override fun loadBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(this.bitmap)
        invalidate()
    }

    override fun loadBitmapFromBase64(base64: String?) {
        if (base64.isNullOrEmpty()) {
            clearCanvas()
        } else {
            val decodedString = Base64.decode(base64, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            loadBitmap(decodedByte)
        }
    }

    private fun clearCanvas() {
        bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        canvas = Canvas(this.bitmap)
        invalidate()
    }

    override fun getBitmap(): Bitmap {
        return bitmap
    }

    override fun getBitmapAsBase64(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
