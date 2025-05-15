package no.uio.ifi.in2000.team46.presentation.map.metalerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.domain.metalerts.Feature
import no.uio.ifi.in2000.team46.utils.BulletText
import no.uio.ifi.in2000.team46.utils.formatTime
import no.uio.ifi.in2000.team46.utils.timeUntilStart
// this file contains the UI for the bottom sheet that displays details about a selected weather alert
// warnings in this file is related to some claryfing parenthesis etc, which we did not find necessary to remove, because we couldnt fin other way to make it work
@Composable
fun MetAlertsBottomSheetContent(
    feature: Feature,
    onClose: () -> Unit
) {
    val props = feature.properties
    val interval = if (feature.timeInfo?.interval?.size ?: 0 >= 2) {
        "${formatTime(feature.timeInfo.interval[0])} – ${formatTime(feature.timeInfo.interval[1])}"
    } else {
        "Ukjent"
    }
    val warningIcon = when (props.riskMatrixColor.lowercase()) {
        "yellow" -> R.drawable.icon_warning_generic_yellow
        "orange" -> R.drawable.icon_warning_generic_orange
        "red" -> R.drawable.icon_warning_generic_red
        else -> R.drawable.icon_warning_generic_yellow // default to yellow if color is unknown
    }
    val startsIn = feature.timeInfo.interval.let { interval ->
        if (interval.size >= 2) {
            timeUntilStart(interval[0], interval[1])
        } else null
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // title and icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = warningIcon),
                contentDescription = "Warning level: ${props.riskMatrixColor}",

                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = props.eventAwarenessName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

        }
        startsIn?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // area and interval
        Text(
            text = "Område: ${props.area}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Periode: $interval",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // description of the alert
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BulletText("Mulige konsekvenser: ${props.consequences}")
            props.instruction.let {
                BulletText("Instruksjoner: $it")
                BulletText("Kontakt: ${props.contact}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // close button
        Button(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Lukk")
        }
    }
}

