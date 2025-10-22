package com.example.listen_to_the_clouds.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.data.model.HomePlaylist
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS
import com.example.listen_to_the_clouds.databinding.ViewSearchPlaylistBinding

class SearchPlaylistAdapter(
    private val onItemClick: (HomePlaylist) -> Unit
) : ListAdapter<HomePlaylist, SearchPlaylistAdapter.PlaylistViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ViewSearchPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlaylistViewHolder(
        private val binding: ViewSearchPlaylistBinding,
        private val onItemClick: (HomePlaylist) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: HomePlaylist) = with(binding) {
            songTitle.text = item.playlistTitle
            playVolume.text = "播放 ${item.playlistTimes}"

            Glide.with(root)
                .load(RESOURCE_ADDRESS + item.playlistCover)
                .placeholder(R.drawable.load)
                .error(R.drawable.load)
                .centerCrop()
                .into(songCover)

            root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<HomePlaylist>() {
            override fun areItemsTheSame(oldItem: HomePlaylist, newItem: HomePlaylist): Boolean =
                oldItem.playlistId == newItem.playlistId

            override fun areContentsTheSame(oldItem: HomePlaylist, newItem: HomePlaylist): Boolean =
                oldItem == newItem
        }
    }
}
