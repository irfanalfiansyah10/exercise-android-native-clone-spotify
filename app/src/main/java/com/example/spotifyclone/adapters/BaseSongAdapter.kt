package com.example.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyclone.data.entities.Song

abstract class BaseSongAdapter(
    private val layoutId: Int,
) : RecyclerView.Adapter<BaseSongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    protected abstract var differ: AsyncListDiffer<Song>
    protected val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.mediaId == newItem.mediaId

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.hashCode() == newItem.hashCode()
    }

    protected var onItemClickListener: ((Song) -> Unit)? = null

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder =
        SongViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

    override fun getItemCount(): Int = songs.size

    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }
}