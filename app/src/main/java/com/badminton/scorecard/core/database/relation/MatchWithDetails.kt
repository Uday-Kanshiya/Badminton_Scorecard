package com.badminton.scorecard.core.database.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.badminton.scorecard.core.database.entity.MatchEntity
import com.badminton.scorecard.core.database.entity.MatchEventEntity
import com.badminton.scorecard.core.database.entity.MatchPlayerCrossRef
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.SetEntity

data class MatchWithPlayers(
    @Embedded val match: MatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "matchId"
    )
    val crossRefs: List<MatchPlayerCrossRef>
)

data class MatchWithSets(
    @Embedded val match: MatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "matchId"
    )
    val sets: List<SetEntity>
)

data class SetWithEvents(
    @Embedded val set: SetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "setId"
    )
    val events: List<MatchEventEntity>
)

data class FullMatchDetails(
    @Embedded val match: MatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MatchPlayerCrossRef::class,
            parentColumn = "matchId",
            entityColumn = "playerId"
        )
    )
    val players: List<PlayerEntity>,
    @Relation(
        entity = SetEntity::class,
        parentColumn = "id",
        entityColumn = "matchId"
    )
    val sets: List<SetWithEvents>
)
