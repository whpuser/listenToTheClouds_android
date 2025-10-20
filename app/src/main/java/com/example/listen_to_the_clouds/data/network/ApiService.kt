package com.example.listen_to_the_clouds.data.network

import com.example.listen_to_the_clouds.data.model.HomePlaylist
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.model.LoginData
import com.example.listen_to_the_clouds.data.model.MusicDetails
import com.example.listen_to_the_clouds.data.model.PaginationData
import com.example.listen_to_the_clouds.data.model.RegisterData
import com.example.listen_to_the_clouds.data.model.ResponseResult
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

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

    //用户登录
    @POST("/user/login")
    suspend fun setLogin(@Body loginData: LoginData): Response<ResponseResult<String>>

    //用户注册
    @POST("user/register")
    suspend fun setRegister(@Body register: RegisterData): Response<ResponseResult<String>>

    //发送验证码
    @POST("/user/sendEmailCode")
    suspend fun setMailbox(@Query("mailbox") mailbox: String): Response<ResponseResult<String>>

}