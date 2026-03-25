package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.content.Context
import android.content.res.Configuration
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Utility object to help with responsive design decisions
 */
object ResponsiveUtils {

    /**
     * Check if the device is in landscape orientation
     * @param context Application context
     * @return true if the device is in landscape orientation
     */
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * Get the screen width in dp
     * @param context Application context
     * @return screen width in dp
     */
    fun getScreenWidthDp(context: Context): Float {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels / displayMetrics.density
    }

    /**
     * Get the screen height in dp
     * @param context Application context
     * @return screen height in dp
     */
    fun getScreenHeightDp(context: Context): Float {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels / displayMetrics.density
    }

    /**
     * Calculate how many columns should be used in a grid based on screen width
     * @param context Application context
     * @param preferredColumnWidthDp Preferred minimum width for each column in dp
     * @return number of columns that fit in the current screen
     */
    fun calculateOptimalColumnCount(context: Context, preferredColumnWidthDp: Float): Int {
        val screenWidthDp = getScreenWidthDp(context)
        return max(1, (screenWidthDp / preferredColumnWidthDp).roundToInt())
    }
}