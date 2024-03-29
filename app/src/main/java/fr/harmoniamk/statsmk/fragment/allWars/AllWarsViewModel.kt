package fr.harmoniamk.statsmk.fragment.allWars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.WarFilterType
import fr.harmoniamk.statsmk.enums.WarSortType
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AllWarsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val filters = mutableListOf<WarFilterType>()

    private val _sharedWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedSortTypeSelected = MutableStateFlow(WarSortType.DATE)
    private val _sharedFilterList = MutableSharedFlow<List<WarFilterType>>()

    val sharedWars = _sharedWars.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asStateFlow()
    val sharedFilterList = _sharedFilterList.asSharedFlow()

    private val wars = mutableListOf<MKWar>()
    private val teams = mutableListOf<Team>()

    fun bind(onItemClick: Flow<MKWar>, onSearch: Flow<String>, onSortClick: Flow<WarSortType>, onFilterClick: Flow<WarFilterType>) {
        databaseRepository.getWars()
            .mapNotNull { list -> list.filter { war -> war.war?.teamHost == preferencesRepository.currentTeam?.mid }.sortedByDescending { it.war?.mid } }
            .onEach {
                wars.clear()
                wars.addAll(it)
                val filteredWars = mutableListOf<MKWar>()
                when (filters.size) {
                    3 -> filteredWars.addAll(wars.filter { it.war?.isOfficial.isTrue && it.isThisWeek && it.hasPlayer(authenticationRepository.user?.uid) })
                    2 -> when {
                        filters.contains(WarFilterType.WEEK) && filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(wars.filter { it.isThisWeek && it.war?.isOfficial.isTrue})
                        filters.contains(WarFilterType.WEEK) && filters.contains(WarFilterType.PLAY) -> filteredWars.addAll(wars.filter { it.isThisWeek && it.hasPlayer(authenticationRepository.user?.uid) })
                        filters.contains(WarFilterType.PLAY) && filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(wars.filter { it.hasPlayer(authenticationRepository.user?.uid) && it.war?.isOfficial.isTrue })
                    }
                    1 -> when {
                        filters.contains(WarFilterType.WEEK) -> filteredWars.addAll(wars.filter { it.isThisWeek })
                        filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(wars.filter { it.war?.isOfficial.isTrue })
                        filters.contains(WarFilterType.PLAY) -> filteredWars.addAll(wars.filter { it.hasPlayer(authenticationRepository.user?.uid) })
                    }
                    else -> filteredWars.addAll(wars)
                }
                _sharedWars.emit(when (_sharedSortTypeSelected.value) {
                    WarSortType.DATE -> filteredWars.sortedByDescending { it.war?.mid }
                    WarSortType.SCORE -> filteredWars.sortedByDescending { it.scoreHost }
                    WarSortType.TEAM -> filteredWars.sortedBy { it.name }
                })
                _sharedFilterList.emit(filters)
            }
            .flatMapLatest { databaseRepository.getTeams() }
            .onEach {
                teams.clear()
                teams.addAll(it)
            }.launchIn(viewModelScope)

        onSearch
            .onEach { searched ->
                val filteredWars = mutableListOf<MKWar>()
                when (searched.isEmpty()) {
                    true -> {
                        filteredWars.addAll(wars)
                        when {
                            filters.contains(WarFilterType.WEEK) -> filteredWars.removeAll(wars.filterNot { it.isThisWeek }
                                .toSet())
                            filters.contains(WarFilterType.OFFICIAL) -> filteredWars.removeAll(wars.filterNot { it.war?.isOfficial.isTrue }
                                .toSet())
                            filters.contains(WarFilterType.PLAY) -> filteredWars.removeAll(wars.filterNot { it.hasPlayer(authenticationRepository.user?.uid) }
                                .toSet())
                        }
                        when (_sharedSortTypeSelected.value) {
                            WarSortType.DATE -> filteredWars.sortByDescending { it.war?.mid }
                            WarSortType.SCORE -> filteredWars.sortByDescending { it.scoreHost }
                            WarSortType.TEAM -> filteredWars.sortBy { it.name }
                        }
                    }
                    else -> {
                        val filteredTeams = teams.filter { it.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(
                            Locale.ROOT)).isTrue || it.shortName?.toLowerCase()?.contains(searched.toLowerCase(
                            Locale.ROOT)).isTrue}
                        filteredTeams.forEach { team -> filteredWars.addAll(wars.filter { it.war?.teamOpponent?.equals(team.mid).isTrue }) }
                        when {
                            filters.contains(WarFilterType.WEEK) -> filteredWars.removeAll(
                                filteredWars.filterNot { it.isThisWeek }.toSet()
                            )
                            filters.contains(WarFilterType.OFFICIAL) -> filteredWars.removeAll(
                                filteredWars.filterNot { it.war?.isOfficial.isTrue }.toSet()
                            )
                            filters.contains(WarFilterType.PLAY) -> filteredWars.removeAll(
                                filteredWars.filterNot { it.hasPlayer(authenticationRepository.user?.uid) }
                                    .toSet()
                            )
                        }
                        when (_sharedSortTypeSelected.value) {
                            WarSortType.DATE -> filteredWars.sortByDescending { it.war?.mid }
                            WarSortType.SCORE -> filteredWars.sortByDescending { it.scoreHost }
                            WarSortType.TEAM -> filteredWars.sortBy { it.name }
                        }
                    }
                }
                _sharedWars.emit(filteredWars)
            }.launchIn(viewModelScope)

        onSortClick
            .onEach {
                val sortedWars = when (it) {
                    WarSortType.DATE -> wars.sortedByDescending { it.war?.mid }
                    WarSortType.TEAM -> wars.sortedBy { it.name }
                    WarSortType.SCORE -> wars.sortedByDescending { it.scoreHost }
                }.toMutableList()
                when {
                    filters.contains(WarFilterType.WEEK) -> sortedWars.removeAll(wars.filterNot { it.isThisWeek }
                        .toSet())
                    filters.contains(WarFilterType.OFFICIAL) -> sortedWars.removeAll(wars.filterNot { it.war?.isOfficial.isTrue }
                        .toSet())
                    filters.contains(WarFilterType.PLAY) -> sortedWars.removeAll(wars.filterNot { it.hasPlayer(authenticationRepository.user?.uid) }
                        .toSet())
                }
                _sharedWars.emit(sortedWars)
                _sharedSortTypeSelected.emit(it)
            }.launchIn(viewModelScope)

        onFilterClick
            .onEach {
                val filteredWars = mutableListOf<MKWar>()
                when (filters.contains(it)) {
                    true -> filters.remove(it)
                    else -> filters.add(it)
                }
                filteredWars.clear()
                when (filters.size) {
                    3 -> filteredWars.addAll(wars.filter { it.war?.isOfficial.isTrue && it.isThisWeek && it.hasPlayer(authenticationRepository.user?.uid) })
                    2 -> when {
                        filters.contains(WarFilterType.WEEK) && filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(wars.filter { it.isThisWeek && it.war?.isOfficial.isTrue})
                        filters.contains(WarFilterType.WEEK) && filters.contains(WarFilterType.PLAY) -> filteredWars.addAll(wars.filter { it.isThisWeek && it.hasPlayer(authenticationRepository.user?.uid) })
                        filters.contains(WarFilterType.PLAY) && filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(wars.filter { it.hasPlayer(authenticationRepository.user?.uid) && it.war?.isOfficial.isTrue })
                    }
                    1 -> when {
                        filters.contains(WarFilterType.WEEK) -> filteredWars.addAll(wars.filter { it.isThisWeek })
                        filters.contains(WarFilterType.OFFICIAL) -> filteredWars.addAll(wars.filter { it.war?.isOfficial.isTrue })
                        filters.contains(WarFilterType.PLAY) -> filteredWars.addAll(wars.filter { it.hasPlayer(authenticationRepository.user?.uid) })
                    }
                    else -> filteredWars.addAll(wars)
                }
                when (_sharedSortTypeSelected.value) {
                    WarSortType.DATE -> filteredWars.sortByDescending { it.war?.mid }
                    WarSortType.TEAM -> filteredWars.sortBy { it.name }
                    WarSortType.SCORE -> filteredWars.sortByDescending { it.scoreHost }
                }
                _sharedWars.emit(filteredWars)
                _sharedFilterList.emit(filters)
            }.launchIn(viewModelScope)

        onItemClick.bind(_sharedWarClick, viewModelScope)

    }

}