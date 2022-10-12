package fr.harmoniamk.statsmk.fragment.teamSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.application.MainApplication
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarTeamViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface): ViewModel() {

    private val _sharedTeams = MutableSharedFlow<List<Team>>()
    private val _sharedTeamSelected = MutableSharedFlow<Team>()

    val sharedTeams = _sharedTeams.asSharedFlow()
    val sharedTeamSelected = _sharedTeamSelected.asSharedFlow()

    private val teams = mutableListOf<Team>()

    fun bind(onTeamClick: Flow<Team>, onSearch: Flow<String>) {
        onTeamClick.bind(_sharedTeamSelected, viewModelScope)
        firebaseRepository.getTeams()
            .map {
                teams.clear()
                teams.addAll(it.filterNot { team -> team.mid == preferencesRepository.currentTeam?.mid })
                teams.sortedBy { it.name }
            }
            .bind(_sharedTeams, viewModelScope)
        onSearch
            .map { searched ->
                teams.filter {
                    it.shortName?.toLowerCase(Locale.ROOT)
                        ?.contains(searched.toLowerCase(Locale.ROOT)).isTrue || it.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
                }.sortedBy { it.name }
            }
            .bind(_sharedTeams, viewModelScope)

    }
}