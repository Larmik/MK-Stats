package fr.harmoniamk.statsmk.features.warDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WarDetailsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedWarPlayers = MutableSharedFlow<List<Pair<String?, Int>>>()
    private val _sharedTracks = MutableSharedFlow<List<MKWarTrack>>()
    private val _sharedBestTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedWorstTrack = MutableSharedFlow<MKWarTrack>()
    private val _sharedTrackClick = MutableSharedFlow<Pair<Int, NewWarTrack>>()

    val sharedWarPlayers = _sharedWarPlayers.asSharedFlow()
    val sharedTracks = _sharedTracks.asSharedFlow()
    val sharedBestTrack = _sharedBestTrack.asSharedFlow()
    val sharedWorstTrack = _sharedWorstTrack.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()

    fun bind(warId: String?, onTrackClick: Flow<Pair<Int, NewWarTrack>>) {
        warId?.let { id ->
            firebaseRepository.getNewWar(id)
                .mapNotNull { it?.warTracks?.map { MKWarTrack(it) } }
                .onEach {
                    val positions = mutableListOf<Pair<String?, Int>>()
                    _sharedTracks.emit(it)
                    _sharedBestTrack.emit(it.sortedByDescending { track -> track.teamScore }.first())
                    _sharedWorstTrack.emit(it.sortedBy { track -> track.teamScore }.first())
                    it.forEach {
                        val trackPositions = it.track?.warPositions?.groupBy { pos -> pos.playerId }
                        trackPositions?.entries?.forEach { entry ->
                            positions.add(Pair(entry.key, entry.value.map { pos -> pos.position }.sum()))
                        }
                    }
                    _sharedWarPlayers.emit(positions.groupBy { it.first }.map { Pair(it.key, it.value.map { it.second }.sum()) })
                }.launchIn(viewModelScope)
            onTrackClick.bind(_sharedTrackClick, viewModelScope)
        }
    }

}