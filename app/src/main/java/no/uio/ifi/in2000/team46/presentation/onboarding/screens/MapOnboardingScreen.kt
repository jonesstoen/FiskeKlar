package no.uio.ifi.in2000.team46.presentation.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.team46.presentation.onboarding.viewmodel.MapOnboardingViewModel

// Data for hvert spotlight-steg
private data class MapOnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val highlightColor: Color,
    val spotlightPosition: Offset, // proporsjonalt (0f..1f)
    val spotlightSize: Size // i dp
)

@Composable
fun MapOnboardingScreen(
    viewModel: MapOnboardingViewModel,
    onFinish: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val dummyFilterInfoVisible = remember { mutableStateOf(false) }

    // Definer spotlight-områder (proporsjonalt til skjermen)
    val steps = listOf(
        MapOnboardingStep(
            title = "Søkefelt",
            description = "Søk etter steder for å se farevarsler og værvarsel",
            icon = Icons.Default.Search,
            highlightColor = Color(0xFF1B4965),
            spotlightPosition = Offset(0.105f, 0.055f),
            spotlightSize = Size(64f, 56f)
        ),
        MapOnboardingStep(
            title = "Zoom-knapper",
            description = "Bruk disse knappene for å zoome inn og ut på kartet. Du kan også bruke to fingre",
            icon = Icons.Default.ZoomIn,
            highlightColor = Color(0xFF5FA8D3),
            spotlightPosition = Offset(0.11f, 0.80f),
            spotlightSize = Size(70f, 130f)
        ),
        MapOnboardingStep(
            title = "Filter-knapp",
            description = "Her kan du filtrere kartet for å se ulike lag og data",
            icon = Icons.Default.Layers,
            highlightColor = Color(0xFF9DC88D),
            spotlightPosition = Offset(0.11f, 0.935f),
            spotlightSize = Size(64f, 64f)
        ),
        MapOnboardingStep(
            title = "Filter-informasjon",
            description = "Når du har aktivert et filter, kan du trykke her for å lese mer om filtrene.",
            icon = Icons.Default.Info,
            highlightColor = Color(0xFF9DC88D),
            spotlightPosition = Offset(0.83f, 0.81f),
            spotlightSize = Size(48f, 48f)
        ),
        MapOnboardingStep(
            title = "Været",
            description = "Her ser du vær og temperatur for valgt sted (trykk og hold inne hvor som helst på kartet for å velge et sted), eller min posisjon dersom noe sted ikke er valgt",
            icon = Icons.Default.WbSunny,
            highlightColor = Color(0xFFBEE9E8),
            spotlightPosition = Offset(0.85f, 0.85f),
            spotlightSize = Size(100f, 72f)
        ),
        MapOnboardingStep(
            title = "Min posisjon",
            description = "Trykk her for å se din nåværende posisjon på kartet",
            icon = Icons.Default.LocationOn,
            highlightColor = Color(0xFFD32F2F),
            spotlightPosition = Offset(0.84f, 0.94f),
            spotlightSize = Size(64f, 64f)
        )
    )

    var currentStep by remember { mutableIntStateOf(0) }

    // Animer spotlight-posisjon og -størrelse
    val animatedSpotlightX by animateFloatAsState(
        targetValue = steps[currentStep].spotlightPosition.x,
        animationSpec = tween(durationMillis = 400)
    )
    val animatedSpotlightY by animateFloatAsState(
        targetValue = steps[currentStep].spotlightPosition.y,
        animationSpec = tween(durationMillis = 400)
    )
    val animatedSpotlightWidth by animateFloatAsState(
        targetValue = steps[currentStep].spotlightSize.width,
        animationSpec = tween(durationMillis = 400)
    )
    val animatedSpotlightHeight by animateFloatAsState(
        targetValue = steps[currentStep].spotlightSize.height,
        animationSpec = tween(durationMillis = 400)
    )

    // Vis dummy filter-info knapp kun på riktig steg
    dummyFilterInfoVisible.value = (currentStep == 3)

    val bottomNavHeight = 80.dp

    // Når onFinish kalles, oppdater ViewModel
    val handleFinish = {
        viewModel.hideMapOnboarding()
        onFinish()
    }

    // Vis Popup bare hvis showMapOnboarding er true
    val showMapOnboarding by viewModel.showMapOnboarding.collectAsState()
    
    if (showMapOnboarding) {
        Popup(
            onDismissRequest = handleFinish,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { screenHeightPx.toDp() - bottomNavHeight })
                    .offset(y = 0.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                // Spotlight-effekt
                Canvas(modifier = Modifier
                    .matchParentSize()
                    .zIndex(2f)) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    // For spotlight-effekten, øk størrelsen litt på hullet for å gjøre det mer synlig
                    val spotlightWidthPx = with(density) { (animatedSpotlightWidth * 1.1f).dp.toPx() }
                    val spotlightHeightPx = with(density) { (animatedSpotlightHeight * 1.1f).dp.toPx() }
                    val center = Offset(
                        x = animatedSpotlightX * canvasWidth,
                        y = animatedSpotlightY * canvasHeight
                    )

                    // Lag en path med avrundede hjørner
                    val left = center.x - spotlightWidthPx / 2
                    val top = center.y - spotlightHeightPx / 2
                    val right = left + spotlightWidthPx
                    val bottom = top + spotlightHeightPx

                    // Lag en path som vi kan tegne gjennom med clear blendMode
                    val path = Path().apply {
                        addOval(androidx.compose.ui.geometry.Rect(left, top, right, bottom))
                    }

                    // Tegn spotlight-cutout med clear blendMode
                    drawPath(
                        path = path,
                        color = Color.Transparent,
                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                    )

                    // Tegn en tykkere kantlinje rundt cutout-en for bedre synlighet
                    drawPath(
                        path = path,
                        color = steps[currentStep].highlightColor,
                        style = Stroke(width = 6f)
                    )
                }

                // Innholdskort
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(4f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(bottom = 80.dp),
                        shape = RoundedCornerShape(24.dp),
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
                                    .padding(bottom = 24.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                steps.forEachIndexed { index, _ ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == currentStep)
                                                    steps[currentStep].highlightColor
                                                else
                                                    Color.Gray.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }

                            // Steg-innhold
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
                                        imageVector = steps[step].icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = steps[step].highlightColor
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = steps[step].title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = steps[step].highlightColor
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = steps[step].description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Navigasjonsknapper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (currentStep > 0) {
                                    TextButton(
                                        onClick = { currentStep-- },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = steps[currentStep].highlightColor
                                        )
                                    ) {
                                        Text("Tilbake")
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(64.dp))
                                }

                                Button(
                                    onClick = {
                                        if (currentStep < steps.size - 1) {
                                            currentStep++
                                        } else {
                                            onFinish()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = steps[currentStep].highlightColor
                                    )
                                ) {
                                    Text(
                                        if (currentStep < steps.size - 1) "Neste" else "Ferdig",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Dummy filter-info knapp (for steg 3)
                if (dummyFilterInfoVisible.value) {
                    val centerX = animatedSpotlightX * screenWidthPx
                    val centerY = animatedSpotlightY * (screenHeightPx - with(density) { bottomNavHeight.toPx() })
                    val buttonSizeDp = 40.dp
                    val buttonSizePx = with(density) { buttonSizeDp.toPx() }

                    Surface(
                        modifier = Modifier
                            .absoluteOffset {
                                IntOffset(
                                    (centerX - buttonSizePx / 2).toInt(),
                                    (centerY - buttonSizePx / 2).toInt()
                                )
                            }
                            .size(buttonSizeDp)
                            .zIndex(3f),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Filter-informasjon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}