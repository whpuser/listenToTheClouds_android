package com.example.listen_to_the_clouds.data.network

import com.example.listen_to_the_clouds.data.model.FavoritesPagination
import com.example.listen_to_the_clouds.data.model.HomePlaylist
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.model.LoginData
import com.example.listen_to_the_clouds.data.model.MusicDetails
import com.example.listen_to_the_clouds.data.model.PaginationData
import com.example.listen_to_the_clouds.data.model.PlaylistDetails
import com.example.listen_to_the_clouds.data.model.PlaylistMusicResponse
import com.example.listen_to_the_clouds.data.model.RegisterData
import com.example.listen_to_the_clouds.data.model.ResponseResult
import com.example.listen_to_the_clouds.data.model.UpdatePlaylist
import com.example.listen_to_the_clouds.data.model.UserCollectionNumberVo
import com.example.listen_to_the_clouds.data.model.UserData
import com.example.listen_to_the_clouds.data.model.UserDate
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

//API 接口
interface ApiService {
    //歌单公开列表
    @GET("/playlist/obtain")
    suspend fun getPaginationPlaylist(
        @Query("offset") offset: Int,
        @Query("pageSize") pageSize: Int,
        @Query("sort") sort: String? = null,
        @Query("keyword") keyword: String? = null
    ): Response<ResponseResult<PaginationData<HomePlaylist>>>

    //歌曲列表
    @GET("/music/list")
    suspend fun getPaginationSong(
        @Query("offset") offset: Int,
        @Query("pageSize") pageSize: Int,
        @Query("sort") sort: String? = null,
        @Query("keyword") keyword: String? = null
    ): Response<ResponseResult<PaginationData<HomeSong>>>

    //歌曲详情
    @GET("/music/details")
    suspend fun getSongDetails(@Query("id") id: Int): Response<ResponseResult<MusicDetails>>

    //歌单详情
    @GET("/playlist/details")
    suspend fun getPlaylistDetails(@Query("id") id: Long): Response<ResponseResult<PlaylistDetails>>

    //用户登录
    @POST("/user/login")
    suspend fun setLogin(@Body loginData: LoginData): Response<ResponseResult<String>>

    //用户注册
    @POST("user/register")
    suspend fun setRegister(@Body register: RegisterData): Response<ResponseResult<String>>

    //发送验证码
    @POST("/user/sendEmailCode")
    suspend fun setMailbox(@Query("mailbox") mailbox: String): Response<ResponseResult<String>>

    //用户详情
    @GET("/user/info")
    suspend fun getUserInfo(): Response<ResponseResult<UserDate>>

    //获取用户收藏与创建总数
    @GET("/user/collection-create/info")
    suspend fun getMyTotal(): Response<ResponseResult<UserCollectionNumberVo>>

    //分页获取歌单音乐列表
    @GET("/playlist/playlistMusicList")
    suspend fun getPlaylistMusicList(
        @Query("offset") offset: Int,
        @Query("pageSize") pageSize: Int,
        @Query("playlistId") playlistId: Long
    ): Response<ResponseResult<PlaylistMusicResponse>>

    //收藏歌单
    @POST("/playlist/update/favorites")
    suspend fun setPlaylistCollection(@Query("playlistId") playlistId: Long): Response<ResponseResult<String>>

    //收藏歌曲与取消收藏
    @POST("/music/update/favorites")
    suspend fun setFavoriteSongs(@Query("musicId") musicId: Long): Response<ResponseResult<String>>

    //创建歌单
    @POST("/playlist/create")
    suspend fun setCreatePlaylist(@Query("title") title: String): Response<ResponseResult<String>>

    //获取用户收藏歌曲列表
    @POST("/music/favorites")
    suspend fun setFavoritesSong(@Body favoritesPagination: FavoritesPagination): Response<ResponseResult<PaginationData<HomeSong>>>

    //获取用户收藏歌单列表
    @POST("/playlist/favorites")
    suspend fun getFavoritesPlaylist(@Body favoritesPagination: FavoritesPagination): Response<ResponseResult<PaginationData<HomePlaylist>>>

    //获取用户创建歌单列表
    @POST("/playlist/user/create")
    suspend fun getUserCreatedPlaylists(@Body favoritesPagination: FavoritesPagination): Response<ResponseResult<PaginationData<HomePlaylist>>>

    //用户反馈
    @POST("/user/feedback")
    suspend fun setUserFeedback(
        @Query("user_id") userId: Long,
        @Query("content") content: String
    ): Response<ResponseResult<String>>

    //用户上传头像
    @Multipart
    @POST("/user/upload/avatar")
    suspend fun uploadUserAvatar(
        @Part file: MultipartBody.Part,
        @Query("user_id") userId: Long
    ): Response<ResponseResult<String>>

    //用户更新信息
    @POST("/user/modify")
    suspend fun setUserModify(@Body userData: UserData): Response<ResponseResult<String>>

    //更新歌单数据
    @Multipart
    @POST("/playlist/user/update")
    suspend fun setUpdatePlaylist(
        @Part("playlistId") playlistId: RequestBody,
        @Part("playlistTitle") playlistTitle: RequestBody,
        @Part playlistCover: MultipartBody.Part?
    ): Response<ResponseResult<String>>

    //删除歌单
    @POST("/playlist/delete")
    suspend fun setPlaylistDelete(@Query("id") id: Long): Response<ResponseResult<String>>

    //歌单添加音乐
    @POST("/playlist/addMusic")
    suspend fun setPlaylistAddMusic(
        @Query("playlistId") playlistId: Long,
        @Query("musicId") musicId: Long
    ): Response<ResponseResult<String>>
}