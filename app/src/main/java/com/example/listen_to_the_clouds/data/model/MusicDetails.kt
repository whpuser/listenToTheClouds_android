package com.example.listen_to_the_clouds.data.model

data class MusicDetails (
   val id:Long,
   val name:String,
   val cover:String,
   val artist:String,
   val type:String,
   val heat:Int,
   val link:String,
   val collect:Int,
   val lyrics:String,
   val updated:String,
   val createTime:String
)