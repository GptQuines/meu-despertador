package com.despertador.app.ui.navigation

sealed class Routes(val route: String, val title: String, val icon: String) {
    data object Alarm : Routes("alarm", "Despertador", "alarm")
    data object WorldClock : Routes("world_clock", "Rel\u00f3gio Mundial", "globe")
    data object Stopwatch : Routes("stopwatch", "Cron\u00f4metro", "timer")
    data object Timer : Routes("timer", "Temporizador", "hourglass")
}
