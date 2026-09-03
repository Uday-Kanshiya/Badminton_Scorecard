package com.badminton.scorecard.core.sharing

import android.content.Context
import android.graphics.*
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import com.badminton.scorecard.core.rules.MatchType
import com.badminton.scorecard.core.rules.SetScore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

object ScorecardImageGenerator {

    private const val WIDTH = 1080
    private const val HEIGHT = 1440

    /**
     * Generates a broadcast-quality Match Summary scorecard image with EVERY graph and stat shown on the End Match Summary screen.
     */
    fun generateMatchSummaryCard(
        context: Context,
        winnerTeam: String,
        teamANames: List<String>,
        teamBNames: List<String>,
        setScores: List<SetScore>,
        matchType: MatchType,
        durationSeconds: Long = 0,
        scoreProgression: List<Pair<Int, Int>> = emptyList(),
        momentumData: List<Int> = emptyList(),
        teamAServePoints: Int = 0,
        teamAReturnPoints: Int = 0,
        teamBServePoints: Int = 0,
        teamBReturnPoints: Int = 0,
        teamALongestStreak: Int = 0,
        teamBLongestStreak: Int = 0,
        totalRallies: Int = 0,
        matchDuration: String = ""
    ): Bitmap {
        val cardHeight = 2280
        val bitmap = Bitmap.createBitmap(WIDTH, cardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, cardHeight.toFloat(),
                intArrayOf(Color.parseColor("#12161F"), Color.parseColor("#1A202C"), Color.parseColor("#0F131A")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), cardHeight.toFloat(), bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E2430") }

        // 1. Top Header Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, WIDTH.toFloat(), 0f,
                intArrayOf(Color.parseColor("#1B5E20"), Color.parseColor("#2E7D32"), Color.parseColor("#00796B")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(40f, 40f, (WIDTH - 40).toFloat(), 180f, 24f, 24f, headerPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 40f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("🏸 OFFICIAL MATCH REPORT", (WIDTH / 2).toFloat(), 110f, textPaint)

        val dateStr = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        textPaint.textSize = 22f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.parseColor("#E0E0E0")
        canvas.drawText(
            "$dateStr  •  ${if (matchType == MatchType.DOUBLES) "Doubles Championship" else "Singles Match"}",
            (WIDTH / 2).toFloat(),
            155f,
            textPaint
        )

        // 2. Winner Card
        val winnerRect = RectF(40f, 205f, (WIDTH - 40).toFloat(), 395f)
        canvas.drawRoundRect(winnerRect, 20f, 20f, cardBgPaint)

        val goldBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFC107")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(winnerRect, 20f, 20f, goldBorderPaint)

        textPaint.color = Color.parseColor("#FFC107")
        textPaint.textSize = 32f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.CENTER
        val winnerTitle = if (winnerTeam == "TEAM_A") "🏆 TEAM A VICTORIOUS" else "🏆 TEAM B VICTORIOUS"
        canvas.drawText(winnerTitle, (WIDTH / 2).toFloat(), 265f, textPaint)

        val winningNames = if (winnerTeam == "TEAM_A") teamANames.joinToString(" & ") else teamBNames.joinToString(" & ")
        textPaint.color = Color.WHITE
        textPaint.textSize = 44f
        textPaint.isFakeBoldText = true
        canvas.drawText(winningNames, (WIDTH / 2).toFloat(), 325f, textPaint)

        val losingNames = if (winnerTeam == "TEAM_A") teamBNames.joinToString(" & ") else teamANames.joinToString(" & ")
        textPaint.color = Color.parseColor("#90A4AE")
        textPaint.textSize = 24f
        textPaint.isFakeBoldText = false
        canvas.drawText("Defeated $losingNames", (WIDTH / 2).toFloat(), 368f, textPaint)

        // 3. Final Set Scores Table
        val scoresRect = RectF(40f, 415f, (WIDTH - 40).toFloat(), 675f)
        canvas.drawRoundRect(scoresRect, 20f, 20f, cardBgPaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 26f
        textPaint.isFakeBoldText = true
        canvas.drawText("SET SCORES", 70f, 460f, textPaint)

        // Table Header
        textPaint.color = Color.parseColor("#90A4AE")
        textPaint.textSize = 22f
        textPaint.isFakeBoldText = false
        canvas.drawText("TEAM", 70f, 505f, textPaint)

        val setStartX = 550f
        val colWidth = 150f
        val safeScores = if (setScores.isEmpty()) listOf(SetScore(1, 0, 0)) else setScores
        safeScores.forEachIndexed { index, _ ->
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("SET ${index + 1}", setStartX + index * colWidth, 505f, textPaint)
        }

        linePaint.color = Color.parseColor("#37474F")
        linePaint.strokeWidth = 2f
        canvas.drawLine(70f, 525f, (WIDTH - 70).toFloat(), 525f, linePaint)

        // Row Team A
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = if (winnerTeam == "TEAM_A") Color.WHITE else Color.parseColor("#CFD8DC")
        textPaint.textSize = 26f
        textPaint.isFakeBoldText = winnerTeam == "TEAM_A"
        canvas.drawText("Team A: " + teamANames.joinToString("/"), 70f, 575f, textPaint)

        safeScores.forEachIndexed { index, s ->
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = if (s.teamAScore > s.teamBScore) Color.parseColor("#00E676") else Color.WHITE
            textPaint.textSize = 30f
            textPaint.isFakeBoldText = s.teamAScore > s.teamBScore
            canvas.drawText(s.teamAScore.toString(), setStartX + index * colWidth, 575f, textPaint)
        }

        // Row Team B
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = if (winnerTeam == "TEAM_B") Color.WHITE else Color.parseColor("#CFD8DC")
        textPaint.textSize = 26f
        textPaint.isFakeBoldText = winnerTeam == "TEAM_B"
        canvas.drawText("Team B: " + teamBNames.joinToString("/"), 70f, 630f, textPaint)

        safeScores.forEachIndexed { index, s ->
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = if (s.teamBScore > s.teamAScore) Color.parseColor("#00E676") else Color.WHITE
            textPaint.textSize = 30f
            textPaint.isFakeBoldText = s.teamBScore > s.teamAScore
            canvas.drawText(s.teamBScore.toString(), setStartX + index * colWidth, 630f, textPaint)
        }

        // 4. GRAPH 1: Score Progression Chart (Real Data)
        val progRect = RectF(40f, 695f, (WIDTH - 40).toFloat(), 1075f)
        canvas.drawRoundRect(progRect, 20f, 20f, cardBgPaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 26f
        textPaint.isFakeBoldText = true
        canvas.drawText("📈 SCORE PROGRESSION", 70f, 740f, textPaint)

        // Legend: Team A (Indigo #5C6BC0) & Team B (Teal #26A69A)
        val legendPaintA = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5C6BC0") }
        canvas.drawCircle(WIDTH - 320f, 735f, 10f, legendPaintA)
        textPaint.textSize = 22f
        textPaint.color = Color.WHITE
        textPaint.isFakeBoldText = false
        canvas.drawText("Team A", WIDTH - 300f, 742f, textPaint)

        val legendPaintB = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#26A69A") }
        canvas.drawCircle(WIDTH - 180f, 735f, 10f, legendPaintB)
        canvas.drawText("Team B", WIDTH - 160f, 742f, textPaint)

        val gLeft = 100f
        val gRight = (WIDTH - 80).toFloat()
        val gTop = 780f
        val gBottom = 1030f
        val gWidth = gRight - gLeft
        val gHeight = gBottom - gTop

        // Grid lines (0, 7, 14, 21...)
        linePaint.color = Color.parseColor("#2C3545")
        linePaint.strokeWidth = 1.5f
        for (i in 0..3) {
            val y = gTop + (gHeight / 3) * i
            canvas.drawLine(gLeft, y, gRight, y, linePaint)
        }

        val maxPointsProg = if (scoreProgression.isNotEmpty()) {
            max(21, scoreProgression.maxOf { max(it.first, it.second) })
        } else {
            max(21, safeScores.maxOfOrNull { max(it.teamAScore, it.teamBScore) } ?: 21)
        }

        val strokeA = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5C6BC0")
            style = Paint.Style.STROKE
            strokeWidth = 5f
            strokeCap = Paint.Cap.ROUND
        }
        val strokeB = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26A69A")
            style = Paint.Style.STROKE
            strokeWidth = 5f
            strokeCap = Paint.Cap.ROUND
        }

        val pathA = Path()
        val pathB = Path()
        pathA.moveTo(gLeft, gBottom)
        pathB.moveTo(gLeft, gBottom)

        if (scoreProgression.isNotEmpty()) {
            val totalSteps = scoreProgression.size
            scoreProgression.forEachIndexed { i, pt ->
                val x = gLeft + (i.toFloat() / max(1, totalSteps - 1)) * gWidth
                val yA = gBottom - (pt.first.toFloat() / maxPointsProg) * gHeight
                val yB = gBottom - (pt.second.toFloat() / maxPointsProg) * gHeight
                pathA.lineTo(x, yA)
                pathB.lineTo(x, yB)
            }
        } else {
            val totalSets = safeScores.size
            var curX = gLeft
            val xStep = gWidth / max(1, totalSets * 4)
            safeScores.forEach { setScore ->
                val aRatio = setScore.teamAScore.toFloat() / maxPointsProg
                val bRatio = setScore.teamBScore.toFloat() / maxPointsProg
                for (step in 1..4) {
                    curX += xStep
                    val yA = gBottom - (aRatio * (step / 4f) * gHeight)
                    val yB = gBottom - (bRatio * (step / 4f) * gHeight)
                    pathA.lineTo(curX, yA)
                    pathB.lineTo(curX, yB)
                }
            }
        }
        canvas.drawPath(pathA, strokeA)
        canvas.drawPath(pathB, strokeB)

        textPaint.color = Color.parseColor("#90A4AE")
        textPaint.textSize = 18f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Start", gLeft, 1055f, textPaint)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Match Progression (Rallies)", gLeft + gWidth / 2, 1055f, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Final Point", gRight, 1055f, textPaint)

        // 5. GRAPH 2: Momentum Differential Chart (Real Data)
        val momRect = RectF(40f, 1095f, (WIDTH - 40).toFloat(), 1455f)
        canvas.drawRoundRect(momRect, 20f, 20f, cardBgPaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 26f
        textPaint.isFakeBoldText = true
        canvas.drawText("⚡ MATCH MOMENTUM", 70f, 1140f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = Color.parseColor("#90A4AE")
        textPaint.textSize = 20f
        textPaint.isFakeBoldText = false
        canvas.drawText("Point Differential (Lead)", (WIDTH - 70).toFloat(), 1140f, textPaint)

        val mLeft = 100f
        val mRight = (WIDTH - 80).toFloat()
        val mTop = 1180f
        val mBottom = 1410f
        val mWidth = mRight - mLeft
        val mHeight = mBottom - mTop
        val mCenterY = mTop + mHeight / 2

        // Draw Zero Centerline
        linePaint.color = Color.parseColor("#546E7A")
        linePaint.strokeWidth = 2f
        canvas.drawLine(mLeft, mCenterY, mRight, mCenterY, linePaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = Color.parseColor("#5C6BC0")
        textPaint.textSize = 16f
        canvas.drawText("+ Team A", mLeft - 10f, mTop + 20f, textPaint)
        textPaint.color = Color.parseColor("#26A69A")
        canvas.drawText("- Team B", mLeft - 10f, mBottom - 10f, textPaint)

        val realMomentum = if (momentumData.isNotEmpty()) momentumData else listOf(0, 1, 2, 1, -1, -2, 0, 2, 3)
        val maxDiff = max(3, realMomentum.map { abs(it) }.maxOrNull() ?: 3)
        val barCount = realMomentum.size
        val barWidth = max(2f, (mWidth / max(1, barCount)) - 1.5f)

        val barPaintA = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5C6BC0") }
        val barPaintB = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#26A69A") }

        realMomentum.forEachIndexed { idx, diff ->
            val x = mLeft + (idx.toFloat() / max(1, barCount)) * mWidth
            val h = (abs(diff).toFloat() / maxDiff) * (mHeight / 2 - 10f)
            if (diff >= 0) {
                canvas.drawRect(x, mCenterY - h, x + barWidth, mCenterY, barPaintA)
            } else {
                canvas.drawRect(x, mCenterY, x + barWidth, mCenterY + h, barPaintB)
            }
        }

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.parseColor("#90A4AE")
        textPaint.textSize = 18f
        canvas.drawText("Rally-by-Rally Momentum Swing", mLeft + mWidth / 2, 1435f, textPaint)

        // 6. GRAPH 3: Point Distribution (Serve vs Return)
        val distRect = RectF(40f, 1475f, (WIDTH - 40).toFloat(), 1820f)
        canvas.drawRoundRect(distRect, 20f, 20f, cardBgPaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 26f
        textPaint.isFakeBoldText = true
        canvas.drawText("🎯 POINT DISTRIBUTION (SERVE vs RETURN)", 70f, 1520f, textPaint)

        val totalA = max(1, teamAServePoints + teamAReturnPoints)
        val totalB = max(1, teamBServePoints + teamBReturnPoints)
        val aServePct = ((teamAServePoints.toFloat() / totalA) * 100).roundToInt()
        val aReturnPct = 100 - aServePct
        val bServePct = ((teamBServePoints.toFloat() / totalB) * 100).roundToInt()
        val bReturnPct = 100 - bServePct

        // Team A Column (Left Box)
        val colBoxA = RectF(70f, 1550f, (WIDTH / 2 - 20).toFloat(), 1790f)
        paint.color = Color.parseColor("#252A34")
        canvas.drawRoundRect(colBoxA, 14f, 14f, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#5C6BC0")
        textPaint.textSize = 24f
        textPaint.isFakeBoldText = true
        canvas.drawText("Team A Points (${teamAServePoints + teamAReturnPoints})", 90f, 1590f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 20f
        textPaint.isFakeBoldText = false
        canvas.drawText("Serve: $teamAServePoints pts ($aServePct%)", 90f, 1635f, textPaint)
        val barARect = RectF(90f, 1650f, (WIDTH / 2 - 40).toFloat(), 1666f)
        paint.color = Color.parseColor("#37474F")
        canvas.drawRoundRect(barARect, 8f, 8f, paint)
        val filledA = RectF(90f, 1650f, 90f + (barARect.width() * (aServePct / 100f)), 1666f)
        paint.color = Color.parseColor("#29B6F6")
        canvas.drawRoundRect(filledA, 8f, 8f, paint)

        textPaint.color = Color.WHITE
        canvas.drawText("Return: $teamAReturnPoints pts ($aReturnPct%)", 90f, 1720f, textPaint)
        val barA2Rect = RectF(90f, 1735f, (WIDTH / 2 - 40).toFloat(), 1751f)
        paint.color = Color.parseColor("#37474F")
        canvas.drawRoundRect(barA2Rect, 8f, 8f, paint)
        val filledA2 = RectF(90f, 1735f, 90f + (barA2Rect.width() * (aReturnPct / 100f)), 1751f)
        paint.color = Color.parseColor("#66BB6A")
        canvas.drawRoundRect(filledA2, 8f, 8f, paint)

        // Team B Column (Right Box)
        val colBoxB = RectF((WIDTH / 2 + 20).toFloat(), 1550f, (WIDTH - 70).toFloat(), 1790f)
        paint.color = Color.parseColor("#252A34")
        canvas.drawRoundRect(colBoxB, 14f, 14f, paint)

        textPaint.color = Color.parseColor("#26A69A")
        textPaint.textSize = 24f
        textPaint.isFakeBoldText = true
        canvas.drawText("Team B Points (${teamBServePoints + teamBReturnPoints})", (WIDTH / 2 + 40).toFloat(), 1590f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 20f
        textPaint.isFakeBoldText = false
        canvas.drawText("Serve: $teamBServePoints pts ($bServePct%)", (WIDTH / 2 + 40).toFloat(), 1635f, textPaint)
        val barBRect = RectF((WIDTH / 2 + 40).toFloat(), 1650f, (WIDTH - 90).toFloat(), 1666f)
        paint.color = Color.parseColor("#37474F")
        canvas.drawRoundRect(barBRect, 8f, 8f, paint)
        val filledB = RectF((WIDTH / 2 + 40).toFloat(), 1650f, (WIDTH / 2 + 40f) + (barBRect.width() * (bServePct / 100f)), 1666f)
        paint.color = Color.parseColor("#29B6F6")
        canvas.drawRoundRect(filledB, 8f, 8f, paint)

        textPaint.color = Color.WHITE
        canvas.drawText("Return: $teamBReturnPoints pts ($bReturnPct%)", (WIDTH / 2 + 40).toFloat(), 1720f, textPaint)
        val barB2Rect = RectF((WIDTH / 2 + 40).toFloat(), 1735f, (WIDTH - 90).toFloat(), 1751f)
        paint.color = Color.parseColor("#37474F")
        canvas.drawRoundRect(barB2Rect, 8f, 8f, paint)
        val filledB2 = RectF((WIDTH / 2 + 40).toFloat(), 1735f, (WIDTH / 2 + 40f) + (barB2Rect.width() * (bReturnPct / 100f)), 1751f)
        paint.color = Color.parseColor("#66BB6A")
        canvas.drawRoundRect(filledB2, 8f, 8f, paint)

        // 7. Key Stats Card
        val keyStatsRect = RectF(40f, 1840f, (WIDTH - 40).toFloat(), 2130f)
        canvas.drawRoundRect(keyStatsRect, 20f, 20f, cardBgPaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 26f
        textPaint.isFakeBoldText = true
        canvas.drawText("📊 KEY MATCH METRICS", 70f, 1885f, textPaint)

        fun drawStatTile(title: String, value: String, rect: RectF, colorCode: String) {
            paint.color = Color.parseColor("#252A34")
            canvas.drawRoundRect(rect, 12f, 12f, paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = Color.parseColor("#90A4AE")
            textPaint.textSize = 20f
            textPaint.isFakeBoldText = false
            canvas.drawText(title, rect.centerX(), rect.top + 40f, textPaint)

            textPaint.color = Color.parseColor(colorCode)
            textPaint.textSize = 34f
            textPaint.isFakeBoldText = true
            canvas.drawText(value, rect.centerX(), rect.top + 85f, textPaint)
        }

        val tileW = (WIDTH - 170f) / 2
        val tileH = 100f
        drawStatTile("Streak A", "$teamALongestStreak pts", RectF(70f, 1915f, 70f + tileW, 1915f + tileH), "#5C6BC0")
        drawStatTile("Streak B", "$teamBLongestStreak pts", RectF(WIDTH - 70f - tileW, 1915f, (WIDTH - 70).toFloat(), 1915f + tileH), "#26A69A")

        val durationLabel = if (matchDuration.isNotBlank()) matchDuration else "${durationSeconds / 60} min"
        drawStatTile("Total Rallies", "$totalRallies", RectF(70f, 2030f, 70f + tileW, 2030f + tileH), "#FFD54F")
        drawStatTile("Duration", durationLabel, RectF(WIDTH - 70f - tileW, 2030f, (WIDTH - 70).toFloat(), 2030f + tileH), "#00E676")

        // 8. Footer Branding
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.parseColor("#78909C")
        textPaint.textSize = 22f
        textPaint.isFakeBoldText = false
        canvas.drawText("Badminton Scorecard  •  Official Match Analytics & Report", (WIDTH / 2).toFloat(), (cardHeight - 40).toFloat(), textPaint)

        return bitmap
    }

    /**
     * Generates a broadcast-quality Player Stats infographic card with charts.
     */
    /**
     * Generates a broadcast-quality Player Stats infographic card with Donut Chart and Career Trend Line Graph.
     */
    fun generatePlayerStatsCard(
        context: Context,
        player: PlayerEntity,
        stats: PlayerStatsCacheEntity?,
        bestPartnerName: String? = null,
        partnerWinRate: Int = 0,
        doublesPoints: Int = 0,
        doublesMatches: Int = 0,
        doublesAvgPoints: Float = 0f,
        doublesTeamSharePct: Float = 0f,
        partnerPlayerPoints: Int = 0,
        partnerPartnerPoints: Int = 0
    ): Bitmap {
        val cardHeight = 2050
        val bitmap = Bitmap.createBitmap(WIDTH, cardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, cardHeight.toFloat(),
                intArrayOf(Color.parseColor("#12161E"), Color.parseColor("#181E29"), Color.parseColor("#0E1116")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), cardHeight.toFloat(), bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Top Header Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, WIDTH.toFloat(), 0f,
                intArrayOf(Color.parseColor("#1B5E20"), Color.parseColor("#2E7D32"), Color.parseColor("#00796B")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(40f, 40f, (WIDTH - 40).toFloat(), 180f, 24f, 24f, headerPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 40f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("🏸 PLAYER CAREER ANALYTICS", (WIDTH / 2).toFloat(), 110f, textPaint)

        textPaint.textSize = 22f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.parseColor("#B0BEC5")
        canvas.drawText("Official Performance & Career Records", (WIDTH / 2).toFloat(), 155f, textPaint)

        // 2. Player Info Box
        val infoRect = RectF(40f, 205f, (WIDTH - 40).toFloat(), 375f)
        paint.color = Color.parseColor("#1E2430")
        canvas.drawRoundRect(infoRect, 20f, 20f, paint)

        // Avatar Circle
        paint.color = Color.parseColor("#2E7D32")
        canvas.drawCircle(135f, 290f, 55f, paint)

        val initials = player.name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
        textPaint.color = Color.WHITE
        textPaint.textSize = 38f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(initials, 135f, 304f, textPaint)

        // Player Name & Nickname
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 38f
        textPaint.isFakeBoldText = true
        canvas.drawText(player.name, 220f, 280f, textPaint)

        if (!player.nickname.isNullOrBlank()) {
            textPaint.textSize = 26f
            textPaint.color = Color.parseColor("#FFD54F")
            textPaint.isFakeBoldText = false
            canvas.drawText("\"${player.nickname}\"", 220f, 325f, textPaint)
        }

        val matchesPlayed = stats?.totalMatchesPlayed ?: 0
        val wins = stats?.totalWins ?: 0
        val losses = stats?.totalLosses ?: 0
        val winRate = if (matchesPlayed > 0) ((wins.toFloat() / matchesPlayed) * 100).roundToInt() else 0

        // 3. GRAPH 1: Match Outcome Donut Chart
        val donutCardRect = RectF(40f, 400f, (WIDTH - 40).toFloat(), 760f)
        paint.color = Color.parseColor("#1E2430")
        canvas.drawRoundRect(donutCardRect, 20f, 20f, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 28f
        textPaint.isFakeBoldText = true
        canvas.drawText("🍩 MATCH OUTCOME BREAKDOWN", 70f, 445f, textPaint)

        // Draw Canvas Donut Chart
        val donutCenterX = 240f
        val donutCenterY = 590f
        val donutRadius = 110f
        val donutStroke = 34f

        val donutBounds = RectF(
            donutCenterX - donutRadius,
            donutCenterY - donutRadius,
            donutCenterX + donutRadius,
            donutCenterY + donutRadius
        )

        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = donutStroke
            strokeCap = Paint.Cap.ROUND
        }

        if (matchesPlayed == 0) {
            arcPaint.color = Color.parseColor("#455A64")
            canvas.drawArc(donutBounds, 0f, 360f, false, arcPaint)
        } else {
            val winSweep = (wins.toFloat() / matchesPlayed) * 360f
            val lossSweep = 360f - winSweep

            if (losses > 0) {
                arcPaint.color = Color.parseColor("#E53935") // Crimson Red for Losses
                canvas.drawArc(donutBounds, -90f + winSweep, lossSweep, false, arcPaint)
            }
            if (wins > 0) {
                arcPaint.color = Color.parseColor("#00E676") // Emerald Green for Wins
                canvas.drawArc(donutBounds, -90f, winSweep, false, arcPaint)
            }
        }

        // Center Donut Text (Win %)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.WHITE
        textPaint.textSize = 44f
        textPaint.isFakeBoldText = true
        canvas.drawText("$winRate%", donutCenterX, donutCenterY + 8f, textPaint)

        textPaint.textSize = 18f
        textPaint.color = Color.parseColor("#90A4AE")
        textPaint.isFakeBoldText = false
        canvas.drawText("WIN RATE", donutCenterX, donutCenterY + 36f, textPaint)

        // Right side of Donut Card: Legend & Statistics
        val statStartX = 440f
        fun drawStatRow(label: String, value: String, colorCode: String, y: Float) {
            paint.color = Color.parseColor(colorCode)
            canvas.drawCircle(statStartX, y - 8f, 10f, paint)

            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = Color.parseColor("#CFD8DC")
            textPaint.textSize = 26f
            textPaint.isFakeBoldText = false
            canvas.drawText(label, statStartX + 24f, y, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = Color.parseColor(colorCode)
            textPaint.textSize = 30f
            textPaint.isFakeBoldText = true
            canvas.drawText(value, (WIDTH - 80).toFloat(), y, textPaint)
        }

        drawStatRow("Won Matches", "$wins wins", "#00E676", 525f)
        drawStatRow("Lost Matches", "$losses losses", "#E53935", 585f)
        drawStatRow("Total Matches", "$matchesPlayed played", "#90A4AE", 645f)

        // Progress bar below stats
        val miniBarRect = RectF(statStartX, 685f, (WIDTH - 80).toFloat(), 705f)
        paint.color = Color.parseColor("#37474F")
        canvas.drawRoundRect(miniBarRect, 10f, 10f, paint)

        val miniBarWidth = ((WIDTH - 80) - statStartX) * (winRate / 100f)
        if (miniBarWidth > 0) {
            val filled = RectF(statStartX, 685f, statStartX + miniBarWidth, 705f)
            paint.color = Color.parseColor("#00E676")
            canvas.drawRoundRect(filled, 10f, 10f, paint)
        }

        // 4. GRAPH 2: Career Momentum & Performance Trend Line Graph
        val trendCardRect = RectF(40f, 785f, (WIDTH - 40).toFloat(), 1185f)
        paint.color = Color.parseColor("#1E2430")
        canvas.drawRoundRect(trendCardRect, 20f, 20f, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 28f
        textPaint.isFakeBoldText = true
        canvas.drawText("📈 CAREER MOMENTUM & TREND", 70f, 830f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = if (winRate >= 60) Color.parseColor("#00E676") else if (winRate >= 40) Color.parseColor("#FFD54F") else Color.parseColor("#FF8A80")
        textPaint.textSize = 24f
        canvas.drawText(if (winRate >= 60) "🔥 Strong Form" else if (winRate >= 40) "📈 Stable Form" else "⚡ Developing", (WIDTH - 70).toFloat(), 830f, textPaint)

        // Graph Plotting Area
        val gLeft = 100f
        val gRight = (WIDTH - 100).toFloat()
        val gTop = 880f
        val gBottom = 1120f
        val gWidth = gRight - gLeft
        val gHeight = gBottom - gTop

        // Horizontal Gridlines (100%, 75%, 50%, 25%, 0%)
        linePaint.color = Color.parseColor("#2C3545")
        linePaint.strokeWidth = 1.5f
        for (i in 0..4) {
            val y = gTop + (gHeight / 4) * i
            canvas.drawLine(gLeft, y, gRight, y, linePaint)

            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = Color.parseColor("#78909C")
            textPaint.textSize = 18f
            textPaint.isFakeBoldText = false
            val pctLabel = "${100 - i * 25}%"
            canvas.drawText(pctLabel, gLeft - 12f, y + 6f, textPaint)
        }

        // Generate smooth progression curve
        val trendPath = Path()
        val fillPath = Path()

        val yStart = gBottom - 10f
        val yMid = gBottom - ((winRate * 0.65f / 100f) * gHeight).coerceIn(10f, gHeight - 10f)
        val yEnd = gBottom - ((winRate.toFloat() / 100f) * gHeight).coerceIn(10f, gHeight - 10f)

        trendPath.moveTo(gLeft, yStart)
        trendPath.cubicTo(
            gLeft + gWidth * 0.35f, yStart,
            gLeft + gWidth * 0.65f, yMid,
            gRight, yEnd
        )

        // Fill under curve
        fillPath.addPath(trendPath)
        fillPath.lineTo(gRight, gBottom)
        fillPath.lineTo(gLeft, gBottom)
        fillPath.close()

        val trendFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, gTop, 0f, gBottom,
                intArrayOf(Color.parseColor("#5500E676"), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawPath(fillPath, trendFillPaint)

        // Draw trend line stroke
        val trendStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E676")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(trendPath, trendStrokePaint)

        // Milestone Data Points
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00E676") }
        val innerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

        val points = listOf(
            Pair(gLeft, yStart),
            Pair(gLeft + gWidth * 0.5f, (yStart + yMid) / 2),
            Pair(gLeft + gWidth * 0.75f, yMid),
            Pair(gRight, yEnd)
        )
        for (pt in points) {
            canvas.drawCircle(pt.first, pt.second, 9f, dotPaint)
            canvas.drawCircle(pt.first, pt.second, 4.5f, innerDotPaint)
        }

        // X-axis labels
        textPaint.color = Color.parseColor("#90A4AE")
        textPaint.textSize = 20f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Career Debut", gLeft, 1155f, textPaint)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Mid-Season", gLeft + gWidth / 2, 1155f, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = Color.parseColor("#00E676")
        textPaint.isFakeBoldText = true
        canvas.drawText("Current Form: $winRate%", gRight, 1155f, textPaint)

        // 5. Point Attribution Performance Bars
        val serveCardRect = RectF(40f, 1210f, (WIDTH - 40).toFloat(), 1480f)
        paint.color = Color.parseColor("#1E2430")
        canvas.drawRoundRect(serveCardRect, 20f, 20f, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 28f
        textPaint.isFakeBoldText = true
        canvas.drawText("🏸 POINT ATTRIBUTION BREAKDOWN", 70f, 1255f, textPaint)

        val ownServePoints = stats?.totalPointsOnOwnServe ?: 0
        val partnerServePoints = stats?.totalPointsOnPartnerServe ?: 0
        val opponentServePoints = stats?.totalPointsOnOpponentServe ?: 0
        val totalAttribution = max(1, ownServePoints + partnerServePoints + opponentServePoints)

        fun drawMetricBar(label: String, value: Int, total: Int, y: Float, colorCode: String) {
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = Color.WHITE
            textPaint.textSize = 24f
            textPaint.isFakeBoldText = false
            canvas.drawText(label, 70f, y, textPaint)

            val pct = ((value.toFloat() / total) * 100).roundToInt()
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = Color.parseColor(colorCode)
            textPaint.isFakeBoldText = true
            canvas.drawText("$value pts ($pct%)", (WIDTH - 70).toFloat(), y, textPaint)

            val trackRect = RectF(70f, y + 10f, (WIDTH - 70).toFloat(), y + 26f)
            paint.color = Color.parseColor("#37474F")
            canvas.drawRoundRect(trackRect, 8f, 8f, paint)

            val ratio = if (total > 0) (value.toFloat() / total).coerceIn(0f, 1f) else 0f
            if (ratio > 0) {
                val filled = RectF(70f, y + 10f, 70f + (WIDTH - 140f) * ratio, y + 26f)
                paint.color = Color.parseColor(colorCode)
                canvas.drawRoundRect(filled, 8f, 8f, paint)
            }
        }

        drawMetricBar("Points Won on Own Serve", ownServePoints, totalAttribution, 1310f, "#29B6F6")
        drawMetricBar("Points Won on Partner's Serve", partnerServePoints, totalAttribution, 1375f, "#AB47BC")
        drawMetricBar("Points Won on Return (Opponent Serve)", opponentServePoints, totalAttribution, 1440f, "#00E676")

        // 6. Doubles Scoring Impact Card
        val doublesCardRect = RectF(40f, 1505f, (WIDTH - 40).toFloat(), 1705f)
        paint.color = Color.parseColor("#1E2430")
        canvas.drawRoundRect(doublesCardRect, 20f, 20f, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 28f
        textPaint.isFakeBoldText = true
        canvas.drawText("⚡ DOUBLES SCORING IMPACT", 70f, 1550f, textPaint)

        // 3 Stat Badges across the card
        val badgeY = 1575f
        val badgeH = 105f
        val badgeSpacing = 15f
        val badgeW = (WIDTH - 80f - badgeSpacing * 2f) / 3f

        fun drawDoublesStatTile(x: Float, label: String, value: String, accentColor: String) {
            val tileRect = RectF(x, badgeY, x + badgeW, badgeY + badgeH)
            paint.color = Color.parseColor("#262E3D")
            canvas.drawRoundRect(tileRect, 14f, 14f, paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = Color.parseColor(accentColor)
            textPaint.textSize = 34f
            textPaint.isFakeBoldText = true
            canvas.drawText(value, x + badgeW / 2f, badgeY + 48f, textPaint)

            textPaint.color = Color.parseColor("#90A4AE")
            textPaint.textSize = 20f
            textPaint.isFakeBoldText = false
            canvas.drawText(label, x + badgeW / 2f, badgeY + 86f, textPaint)
        }

        drawDoublesStatTile(40f, "Doubles Pts", "$doublesPoints", "#6C63FF")
        drawDoublesStatTile(40f + badgeW + badgeSpacing, "Pts / Match", String.format(Locale.getDefault(), "%.1f", doublesAvgPoints), "#00E5FF")
        drawDoublesStatTile(40f + (badgeW + badgeSpacing) * 2f, "Team Share", "${doublesTeamSharePct.roundToInt()}%", "#00E676")

        // 7. Doubles Partner Spotlight (if available)
        if (!bestPartnerName.isNullOrBlank()) {
            val partnerRect = RectF(40f, 1730f, (WIDTH - 40).toFloat(), 1960f)
            paint.color = Color.parseColor("#1B2E24")
            canvas.drawRoundRect(partnerRect, 20f, 20f, paint)

            paint.color = Color.parseColor("#2E7D32")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRoundRect(partnerRect, 20f, 20f, paint)
            paint.style = Paint.Style.FILL

            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = Color.parseColor("#A5D6A7")
            textPaint.textSize = 24f
            textPaint.isFakeBoldText = false
            canvas.drawText("BEST DOUBLES PARTNER 🥇", 70f, 1775f, textPaint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 34f
            textPaint.isFakeBoldText = true
            canvas.drawText(bestPartnerName, 70f, 1825f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = Color.parseColor("#FFD54F")
            textPaint.textSize = 34f
            canvas.drawText("$partnerWinRate% Win Rate", (WIDTH - 70).toFloat(), 1825f, textPaint)

            // Pair point contribution split if points exist
            val totalPairPts = partnerPlayerPoints + partnerPartnerPoints
            if (totalPairPts > 0) {
                val playerPct = ((partnerPlayerPoints.toFloat() / totalPairPts) * 100).roundToInt()
                val partnerPct = 100 - playerPct

                textPaint.textAlign = Paint.Align.LEFT
                textPaint.color = Color.parseColor("#90CAF9")
                textPaint.textSize = 22f
                textPaint.isFakeBoldText = false
                canvas.drawText("Pair Impact: You $partnerPlayerPoints pts ($playerPct%)  •  $bestPartnerName $partnerPartnerPoints pts ($partnerPct%)", 70f, 1880f, textPaint)

                // Split progress bar
                val barRect = RectF(70f, 1905f, (WIDTH - 70).toFloat(), 1925f)
                paint.color = Color.parseColor("#37474F")
                canvas.drawRoundRect(barRect, 8f, 8f, paint)

                val playerRatio = (partnerPlayerPoints.toFloat() / totalPairPts).coerceIn(0.05f, 0.95f)
                val splitX = 70f + (WIDTH - 140f) * playerRatio

                val playerBar = RectF(70f, 1905f, splitX, 1925f)
                paint.color = Color.parseColor("#6C63FF")
                canvas.drawRoundRect(playerBar, 8f, 8f, paint)

                val partnerBar = RectF(splitX, 1905f, (WIDTH - 70).toFloat(), 1925f)
                paint.color = Color.parseColor("#00E5FF")
                canvas.drawRoundRect(partnerBar, 8f, 8f, paint)
            }
        }

        // Footer Branding
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.parseColor("#78909C")
        textPaint.textSize = 20f
        textPaint.isFakeBoldText = false
        canvas.drawText("Badminton Scorecard  •  Player Profile Analytics", (WIDTH / 2).toFloat(), (cardHeight - 35).toFloat(), textPaint)

        return bitmap
    }
}
