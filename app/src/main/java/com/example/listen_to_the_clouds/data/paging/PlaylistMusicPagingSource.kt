package com.example.listen_to_the_clouds.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.network.ApiService
import retrofit2.HttpException
import java.io.IOException

/**
 * PlaylistMusicPagingSource - 歌单音乐列表分页数据源
 */
class PlaylistMusicPagingSource(
    private val apiService: ApiService,
    private val playlistId: Long,
    private val pageSize: Int
) : PagingSource<Int, HomeSong>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HomeSong> {
        val page = params.key ?: 1
        val size = params.loadSize.coerceAtMost(pageSize)
        val offset = (page - 1) * size

        return try {
            // 调用接口获取分页数据
            val response = apiService.getPlaylistMusicList(
                offset = offset,
                pageSize = size,
                playlistId = playlistId
            )

            // 检查响应状态
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }

            val body = response.body()
            val data = body?.data
            val songs = data?.list ?: emptyList()
            val total = data?.total ?: 0
            
            Log.d("PlaylistMusicPaging", "page=$page, offset=$offset, size=$size, total=$total, songs=${songs.size}")

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
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, HomeSong>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
