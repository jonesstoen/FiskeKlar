package no.uio.ifi.in2000.team46.presentation.onboarding.screens

import android.annotation.SuppressLint
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


// simple onbaording screen that shows a series of dialog boxes explaining the features of the app.

// Defineing the different steps in the onboarding process
enum class OnboardingStepType {
    WELCOME,
    SEARCH,
    ZOOM,
    LAYERS,
    LAYER_INFO,
    WEATHER,
    LOCATION,
    LONG_PRESS,
    COMPLETE
}

// Data class to define the features of the onboarding
data class OnboardingFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val arrowDirection: ArrowDirection = ArrowDirection.UP  // directon for arrow
)

enum class ArrowDirection {
    UP, UP_LEFT, DOWN_LEFT, DOWN_RIGHT
}

@Composable
fun Arrow(
    direction: ArrowDirection,
    color: Color,
    modifier: Modifier = Modifier
) {
    val rotation = when (direction) {
        ArrowDirection.UP -> 270f
        ArrowDirection.UP_LEFT -> 225f
        ArrowDirection.DOWN_LEFT -> 135f
        ArrowDirection.DOWN_RIGHT -> 45f
    }
    
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
        contentDescription = null,
        tint = color,
        modifier = modifier
            .size(32.dp)
            .rotate(rotation)
    )
}

