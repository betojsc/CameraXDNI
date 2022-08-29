package com.jsalazar.cameraxdni.utils

import android.content.Context
import android.view.OrientationEventListener
import android.view.Surface

/**
 * Listens to motion sensor reading and converts the orientation degrees to [Surface]
 * rotation.
 *
 * @hide
 */
abstract class SensorRotationListener(context: Context) :
    OrientationEventListener(context) {
    private var mRotation = INVALID_SURFACE_ROTATION
    override fun onOrientationChanged(orientation: Int) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            // Short-circuit if orientation is unknown. Unknown rotation can't be handled so it
            // shouldn't be sent.
            return
        }
        val newRotation: Int = if (orientation >= 315 || orientation < 45) {
            Surface.ROTATION_0
        } else if (orientation >= 225) {
            Surface.ROTATION_90
        } else if (orientation >= 135) {
            Surface.ROTATION_180
        } else {
            Surface.ROTATION_270
        }
        if (mRotation != newRotation) {
            mRotation = newRotation
            onRotationChanged(newRotation)
        }
    }

    /**
     * Invoked when rotation changes.
     *
     *
     *  The output rotation is defined as the UI Surface rotation, or what the Surface rotation
     * should be if the app's orientation is not locked.
     */
    abstract fun onRotationChanged(rotation: Int)

    companion object {
        const val INVALID_SURFACE_ROTATION = -1
    }
}