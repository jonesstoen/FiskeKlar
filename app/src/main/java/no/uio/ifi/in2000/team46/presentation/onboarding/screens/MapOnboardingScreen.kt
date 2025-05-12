package no.uio.ifi.in2000.team46.presentation.onboarding.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel.MapOnboardingViewModel

/**
 * En enkel onboarding-skjerm som viser en serie med dialogbokser som forklarer
 * de ulike funksjonene i appen.
 */

// Definerer de ulike stegene i onboarding-prosessen
enum class OnboardingStepType {
    WELCOME,           // Velkommen til appen
    SEARCH,            // Viser søkefunksjonen
    ZOOM,              // Forklarer zoom-funksjonalitet
    LAYERS,            // Viser lag-menyen
    WEATHER,           // Forklarer værvisningen
    LOCATION,          // Viser lokasjonsfunksjonen
    COMPLETE           // Avslutning av onboarding
}

// Data class for å definere en funksjon som skal vises i onboarding
data class OnboardingFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val xPercent: Float = 0.5f,  // X-posisjon som prosent av skjermbredden (0.0-1.0)
    val yPercent: Float = 0.5f,  // Y-posisjon som prosent av skjermhøyden (0.0-1.0)
    val arrowDirection: ArrowDirection = ArrowDirection.UP  // Retning for pilen
)

// Enum for å definere retningen til pilen
enum class ArrowDirection {
    UP, UP_LEFT, DOWN_LEFT, DOWN_RIGHT
}

// Komponent for å vise en pil
@Composable
fun Arrow(
    direction: ArrowDirection,
    color: Color,
    modifier: Modifier = Modifier
) {
    val rotation = when (direction) {
        ArrowDirection.UP -> 270f          // Peker rett opp
        ArrowDirection.UP_LEFT -> 225f    // Skrått opp til venstre (45 grader fra venstre)
        ArrowDirection.DOWN_LEFT -> 135f  // Skrått ned til venstre (45 grader fra venstre)
        ArrowDirection.DOWN_RIGHT -> 45f  // Skrått ned til høyre (45 grader fra høyre)
    }
    
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
        contentDescription = null,
        tint = color,
        modifier = modifier
            .size(32.dp)  // Mindre pil for bedre plassering ved sirkelen
            .rotate(rotation)
    )
}

