package fr.harmoniamk.statsmk.fragment.playerList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.fragment.settings.managePlayers.ManagePlayersItemViewModel
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@HiltViewModel
class PlayerListViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface) : ViewModel() {

    private val _sharedPlayerList = MutableSharedFlow<List<ManagePlayersItemViewModel>>()
    private val _sharedAddPlayerList = MutableSharedFlow<List<UserSelector>>()
    private val _sharedAddPlayer = MutableSharedFlow<Unit>()
    private val _sharedEdit = MutableSharedFlow<User>()
    private val _sharedEditName = MutableSharedFlow<User>()
    private val _sharedShowDialog = MutableSharedFlow<Boolean>()
    private val _sharedNewName = MutableSharedFlow<String?>()
    private val _sharedPlayerAdded = MutableSharedFlow<Unit>()
    private val _sharedAddToTeamButtonVisible = MutableSharedFlow<Boolean>()
    private val _sharedButtonVisible = MutableSharedFlow<Boolean>()


    val sharedPlayerList = _sharedPlayerList.asSharedFlow()
    val sharedAddPlayerList = _sharedAddPlayerList.asSharedFlow()
    val sharedAddPlayer = _sharedAddPlayer.asSharedFlow()
    val sharedEditName = _sharedEditName.asSharedFlow()
    val sharedNewName = _sharedNewName.asSharedFlow()
    val sharedPlayerAdded = _sharedPlayerAdded.asSharedFlow()
    val sharedAddToTeamButtonVisible = _sharedAddToTeamButtonVisible.asSharedFlow()
    val sharedButtonvisible = _sharedButtonVisible.asSharedFlow()


    private val players = mutableListOf<ManagePlayersItemViewModel>()
    private val allPlayers = mutableListOf<ManagePlayersItemViewModel>()
    private val playersToAdd = mutableListOf<UserSelector>()

    fun bind(onAdd: Flow<Unit>, onEdit: Flow<User>, onSearch: Flow<String>, onPlayerSelected: Flow<UserSelector>, onAddToTeam: Flow<Unit>) {
        refresh()
        onAdd.bind(_sharedAddPlayer, viewModelScope)
        onEdit.onEach {
            when (it.team == "-1") {
                true -> _sharedEditName.emit(it)
                else -> {
                    _sharedEdit.emit(it)
                    _sharedShowDialog.emit(true)
                }

            }
        }.launchIn(viewModelScope)
        onSearch.map { searched ->
            createPlayersList(modelList = allPlayers.filter { it.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue })}
            .onEach { _sharedAddPlayerList.emit(it.map { UserSelector(it.player) }) }
            .bind(_sharedPlayerList, viewModelScope)

        onPlayerSelected
            .onEach {
                when (it.isSelected) {
                    true -> playersToAdd.add(it)
                    else -> playersToAdd.remove(it)
                }
                _sharedAddToTeamButtonVisible.emit(playersToAdd.isNotEmpty())
            }.launchIn(viewModelScope)

        onAddToTeam
            .onEach {
                playersToAdd
                    .mapNotNull { it.user?.apply { this.team = preferencesRepository.currentTeam?.mid } }
                    .forEach {
                        firebaseRepository.writeUser(it).first()
                    }
                _sharedPlayerAdded.emit(Unit)
            }.launchIn(viewModelScope)
    }

    fun refresh() {
        allPlayers.clear()
        databaseRepository.getUsers()
            .map {
                val role = authenticationRepository.userRole.firstOrNull() ?: 0
                it.filter { user -> user.team == "-1" }
                    .map { ManagePlayersItemViewModel(
                        it, false, preferencesRepository, authenticationRepository, isConnected = networkRepository.networkAvailable
                    ) }
                    .filter { !it.hasAccount || (it.hasAccount && role >= UserRole.LEADER.ordinal)}

            }
            .onEach { allPlayers.addAll(it) }
            .onEach { _sharedAddPlayerList.emit(it.map { UserSelector(it.player) }) }
            .onEach { _sharedButtonVisible.emit(networkRepository.networkAvailable) }
            .bind(_sharedPlayerList, viewModelScope)
    }

    fun bindAddDialog(onPlayedAdded: Flow<Unit>) {
        onPlayedAdded.onEach { refresh() }.launchIn(viewModelScope)
    }

    private fun createPlayersList(list: List<User>? = null, modelList: List<ManagePlayersItemViewModel>? = null): List<ManagePlayersItemViewModel> {
        players.clear()
        players.add(ManagePlayersItemViewModel(isCategory = true, isConnected = networkRepository.networkAvailable))
        list?.let {
            players.addAll(list.map { ManagePlayersItemViewModel(player = it, preferencesRepository = preferencesRepository, authenticationRepository = authenticationRepository, isConnected = networkRepository.networkAvailable) }.filterNot { it.isAlly }.sortedBy { it.name })
            players.add(ManagePlayersItemViewModel(isCategory = true, isConnected = networkRepository.networkAvailable))
            players.addAll(list.map { ManagePlayersItemViewModel(player = it, preferencesRepository = preferencesRepository, authenticationRepository = authenticationRepository, isConnected = networkRepository.networkAvailable) }.filter { it.isAlly }.sortedBy { it.name })
        }
        modelList?.let {
            players.addAll(modelList.filterNot { it.isAlly }.sortedBy { it.name })
            players.add(ManagePlayersItemViewModel(isCategory = true, isConnected = networkRepository.networkAvailable))
            players.addAll(modelList.filter { it.isAlly }.sortedBy { it.name })
        }
        return players
    }

    fun bindDialog(user: User, onTextChange: Flow<String>, onValidate: Flow<Unit>, onDismiss: Flow<Unit>) {
            var name = user.name
            onTextChange.onEach { name = it }.launchIn(viewModelScope)
            onValidate
                .flatMapLatest { databaseRepository.getUser(user.mid) }
                .mapNotNull { it?.copy(name = name) }
                .flatMapLatest { firebaseRepository.writeUser(it) }
                .onEach { _sharedNewName.emit(name) }
                .launchIn(viewModelScope)
            onDismiss.mapNotNull { name }.bind(_sharedNewName, viewModelScope)




    }

}