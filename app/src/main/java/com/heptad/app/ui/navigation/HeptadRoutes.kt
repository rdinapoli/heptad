package com.heptad.app.ui.navigation

/**
 * Navigation routes for the Heptad app.
 */
sealed class HeptadRoute(val route: String) {
    /**
     * Main menu screen - the app's entry point.
     */
    object MainMenu : HeptadRoute("main_menu")

    /**
     * Daily puzzle screen.
     */
    object DailyPuzzle : HeptadRoute("daily_puzzle")

    /**
     * Random puzzle screen (unlimited play mode).
     */
    object RandomPuzzle : HeptadRoute("random_puzzle")

    /**
     * Settings screen.
     */
    object Settings : HeptadRoute("settings")
}