@Composable
fun FeatureIndicator(
    feature: OnboardingFeature,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // animation for the pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(feature.color)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        when (feature.arrowDirection) {
            ArrowDirection.UP -> {
                Arrow(
                    direction = feature.arrowDirection,
                    color = feature.color,
                    modifier = Modifier
                        .offset(y = (-32).dp)
                        .align(Alignment.TopCenter)
                )
            }
            ArrowDirection.UP_LEFT -> {

                Arrow(
                    direction = feature.arrowDirection,
                    color = feature.color,
                    modifier = Modifier
                        .offset(x = (-22).dp, y = (-22).dp)
                        .align(Alignment.TopStart)
                )
            }
            ArrowDirection.DOWN_LEFT -> {
                Arrow(
                    direction = feature.arrowDirection,
                    color = feature.color,
                    modifier = Modifier
                        .offset(x = (-22).dp, y = 22.dp)
                        .align(Alignment.BottomStart)
                )
            }
            ArrowDirection.DOWN_RIGHT -> {
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

@SuppressLint("ConfigurationScreenWidthHeight")
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
    
    // defining all the features of the onboarding
    val features = remember {
        listOf(
            OnboardingFeature(
                title = "Søkefelt",
                description = "Søk etter steder for å se farevarsler og værvarsel",
                icon = Icons.Default.Search,
                color = Color(0xFFCE62FA),
                xPercent = 0f,
                yPercent = 0.09f,
                arrowDirection = ArrowDirection.UP
            ),
            OnboardingFeature(
                title = "Zoom-knapper",
                description = "Bruk disse knappene for å zoome inn og ut på kartet. Du kan også bruke to fingre",
                icon = Icons.Default.ZoomIn,
                color = Color(0xFF5FA8D3),
                xPercent = 0.2f,
                yPercent = 0.76f,
                arrowDirection = ArrowDirection.UP_LEFT
            ),
            OnboardingFeature(
                title = "Kartlag",
                description = "Trykk her for å vise eller skjule ulike informasjonslag på kartet",
                icon = Icons.Default.Layers,
                color = Color(0xFF9DC88D),
                xPercent = 0.2f,
                yPercent = 0.7f,
                arrowDirection = ArrowDirection.DOWN_LEFT
            ),
            OnboardingFeature(
                title = "Værvisning",
                description = "Her vises været for valgt lokasjon. Trykk for detaljert værmelding",
                icon = Icons.Default.WbSunny,
                color = Color(0xFFFCB927),
                xPercent = 0.5f,
                yPercent = 0.62f,
                arrowDirection = ArrowDirection.DOWN_RIGHT
            ),
            OnboardingFeature(
                title = "Min posisjon",
                description = "Her kan du trykke for å sentrere kartet til din nåværende posisjon",
                icon = Icons.Default.LocationOn,
                color = Color(0xFFD32F2F),
                xPercent = 0.5f,
                yPercent = 0.7f,
                arrowDirection = ArrowDirection.DOWN_RIGHT
            )
        )
    }
    
    // to go to the next step
    val goToNextStep = {
        currentStep = when (currentStep) {
            OnboardingStepType.WELCOME -> OnboardingStepType.SEARCH
            OnboardingStepType.SEARCH -> OnboardingStepType.ZOOM
            OnboardingStepType.ZOOM -> OnboardingStepType.LAYERS
            OnboardingStepType.LAYERS -> OnboardingStepType.LAYER_INFO
            OnboardingStepType.LAYER_INFO -> OnboardingStepType.WEATHER
            OnboardingStepType.WEATHER -> OnboardingStepType.LOCATION
            OnboardingStepType.LOCATION -> OnboardingStepType.LONG_PRESS
            OnboardingStepType.LONG_PRESS -> OnboardingStepType.COMPLETE
            OnboardingStepType.COMPLETE -> {
                viewModel.hideMapOnboarding()
                onFinish()
                OnboardingStepType.COMPLETE
            }
        }
    }
    
    // to go to the previous step
    val goToPreviousStep = {
        currentStep = when (currentStep) {
            OnboardingStepType.WELCOME -> OnboardingStepType.WELCOME
            OnboardingStepType.SEARCH -> OnboardingStepType.WELCOME
            OnboardingStepType.ZOOM -> OnboardingStepType.SEARCH
            OnboardingStepType.LAYERS -> OnboardingStepType.ZOOM
            OnboardingStepType.LAYER_INFO -> OnboardingStepType.LAYERS
            OnboardingStepType.WEATHER -> OnboardingStepType.LAYER_INFO
            OnboardingStepType.LOCATION -> OnboardingStepType.WEATHER
            OnboardingStepType.LONG_PRESS -> OnboardingStepType.LOCATION
            OnboardingStepType.COMPLETE -> OnboardingStepType.LONG_PRESS
        }
    }
    
    // to skip the onboarding process
    val skipOnboarding = {
        viewModel.hideMapOnboarding()
        onFinish()
    }

    val handleFeatureInteraction = { step: OnboardingStepType ->
        if (currentStep == step) {
            goToNextStep()
        }
    }

    val showMapOnboarding by viewModel.showMapOnboarding.collectAsState()

    if (showMapOnboarding) {
        Box(modifier = Modifier.fillMaxSize()) {
            // layer to thint the map behind the indicator
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(enabled = false) {}
            )

            Box(modifier = Modifier.fillMaxSize().clickable(enabled = false) {}) {
                // fetching the screen dimensions
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                
                // show the indicator based on step
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
                    else -> { /* else show nothing*/ }
                }
            }
        }
        // popup with instructions
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
                    // progress indicators for each step
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // showing the indicator
                        OnboardingStepType.entries.filter { it != OnboardingStepType.COMPLETE }.forEachIndexed { index, step ->
                            val isCurrentStep = step == currentStep
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (isCurrentStep) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCurrentStep) {
                                            when (step) {
                                                OnboardingStepType.WELCOME -> Color(0xFFF52860)
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

                    // content based on current step
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        }
                    ) { step ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (step) {
                                    OnboardingStepType.WELCOME -> Icons.Default.Explore
                                    OnboardingStepType.SEARCH -> features[0].icon
                                    OnboardingStepType.ZOOM -> features[1].icon
                                    OnboardingStepType.LAYERS -> features[2].icon
                                    OnboardingStepType.LAYER_INFO -> Icons.Default.Info
                                    OnboardingStepType.WEATHER -> features[3].icon
                                    OnboardingStepType.LOCATION -> features[4].icon
                                    OnboardingStepType.LONG_PRESS -> Icons.Default.TouchApp
                                    OnboardingStepType.COMPLETE -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = when (step) {
                                    OnboardingStepType.WELCOME -> Color(0xFFF52860)
                                    OnboardingStepType.SEARCH -> features[0].color
                                    OnboardingStepType.ZOOM -> features[1].color
                                    OnboardingStepType.LAYERS -> features[2].color
                                    OnboardingStepType.LAYER_INFO -> Color(0xFF42A5F5)
                                    OnboardingStepType.WEATHER -> features[3].color
                                    OnboardingStepType.LOCATION -> features[4].color
                                    OnboardingStepType.LONG_PRESS -> Color(0xFFEF8632 )
                                    OnboardingStepType.COMPLETE -> Color(0xFF4CAF50)
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // title
                            Text(
                                text = when (step) {
                                    OnboardingStepType.WELCOME -> "Velkommen til kartet!"
                                    OnboardingStepType.SEARCH -> features[0].title
                                    OnboardingStepType.ZOOM -> features[1].title
                                    OnboardingStepType.LAYERS -> features[2].title
                                    OnboardingStepType.LAYER_INFO -> "Informasjonsboks"
                                    OnboardingStepType.WEATHER -> features[3].title
                                    OnboardingStepType.LOCATION -> features[4].title
                                    OnboardingStepType.LONG_PRESS -> "Langt trykk"
                                    OnboardingStepType.COMPLETE -> "Du er klar!"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = when (step) {
                                    OnboardingStepType.WELCOME -> Color(0xFFF52860)
                                    OnboardingStepType.SEARCH -> features[0].color
                                    OnboardingStepType.ZOOM -> features[1].color
                                    OnboardingStepType.LAYERS -> features[2].color
                                    OnboardingStepType.LAYER_INFO -> Color(0xFF42A5F5)
                                    OnboardingStepType.WEATHER -> features[3].color
                                    OnboardingStepType.LOCATION -> features[4].color
                                    OnboardingStepType.LONG_PRESS -> Color(0xFFEF8632)
                                    OnboardingStepType.COMPLETE -> Color(0xFF4CAF50)
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // description
                            Text(
                                text = when (step) {
                                    OnboardingStepType.WELCOME -> "La oss ta en rask gjennomgang av funksjonene til kartet."
                                    OnboardingStepType.SEARCH -> features[0].description
                                    OnboardingStepType.ZOOM -> features[1].description
                                    OnboardingStepType.LAYERS -> features[2].description
                                    OnboardingStepType.LAYER_INFO -> "Når du aktiverer et kartlag vil det dukke opp en informasjonsboks på kartet, som gir deg informasjon om laget"
                                    OnboardingStepType.WEATHER -> features[3].description
                                    OnboardingStepType.LOCATION -> features[4].description
                                    OnboardingStepType.LONG_PRESS -> "Trykk og hold inne hvor som helst på kartet for å legge ut en markør og se været for den posisjonen"
                                    OnboardingStepType.COMPLETE -> "Du har nå lært om de viktigste funksjonene i appen. God tur på sjøen!"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (currentStep == OnboardingStepType.WELCOME) {

                                TextButton(
                                    onClick = skipOnboarding
                                ) {
                                    Text("Hopp over")
                                }
                            } else {

                                OutlinedButton(
                                    onClick = goToPreviousStep,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = when (currentStep) {
                                            OnboardingStepType.SEARCH -> features[0].color
                                            OnboardingStepType.ZOOM -> features[1].color
                                            OnboardingStepType.LAYERS -> features[2].color
                                            OnboardingStepType.LAYER_INFO -> Color(0xFF42A5F5)
                                            OnboardingStepType.WEATHER -> features[3].color
                                            OnboardingStepType.LOCATION -> features[4].color
                                            OnboardingStepType.LONG_PRESS -> Color(0xFFEF8632)
                                            OnboardingStepType.COMPLETE -> Color(0xFF4CAF50)
                                            else -> Color(0xFF1B4965)
                                        }
                                    )
                                ) {
                                    Text("Tilbake")
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                            Button(
                                onClick = goToNextStep,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (currentStep) {
                                        OnboardingStepType.WELCOME -> Color(0xFFF52860)
                                        OnboardingStepType.SEARCH -> features[0].color
                                        OnboardingStepType.ZOOM -> features[1].color
                                        OnboardingStepType.LAYERS -> features[2].color
                                        OnboardingStepType.LAYER_INFO -> Color(0xFF42A5F5)
                                        OnboardingStepType.WEATHER -> features[3].color
                                        OnboardingStepType.LOCATION -> features[4].color
                                        OnboardingStepType.LONG_PRESS -> Color(0xFFEF8632)
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
