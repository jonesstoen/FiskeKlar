package no.uio.ifi.in2000.team46.presentation.grib.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// this composable shows a legend box with icons representing different wind speed ranges
// it supports switching between light and dark versions of each icon depending on theme

@SuppressLint("DiscouragedApi")
@Composable
fun WindLegend(
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    // each entry has a label (speed range) and base drawable name
    val windLegendItems = listOf(
        Pair("0.0 – 0.2 m/s", "symbol_wind_speed_00"),
        Pair("0.3 – 1.5 m/s", "symbol_wind_speed_15"),
        Pair("1.6 – 3.3 m/s", "symbol_wind_speed_33"),
        Pair("3.4 – 5.4 m/s", "symbol_wind_speed_54"),
        Pair("5.5 – 7.9 m/s", "symbol_wind_speed_79"),
        Pair("8.0 – 10.7 m/s", "symbol_wind_speed_107"),
        Pair("10.8 – 13.8 m/s", "symbol_wind_speed_138"),
        Pair("13.9 – 17.1 m/s", "symbol_wind_speed_171"),
        Pair("17.2 – 20.7 m/s", "symbol_wind_speed_207"),
        Pair("20.8 – 24.4 m/s", "symbol_wind_speed_244"),
        Pair("24.5 – 28.4 m/s", "symbol_wind_speed_284"),
        Pair("28.5 – 32.6 m/s", "symbol_wind_speed_326"),
        Pair("≥ 32.7 m/s", "symbol_wind_speed_max")
    )

    val context = LocalContext.current

    Surface(
        modifier = modifier
            .padding(8.dp)
            .width(170.dp)
            .height(300.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // header with title and divider
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)
            ) {
                Text("Vindstyrker (m/s)", style = MaterialTheme.typography.titleSmall)
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // scrollable list of icon-label pairs
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                windLegendItems.forEach { (label, baseName) ->
                    // choose dark or light version of icon depending on theme
                    val resName = if (isDark) "${baseName}_white" else baseName
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)

                    if (resId != 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = resId),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
