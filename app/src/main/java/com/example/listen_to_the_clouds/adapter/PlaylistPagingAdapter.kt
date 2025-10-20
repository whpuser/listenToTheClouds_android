package com.example.listen_to_the_clouds.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.data.model.HomePlaylist
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS
import com.example.listen_to_the_clouds.databinding.ViewHomePlaylistsBinding

class PlaylistPagingAdapter(
    private val onItemClick: (HomePlaylist) -> Unit
) : PagingDataAdapter<HomePlaylist, PlaylistPagingAdapter.PlaylistViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        // 加载布局
        val binding = ViewHomePlaylistsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        return PlaylistViewHolder(binding, onItemClick)
    }

    //获取当前位置的数据
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    //用于绑定每个 item 的视图与数据
    class PlaylistViewHolder(
        private val binding: ViewHomePlaylistsBinding,
        private val onItemClick: (HomePlaylist) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: HomePlaylist) = with(binding) {
            title.text = item.playlistTitle
            played.text = item.playlistTimes.toString()+" 次"

            Glide.with(root)
                .load(RESOURCE_ADDRESS + item.playlistCover)
                .placeholder(R.drawable.load)
                .error(R.drawable.load)
                .centerCrop()
                .into(failed)

            root.setOnClickListener { onItemClick(item) }
        }
    }

    //用来判断新旧数据是否相同，从而刷新数据
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<HomePlaylist>() {
            override fun areItemsTheSame(oldItem: HomePlaylist, newItem: HomePlaylist): Boolean =
                oldItem.playlistId == newItem.playlistId

            override fun areContentsTheSame(oldItem: HomePlaylist, newItem: HomePlaylist): Boolean =
                oldItem == newItem
        }
    }
}
