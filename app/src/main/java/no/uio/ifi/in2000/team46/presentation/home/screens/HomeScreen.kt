package no.uio.ifi.in2000.team46.presentation.home.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel.OnboardingViewModel
import no.uio.ifi.in2000.team46.presentation.onboarding.screens.OnboardingScreen
import java.time.LocalTime

// color palette from https://coolors.co/1b4965-5fa8d3-9dc88d-bee9e8-cae9ff
private val Navy = Color(0xFF1B4965)



// composable that shows home screen with greeting, quick actions and random fishing tip

@Composable
fun HomeScreen(
    viewModel: ProfileViewModel,
    onboardingViewModel: OnboardingViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToFishLog: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    // observe user data and onboarding flag
    val user by viewModel.user.collectAsState()
    val showOnboarding by onboardingViewModel.showOnboarding.collectAsState()

    // show onboarding screen if flag is true
    if (showOnboarding) {
        OnboardingScreen(
            onFinish = { onboardingViewModel.hideOnboarding() }
        )
    }

    // compute greeting based on time and user name
    val greeting = remember(user) {
        val name = user?.name?.split(" ")?.firstOrNull().orEmpty()
        val timeGreeting = when (LocalTime.now().hour) {
            in 5..10 -> "god morgen"
            in 11..14 -> "god formiddag"
            in 15..17 -> "god ettermiddag"
            else -> "god kveld"
        }
        if (name.isNotBlank()) "$timeGreeting, $name" else timeGreeting
    }

    // list of fishing tips used in random tip card
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
        "Bruk levende agn på varme sommerdager.",
        "Let etter fugleaktivitet, ofte er det fisk under.",
        "Ved høyvann fisk nær land.",
        "Skift fiskested hvis ingen napp på 20 minutter.",
        "Fisk nært flytende tang.",
        "Se etter mørke flekker på bunnen, det kan være fisk!",
        "Bruk polaroid-solbriller for å se under vannflaten.",
        "Prøv forskjellige innsveivingsteknikker.",
        "Fisk i nærheten av brygger og moloer.",
        "I regn: bruk lyse sluker.",
        "I stille vær: bruk små, rolige sluker.",
        "Se på tidevannet før du planlegger turen.",
        "Fisk rett etter regn, kan være god aktivitet.",
        "Fisk saktere i kaldt vann.",
        "Skift størrelse på agnet om fisken ikke biter.",
        "Prøv med naturlig agn som reke eller mark.",
        "Se etter strømkanter hvor fisk kan stå.",
        "Bruk lett utstyr for småfisk.",
        "Bruk tyngre utstyr om det blåser mye.",
        "Hold kroken skarp, sjekk den ofte.",
        "Varier innsveivingen, gjør små rykk.",
        "Vær stille på land, vibrasjoner kan skremme fisk.",
        "Fisk i skyggeområder midt på dagen.",
        "Prøv med popper hvis fisken er aktiv i overflaten.",
        "Bruk duftspray på kunstig agn.",
        "Bytt til mindre krok om fisken bare napper forsiktig.",
        "Fisk nært utløp av bekker og elver.",
        "Rull agnet over bunnen for å lokke bunnfisk.",
        "Kast oppstrøms i elver og sveiv sakte nedstrøms.",
        "Bruk tyngre sluker i sterk strøm.",
        "Bruk UV-aktive sluker i gråvær.",
        "Lær deg å knyte gode fiskeknuter.",
        "Bruk agn med naturlig lukt.",
        "Langt å gå, fisk å få.",
        "Det er flere fisk på havet, enn på motorveien",
        "Hvis flyvefisk, kast opp",
        "Prøv nattfiske på varme sommernetter.",
        "Bruk slow-jigging teknikk i dype områder.",
        "Se etter skiftninger i bunnforhold.",
        "Fisk der ferskvann møter saltvann.",
        "Fisk nær kunstige strukturer (brygger, pæler).",
        "Prøv overflateagn ved lavt vann.",
        "Små vibrasjoner kan utløse hugg.",
        "Bruk agn som etterligner byttefisk.",
        "Bruk sterkere fortom om det er mye vegetasjon.",
        "Rens utstyret ditt etter hver tur.",
        "Ha med ekstra snelle og line.",
        "Se etter vak på vannflaten.",
        "Fisk sakte i skumringen.",
        "Test nye teknikker, ikke bare det du kan.",
        "Bruk metallagn på store dybder.",
        "Gå forsiktig i vannet, ikke skrem fisken.",
        "Se etter steder med strøm og stillere partier.",
        "Bruk propellagn for å lokke overflatefisk.",
        "Bruk lokkelyder om natten.",
        "Velg tynnere line ved lite napp.",
        "Fisk under broer, ofte mye fisk der.",
        "I kaldt vær: fisk midt på dagen når det er varmest.",
        "Bruk en fiskekalender-app for beste tider.",
        "Vær tålmodig, noen ganger tar det tid.",
        "Se på månefaser, de kan påvirke fisket.",
        "Bruk naturlige farger i klart sollys.",
        "Fisk med vinden i ryggen for lengre kast.",
        "Sørg for at agnet ditt ser skadet ut – det lokker!",
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
        "Hvis abboren går oppstrøms på land, sjekk om du har vridd innsjøen baklengs.",
        "Velg sluker som lager mye bevegelse i kaldt vann.",
        "Bruk krepsimitasjoner på bunnen.",
        "Ha det gøy – fisking handler om opplevelsen!",
        "Hvis du fisker i IFI-dammen, se opp for Kiwi Ulven!",
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // logo and help button row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "app logo",
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center)
                )
                IconButton(
                    onClick = { onboardingViewModel.showOnboarding() },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = "show onboarding",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // greeting text below logo
            Text(
                text = "$greeting!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp)
            )

            // grid of quick access cards
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
                        text = "kart",
                        onClick = onNavigateToMap,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.AutoMirrored.Filled.List,
                        text = "fiskelogg",
                        onClick = onNavigateToFishLog,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.WbSunny,
                        text = "været",
                        onClick = onNavigateToWeather,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.Default.Favorite,
                        text = "favoritt-steder",
                        onClick = onNavigateToFavorites,
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // random fishing tip card at bottom
            RandomFishTipBox(fishingTips = fishingTips)
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // card showing icon and label for quick navigation
    Card(
        onClick = onClick,
        modifier = modifier.height(190.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun FishTipDialog(
    tip: String,
    onDismiss: () -> Unit
) {
    // dialog showing full fishing tip text
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "lukk", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            }
        },
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(text = "Dagens fisketips 🎣", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Text(text = tip, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().padding(16.dp), lineHeight = 26.sp)
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun RandomFishTipBox(
    fishingTips: List<String>
) {
    // box showing a random tip and allowing refresh or full-screen view
    var currentTip by remember { mutableStateOf(fishingTips.random()) }
    var showFullScreenTip by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp)
            .clickable { showFullScreenTip = true },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB5D5C5)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Column(modifier = Modifier.align(Alignment.CenterStart).padding(end = 36.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "🎣 Dagens fisketips", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Navy)
                Text(text = "\"$currentTip\"", style = MaterialTheme.typography.bodySmall, color = Navy, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { currentTip = fishingTips.random() }, modifier = Modifier.align(Alignment.TopEnd).size(32.dp)) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "nytt tips", tint = Navy, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showFullScreenTip) {
        FishTipDialog(tip = currentTip, onDismiss = { showFullScreenTip = false })
    }
}

