package no.uio.ifi.in2000.team46.presentation.map.metalerts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import no.uio.ifi.in2000.team46.domain.metalerts.Feature
import no.uio.ifi.in2000.team46.domain.metalerts.Geometry
import no.uio.ifi.in2000.team46.domain.metalerts.MetAlertsResponse
import no.uio.ifi.in2000.team46.domain.metalerts.Properties
import no.uio.ifi.in2000.team46.domain.metalerts.WhenInfo

// This file contains the UI for the details panel that displays information about a selected weather alert
@Composable
fun MetAlertsDetails(metAlertsViewModel: MetAlertsViewModel) {
    val selectedFeature by metAlertsViewModel.selectedFeature.collectAsState()

    selectedFeature?.let { feature ->
        val props = feature.properties
        Column {
            Text("Area: ${props.area}")
            Text("Title: ${props.title}")
            Text("Description: ${props.description}")
            Text("Consequences: ${props.consequences}")
            Text("Risk Color: ${props.riskMatrixColor}")
            Text("Awareness Level: ${props.awarenessLevel}")
            Text("Instructions: ${props.instruction}")

        }
    } ?: Text("Select an alert on the map to see details")
}
@Composable
@Preview(showBackground = true)
fun MetAlertsDetailsPreview() {
    // Mock data for preview
    val mockFeature = Feature(
        geometry = Geometry(
            coordinates = listOf<Any>(),
            type = "Polygon"
        ),
        properties = Properties(
            altitudeAboveSeaLevel = 0,
            area = "Oslo",
            awarenessResponse = "Be aware",
            awarenessSeriousness = "Moderate",
            awarenessLevel = "2",
            awarenessType = "Weather",
            ceilingAboveSeaLevel = 0,
            certainty = "Likely",
            consequences = "Possible flooding in low-lying areas",
            contact = "met.no",
            county = listOf("Oslo", "Viken"),
            description = "Heavy rainfall expected in the Oslo area",
            event = "Rain",
            eventAwarenessName = "Heavy rainfall",
            eventEndingTime = "2023-06-15T18:00:00Z",
            geographicDomain = "Land",
            id = "123456",
            instruction = "Stay indoors if possible",
            resources = listOf(),
            riskMatrixColor = "Yellow",
            severity = "Moderate",
            status = "Actual",
            title = "Heavy rainfall warning for Oslo",
            triggerLevel = "2",
            type = "Warning",
            web = "https://www.met.no"
        ),
        timeInfo = WhenInfo(
            interval = listOf("2023-06-15T12:00:00Z", "2023-06-15T18:00:00Z")
        ),
        type = "Feature"
    )

    val mockResponse = MetAlertsResponse(
        features = listOf(mockFeature),
        lang = "en",
        lastChange = "2023-06-14T10:00:00Z",
        type = "FeatureCollection"
    )

    // Create direct UI rendering of the mock data
    Column {
        Text("Language: ${mockResponse.lang}")
        Text("Last Change: ${mockResponse.lastChange}")

        LazyColumn {
            items(mockResponse.features) { feature ->
                val props = feature.properties
                Column {
                    Text("Title: ${props.title}")
                    Text("Description: ${props.description}")
                    Text("Consequences: ${props.consequences}")
                    Text("Risk Color: ${props.riskMatrixColor}")
                }
                Divider()
            }
        }
    }
}