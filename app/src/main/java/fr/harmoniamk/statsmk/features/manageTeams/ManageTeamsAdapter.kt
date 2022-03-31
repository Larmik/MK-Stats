package fr.harmoniamk.statsmk.features.manageTeams

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.harmoniamk.statsmk.database.model.Team
import fr.harmoniamk.statsmk.databinding.ManagePlayersItemBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

class ManageTeamsAdapter(private val items: MutableList<Team> = mutableListOf()) : RecyclerView.Adapter<ManageTeamsAdapter.ManageTeamsViewHolder>(),
    CoroutineScope {

    val sharedEdit = MutableSharedFlow<Unit>()
    val sharedDelete = MutableSharedFlow<Team>()

    class ManageTeamsViewHolder(val binding: ManagePlayersItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(team: Team) {
            binding.name.text = team.name
            binding.checkmark.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageTeamsViewHolder =
        ManageTeamsViewHolder(ManagePlayersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ManageTeamsViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.editBtn.clicks().bind(sharedEdit, this)
        holder.binding.deleteBtn.clicks().map { item }.bind(sharedDelete, this)
    }

    fun addTeams(teams: List<Team>) {
        if (teams.size != itemCount) {
            notifyItemRangeRemoved(0, itemCount)
            items.clear()
            items.addAll(teams)
            notifyItemRangeInserted(0, itemCount)
        }
    }

    override fun getItemCount() = items.size

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}