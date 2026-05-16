package com.pborrull.ft_hangouts.models

data class Contact(
    var id: Long,
    var name: String,
    var phone: String,
    var email: String,
    var address: String,
    var notes: String,
    var photo_uri: String? = null
)
