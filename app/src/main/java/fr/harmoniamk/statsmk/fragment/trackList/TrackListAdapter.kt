package fr.harmoniamk.statsmk.fragment.trackList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.databinding.TrackItemBinding
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class TrackListAdapter(val items: MutableList<Maps> = Maps.values().toMutableList()) :
    RecyclerView.Adapter<TrackListAdapter.TrackListViewHolder>(), CoroutineScope {

    private val _sharedClick = MutableSharedFlow<Int>()
    val sharedClick = _sharedClick.asSharedFlow()

    @ExperimentalCoroutinesApi
    class TrackListViewHolder(val binding: TrackItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Maps) {
            binding.cupIv.isVisible = true
            binding.trackIv.clipToOutline = true
            binding.trackIv.setImageResource(track.picture)
            binding.cupIv.setImageResource(track.cup.picture)
            binding.shortname.text = track.name
            binding.name.setText(track.label)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TrackListViewHolder(
        TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: TrackListViewHolder, position: Int) {
        val track = items[position]
        holder.bind(track)
        holder.binding.root.clicks()
            .onEach { _sharedClick.emit(track.ordinal) }
            .launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addTracks(tracks: List<Maps>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(tracks)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}