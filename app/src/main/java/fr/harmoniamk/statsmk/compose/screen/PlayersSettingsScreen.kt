package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKCPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.PlayerSettingsViewModel
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable

fun PlayersSettingsScreen(
    viewModel: PlayerSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPlayerClick: (String) -> Unit
) {
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val players by viewModel.sharedPlayers.collectAsState()
    val isLoading by viewModel.sharedLoader.collectAsState()

    BackHandler { onBack() }
    MKBaseScreen(title = R.string.joueurs) {
        MKTextField(
            value = searchState.value,
            onValueChange = {
                searchState.value = it
                viewModel.onSearch(it.text)
            },
            placeHolderRes = R.string.rechercher_un_joueur
        )
        when (isLoading) {
            true -> CircularProgressIndicator(
                modifier = Modifier.padding(vertical = 10.dp), color = colorResource(
                    id = R.color.harmonia_dark
                )
            )
            else ->   LazyColumn {
                items(items = players) {
                    MKCPlayerItem(player = MKCLightPlayer(it), onPlayerClick = onPlayerClick)
                }
            }
        }

    }
}