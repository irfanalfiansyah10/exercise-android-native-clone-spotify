package com.example.spotifyclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.exoplayer.isPlaying
import com.example.spotifyclone.exoplayer.toSong
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import com.example.spotifyclone.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: Song? = null
    private var playbackState: PlaybackStateCompat? = null
    private var shouldUpdateSeekbar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        subscribeToObservers()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                seekbar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })
        ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let { song ->
                mainViewModel.playOrToggleSong(song, true)
            }
        }
        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }
        ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
    }

    private fun updateTitleSongImage(song: Song) {
        val title = "${song.title} - ${song.subtitle}"
        tvSongName.text = title
        glide.load(song.imageUrl).into(ivSongImage)
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms)
    }

    private fun setMaxPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvSongDuration.text = dateFormat.format(ms)
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if (curPlayingSong == null && songs.isNotEmpty()) {
                                curPlayingSong = songs[0]
                                updateTitleSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            it?.let { metadata ->
                curPlayingSong = metadata.toSong()
                updateTitleSongImage(curPlayingSong!!)
            }
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) { state ->
            playbackState = state
            ivPlayPauseDetail.setImageResource(
                if (playbackState?.isPlaying == true)
                    R.drawable.ic_pause
                else
                    R.drawable.ic_play
            )
            seekBar.progress = state?.position?.toInt() ?: 0
        }

        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            it?.let { progress ->
                if (shouldUpdateSeekbar) {
                    seekBar.progress = progress.toInt()
                    setCurPlayerTimeToTextView(progress)
                }
            }
        }

        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            it?.let { max ->
                seekBar.max = max.toInt()
                setMaxPlayerTimeToTextView(max)
            }
        }
    }
}