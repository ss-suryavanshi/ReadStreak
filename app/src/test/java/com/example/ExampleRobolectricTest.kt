package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ReadingLog
import com.example.ui.components.StreakCalculator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Read Streak", appName)
  }

  @Test
  fun `calculate consecutive goal streak accurately`() {
    val today = LocalDate.of(2026, 9, 27)
    val logs = listOf(
      ReadingLog(dateString = "2026-09-25", minutesRead = 25, pagesRead = 18),
      ReadingLog(dateString = "2026-09-26", minutesRead = 30, pagesRead = 22),
      ReadingLog(dateString = "2026-09-27", minutesRead = 20, pagesRead = 15)
    )
    val calc = StreakCalculator.calculateGoalStreak(logs = logs, dailyGoalMins = 20, today = today)
    assertEquals(3, calc.currentStreak)
    assertEquals(3, calc.bestStreak)
    assertTrue(calc.isTodayGoalMet)
  }
}
