package no.uio.ifi.in2000.team46.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.presentation.ui.components.BottomNavBar
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.profile.ProfileViewModel
import java.time.LocalTime

// Fargepalett
// color palette from https://coolors.co/1b4965-5fa8d3-9dc88d-bee9e8-cae9ff
private val Navy = Color(0xFF1B4965)
private val LightBlue = Color(0xFF5FA8D3)
private val Sage = Color(0xFF9DC88D)
private val LightSage = Color(0xFFBEE9E8)
val Background = Color(0xFFCAE9FF)
private val CardGreen = Color(0xFFB5C9B7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ProfileViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToFishLog: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val user by viewModel.user.collectAsState()

    val greeting = remember(user) {
        val name = user?.name?.split(" ")?.firstOrNull() ?: ""
        val timeGreeting = when (LocalTime.now().hour) {
            in 5..10 -> "God morgen"
            in 11..14 -> "God formiddag"
            in 15..17 -> "God ettermiddag"
            else -> "God kveld"
        }
        if (name.isNotBlank()) "$timeGreeting, $name!" else "$timeGreeting!"
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "profile" -> onNavigateToProfile()
                        "alerts" -> onNavigateToAlerts()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App logo and name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )
            }

            // Greeting shown to the user
            Text(
                text = "$greeting!",
                style = MaterialTheme.typography.headlineSmall,
                color = Navy
            )

            // Grid of quickactions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.Map,
                        text = "Kart",
                        onClick = onNavigateToMap,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.Default.List,
                        text = "Fiskelogg",
                        onClick = onNavigateToFishLog,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.WbSunny,
                        text = "Værvarsel",
                        onClick = onNavigateToWeather,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.Default.Favorite,
                        text = "Favoritter",
                        onClick = onNavigateToFavorites,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(190.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardGreen
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(40.dp),
                tint = Navy
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Navy
            )
        }
    }
} 