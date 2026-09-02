package com.badminton.scorecard.core.rules

/**
 * Type of badminton match: Singles (1v1) or Doubles (2v2).
 */
enum class MatchType { SINGLES, DOUBLES }

/**
 * Identifies which team is which.
 */
enum class TeamSide { TEAM_A, TEAM_B }

/**
 * Indicates the court side for serving/receiving.
 */
enum class CourtSide { RIGHT, LEFT }

/**
 * The current status of the match.
 */
enum class MatchStatus { IN_PROGRESS, COMPLETED, PAUSED, ABANDONED }
