// File: app/src/test/java/no/uio/ifi/in2000/team46/GribApiTest.kt
package no.uio.ifi.in2000.team46

import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.team46.data.repository.GribFilesRepository

fun main() = runBlocking {
    val repository = GribFilesRepository()
    val gribData = repository.getCurrentGribFile()

    if (gribData != null) {
        println("API reachable. Received ${gribData.size} bytes.")
    } else {
        println("API not reachable or no data received.")
    }
}