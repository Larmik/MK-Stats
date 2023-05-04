package fr.harmoniamk.statsmk.fragment.stats.mapRanking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.application.MainApplication
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.enums.TrackSortType
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.TrackStats
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class MapRankingViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {


    private val _sharedMaps = MutableSharedFlow<List<TrackStats>>()
    private val _sharedGoToStats = MutableSharedFlow<Pair<String?, Int>>()
    private val _sharedSortTypeSelected = MutableStateFlow(TrackSortType.TOTAL_PLAYED)
    private val _sharedIndivStatsEnabled = MutableStateFlow(true)


    val sharedMaps = _sharedMaps.asSharedFlow()
    val sharedGoToStats = _sharedGoToStats.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asStateFlow()
    val sharedIndivStatsEnabled = _sharedIndivStatsEnabled.asStateFlow()
    private val temp = mutableListOf<NewWarTrack>()
    val final = mutableListOf<TrackStats>()

    fun bind(warList: List<MKWar>, onTrackClick: Flow<Int>, onSortClick: Flow<TrackSortType>, onSearch: Flow<String>, onIndivStatsSelected: Flow<Boolean>) {
        val onlyIndiv = preferencesRepository.currentTeam?.mid == null
        flowOf(warList)
            .onEach { delay(100) }
            .filter {
                (!onlyIndiv && it.mapNotNull { war -> war.war?.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                        || it.map {war -> war.war?.teamOpponent}.contains(preferencesRepository.currentTeam?.mid))
                        || onlyIndiv
            }
            .mapNotNull { list -> list.filter { it.isOver && (!onlyIndiv || (onlyIndiv && it.hasPlayer(authenticationRepository.user?.uid)))} }
            .map { list ->
                val allTracksPlayed = mutableListOf<NewWarTrack>()
                list.mapNotNull { it.war?.warTracks }.forEach {
                    allTracksPlayed.addAll(it)
                }
                allTracksPlayed
            }
            .map { list ->
                temp.clear()
                temp.addAll(list)
                final.clear()
                final.addAll(sortTracks(_sharedSortTypeSelected.value).map {
                    TrackStats(
                        map = Maps.values()[it.first ?: -1],
                        totalPlayed = it.second.size,
                        winRate = (it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100) / it.second.size
                    )
                }.filter { it.totalPlayed >= 2 })
                final
            }
            .bind(_sharedMaps, viewModelScope)

        onTrackClick.onEach {
            _sharedGoToStats.emit(Pair(authenticationRepository.user?.uid, it))
        }.launchIn(viewModelScope)
        onIndivStatsSelected.onEach { indivEnabled ->
            _sharedIndivStatsEnabled.emit(indivEnabled)
            final.clear()
            final.addAll(sortTracks(_sharedSortTypeSelected.value).map {
                TrackStats(
                    map = Maps.values()[it.first ?: -1],
                    totalPlayed = it.second.size,
                    winRate = (it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100) / it.second.size
                )
            }.filter { it.totalPlayed >= 2 && (!onlyIndiv ||(onlyIndiv && indivEnabled))})
            _sharedMaps.emit(final)
        }.launchIn(viewModelScope)

        onSearch
            .map { searched ->
                final
                    .filter {
                    it.map?.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue
                            || MainApplication.instance?.applicationContext?.getString(it.map?.label ?: R.string.app_name)?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
                }
            }
            .bind(_sharedMaps, viewModelScope)

        onSortClick
            .map {
                _sharedSortTypeSelected.emit(it)
                final.clear()
                final.addAll(sortTracks(it).map {
                    TrackStats(
                        map = Maps.values()[it.first ?: -1],
                        totalPlayed = it.second.size,
                        winRate = (it.second.filter {
                            MKWarTrack(it).displayedDiff.contains(
                                '+'
                            )
                        }.size * 100) / it.second.size
                    )
                }.filter { it.totalPlayed >= 2 })
                        final


            }.bind(_sharedMaps, viewModelScope)
    }

    private fun sortTracks(type: TrackSortType) =
        when (type) {
            TrackSortType.TOTAL_PLAYED -> temp
                .filter { !_sharedIndivStatsEnabled.value || (_sharedIndivStatsEnabled.value && MKWarTrack(it).hasPlayer(authenticationRepository.user?.uid)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.size }
            TrackSortType.TOTAL_WIN -> temp
                .filter { !_sharedIndivStatsEnabled.value || (_sharedIndivStatsEnabled.value && MKWarTrack(it).hasPlayer(authenticationRepository.user?.uid)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size }
            TrackSortType.WINRATE -> temp
                .filter { !_sharedIndivStatsEnabled.value || (_sharedIndivStatsEnabled.value && MKWarTrack(it).hasPlayer(authenticationRepository.user?.uid)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100 / it.second.size }
            TrackSortType.AVERAGE_DIFF -> temp
                .filter { !_sharedIndivStatsEnabled.value || (_sharedIndivStatsEnabled.value && MKWarTrack(it).hasPlayer(authenticationRepository.user?.uid)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.map { MKWarTrack(it).diffScore }.sum() / it.second.size }







    }


}