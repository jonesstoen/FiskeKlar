package no.uio.ifi.in2000.team46.presentation.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// summary: presents a multi-step onboarding dialog with icons and navigation controls
// main function: displays sequential screens explaining app features and handles user navigation through steps

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val highlightColor: Color
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    // list of onboarding steps with title, description, icon and accent color
    val steps = listOf(
        OnboardingStep(
            title = "Velkommen til Fiskeappen!",
            description = "Din komplette fiskeguide for en trygg og god fiskeopplevelse",
            icon = Icons.Default.Home,
            highlightColor = Color(0xFF1B4965)
        ),
        OnboardingStep(
            title = "Kart og Fiskesteder",
            description = "Utforsk fiskesteder, se værvarsel, fartøysposisjoner, farevarsler og mer",
            icon = Icons.Default.Map,
            highlightColor = Color(0xFF5FA8D3)
        ),
        OnboardingStep(
            title = "Fiskelogg",
            description = "Registrer dine fangster og hold oversikt over fisketuren din",
            icon = Icons.AutoMirrored.Filled.List,
            highlightColor = Color(0xFF9DC88D)
        ),
        OnboardingStep(
            title = "Værvarsel",
            description = "Sjekk værvarsel for hvor som helst i verden",
            icon = Icons.Default.WbSunny,
            highlightColor = Color(0xFFFCB927)
        ),
        OnboardingStep(
            title = "Favoritter",
            description = "Legg til dine favorittsteder og områder for enklere tilgang til vær og farevarsler",
            icon = Icons.Default.Favorite,
            highlightColor = Color(0xFFE57373)
        ),
        OnboardingStep(
            title = "SOS og Sikkerhet",
            description = "Rask tilgang til nødvarsling og rute til nærmeste fartøy",
            icon = Icons.Default.Warning,
            highlightColor = Color(0xFFD32F2F)
        )
    )

    // state holding current step index
    var currentStep by remember { mutableIntStateOf(0) }

    // dialog container that blocks back press and outside clicks
    Dialog(
        onDismissRequest = onFinish,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
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

                // animate transition between step content
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    }
                ) { step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // icon with circular background highlight
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(steps[step].highlightColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = steps[step].icon,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = steps[step].highlightColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // step title
                        Text(
                            text = steps[step].title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = steps[step].highlightColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // step description
                        Text(
                            text = steps[step].description,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // navigation buttons: back or next/finish
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
                        // placeholder to align buttons
                        Spacer(modifier = Modifier.width(64.dp))
                    }

                    Button(
                        onClick = {
                            // advance to next step or finish
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
                            if (currentStep < steps.size - 1) "Neste" else "Kom i gang",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
