package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.repository.PreferencesRepository
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class Stats(
    val warStats: WarStats,
    val mostPlayedTeam: TeamStats?,
    val warScores: List<WarScore>,
    val maps: List<TrackStats>,
    val averageForMaps: List<TrackStats>,
) {
     val highestScore: WarScore? = warScores.maxByOrNull { it.score }
     val lowestScore: WarScore? = warScores.minByOrNull { it.score }
     val bestMap: TrackStats? = averageForMaps.maxByOrNull { it.score }
     val worstMap: TrackStats? = averageForMaps.minByOrNull { it.score }
     val mostPlayedMap: TrackStats? = averageForMaps.maxByOrNull { it.totalPlayed }
     val lessPlayedMap: TrackStats? = averageForMaps.minByOrNull { it.totalPlayed }
     val averagePoints: Int = warScores.map { it.score }.sum() / warScores.count()
     val averageMapPoints: Int = (maps.map { it.score }.sum() / maps.size)
     val averagePlayerMapPoints: Int = averageMapPoints.pointsToPosition()
 }

class WarScore(
    val war: MKWar,
    val score: Int
) {
    val opponentLabel = "vs ${war.name?.split('-')?.lastOrNull()?.trim()}"
}

data class TrackStats(
    val map: Maps? = null,
    val trackIndex: Int? = null,
    val score: Int,
    val totalPlayed: Int = 0
)

class TeamStats(val teamName: String?, totalPlayed: Int?) {
    val totalPlayedLabel = "$totalPlayed matchs joués"
}

class WarStats(list : List<MKWar>) {
    val warsPlayed = list.count()
    val warsWon = list.filterNot { war -> war.displayedDiff.contains('-') }.count()
    val winRate = "${((warsWon*100) / warsPlayed)} %"
    val highestVictory = list.maxByOrNull { war -> war.scoreHost }.takeIf { it?.displayedDiff?.contains("+").isTrue }
    val loudestDefeat = list.minByOrNull { war -> war.scoreHost }.takeIf { it?.displayedDiff?.contains("-").isTrue }
}

class MapDetails(
    val war: MKWar,
    val warTrack: MKWarTrack
)

@FlowPreview
@ExperimentalCoroutinesApi
class MapStats(
    val list: List<MapDetails>,
    preferencesRepository: PreferencesRepositoryInterface
) {
    private val playerScoreList = list
        .filter { pair -> pair.war.hasPlayer(preferencesRepository.currentUser?.mid) }
        .mapNotNull { it.warTrack.track?.warPositions }
        .map { it.singleOrNull { it.playerId == preferencesRepository.currentUser?.mid } }
        .mapNotNull { it?.position.positionToPoints() }

    val trackPlayed = list.size
    val trackWon = list.filter { pair -> pair.warTrack.displayedDiff.contains('+')}.size
    val winRate = "${(trackWon*100) / trackPlayed} %"
    val teamScore = list.map { pair -> pair.warTrack }.map { it.teamScore }.sum() / list.size
    val playerScore = playerScoreList.takeIf { it.isNotEmpty() }?.let {  (playerScoreList.sum() / playerScoreList.size).pointsToPosition() } ?: 0
    val highestVictory = list.getVictory()
    val loudestDefeat = list.getDefeat()

}