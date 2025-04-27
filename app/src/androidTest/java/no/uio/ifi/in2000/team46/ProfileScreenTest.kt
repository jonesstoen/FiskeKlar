package no.uio.ifi.in2000.team46


import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import no.uio.ifi.in2000.team46.data.local.database.entities.User as DbUser
import no.uio.ifi.in2000.team46.presentation.profile.screens.ProfileScreen
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileUiContract
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var vm: FakeProfileViewModel

    @Before
    fun setup() {
        vm = FakeProfileViewModel()
    }

    @Test
    fun whenUserIsNull_showsInputForm() {
        vm._user.value = null

        composeTestRule.setContent {
            ProfileScreen(
                viewModel = vm,
                onNavigateToHome = {},
                onNavigateToAlerts = {}
            )
        }

        composeTestRule
            .onNodeWithText("Rediger profil")
            .assertIsDisplayed()
    }

    @Test
    fun whenUserIsNotNull_showsProfileContent() {
        val sample = DbUser(
            id = 1,
            name = "Ola",
            username = "Nordmann",
            memberSince = "April 2025",
            profileImageUri = null
        )
        vm._user.value = sample

        composeTestRule.setContent {
            ProfileScreen(
                viewModel = vm,
                onNavigateToHome = {},
                onNavigateToAlerts = {}
            )
        }

        composeTestRule
            .onNodeWithText("Ola")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Nordmann")
            .assertIsDisplayed()
    }





    @Test
    fun savingForm_invokesSaveUserOnViewModel() {
        vm._user.value = null

        composeTestRule.setContent {
            ProfileScreen(
                viewModel = vm,
                onNavigateToHome = {},
                onNavigateToAlerts = {}
            )
        }

        composeTestRule
            .onNodeWithText("Navn")
            .performTextInput("Kari")
        composeTestRule
            .onNodeWithText("Kallenavn")
            .performTextInput("Testis")

        composeTestRule
            .onNodeWithText("Lagre endringer")
            .performClick()

        assert(vm.savedName == "Kari")
        assert(vm.savedUsername == "Testis")
        assert(vm.savedImageUriString == null || vm.savedImageUriString!!.startsWith("content://"))
    }

    class FakeProfileViewModel : ProfileUiContract {
        val _user = MutableStateFlow<DbUser?>(null)
        override val user: StateFlow<DbUser?> = _user

        var savedName: String? = null
        var savedUsername: String? = null
        var savedImageUriString: String? = null

        override fun saveUser(name: String, username: String, imageUri: String?) {
            savedName = name
            savedUsername = username
            savedImageUriString = imageUri
            _user.value = DbUser(
                id = 1,
                name = name,
                username = username,
                memberSince = "April 2025",
                profileImageUri = imageUri
            )
        }

        var didClear = false
        override fun clearUser() {
            didClear = true
            _user.value = null
        }
    }
}