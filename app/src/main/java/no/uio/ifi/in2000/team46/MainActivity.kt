package no.uio.ifi.in2000.team46

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import no.uio.ifi.in2000.team46.presentation.ui.screens.MapScreen
import no.uio.ifi.in2000.team46.presentation.ui.theme.TEAM46Theme
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this,"kPH7fJZHXa4Pj6d1oIuw" , WellKnownTileServer.MapTiler)
        enableEdgeToEdge()
        setContent {
            TEAM46Theme {
                Scaffold {contentPadding ->
                    MapScreen(modifier = Modifier.padding(contentPadding))
                }
            }
        }
    }
}

