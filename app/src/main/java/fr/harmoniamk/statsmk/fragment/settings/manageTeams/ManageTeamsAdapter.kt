package fr.harmoniamk.statsmk.fragment.settings.manageTeams

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.databinding.ManagePlayersItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class ManageTeamsAdapter(private val items: MutableList<ManageTeamsItemViewModel> = mutableListOf()) : RecyclerView.Adapter<ManageTeamsAdapter.ManageTeamsViewHolder>(),
    CoroutineScope {

    val sharedEdit = MutableSharedFlow<Team>()

    @ExperimentalCoroutinesApi
    @FlowPreview
    inner class ManageTeamsViewHolder(val binding: ManagePlayersItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(teamVM: ManageTeamsItemViewModel) {
            binding.name.text = teamVM.name
            binding.checkmark.visibility = View.INVISIBLE
            teamVM.buttonVisibility
                .onEach { binding.editBtn.visibility = it }
                .launchIn(this@ManageTeamsAdapter)
            binding.checkmark.visibility = teamVM.checkMarkVisibility
            binding.shortnameTv.visibility = View.VISIBLE
            binding.shortnameTv.text = teamVM.shortName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageTeamsViewHolder =
        ManageTeamsViewHolder(ManagePlayersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ManageTeamsViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.editBtn.clicks().map{ item.team }.bind(sharedEdit, this)
    }

    fun addTeams(teams: List<ManageTeamsItemViewModel>) {
        notifyItemRangeRemoved(0, itemCount)
        items.clear()
        items.addAll(teams)
        notifyItemRangeInserted(0, itemCount)
    }

    override fun getItemCount() = items.size

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}