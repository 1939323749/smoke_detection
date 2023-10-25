/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.cupcake.data

import android.net.Uri
import coil.compose.AsyncImagePainter

/**
 * Data class that represents the current UI state in terms of [quantity], [flavor],
 * [dateOptions], selected pickup [date] and [price]
 */
data class OrderUiState(
    val source:Int=0,
    val uri: Uri?=null,
    /** Selected cupcake quantity (1, 6, 12) */
    val quantity: Int = 0,
    /** Flavor of the cupcakes in the order (such as "Chocolate", "Vanilla", etc..) */
    val flavor: String = "",
    /** Selected date for pickup (such as "Jan 1") */
    val additionalItem:String = "",
    val date: String = "",
    /** Total price for the order */
    val price: String = "",
    /** Available pickup dates for the order*/
    val pickupOptions: List<String> = listOf(),

    val itemQuantity: Int =0
)
