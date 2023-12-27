/*
* MIT License
*
* Copyright (c) 2022 Shreyas Patil
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*
*/
package dev.shreyaspatil.capturable.controller

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Controller for capturing [Composable] content.
 * @see dev.shreyaspatil.capturable.Capturable for implementation details.
 */
class CaptureController internal constructor() {

    /**
     * Medium for providing capture requests
     */
    private val _captureRequests = MutableSharedFlow<CaptureRequest>(extraBufferCapacity = 1)
    internal val captureRequests = _captureRequests.asSharedFlow()

    /**
     * Creates and sends a Bitmap capture request with specified [config].
     * The [CaptureResult] can be gathered using the [onCapture] callback.
     *
     * Make sure to call this method as a part of callback function and not as a part of the
     * [Composable] function itself.
     *
     * @param config Bitmap config of the desired bitmap. Defaults to [Bitmap.Config.ARGB_8888]
     */
    fun capture(
        config: Bitmap.Config = Bitmap.Config.ARGB_8888,
        onCapture: (CaptureResult) -> Unit
    ) {
        _captureRequests.tryEmit(CaptureRequest(config, onCapture))
    }

    /**
     * Creates and sends a Bitmap capture request with specified [config].
     * The [CaptureResult] is returned from the function.
     *
     * Make sure to call this method as a part of callback function and not as a part of the
     * [Composable] function itself.
     *
     * @param config Bitmap config of the desired bitmap. Defaults to [Bitmap.Config.ARGB_8888]
     */
    suspend fun capture(config: Bitmap.Config = Bitmap.Config.ARGB_8888): CaptureResult {
        return suspendCoroutine { continuation ->
            val request = CaptureRequest(config) {
                continuation.resume(it)
            }
            _captureRequests.tryEmit(request)
        }
    }

    /**
     * Holds information of capture request
     */
    internal class CaptureRequest(
        val config: Bitmap.Config,
        val onCapture: (CaptureResult) -> Unit
    )
}

/**
 * Creates [CaptureController] and remembers it.
 */
@Composable
fun rememberCaptureController(): CaptureController {
    return remember { CaptureController() }
}

@Immutable
sealed interface CaptureResult {

    @JvmInline
    @Immutable
    value class Success(val bitmap: ImageBitmap) : CaptureResult

    @JvmInline
    @Immutable
    value class Error(val error: Throwable) : CaptureResult
}
