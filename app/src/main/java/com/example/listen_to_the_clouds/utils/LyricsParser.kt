package com.example.listen_to_the_clouds.utils

/**
 * 歌词行数据类
 */
data class LyricLine(
    val time: Long,        // 时间戳（毫秒）
    val content: String    // 歌词内容
)

/**
 * LRC 格式歌词解析工具
 */
object LyricsParser {
    
    /**
     * 解析 LRC 格式歌词
     * @param lrcText LRC 格式的歌词文本
     * @return 歌词行列表，按时间排序
     */
    fun parse(lrcText: String?): List<LyricLine> {
        if (lrcText.isNullOrBlank()) {
            return emptyList()
        }
        
        val lyricLines = mutableListOf<LyricLine>()
        val lines = lrcText.split("\n")
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            // 匹配时间标签 [mm:ss.xxx] 或 [mm:ss]
            val regex = """\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""".toRegex()
            val matchResult = regex.find(trimmedLine)
            
            if (matchResult != null) {
                val (minutes, seconds, milliseconds, content) = matchResult.destructured
                
                // 计算总毫秒数
                val timeInMillis = minutes.toLong() * 60 * 1000 +
                        seconds.toLong() * 1000 +
                        milliseconds.toLong().let {
                            // 如果是两位数，需要乘以10（例如：00 -> 000）
                            if (milliseconds.length == 2) it * 10 else it
                        }
                
                lyricLines.add(LyricLine(timeInMillis, content.trim()))
            }
        }
        
        // 按时间排序
        return lyricLines.sortedBy { it.time }
    }
    
    /**
     * 根据当前播放时间获取当前歌词行索引
     * @param lyrics 歌词列表
     * @param currentTime 当前播放时间（毫秒）
     * @return 当前歌词行索引，如果没有则返回 -1
     */
    fun getCurrentLineIndex(lyrics: List<LyricLine>, currentTime: Long): Int {
        if (lyrics.isEmpty()) return -1
        
        for (i in lyrics.indices.reversed()) {
            if (currentTime >= lyrics[i].time) {
                return i
            }
        }
        
        return -1
    }
}
