package fr.harmoniamk.statsmk.fragment.stats.playerStatsDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.WarFilterType
import fr.harmoniamk.statsmk.enums.WarSortType
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PlayerStatsDetailsViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
): ViewModel() {

    private val filters = mutableListOf<WarFilterType>()

    private val _sharedWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedSortTypeSelected = MutableStateFlow(WarSortType.DATE)
    private val _sharedFilterList = MutableSharedFlow<List<WarFilterType>>()
    private val _sharedLoaded = MutableSharedFlow<Unit>()

    val sharedWars = _sharedWars.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asStateFlow()
    val sharedFilterList = _sharedFilterList.asSharedFlow()
    val sharedLoaded = _sharedLoaded.asSharedFlow()

    private val wars = mutableListOf<MKWar>()

    fun bind(list: List<MKWar>, onSortClick: Flow<WarSortType>, onFilterClick: Flow<WarFilterType>, onItemClick: Flow<MKWar>) {
        list.withName(databaseRepository)
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
                _sharedLoaded.emit(Unit)
            }
            .launchIn(viewModelScope)

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