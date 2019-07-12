package com.example.imageoverlay.winsontanLibrary

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

interface IWScratchView {

    /**
     * Whether the view receive user on touch motion
     *
     * @return true if scratchable
     */
    abstract fun isScratchable(): Boolean

    /**
     * If true, set the view allow receive on touch to reveal the view
     * By default, scratchable is true
     *
     * @param flag - flag for enable/disable scratch
     */
    abstract fun setScratchable(flag: Boolean)

    /**
     * Set the color of overlay
     *
     * @param ResId - resources identifier for color in INT type
     */
    abstract fun setOverlayColor(ResId: Int)

    /**
     * Set the radius size of the circle to be revealed
     *
     * @param size - radius size of circle in pixel unit
     */
    abstract fun setRevealSize(size: Int)

    /**
     * Set turn on/off effect of anti alias of circle revealed
     * By default, anti alias is turn off
     *
     * @param flag - set true to turn on anti alias
     */
    abstract fun setAntiAlias(flag: Boolean)

    /**
     * Reset the scratch view
     *
     */
    abstract fun resetView()

    /**
     * Set drawable for scratch view
     *
     * @param drawable - Set drawable for scratch view
     */
    abstract fun setScratchDrawable(drawable: Drawable)

    /**
     * Set bitmap for scratch view
     *
     * @param bitmap - Set bitmap for scratch view
     */
    abstract fun setScratchBitmap(b: Bitmap)

    /**
     * Get scratched ratio (contribution from daveyfong)
     *
     * @return  float - return Scratched ratio
     */
    abstract fun getScratchedRatio(): Float

    /**
     * Get scratched ratio (contribution from daveyfong)
     *
     * @param int - Scratch speed
     * @return  float - return Scratched ratio
     */
    abstract fun getScratchedRatio(speed: Int): Float

    abstract fun setOnScratchCallback(callback: WScratchView.OnScratchCallback)

    abstract fun setScratchAll(scratchAll: Boolean)

    abstract fun setBackgroundClickable(clickable: Boolean)
}
