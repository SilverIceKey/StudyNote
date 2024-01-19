# 关于自定义画板View针对压感进行处理

最近有一个需求是针对手写笔的压感对绘画的线条进行粗细变化，最后实现之后发现起始原理也挺简单，但是找的过程兼职折磨。

话不多说先上代码链接[自定义画板](./code/PaintView.kt),接下来是简单的代码解析：

### 1、简单介绍：

画板使用的是双缓冲模式即自定义一个画板然后使用一个Bitmap为底先在Bitmap上绘制再在自定义View的onDraw方法中对Bitmap进行绘制，这样可以减少后期因为Path过多导致onDraw的绘画卡顿问题。

### 2、关于压感：

因为要防止画笔的粗细变化过大所以引入敏感度值来进行过度，下面是笔触的计算方法：

``````kotlin
private fun calculateStrokeWidth(pressure: Float, maxStrokeWidth: Float): Float {
        // 平滑压感变化
        lastPressure += (pressure - lastPressure) * 0.3f // 0.3f 是敏感度参数
        // 计算笔触宽度（不超过最大宽度）
        return (lastPressure * maxStrokeWidth).coerceAtMost(maxStrokeWidth)
}
``````

根据我的查询Android的Path原生是不支持不同位置的不同粗细策略，然后查询了不少之后发现Android这边可以使用PathMeasure对Path进行截取，所以我这边给出来的策略是在onTouchEvent的Action_MOVE中对完整的Path进行分段截取然后改变Paint的粗细之后进行绘画实现压感的效果，以下是相关代码：

```kotlin
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
```

一开始本来是想着每次画完之后重置主Path然后移动到之前的位置重新绘画的，强制分段，但是发现这样画出来的线是堪称完美的虚线，然后就放弃了。

至此针对压感实现绘画进行粗细变化的效果就完成了，感谢。