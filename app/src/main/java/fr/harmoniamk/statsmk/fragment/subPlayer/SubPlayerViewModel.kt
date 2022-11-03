package fr.harmoniamk.statsmk.fragment.subPlayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class SubPlayerViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedCurrentPlayers = MutableSharedFlow<List<UserSelector>>()
    private val _sharedOtherPlayers = MutableSharedFlow<List<UserSelector>>()
    private val _sharedDismissDialog = MutableSharedFlow<Unit>()

    val sharedCurrentPlayers = _sharedCurrentPlayers.asSharedFlow()
    val sharedOtherPlayers = _sharedOtherPlayers.asSharedFlow()
    val sharedDismissDialog = _sharedDismissDialog.asSharedFlow()

    fun bind(onSubClick: Flow<Unit>, onCancel: Flow<Unit>, onOldPlayerSelect: Flow<User>, onNewPlayerSelect: Flow<User>) {
        var oldPlayer: User? = null
        var newPlayer: User? = null
        firebaseRepository.getUsers()
            .onEach {
                _sharedCurrentPlayers.emit(it.filter { user -> user.currentWar == preferencesRepository.currentWar?.mid }.map { UserSelector(user = it, isSelected = false) } )
                _sharedOtherPlayers.emit(it.filter { user -> user.currentWar == "-1" }.map { UserSelector(user = it, isSelected = false) } )
            }.launchIn(viewModelScope)
        onOldPlayerSelect.onEach { oldPlayer = it }.launchIn(viewModelScope)
        onNewPlayerSelect.onEach { newPlayer = it }.launchIn(viewModelScope)

        onSubClick
            .mapNotNull { oldPlayer?.copy(currentWar = "-1") }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .mapNotNull { newPlayer?.copy(currentWar = preferencesRepository.currentWar?.mid) }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .bind(_sharedDismissDialog, viewModelScope)
        onCancel.bind(_sharedDismissDialog, viewModelScope)
    }

}