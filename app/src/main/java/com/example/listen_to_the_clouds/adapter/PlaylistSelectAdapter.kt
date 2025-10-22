package com.example.listen_to_the_clouds.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.data.model.HomePlaylist
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS

class PlaylistSelectAdapter(
    private val playlists: List<HomePlaylist>,
    private val onPlaylistClick: (HomePlaylist) -> Unit
) : RecyclerView.Adapter<PlaylistSelectAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistCover: ImageView = view.findViewById(R.id.playlistCover)
        val playlistTitle: TextView = view.findViewById(R.id.playlistTitle)
        val playlistCount: TextView = view.findViewById(R.id.playlistCount)
        val playlistItem: View = view.findViewById(R.id.playlistItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        
        holder.playlistTitle.text = playlist.playlistTitle
//        holder.playlistCount.text = "${playlist.musicNumber} 首歌曲"
        
        // 加载封面
        Glide.with(holder.itemView.context)
            .load(RESOURCE_ADDRESS + playlist.playlistCover)
            .placeholder(R.drawable.load)
            .error(R.drawable.test)
            .centerCrop()
            .into(holder.playlistCover)
        
        // 点击事件
        holder.playlistItem.setOnClickListener {
            onPlaylistClick(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size
}
