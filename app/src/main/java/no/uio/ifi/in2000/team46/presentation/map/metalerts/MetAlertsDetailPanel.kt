package no.uio.ifi.in2000.team46.presentation.map.metalerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.domain.model.metalerts.Feature
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

@Composable
fun MetAlertsDetailsPanel(
    selectedMetAlert: Feature?,
    metAlertsViewModel: MetAlertsViewModel,
    modifier: Modifier = Modifier
) {
    selectedMetAlert?.let {
        Box(
            modifier = modifier
                .padding(16.dp)

                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp)
        ) {
            Column {
                IconButton(
                    onClick = { metAlertsViewModel.selectFeature(null) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }

                MetAlertsDetails(metAlertsViewModel = metAlertsViewModel)
            }
        }
    }
}