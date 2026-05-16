package com.pborrull.ft_hangouts.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val id: Long = 0,
    val contactId: Long,
    val body: String,
    val timestamp: Long,
    val sentByMe: Boolean = false
) : Parcelable