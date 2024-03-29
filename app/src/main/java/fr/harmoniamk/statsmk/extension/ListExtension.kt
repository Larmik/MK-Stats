package fr.harmoniamk.statsmk.extension

import android.util.Log
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.*
import fr.harmoniamk.statsmk.model.local.*
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlin.collections.count
import kotlin.collections.map

fun <T> List<T>.safeSubList(from: Int, to: Int): List<T> = when {
    this.size < to -> this
    to < from -> listOf()
    else -> this.subList(from, to)
}

fun Any?.toMapList(): List<Map<*,*>>? = this as? List<Map<*,*>>
fun Any?.toStringList(): List<String>? = this as? List<String>

fun List<MKWar>.withFullStats(databaseRepository: DatabaseRepositoryInterface, userId: String? = null, teamId: String? = null, isIndiv: Boolean = false) = flow {
    Log.d("MKDebugOnly", "ListExtension withFullStats: userId = $userId, teamId = $teamId, isIndiv= $isIndiv")
    val maps = mutableListOf<TrackStats>()
    val warScores = mutableListOf<WarScore>()
    val averageForMaps = mutableListOf<TrackStats>()
    val wars = when  {
        isIndiv && userId != null && teamId != null -> this@withFullStats.filter { it.hasPlayer(userId) && it.hasTeam(teamId) }
        isIndiv && userId != null -> this@withFullStats.filter { it.hasPlayer(userId) }
        teamId != null -> this@withFullStats.filter { it.hasTeam(teamId) }
        else -> this@withFullStats
    }

    val mostPlayedTeamId = wars
        .asSequence()
        .groupBy { it.war?.teamOpponent }
        .toList().maxByOrNull { it.second.size }

    val mostDefeatedTeamId = wars
        .asSequence()
        .filterNot { it.displayedDiff.contains('-') }
        .groupBy { it.war?.teamOpponent }
        .toList().maxByOrNull { it.second.size }

    val lessDefeatedTeamId = wars
        .asSequence()
        .filter { it.displayedDiff.contains('-') }
        .groupBy { it.war?.teamOpponent }
        .toList().maxByOrNull { it.second.size }

    wars.map { Pair(it, it.war?.warTracks?.map { MKWarTrack(it) }) }
        .forEach {
            var currentPoints = 0
            it.second?.forEach { track ->
                val playerScoreForTrack = track.track?.warPositions
                    ?.singleOrNull { pos -> pos.playerId == userId }
                    ?.position.positionToPoints()
                var teamScoreForTrack = 0
                track.track?.warPositions?.map { it.position.positionToPoints() }?.forEach {
                    teamScoreForTrack += it
                }
                currentPoints += when (isIndiv) {
                    true -> playerScoreForTrack
                    else -> teamScoreForTrack
                }
                var shockCount = 0
                track.track?.shocks?.filter { (!isIndiv || userId == null) || it.playerId == userId }?.map { it.count }?.forEach {
                    shockCount += it
                }
                maps.add(TrackStats(trackIndex = track.track?.trackIndex, teamScore = teamScoreForTrack, playerScore = playerScoreForTrack, shockCount = shockCount))
            }
            warScores.add(WarScore(it.first, currentPoints))
            currentPoints = 0
        }
    maps.groupBy { it.trackIndex }
        .filter { it.value.isNotEmpty() }
        .forEach { entry ->
            val stats = TrackStats(
                map = Maps.values()[entry.key ?: -1],
                teamScore = (entry.value.map { it.teamScore }.sum() / entry.value.map { it.teamScore }.count()),
                playerScore = (entry.value.map { it.playerScore }.sum() / entry.value.map { it.playerScore }.count()),
                totalPlayed = entry.value.size
            )
            averageForMaps.add(stats)
        }

    val mostPlayedTeamData = databaseRepository.getTeam(mostPlayedTeamId?.first)
        .mapNotNull { TeamStats(it, mostPlayedTeamId?.second?.size) }
        .firstOrNull()
    val mostDefeatedTeamData = databaseRepository.getTeam(mostDefeatedTeamId?.first)
        .mapNotNull { TeamStats(it, mostDefeatedTeamId?.second?.size) }
        .firstOrNull()
    val lessDefeatedTeamData = databaseRepository.getTeam(lessDefeatedTeamId?.first)
        .mapNotNull { TeamStats(it, lessDefeatedTeamId?.second?.size) }
        .firstOrNull()

    val newStats = Stats(
        warStats = WarStats(wars),
        warScores = warScores,
        maps = maps,
        averageForMaps = averageForMaps,
        mostPlayedTeam = mostPlayedTeamData,
        mostDefeatedTeam = mostDefeatedTeamData,
        lessDefeatedTeam = lessDefeatedTeamData
    )
    emit(newStats)
}
fun List<MKWar?>.withName(databaseRepository: DatabaseRepositoryInterface) = flow {
    val temp = mutableListOf<MKWar>()
    Log.d("MKDebugOnly", "ListExtension withName: ")
    this@withName.forEach { war ->
        war?.let {
            val hostName = databaseRepository.getTeam(it.war?.teamHost).firstOrNull()?.shortName
            val opponentName = databaseRepository.getTeam(it.war?.teamOpponent).firstOrNull()?.shortName
            temp.add(it.apply { this.name = "$hostName - $opponentName" })
        }
    }
    emit(temp)
}