// Komponent for å vise indikator for en funksjon
@Composable
fun FeatureIndicator(
    feature: OnboardingFeature,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Animasjon for pulserende effekt
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )
    
    // Bestem layout basert på pilens retning
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulserende sirkel i midten med fyllfarge
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(feature.color)  // Fyller sirkelen med den valgte fargen
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                tint = Color.White,  // Hvit farge på ikonet for kontrast
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Plasser pilen i hjørnet av sirkelen basert på retningen
        when (feature.arrowDirection) {
            ArrowDirection.UP -> {
                // Pil på toppen av sirkelen, peker oppover
                Arrow(
                    direction = feature.arrowDirection,
                    color = feature.color,
                    modifier = Modifier
                        .offset(y = (-32).dp)
                        .align(Alignment.TopCenter)
                )
            }
            ArrowDirection.UP_LEFT -> {
                // Pil i øvre venstre hjørne, peker skrått opp mot venstre
                Arrow(
                    direction = feature.arrowDirection,
                    color = feature.color,
                    modifier = Modifier
                        .offset(x = (-22).dp, y = (-22).dp)
                        .align(Alignment.TopStart)
                )
            }
            ArrowDirection.DOWN_LEFT -> {
                // Pil i nedre venstre hjørne, peker skrått ned mot venstre
                Arrow(
                    direction = feature.arrowDirection,
                    color = feature.color,
                    modifier = Modifier
                        .offset(x = (-22).dp, y = 22.dp)
                        .align(Alignment.BottomStart)
                )
            }
            ArrowDirection.DOWN_RIGHT -> {
                // Pil i nedre høyre hjørne, peker skrått ned mot høyre
                Arrow(
                    direction = feature.arrowDirection,
                    color = feature.color,
                    modifier = Modifier
                        .offset(x = 22.dp, y = 22.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
fun MapOnboardingScreen(
    viewModel: MapOnboardingViewModel,
    onFinish: () -> Unit,
    // Callbacks for interactive features
    onZoom: () -> Unit = {},
    onToggleLayers: () -> Unit = {},
    onShowLocation: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(OnboardingStepType.WELCOME) }
    
    // Definerer alle funksjoner som skal vises i onboarding
    val features = remember {
        listOf(
            OnboardingFeature(
                title = "Søkefelt",
                description = "Søk etter steder for å se farevarsler og værvarsel",
                icon = Icons.Default.Search,
                color = Color(0xFFCE62FA),
                xPercent = 0f,  // Plassering for søkefeltet (øverst til venstre)
                yPercent = 0.09f,
                arrowDirection = ArrowDirection.UP  // Peker oppover
            ),
            OnboardingFeature(
                title = "Zoom-knapper",
                description = "Bruk disse knappene for å zoome inn og ut på kartet. Du kan også bruke to fingre",
                icon = Icons.Default.ZoomIn,
                color = Color(0xFF5FA8D3),
                xPercent = 0.2f,
                yPercent = 0.76f,
                arrowDirection = ArrowDirection.UP_LEFT  // Peker skrått opp mot venstre
            ),
            OnboardingFeature(
                title = "Kartlag",
                description = "Trykk her for å vise eller skjule ulike informasjonslag på kartet",
                icon = Icons.Default.Layers,
                color = Color(0xFF9DC88D),
                xPercent = 0.2f,
                yPercent = 0.7f,
                arrowDirection = ArrowDirection.DOWN_LEFT  // Peker skrått opp mot venstre
            ),
            OnboardingFeature(
                title = "Værvisning",
                description = "Her vises været for valgt lokasjon. Trykk for detaljert værmelding",
                icon = Icons.Default.WbSunny,
                color = Color(0xFFFCB927),
                xPercent = 0.5f,
                yPercent = 0.62f,
                arrowDirection = ArrowDirection.DOWN_RIGHT  // Peker skrått ned mot venstre
            ),
            OnboardingFeature(
                title = "Min posisjon",
                description = "Trykk her for å sentrere kartet på din nåværende posisjon",
                icon = Icons.Default.LocationOn,
                color = Color(0xFFD32F2F),
                xPercent = 0.5f,
                yPercent = 0.7f,
                arrowDirection = ArrowDirection.DOWN_RIGHT  // Peker skrått ned mot høyre
            )
        )
    }
    
    // Funksjon for å gå til neste steg
    val goToNextStep = {
        currentStep = when (currentStep) {
            OnboardingStepType.WELCOME -> OnboardingStepType.SEARCH
            OnboardingStepType.SEARCH -> OnboardingStepType.ZOOM
            OnboardingStepType.ZOOM -> OnboardingStepType.LAYERS
            OnboardingStepType.LAYERS -> OnboardingStepType.WEATHER
            OnboardingStepType.WEATHER -> OnboardingStepType.LOCATION
            OnboardingStepType.LOCATION -> OnboardingStepType.COMPLETE
            OnboardingStepType.COMPLETE -> {
                viewModel.hideMapOnboarding()
                onFinish()
                OnboardingStepType.COMPLETE
            }
        }
    }
    
    // Funksjon for å gå til forrige steg
    val goToPreviousStep = {
        currentStep = when (currentStep) {
            OnboardingStepType.WELCOME -> OnboardingStepType.WELCOME // Bli på første steg
            OnboardingStepType.SEARCH -> OnboardingStepType.WELCOME
            OnboardingStepType.ZOOM -> OnboardingStepType.SEARCH
            OnboardingStepType.LAYERS -> OnboardingStepType.ZOOM
            OnboardingStepType.WEATHER -> OnboardingStepType.LAYERS
            OnboardingStepType.LOCATION -> OnboardingStepType.WEATHER
            OnboardingStepType.COMPLETE -> OnboardingStepType.LOCATION
        }
    }
    
    // Funksjon for å hoppe over onboarding
    val skipOnboarding = {
        viewModel.hideMapOnboarding()
        onFinish()
    }

    // Funksjon for u00e5 markere at brukeren har interagert med en funksjon
    val handleFeatureInteraction = { step: OnboardingStepType ->
        if (currentStep == step) {
            goToNextStep()
        }
    }
    
    // Vis onboarding bare hvis showMapOnboarding er true
    val showMapOnboarding by viewModel.showMapOnboarding.collectAsState()

    if (showMapOnboarding) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Semi-transparent tint over hele skjermen (som bakgrunn)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {}
            )
            
            // Overlay med indikatorer for hver funksjon (oppu00e5 tinten)
            Box(modifier = Modifier.fillMaxSize().clickable(enabled = false) {}) {
                // Hent skjermstu00f8rrelse en gang for alle indikatorer
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                
                // Vis indikator basert pu00e5 gjeldende steg
                when (currentStep) {
                    OnboardingStepType.SEARCH -> {
                        FeatureIndicator(
                            feature = features[0],
                            modifier = Modifier.offset(
                                x = screenWidth * features[0].xPercent,
                                y = screenHeight * features[0].yPercent
                            )
                        )
                    }
                    OnboardingStepType.ZOOM -> {
                        FeatureIndicator(
                            feature = features[1],
                            modifier = Modifier.offset(
                                x = screenWidth * features[1].xPercent,
                                y = screenHeight * features[1].yPercent
                            ),
                            onClick = { onZoom(); handleFeatureInteraction(OnboardingStepType.ZOOM) }
                        )
                    }
                    OnboardingStepType.LAYERS -> {
                        FeatureIndicator(
                            feature = features[2],
                            modifier = Modifier.offset(
                                x = screenWidth * features[2].xPercent,
                                y = screenHeight * features[2].yPercent
                            ),
                            onClick = { onToggleLayers(); handleFeatureInteraction(OnboardingStepType.LAYERS) }
                        )
                    }
                    OnboardingStepType.WEATHER -> {
                        FeatureIndicator(
                            feature = features[3],
                            modifier = Modifier.offset(
                                x = screenWidth * features[3].xPercent,
                                y = screenHeight * features[3].yPercent
                            )
                        )
                    }
                    OnboardingStepType.LOCATION -> {
                        FeatureIndicator(
                            feature = features[4],
                            modifier = Modifier.offset(
                                x = screenWidth * features[4].xPercent,
                                y = screenHeight * features[4].yPercent
                            ),
                            onClick = { onShowLocation(); handleFeatureInteraction(OnboardingStepType.LOCATION) }
                        )
                    }
                    else -> { /* Ikke vis noen indikator for WELCOME og COMPLETE */ }
                }
            }
        }
        // Vis popup med instruksjoner
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Fremdriftsindikator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Vis indikatorer for hvert steg (unntatt COMPLETE)
                        OnboardingStepType.values().filter { it != OnboardingStepType.COMPLETE }.forEachIndexed { index, step ->
                            val isCurrentStep = step == currentStep
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (isCurrentStep) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCurrentStep) {
                                            when (step) {
                                                OnboardingStepType.WELCOME -> Color(0xFF1B4965)
                                                OnboardingStepType.SEARCH -> features[0].color
                                                OnboardingStepType.ZOOM -> features[1].color
                                                OnboardingStepType.LAYERS -> features[2].color
                                                OnboardingStepType.WEATHER -> features[3].color
                                                OnboardingStepType.LOCATION -> features[4].color
                                                else -> Color.Gray
                                            }
                                        } else {
                                            Color.Gray.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }
                    }

                    // Innhold basert på gjeldende steg
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        }
                    ) { step ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Ikon
                            Icon(
                                imageVector = when (step) {
                                    OnboardingStepType.WELCOME -> Icons.Default.Explore
                                    OnboardingStepType.SEARCH -> features[0].icon
                                    OnboardingStepType.ZOOM -> features[1].icon
                                    OnboardingStepType.LAYERS -> features[2].icon
                                    OnboardingStepType.WEATHER -> features[3].icon
                                    OnboardingStepType.LOCATION -> features[4].icon
                                    OnboardingStepType.COMPLETE -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = when (step) {
                                    OnboardingStepType.WELCOME -> Color(0xFF1B4965)
                                    OnboardingStepType.SEARCH -> features[0].color
                                    OnboardingStepType.ZOOM -> features[1].color
                                    OnboardingStepType.LAYERS -> features[2].color
                                    OnboardingStepType.WEATHER -> features[3].color
                                    OnboardingStepType.LOCATION -> features[4].color
                                    OnboardingStepType.COMPLETE -> Color(0xFF4CAF50)
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tittel
                            Text(
                                text = when (step) {
                                    OnboardingStepType.WELCOME -> "Velkommen til Havvarsel"
                                    OnboardingStepType.SEARCH -> features[0].title
                                    OnboardingStepType.ZOOM -> features[1].title
                                    OnboardingStepType.LAYERS -> features[2].title
                                    OnboardingStepType.WEATHER -> features[3].title
                                    OnboardingStepType.LOCATION -> features[4].title
                                    OnboardingStepType.COMPLETE -> "Du er klar!"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = when (step) {
                                    OnboardingStepType.WELCOME -> Color(0xFF1B4965)
                                    OnboardingStepType.SEARCH -> features[0].color
                                    OnboardingStepType.ZOOM -> features[1].color
                                    OnboardingStepType.LAYERS -> features[2].color
                                    OnboardingStepType.WEATHER -> features[3].color
                                    OnboardingStepType.LOCATION -> features[4].color
                                    OnboardingStepType.COMPLETE -> Color(0xFF4CAF50)
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Beskrivelse
                            Text(
                                text = when (step) {
                                    OnboardingStepType.WELCOME -> "La oss ta en rask gjennomgang av funksjonene i appen."
                                    OnboardingStepType.SEARCH -> features[0].description
                                    OnboardingStepType.ZOOM -> features[1].description
                                    OnboardingStepType.LAYERS -> features[2].description
                                    OnboardingStepType.WEATHER -> features[3].description
                                    OnboardingStepType.LOCATION -> features[4].description
                                    OnboardingStepType.COMPLETE -> "Du har nå lært om de viktigste funksjonene i appen. God tur på sjøen!"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Knapper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Venstre side: Tilbake-knapp eller Hopp over-knapp
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (currentStep == OnboardingStepType.WELCOME) {
                                // Hopp over-knapp (vises bare på første steg)
                                TextButton(
                                    onClick = skipOnboarding
                                ) {
                                    Text("Hopp over")
                                }
                            } else {
                                // Tilbake-knapp (vises ikke på første steg)
                                OutlinedButton(
                                    onClick = goToPreviousStep,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = when (currentStep) {
                                            OnboardingStepType.SEARCH -> features[0].color
                                            OnboardingStepType.ZOOM -> features[1].color
                                            OnboardingStepType.LAYERS -> features[2].color
                                            OnboardingStepType.WEATHER -> features[3].color
                                            OnboardingStepType.LOCATION -> features[4].color
                                            OnboardingStepType.COMPLETE -> Color(0xFF4CAF50)
                                            else -> Color(0xFF1B4965)
                                        }
                                    )
                                ) {
                                    Text("Tilbake")
                                }
                            }
                        }
                        
                        // Høyre side: Neste/Ferdig-knapp
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                            Button(
                                onClick = goToNextStep,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (currentStep) {
                                        OnboardingStepType.WELCOME -> Color(0xFF1B4965)
                                        OnboardingStepType.SEARCH -> features[0].color
                                        OnboardingStepType.ZOOM -> features[1].color
                                        OnboardingStepType.LAYERS -> features[2].color
                                        OnboardingStepType.WEATHER -> features[3].color
                                        OnboardingStepType.LOCATION -> features[4].color
                                        OnboardingStepType.COMPLETE -> Color(0xFF4CAF50)
                                    }
                                )
                            ) {
                                Text(
                                    text = if (currentStep == OnboardingStepType.WELCOME) "Start" else if (currentStep == OnboardingStepType.COMPLETE) "Avslutt" else "Neste",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
