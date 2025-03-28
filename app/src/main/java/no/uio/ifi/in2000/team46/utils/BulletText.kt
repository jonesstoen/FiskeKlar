package no.uio.ifi.in2000.team46.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
/**
 * BulletText is a  Composable function that displays a list of bullet points.
 */
@Composable
fun BulletText(text: String) {
    Row {
        Text("•", modifier = Modifier.padding(end = 8.dp))
        Text(text)
    }
}
