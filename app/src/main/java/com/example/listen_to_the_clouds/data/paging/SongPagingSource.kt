package com.example.listen_to_the_clouds.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.network.ApiService
import retrofit2.HttpException
import java.io.IOException

/**
 * SongPagingSource
 *
 * 支持排序、关键字搜索，并通过
 */
class SongPagingSource(
    private val apiService: ApiService,            // 网络接口实例（通过依赖注入传入）
    private val pageSize: Int,                // 每页数量
    private val sort: String? = "sort_date.desc",  // 排序方式
    private val keyword: String? = null,           // 搜索关键字
) : PagingSource<Int, HomeSong>() {

    /**
     * 加载分页数据。
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HomeSong> {
        val page = params.key ?: 1
        val size = params.loadSize.coerceAtMost(pageSize)
        val offset = (page - 1) * size

        return try {
            // 调用接口获取分页数据
            val response = apiService.getPaginationSong(
                offset = offset,
                pageSize = size,
                sort = sort,
                keyword = keyword
            )

            // 检查响应状态
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }

            val body = response.body()
            val data = body?.data
            val songs = data?.list ?: emptyList()
            val total = data?.total ?: 0
            Log.d("TAG", "数据: $body")
            // 计算分页 key
            val prevKey = if (page == 1) null else page - 1
            val nextKey = if (offset + songs.size >= total || songs.isEmpty()) null else page + 1

            // 返回分页数据
            LoadResult.Page(
                data = songs,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            // 网络错误（断网、超时等）
            LoadResult.Error(e)
        } catch (e: HttpException) {
            // HTTP 协议错误
            LoadResult.Error(e)
        } catch (e: Exception) {
            // 其他未知错误
            LoadResult.Error(e)
        }
    }

    /**
     * 确定刷新时的页码。
     */
    override fun getRefreshKey(state: PagingState<Int, HomeSong>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
