package com.example.listen_to_the_clouds.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS
import com.example.listen_to_the_clouds.databinding.ViewPlaylistMusicBinding
import com.example.listen_to_the_clouds.databinding.ViewSearchPlaylistBinding

class PlaylistAdapter(
    private val onItemClick: (HomeSong) -> Unit // 点击事件回调
) : PagingDataAdapter<HomeSong, PlaylistAdapter.SongViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ViewPlaylistMusicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class SongViewHolder(
        private val binding: ViewPlaylistMusicBinding,
        private val onItemClick: (HomeSong) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: HomeSong) = with(binding) {

            if (item.collect != 0) {
                collection.visibility = View.VISIBLE
            } else {
                collection.visibility = View.GONE
            }


            type.text = item.type
            songTitle.text = item.name
            author.text = item.artist


            Glide.with(root)
                .load(RESOURCE_ADDRESS + item.cover)
                .placeholder(R.drawable.load)
                .error(R.drawable.load)
                .centerCrop()
                .into(songCover)

            root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<HomeSong>() {
            override fun areItemsTheSame(oldItem: HomeSong, newItem: HomeSong): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: HomeSong, newItem: HomeSong): Boolean =
                oldItem == newItem
        }
    }
}
