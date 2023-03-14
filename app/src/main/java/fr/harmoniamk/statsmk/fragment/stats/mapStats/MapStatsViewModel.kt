package fr.harmoniamk.statsmk.fragment.stats.mapStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.MapDetails
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
@FlowPreview
class MapStatsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedMapClick = MutableSharedFlow<MapDetails>()
    private val _sharedStats = MutableSharedFlow<MapStats>()

    val sharedMapClick = _sharedMapClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()



    fun bind(trackIndex: Int,
             onMapClick: Flow<MapDetails>,
             onVictoryClick: Flow<Unit>,
             onDefeatClick: Flow<Unit>,
             isIndiv: Boolean?,
             isWeek: Boolean?,
             isMonth: Boolean?
    ) {
        val mapDetailsList = mutableListOf<MapDetails>()
        val onlyIndiv = isIndiv.isTrue || preferencesRepository.currentTeam?.mid == null

         firebaseRepository.getNewWars()
             .filter {
                 (!onlyIndiv && it.mapNotNull { war -> war.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                         || it.map {war -> war.teamOpponent}.contains(preferencesRepository.currentTeam?.mid))
                         || onlyIndiv
             }
             .mapNotNull { list -> list
                 .map { MKWar(it) }
                 .filter { it.isOver }
                 .filter {  !onlyIndiv || (onlyIndiv && it.hasPlayer(authenticationRepository.user?.uid)) }
                 .filter {  !isWeek.isTrue || (isWeek.isTrue && it.isThisWeek) }
                 .filter {  !isMonth.isTrue || (isMonth.isTrue && it.isThisMonth) }
              }
             .map {
                val finalList = mutableListOf<MapDetails>()
                 it.withName(firebaseRepository).firstOrNull()?.let { list ->
                     list.forEach { mkWar ->
                         mkWar.warTracks?.filter { track -> track.index == trackIndex }?.forEach { track ->
                             val position = track.track?.warPositions?.singleOrNull { it.playerId == authenticationRepository.user?.uid }?.position?.takeIf { isIndiv.isTrue }
                             finalList.add(MapDetails(mkWar, MKWarTrack(track.track), position))
                         }
                     }
                 }
                finalList
            }
            .filter { it.isNotEmpty() }
            .onEach {
                mapDetailsList.clear()
                mapDetailsList.addAll(it.filter { !onlyIndiv || (onlyIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(authenticationRepository.user?.uid) }.isTrue) })
                _sharedStats.emit(MapStats(mapDetailsList, onlyIndiv, authenticationRepository.user?.uid))
            }.launchIn(viewModelScope)

        flowOf(
            onMapClick,
            onVictoryClick.mapNotNull { mapDetailsList.getVictory() },
            onDefeatClick.mapNotNull { mapDetailsList.getDefeat() }
        )
            .flattenMerge()
            .bind(_sharedMapClick, viewModelScope)

    }

}