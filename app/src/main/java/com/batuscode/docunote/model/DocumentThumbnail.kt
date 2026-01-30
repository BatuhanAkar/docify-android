package com.batuscode.docunote.model

import androidx.compose.ui.graphics.ImageBitmap
import java.util.Objects

data class DocumentThumbnail(
    var document: Document ,
    var image: ImageBitmap

) {
   /* override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null || javaClass != other.javaClass) return false

        val documentThumbnail: DocumentThumbnail = other as DocumentThumbnail

        return document.id == documentThumbnail.document.id
    }

    override fun hashCode(): Int {
        return Objects.hash(document.id)
    }*/
}
