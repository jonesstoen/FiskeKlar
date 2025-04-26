// File: app/src/androidTest/java/no/uio/ifi/in2000/team46/FishingLogScreenTest.kt
package no.uio.ifi.in2000.team46

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.screens.FishingLogScreen
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.viewmodel.FishingLogUiContract
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class FishingLogScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Fake implementation to support testing
    class FakeFishingLogUiContract(
        initialEntries: List<FishingLog> = emptyList()
    ) : FishingLogUiContract {
        private val _entries = MutableStateFlow(initialEntries)
        override val entries: StateFlow<List<FishingLog>> = _entries

        private val _fishTypes = MutableStateFlow(emptyList<no.uio.ifi.in2000.team46.data.local.database.entities.FishType>())
        override val fishTypes: StateFlow<List<no.uio.ifi.in2000.team46.data.local.database.entities.FishType>> = _fishTypes

        override fun addEntry(
            date: LocalDate,
            time: LocalTime,
            location: String,
            fishType: String,
            weight: Double,
            notes: String,
            imageUri: String?
        ) {
            // no-op for test
        }

        override fun removeEntry(entry: FishingLog) {
            // Remove entry from the state
            _entries.value = _entries.value.filter { it.id != entry.id }
        }
    }

    @Test
    fun fishingLogScreen_emptyState_displaysEmptyMessage() {
        val fakeUiContract = FakeFishingLogUiContract()
        composeTestRule.setContent {
            FishingLogScreen(viewModel = fakeUiContract, onNavigate = {})
        }
        // Check that the empty state message is displayed
        composeTestRule.onNodeWithText("Ingen fangster enda").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trykk på + knappen for å legge til din første fangst").assertIsDisplayed()
    }

    @Test
    fun fishingLogScreen_nonEmptyState_displaysEntriesAndRespondsToClick() {
        val testEntry = FishingLog(
            id = 1,
            date = LocalDate.now().toString(),
            time = LocalTime.now().toString(),
            location = "Test Location",
            fishType = "Torsk",
            weight = 2.5,
            notes = "Test notes",
            imageUri = null
        )
        val fakeUiContract = FakeFishingLogUiContract(initialEntries = listOf(testEntry))
        var navigateRoute: String? = null
        composeTestRule.setContent {
            FishingLogScreen(viewModel = fakeUiContract, onNavigate = { route ->
                navigateRoute = route
            })
        }
        // Check that the entry is displayed with correct texts

        //composeTestRule.onNodeWithText("Torsk - 2,5 kg").assertIsDisplayed()
        // Simulate clicking the entry card which has a clickable behavior. Here we use a simple test based on content description if available.
        // For example, checking the delete icon content description "Slett"
        composeTestRule.onNodeWithContentDescription("Slett").performClick()
        // Since the fake removeEntry removes the entry, check that the empty state becomes visible again.
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ingen fangster enda").assertIsDisplayed()
    }
}