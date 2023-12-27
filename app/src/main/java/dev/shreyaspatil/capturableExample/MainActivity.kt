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
package dev.shreyaspatil.capturableExample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.CaptureResult
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import dev.shreyaspatil.capturableExample.ui.theme.CapturableExampleTheme
import dev.shreyaspatil.capturableExample.ui.theme.Teal200
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CapturableExampleTheme {
                Surface(color = Teal200, modifier = Modifier.fillMaxSize()) {
                    TicketScreen()
                }
            }
        }
    }
}

@Immutable
sealed interface TicketScreenState {
    @JvmInline
    @Immutable
    value class HasImage(val bitmap: ImageBitmap) : TicketScreenState

    @JvmInline
    @Immutable
    value class HasError(val error: String) : TicketScreenState

    @Immutable
    data object Empty : TicketScreenState
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TicketScreen() {
    val uiScope = rememberCoroutineScope()

    var state by remember { mutableStateOf<TicketScreenState>(TicketScreenState.Empty) }
    val captureController = rememberCaptureController()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(24.dp)
    ) {
        // The content to be captured ⬇️
        Ticket(
            modifier = Modifier.capturable(captureController)
        )

        Spacer(modifier = Modifier.size(32.dp))

        // Captures ticket bitmap on click
        Button(onClick = {
            uiScope.launch {
                val result = captureController.capture()
                state = when (result) {
                    is CaptureResult.Success -> TicketScreenState.HasImage(result.bitmap)
                    is CaptureResult.Error -> TicketScreenState.HasError(
                        result.error.localizedMessage
                            ?: result.error.message
                            ?: result.error.stackTraceToString()
                    )
                }
            }
        }) {
            Text("Preview Ticket Image")
        }

        // When Ticket's Bitmap image is captured, show preview in dialog
        if (state is TicketScreenState.HasImage) {
            Dialog(onDismissRequest = { }) {
                Surface {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Preview of Ticket image \uD83D\uDC47")
                        Spacer(Modifier.size(16.dp))
                        Image(
                            bitmap = (state as TicketScreenState.HasImage).bitmap,
                            contentDescription = "Preview of ticket"
                        )
                        Spacer(Modifier.size(4.dp))
                        Button(onClick = { state = TicketScreenState.Empty }) {
                            Text("Close Preview")
                        }
                    }
                }
            }
        }

        if (state is TicketScreenState.HasError) {
            Dialog(onDismissRequest = { }) {
                Surface {
                    Box(
                        modifier = Modifier.padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Preview of Ticket image \uD83D\uDC47")
                        Text((state as TicketScreenState.HasError).error)
                    }
                }
            }
        }
    }
}

@Composable
fun Ticket(modifier: Modifier) {
    Card(modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BookingConfirmedContent()
            MovieTitle()
            Spacer(modifier = Modifier.size(32.dp))
            BookingDetail()
            Spacer(modifier = Modifier.size(32.dp))
            BookingQRCode()
        }
    }
}

@Composable
fun BookingConfirmedContent() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Booking Confirmed",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.weight(0.8f)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_baseline_check_circle_24),
            contentDescription = "Successfully booked",
            modifier = Modifier
                .weight(0.2f)
                .size(128.dp)
        )
    }
}

@Composable
fun MovieTitle() {
    Text(
        text = "Jetpack Compose - The Movie",
        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
    )
}

@Composable
fun BookingDetail() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Sat, 1 Jan", style = MaterialTheme.typography.caption)
            Text("12:45 PM", style = MaterialTheme.typography.subtitle2)
        }

        Column {
            Text("SCREEN", style = MaterialTheme.typography.caption)
            Text("JET 01", style = MaterialTheme.typography.subtitle2)
        }

        Column {
            Text("SEATS", style = MaterialTheme.typography.caption)
            Text("J1, J2, J3", style = MaterialTheme.typography.subtitle2)
        }
    }
}

@Composable
fun BookingQRCode() {
    Text(
        text = "----- SCAN QR CODE AT CINEMA -----",
        style = MaterialTheme.typography.overline
    )
    Icon(
        painter = painterResource(R.drawable.ic_baseline_qr_code_24),
        contentDescription = "Success",
        modifier = Modifier.size(128.dp)
    )

    Text("Booking ID: JETPACK0000012345", style = MaterialTheme.typography.caption)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CapturableExampleTheme {
        TicketScreen()
    }
}