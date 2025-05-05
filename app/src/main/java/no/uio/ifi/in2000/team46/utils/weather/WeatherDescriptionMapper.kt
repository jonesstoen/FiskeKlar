package no.uio.ifi.in2000.team46.utils.weather

/**
 * Utility-klasse for å mappe værkoder til beskrivende tekst
 */
object WeatherDescriptionMapper {
    fun getWeatherDescription(symbolCode: String?): String {
        return when {
            symbolCode == null -> "Værdata ikke tilgjengelig"
            
            // Klarvær og lettskyet
            symbolCode.contains("clearsky") -> "Klart vær med sol"
            symbolCode.contains("fair") -> "Lettskyet med gode solforhold"
            
            // Skyet vær
            symbolCode.contains("partlycloudy") -> "Delvis skyet, perioder med sol"
            symbolCode.contains("cloudy") -> "Overskyet"
            
            // Regn
            symbolCode.contains("lightrainshowers") -> "Lette regnbyger"
            symbolCode.contains("rainshowers") -> "Regnbyger"
            symbolCode.contains("heavyrainshowers") -> "Kraftige regnbyger"
            symbolCode.contains("lightrain") -> "Lett regn"
            symbolCode.contains("rain") -> "Regn"
            symbolCode.contains("heavyrain") -> "Kraftig regn"
            
            // Snø
            symbolCode.contains("lightsnowshowers") -> "Lette snøbyger"
            symbolCode.contains("snowshowers") -> "Snøbyger"
            symbolCode.contains("heavysnowshowers") -> "Kraftige snøbyger"
            symbolCode.contains("lightsnow") -> "Lett snø"
            symbolCode.contains("snow") -> "Snø"
            symbolCode.contains("heavysnow") -> "Kraftig snøfall"
            
            // Sludd
            symbolCode.contains("lightsleetshowers") -> "Lette sluddbyger"
            symbolCode.contains("sleetshowers") -> "Sluddbyger"
            symbolCode.contains("heavysleetshowers") -> "Kraftige sluddbyger"
            symbolCode.contains("lightsleet") -> "Lett sludd"
            symbolCode.contains("sleet") -> "Sludd"
            symbolCode.contains("heavysleet") -> "Kraftig sludd"
            
            // Tåke
            symbolCode.contains("fog") -> "Tåke, redusert sikt"
            
            // Tordenvær
            symbolCode.contains("thunder") && symbolCode.contains("rain") -> "Regnvær med torden"
            symbolCode.contains("thunder") && symbolCode.contains("snow") -> "Snøvær med torden"
            symbolCode.contains("thunder") && symbolCode.contains("sleet") -> "Sludd med torden"
            symbolCode.contains("thunder") -> "Tordenvær"
            
            else -> "Varierende vær"
        }
    }
} 