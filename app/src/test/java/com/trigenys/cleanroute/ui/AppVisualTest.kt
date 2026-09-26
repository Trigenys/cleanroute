package com.trigenys.cleanroute.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.trigenys.cleanroute.ui.theme.CleanRouteTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w390dp-h844dp")
class AppVisualTest {
    @Test
    fun appHomeLight() {
        captureRoboImage {
            CleanRouteTheme(darkTheme = false) {
                App()
            }
        }
    }

    @Test
    fun appHomeDark() {
        captureRoboImage {
            CleanRouteTheme(darkTheme = true) {
                App()
            }
        }
    }
}
