package com.example.listen_to_the_clouds.ui.playback

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import androidx.core.content.ContextCompat
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.utils.LyricLine
import kotlin.math.abs

/**
 * 自定义歌词视图 - 支持渐变变色和手动滚动
 */
class LyricsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var lyrics: List<LyricLine> = emptyList()
    private var currentLineIndex: Int = -1
    private var currentTime: Long = 0L
    
    // 颜色
    private val normalColor = ContextCompat.getColor(context, android.R.color.darker_gray)
    private val highlightColor = ContextCompat.getColor(context, R.color.main_color)
    
    // 画笔
    private val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 40f
        color = normalColor
        textAlign = Paint.Align.CENTER
    }
    
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        color = highlightColor
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    
    // 用于绘制渐变效果的画笔
    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    
    // 行高
    private val lineHeight = 80f
    
    // 滚动相关
    private var scrollY = 0f
    private var targetScrollY = 0f
    private val scroller = Scroller(context)
    private var isTouching = false
    private var isUserScrolling = false
    private var lastAutoScrollTime = 0L
    
    // 手势检测
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (!isTouching) return false
            
            scrollY += distanceY
            
            // 限制滚动范围
            val maxScrollY = (lyrics.size - 1) * lineHeight
            scrollY = scrollY.coerceIn(0f, maxScrollY.coerceAtLeast(0f))
            
            isUserScrolling = true
            invalidate()
            return true
        }
        
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (lyrics.isEmpty()) return false
            
            val maxScrollY = (lyrics.size - 1) * lineHeight
            scroller.fling(
                0, scrollY.toInt(),
                0, (-velocityY).toInt(),
                0, 0,
                0, maxScrollY.toInt()
            )
            invalidate()
            return true
        }
    })
    
    /**
     * 设置歌词数据
     */
    fun setLyrics(lyrics: List<LyricLine>) {
        this.lyrics = lyrics
        this.currentLineIndex = -1
        this.scrollY = 0f
        this.targetScrollY = 0f
        this.isUserScrolling = false
        invalidate()
    }
    
    /**
     * 更新当前播放位置
     */
    fun updateTime(currentTime: Long) {
        this.currentTime = currentTime
        
        if (lyrics.isEmpty()) return
        
        // 查找当前歌词行
        var newIndex = -1
        for (i in lyrics.indices.reversed()) {
            if (currentTime >= lyrics[i].time) {
                newIndex = i
                break
            }
        }
        
        // 如果当前行发生变化且用户没有在手动滚动
        if (newIndex != currentLineIndex) {
            currentLineIndex = newIndex
            
            // 如果用户最近没有手动滚动（5秒内），则自动滚动
            val currentSystemTime = System.currentTimeMillis()
            if (!isUserScrolling || currentSystemTime - lastAutoScrollTime > 5000) {
                isUserScrolling = false
                targetScrollY = if (currentLineIndex >= 0) {
                    currentLineIndex * lineHeight
                } else {
                    0f
                }
            }
        }
        
        invalidate()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouching = true
                scroller.forceFinished(true)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
                // 3秒后恢复自动滚动
                postDelayed({
                    if (!isTouching) {
                        isUserScrolling = false
                        lastAutoScrollTime = System.currentTimeMillis()
                    }
                }, 3000)
            }
        }
        
        gestureDetector.onTouchEvent(event)
        return true
    }
    
    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollY = scroller.currY.toFloat()
            invalidate()
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (lyrics.isEmpty()) {
            // 显示无歌词提示
            canvas.drawText(
                "暂无歌词",
                width / 2f,
                height / 2f,
                normalPaint
            )
            return
        }
        
        // 平滑滚动（仅在非用户滚动时）
        if (!isUserScrolling && !isTouching) {
            if (scrollY != targetScrollY) {
                val diff = targetScrollY - scrollY
                scrollY += diff * 0.15f
                if (abs(diff) < 1f) {
                    scrollY = targetScrollY
                }
                invalidate()
            }
        }
        
        val centerY = height / 2f
        val startY = centerY - scrollY
        
        // 绘制歌词
        for (i in lyrics.indices) {
            val y = startY + i * lineHeight
            
            // 只绘制可见区域的歌词
            if (y < -lineHeight || y > height + lineHeight) {
                continue
            }
            
            val lyricText = lyrics[i].content
            val centerX = width / 2f
            
            if (i == currentLineIndex) {
                // 当前行 - 绘制渐变效果
                drawGradientText(canvas, lyricText, centerX, y, i)
            } else {
                // 其他行 - 普通绘制
                canvas.drawText(lyricText, centerX, y, normalPaint)
            }
        }
    }
    
    /**
     * 绘制渐变文字（从左到右）
     */
    private fun drawGradientText(canvas: Canvas, text: String, centerX: Float, y: Float, lineIndex: Int) {
        val textWidth = highlightPaint.measureText(text)
        val leftX = centerX - textWidth / 2f
        
        if (lineIndex >= lyrics.size) return
        
        val currentLyric = lyrics[lineIndex]
        val nextLyric = if (lineIndex + 1 < lyrics.size) lyrics[lineIndex + 1] else null
        
        // 计算当前行的播放进度
        val elapsed = currentTime - currentLyric.time
        
        val progress = if (nextLyric != null) {
            val totalDuration = nextLyric.time - currentLyric.time
            
            // 使用固定变色时长（2秒）或实际时长的较小值
            val fixedAnimationDuration = 2000L // 固定2秒
            val animationDuration = minOf(fixedAnimationDuration, (totalDuration * 0.8f).toLong())
            
            when {
                elapsed < 0 -> 0f // 还没到这一行
                elapsed >= animationDuration -> 1f // 变色已完成
                else -> (elapsed.toFloat() / animationDuration).coerceIn(0f, 1f) // 正在变色
            }
        } else {
            // 最后一行，使用固定时长（2秒变色完成）
            val animationDuration = 2000L
            when {
                elapsed < 0 -> 0f
                elapsed >= animationDuration -> 1f
                else -> (elapsed.toFloat() / animationDuration).coerceIn(0f, 1f)
            }
        }
        
        if (progress >= 0.99f) {
            // 完全高亮
            canvas.drawText(text, centerX, y, highlightPaint)
        } else if (progress <= 0.01f) {
            // 完全未高亮
            canvas.drawText(text, centerX, y, normalPaint)
        } else {
            // 渐变效果 - 使用 LinearGradient 一次性绘制
            val shader = LinearGradient(
                leftX, y,
                leftX + textWidth, y,
                intArrayOf(highlightColor, normalColor),
                floatArrayOf(progress, progress),
                Shader.TileMode.CLAMP
            )
            gradientPaint.shader = shader
            canvas.drawText(text, centerX, y, gradientPaint)
        }
    }
    
    /**
     * 清空歌词
     */
    fun clear() {
        lyrics = emptyList()
        currentLineIndex = -1
        scrollY = 0f
        targetScrollY = 0f
        isUserScrolling = false
        invalidate()
    }
}