fun WarDispo.withLineUpAndOpponent(databaseRepository: DatabaseRepositoryInterface) = flow {
    this@withLineUpAndOpponent.opponentId?.takeIf { it != "null" }.let { id ->
        val opponentName = databaseRepository.getTeam(id).firstOrNull()?.name
        val lineupNames = mutableListOf<String?>()
        var hostName: String? = null
        this@withLineUpAndOpponent.lineUp?.forEach {
            val playerName = databaseRepository.getUser(it.userId).firstOrNull()?.name
            lineupNames.add(playerName)
        }
        this@withLineUpAndOpponent.host?.takeIf { it != "null" }?.let {
             hostName = when (this@withLineUpAndOpponent.host?.contains("-")) {
                true -> this@withLineUpAndOpponent.host
                else -> databaseRepository.getUser(this@withLineUpAndOpponent.host).firstOrNull()?.name
            }
        }
        emit(this@withLineUpAndOpponent.apply {
            this.lineupNames = lineupNames.filterNotNull()
            this.opponentName = opponentName
            this.hostName = hostName
        })
    }
}
fun NewWar?.withName(databaseRepository: DatabaseRepositoryInterface) = flow {
    this@withName?.let {
        val hostName = databaseRepository.getTeam(it.teamHost).firstOrNull()?.shortName
        val opponentName = databaseRepository.getTeam(it.teamOpponent).firstOrNull()?.shortName
        emit(MKWar(it).apply { this.name = "$hostName - $opponentName" })
    }
}

fun List<Team>.withFullTeamStats(wars: List<MKWar>?, databaseRepository: DatabaseRepositoryInterface, userId: String? = null, weekOnly: Boolean = false, monthOnly: Boolean = false, isIndiv: Boolean = false) = flow {
    val temp = mutableListOf<OpponentRankingItemViewModel>()
    Log.d("MKDebugOnly", "ListExtension withFullTeamStats:  wars = ${wars?.map { it.name }}, weekOnly = $weekOnly, monthOnly = $monthOnly, isIndiv= $isIndiv")
    this@withFullTeamStats.forEach { team ->
        wars
            ?.filter { (weekOnly && it.isThisWeek) || !weekOnly }
            ?.filter { (monthOnly && it.isThisMonth) || !monthOnly }
            ?.withFullStats(databaseRepository, teamId = team.mid, userId = userId, isIndiv = isIndiv)?.first()
            ?.let {
                if (it.warStats.list.isNotEmpty())
                    temp.add(OpponentRankingItemViewModel(team, it, userId, isIndiv))
            }
    }
    emit(temp)
}

fun List<Penalty>.withTeamName(databaseRepository: DatabaseRepositoryInterface) = flow {
    val temp = mutableListOf<Penalty>()
    this@withTeamName.forEach {
        val teamName = databaseRepository.getTeam(it.teamId).firstOrNull()?.name
        temp.add(it.apply { this.teamName = teamName })
    }
    emit(temp)
}

fun List<Int?>?.sum(): Int {
    var sum = 0
    this?.filterNotNull()?.forEach { sum += it }
    return sum
}

fun List<Map<*,*>>?.parseTracks() : List<NewWarTrack>? =
    this?.map { track ->
        NewWarTrack(
            mid = track["mid"].toString(),
            trackIndex = track["trackIndex"].toString().toInt(),
            warPositions = (track["warPositions"]?.toMapList())
                ?.map {
                    NewWarPositions(
                        mid = it["mid"].toString(),
                        playerId = it["playerId"].toString(),
                        position = it["position"].toString().toInt()
                    )
                },
            shocks = (track["shocks"]?.toMapList())
                ?.map {
                    Shock(
                        playerId = it["playerId"].toString(),
                        count = it["count"].toString().toInt()
                    )
                }
        )

}

fun List<Map<*,*>>?.parsePenalties() : List<Penalty>? =
    this?.map { item ->
        Penalty(
            teamId = item["teamId"].toString(),
            amount = item["amount"].toString().toInt()
        )
}
fun List<Map<*,*>>?.parsePlayerDispos() : List<PlayerDispo>? =
    this?.map { item ->
        PlayerDispo(
            players = item["players"].toStringList(),
            dispo =item["dispo"].toString().toInt()
        )
}

fun List<Map<*,*>>?.parseLineUp() : List<LineUp>? =
    this?.map { item ->
        LineUp(
            userId = item["userId"].toString(),
            userDiscordId =item["userDiscordId"].toString()
        )
}

fun List<MapDetails>.getVictory() = this.maxByOrNull { it.warTrack.teamScore }?.takeIf { it.warTrack.displayedDiff.contains('+') }
fun List<MapDetails>.getDefeat() = this.minByOrNull { it.warTrack.teamScore }?.takeIf { it.warTrack.displayedDiff.contains('-') }