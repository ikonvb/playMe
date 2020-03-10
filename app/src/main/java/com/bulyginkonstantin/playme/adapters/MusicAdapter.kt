package com.bulyginkonstantin.playme.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bulyginkonstantin.playme.R
import com.bulyginkonstantin.playme.data.Music

class MusicAdapter(private var musicList: MutableList<Music>, private var itemClicked: ItemClicked) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var music: Music
        private var artistName = view.findViewById<TextView>(R.id.tvArtist)
        private var songName = view.findViewById<TextView>(R.id.tvSong)

        init {
            view.setOnClickListener(this)
        }

        fun bindMusic(music: Music) {
            this.music = music
            artistName.text = music.artistName
            songName.text = music.songName
        }

        override fun onClick(v: View?) {
            itemClicked.itemClicked(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.music_items, parent, false)
        return MusicViewHolder(view)
    }

    override fun getItemCount(): Int {
       return musicList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val item = musicList[position]
        holder.bindMusic(item)
    }

}