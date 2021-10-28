package io.github.wagnerarcieri.memorygametemplate.model

import com.google.firebase.firestore.PropertyName

data class UserImageList (
    @PropertyName("images") val images: List<String>? = null
        )