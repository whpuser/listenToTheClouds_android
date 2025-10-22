package com.example.listen_to_the_clouds.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS
import com.example.listen_to_the_clouds.databinding.ViewHomeMusicBinding

class SongListAdapter(
    private val onItemClick: (HomeSong) -> Unit
) : ListAdapter<HomeSong, SongListAdapter.SongViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ViewHomeMusicBinding.inflate(
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
        private val binding: ViewHomeMusicBinding,
        private val onItemClick: (HomeSong) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: HomeSong) = with(binding) {

            if (item.collect != 0) {
                collection.visibility = View.VISIBLE
            } else {
                collection.visibility = View.GONE
            }

            songTitle.text = item.name
            type.text = item.type
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
