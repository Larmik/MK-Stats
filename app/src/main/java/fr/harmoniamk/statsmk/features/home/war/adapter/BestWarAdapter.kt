package fr.harmoniamk.statsmk.features.home.war.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.firebase.model.War
import fr.harmoniamk.statsmk.databinding.BestTournamentItemBinding
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
class BestWarAdapter(val items: MutableList<War> = mutableListOf()) :
    RecyclerView.Adapter<BestWarAdapter.BestWarViewHolder>(), CoroutineScope {

    private val _sharedItemClick = MutableSharedFlow<War>()
    val sharedItemClick = _sharedItemClick.asSharedFlow()

    class BestWarViewHolder(val binding: BestTournamentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(war: War, position: Int) {
            binding.nameTv.text = war.name
            binding.totalScoreTv.text = war.displayedScore
            binding.timeTv.text = war.updatedDate
            binding.tmInfos.isVisible = false
            binding.ratioScoreTv.text = war.displayedAverage
            binding.trophy.setImageResource(
                when (position) {
                    0 -> R.drawable.gold
                    1 -> R.drawable.silver
                    else -> R.drawable.bronze
                }
            )
            binding.topScoreTv.text = war.displayedDiff
            binding.currentTmTop.text = "Diff."
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BestWarViewHolder(
        BestTournamentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: BestWarViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)
        holder.binding.root.clicks()
            .onEach { _sharedItemClick.emit(item) }
            .launchIn(this)
    }

    override fun getItemCount() = items.size

    fun addTournaments(wars: List<War>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(wars)
        notifyItemRangeInserted(0, itemCount)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}