package no.uio.ifi.in2000.team46.presentation.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import java.time.LocalTime
import kotlin.random.Random

// Fargepalett
// color palette from https://coolors.co/1b4965-5fa8d3-9dc88d-bee9e8-cae9ff
private val Navy = Color(0xFF1B4965)
val Background = Color(0xFFCAE9FF)
private val CardGreen = Color(0xFFB5C9B7)

@RequiresApi(Build.VERSION_CODES.O)
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

    val fishingTips = listOf(
        "Fisk grunt ved soloppgang.",
        "Bruk mørke sluker i grumsete vann.",
        "Små sluker fungerer best i klart vann.",
        "Senk hastigheten på innsveiving i kaldt vann.",
        "Fisk nær steiner og undervannsstrukturer.",
        "Ved lavt vann: let etter fisk nær kanter.",
        "Bruk naturlige farger i klart vann.",
        "Bruk sterke farger i uklart vann.",
        "Bytt sluk ofte hvis du ikke får napp.",
        "Fisk dypere midt på dagen.",
        "Morgen og kveld gir oftest best fangst.",
        "Bruk fluorcarbon som fortom for bedre skjul.",
        "Fisk saktere ved kaldt vær.",
        "Bruk levende agn på varme sommerdager.",
        "Let etter fugleaktivitet – ofte fisk under.",
        "Ved høyvann fisk nær land.",
        "Skift fiskested hvis ingen napp på 20 minutter.",
        "Lytt etter små plaskelyder – tegn på småfisk.",
        "Fisk nært flytende tang.",
        "Se etter mørke flekker på bunnen – kan være fisk!",
        "Bruk polaroid-solbriller for å se under vannflaten.",
        "Prøv forskjellige innsveivingsteknikker.",
        "Fisk i nærheten av brygger og moloer.",
        "I regn: bruk lyse sluker.",
        "I stille vær: bruk små, rolige sluker.",
        "Se på tidevannet før du planlegger turen.",
        "Fisk rett etter regn – kan være god aktivitet.",
        "Fisk saktere i kaldt vann.",
        "Skift størrelse på agnet om fisken ikke biter.",
        "Prøv med naturlig agn som reke eller børstemark.",
        "Se etter strømkanter hvor fisk kan stå.",
        "Bruk lett utstyr for småfisk.",
        "Bruk tyngre utstyr om det blåser mye.",
        "Hold kroken skarp – sjekk den ofte.",
        "Varier innsveivingen – gjør små rykk.",
        "Vær stille på land – vibrasjoner kan skremme fisk.",
        "Fisk i skyggeområder midt på dagen.",
        "Prøv med popper hvis fisken er aktiv i overflaten.",
        "Bruk duftspray på kunstig agn.",
        "Bytt til mindre krok om fisken bare napper forsiktig.",
        "Fisk nært utløp av bekker og elver.",
        "Rull agnet over bunnen for å lokke bunnfisk.",
        "Kast oppstrøms i elver og sveiv sakte nedstrøms.",
        "Bruk tyngre sluker i sterk strøm.",
        "Ved klart vann: bruk lang fortom.",
        "Bruk UV-aktive sluker i gråvær.",
        "Lær deg å knyte gode fiskeknuter.",
        "Bruk agn med naturlig lukt.",
        "Ved flom: fisk i sidekanaler og bakvann.",
        "Prøv nattfiske på varme sommernetter.",
        "Bruk slow-jigging teknikk i dype områder.",
        "Se etter skiftninger i bunnforhold.",
        "Fisk der ferskvann møter saltvann.",
        "Fisk nær kunstige strukturer (brygger, pæler).",
        "Bruk mindre sluker for forsiktig fisk.",
        "Prøv overflateagn ved lavt vann.",
        "Små vibrasjoner kan utløse hugg.",
        "Bruk agn som etterligner byttefisk.",
        "Bruk sterkere fortom om det er mye vegetasjon.",
        "Rens utstyret ditt etter hver tur.",
        "Ha med ekstra snelle og line.",
        "Se etter vak på vannflaten.",
        "Fisk sakte i skumringen.",
        "Test nye teknikker – ikke bare det du kan.",
        "Bruk metallagn på store dybder.",
        "Bruk split-shot søkke for å variere dybden.",
        "Gå forsiktig i vannet – ikke skrem fisken.",
        "Se etter steder med strøm og stillere partier.",
        "Bruk propellagn for å lokke overflatefisk.",
        "Bruk fiskerapport-apper for å finne hotspots.",
        "Bruk lokkelyder om natten.",
        "Velg tynnere line ved lite napp.",
        "Fisk under broer – ofte mye fisk der.",
        "I kaldt vær: fisk midt på dagen når det er varmest.",
        "Bruk en fiskekalender-app for beste tider.",
        "Vær tålmodig – noen ganger tar det tid.",
        "Se på månefaser – de kan påvirke fisket.",
        "Bruk naturlige farger i klart sollys.",
        "Fisk med vinden i ryggen for lengre kast.",
        "Sørg for at agnet ditt ser “skadet” ut – det lokker!",
        "Beveg sluken i rykkvise bevegelser.",
        "Bruk tyngre agn i sterk vind.",
        "Kast mot strukturer og trekk ut.",
        "Bytt retning på kastene dine ofte.",
        "Juster bremsen på snellen riktig.",
        "Bruk små wobblere i grunne innsjøer.",
        "Bruk lange stenger for bedre kastelengde.",
        "Fisk overgressflater med weedless rigg.",
        "Bruk små spinnere i fjellvann.",
        "Prøv vertikaljigging over dyphull.",
        "Se etter strømbrudd på elver.",
        "Prøv agn som lager vibrasjoner i mørkt vann.",
        "Bruk små jigger på kaldere dager.",
        "Rist litt i stangtuppen for å lokke fisken.",
        "Variér pauser i innsveivingen.",
        "Prøv slow-rolling teknikk på spinnerbaits.",
        "Bruk kroker med røde detaljer for å simulere sår.",
        "Velg sluker som lager mye bevegelse i kaldt vann.",
        "Bruk krepsimitasjoner på bunnen.",
        "Ha det gøy – fisking handler om opplevelsen!"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

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
                style = MaterialTheme.typography.headlineSmall,      // hent fra AppTypography
                color = MaterialTheme.colorScheme.onBackground
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
            RandomFishTipBox(fishingTips = fishingTips)
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
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
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

            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                style     = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun FishTipDialog(
    tip: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Lukk",
                    color = Navy,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        containerColor = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Dagens fisketips \uD83C\uDFA3 ",
                    color = Navy,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Text(
                text = tip,
                color = Navy,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                lineHeight = 26.sp
            )
        },
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
fun RandomFishTipBox(
    fishingTips: List<String>
) {
    var currentTip by remember { mutableStateOf(fishingTips.random()) }
    var showFullScreenTip by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp)
            .clickable { showFullScreenTip = true }, // Klikk på hele kortet
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFB5D5C5)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = "🎣 Ønsker du dagens fisketips?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Text(
                    text = "\"$currentTip\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Navy
                )
            }
            IconButton(
                onClick = { currentTip = fishingTips.random() }, // Nytt tips!
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Nytt tips",
                    tint = Navy
                )
            }
        }
    }

    if (showFullScreenTip) {
        FishTipDialog(
            tip = currentTip,
            onDismiss = { showFullScreenTip = false } // Lukk dialogen
        )
    }
}