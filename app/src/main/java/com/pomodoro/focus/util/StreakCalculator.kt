package com.pomodoro.focus.util

import com.pomodoro.focus.data.local.entity.DailyRecordEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StreakCalculator {
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Streak naik jika hari itu punya minimal 1 completed session
     * (dengan atau tanpa achieved target).
     */
    fun isStreakDay(record: DailyRecordEntity?): Boolean =
        record != null && record.completedSessions >= 1

    /**
     * Tier untuk calendar heatmap:
     * 0 = no activity, 1 = partial (session only), 2 = perfect (session + target)
     */
    fun dayTier(record: DailyRecordEntity?): Int = when {
        record == null -> 0
        record.completedSessions >= 1 && record.achievedTargets >= 1 -> 2
        record.completedSessions >= 1 -> 1
        else -> 0
    }

    /**
     * Hitung current streak dari hari ini mundur.
     */
    fun calculateStreak(records: List<DailyRecordEntity>): Int {
        val recordMap = records.associateBy { it.dateKey }
        var streak = 0
        var date = LocalDate.now()

        while (true) {
            val key = date.format(fmt)
            val record = recordMap[key]
            if (isStreakDay(record)) {
                streak++
                date = date.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Pesan motivasi dinamis berdasarkan streak.
     */
    fun streakMessage(streak: Int): String = when {
        streak == 0 -> "Yahh, apinya padam! Nyalakan kembali streakmu hari ini!"
        streak in 1..2 -> "Bara api mulai menyala! Jaga fokusmu tetap membara."
        streak in 3..6 -> "Konsistensi hebat! Api produktivitasmu makin stabil."
        streak == 7 -> "7 hari penuh! Teruskan streak apimu!"
        streak in 8..13 -> "Luar biasa! Kamu membangun kebiasaan nyata."
        streak == 14 -> "2 minggu tanpa putus! Fokusmu tak terbendung."
        streak in 15..29 -> "Hampir sebulan! Kamu sudah jadi mesin produktivitas."
        streak >= 30 -> "Legenda! $streak hari berturut-turut. Tak ada yang bisa menghentikanmu."
        else -> "Teruskan!"
    }
}
