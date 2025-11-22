package edu.wku.toppernav

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import edu.wku.toppernav.ui.screens.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationFlowInstrumentedTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun appLaunches_andRenders() {
        // Smoke test: if activity launches without crash, basic wiring is OK.
        // Compose semantics assertions can be added if test tags exist.
        // For now, this is a minimal connected test to satisfy instrumentation harness.
    }
}

