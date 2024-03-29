package fr.harmoniamk.statsmk.fragment.stats.periodicStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.model.local.*
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class PeriodicStatsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    private val _sharedTrackClick = MutableSharedFlow<Pair<String?, Int>>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedWeekStatsEnabled = MutableStateFlow(true)
    private val _sharedStats = MutableSharedFlow<Stats>()
    private val _sharedTeamClick = MutableSharedFlow<Pair<String?, OpponentRankingItemViewModel>>()

    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()
    val sharedWeekStatsEnabled = _sharedWeekStatsEnabled.asStateFlow()
    val sharedTeamClick = _sharedTeamClick.asSharedFlow()

    private var bestMap: TrackStats? = null
    private var worstMap: TrackStats? = null
    private var mostPlayedMap: TrackStats? = null
    private var highestVicory: MKWar? = null
    private var loudestDefeat: MKWar? = null
    private var mostPlayedTeam: OpponentRankingItemViewModel? = null
    private var mostDefeatedTeam: OpponentRankingItemViewModel? = null
    private var lessDefeatedTeam: OpponentRankingItemViewModel? = null

    fun bind(
        onBestClick: Flow<Unit>,
        onWorstClick: Flow<Unit>,
        onMostPlayedClick: Flow<Unit>,
        onVictoryClick: Flow<Unit>,
        onDefeatClick: Flow<Unit>,
        onWeekStatsSelected: Flow<Boolean>,
        onMostPlayedTeamClick: Flow<Unit>,
        onMostDefeatedTeamClick: Flow<Unit>,
        onLessDefeatedTeamClick: Flow<Unit>
    ) {

        refresh(hebdo = _sharedWeekStatsEnabled.value)
        flowOf(
            onBestClick.mapNotNull { bestMap },
            onWorstClick.mapNotNull { worstMap },
            onMostPlayedClick.mapNotNull { mostPlayedMap },
        ).flattenMerge()
            .map { Maps.values().indexOf(it.map) }
            .map { Pair(authenticationRepository.user?.uid, it) }
            .bind(_sharedTrackClick, viewModelScope)

        flowOf(onMostPlayedTeamClick.mapNotNull { mostPlayedTeam }, onMostDefeatedTeamClick.mapNotNull { mostDefeatedTeam }, onLessDefeatedTeamClick.mapNotNull { lessDefeatedTeam })
            .flattenMerge()
            .map { Pair(authenticationRepository.user?.uid, it) }
            .bind(_sharedTeamClick, viewModelScope)

        flowOf(onVictoryClick.mapNotNull { highestVicory }, onDefeatClick.mapNotNull { loudestDefeat })
            .flattenMerge()
            .bind(_sharedWarClick, viewModelScope)

        onWeekStatsSelected.onEach { weekEnabled ->
            _sharedWeekStatsEnabled.emit(weekEnabled)
            refresh(weekEnabled)
        }.launchIn(viewModelScope)
    }

    private fun refresh(hebdo: Boolean) {
        val warList = mutableListOf<MKWar>()
        databaseRepository.getWars()
            .map { it.filter { war -> war.hasTeam(preferencesRepository.currentTeam?.mid) } }
            .filterNot { it.isEmpty() }
            .mapNotNull { wars -> wars.filter {
                when (hebdo) {
                    true -> it.isThisWeek
                    else -> it.isThisMonth
                }
            } }
            .onEach { warList.addAll(it) }
            .flatMapLatest { it.withName(databaseRepository) }
            .flatMapLatest { it.withFullStats(databaseRepository) }
            .onEach { stats ->
                bestMap = stats.bestMap
                worstMap = stats.worstMap
                mostPlayedMap = stats.mostPlayedMap
                highestVicory = stats.warStats.highestVictory
                loudestDefeat = stats.warStats.loudestDefeat
                val teamStats = listOfNotNull(
                    stats.mostPlayedTeam?.team,
                    stats.mostDefeatedTeam?.team,
                    stats.lessDefeatedTeam?.team
                ).withFullTeamStats(warList, databaseRepository, authenticationRepository.user?.uid, isIndiv = true).first()
                mostPlayedTeam = teamStats.getOrNull(0)
                mostDefeatedTeam = teamStats.getOrNull(1)
                lessDefeatedTeam = teamStats.getOrNull(2)
                _sharedStats.emit(stats)
            }
            .launchIn(viewModelScope)

    }

}