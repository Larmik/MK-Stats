package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.entities.TeamEntity
import fr.harmoniamk.statsmk.extension.toTeamColor
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.Team

@Composable
fun MKTeamItem(team: Team? = null, teamToManage: Team? = null, editVisible: Boolean = false, teamRanking: OpponentRankingItemViewModel? = null, isVertical: Boolean = false, onClick: (String) -> Unit, onEditClick: (String) -> Unit) {
    val finalTeam = team ?: teamToManage ?: teamRanking?.team
    val teamId = team?.mid ?: teamRanking?.team?.mid
    Card(
        Modifier
            .padding(5.dp)
            .clickable { teamId?.let { onClick(it) } }, backgroundColor = colorResource(id = R.color.white_alphaed)) {
                Row(modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    finalTeam?.shortName?.let {
                        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.defaultMinSize(minWidth = 70.dp).background(color = finalTeam.teamColor.toTeamColor(), shape = RoundedCornerShape(5.dp))) {
                            MKText(text = it, fontSize = 16, font = R.font.montserrat_bold, textColor = R.color.white, modifier = Modifier.padding(5.dp))
                        }
                    }
                    finalTeam?.name?.let { MKText(text = it, font = R.font.montserrat_bold, fontSize = 18, modifier = Modifier.weight(1f)) }
                    teamToManage?.takeIf { editVisible }?.let {
                        Image(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = null,
                            modifier = Modifier
                                .size(25.dp)
                                .clickable { onEditClick(it.mid) }
                        )
                    }
                    teamRanking?.let {
                        Row {
                            Column {
                                MKText(text = R.string.wars_jou_es, fontSize = 12)
                                MKText(text = R.string.winrate, fontSize = 12)
                                MKText(text = R.string.score_moyen, fontSize = 12)
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Column {
                                MKText(text = it.warsPlayedLabel, font = R.font.montserrat_bold, fontSize = 12)
                                MKText(text = it.winrateLabel, font = R.font.montserrat_bold, fontSize = 12)
                                MKText(text = it.averageLabel, font = R.font.montserrat_bold, fontSize = 12)
                            }
                        }
                    }
        }
    }
}

@Composable
@Preview
fun MKTeamItemPreview() {
    MKTeamItem(onEditClick = {}, onClick = {}, team = Team(
        TeamEntity(
            mid = "mid",
            name = "Harmonia",
            shortName = "HR"
        )
    ))
}