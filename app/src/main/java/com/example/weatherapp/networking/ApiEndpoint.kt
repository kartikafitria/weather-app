package com.example.weatherapp.networking

object ApiEndpoint {
    var BASEURL = "https://api.openweathermap.org/data/2.5/"
    var CurrentWeather = "weather?"
    var ListWeather = "forecast?"
    var Daily = "forecast/daily?"
    var UnitsAppid = "&units=metric&appid=9a964ff634462a9d5665e2fde5d6672f"
    var UnitsAppidDaily = "&units=metric&appid=9a964ff634462a9d5665e2fde5d6672f"
}