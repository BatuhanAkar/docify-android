package com.batuscode.docunote.v2.data.repository

import android.net.Uri
import com.batuscode.docunote.v2.domain.model.SourceEntity
import com.batuscode.docunote.v2.domain.model.SourceType
import com.batuscode.docunote.v2.domain.model.UploadState
import com.batuscode.docunote.v2.domain.repository.StorageService
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class StorageServiceImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageService {

    override fun uploadSource(source: SourceEntity): Flow<UploadState> = callbackFlow {
        // Eğer kaynak WEBSITE veya YOUTUBE ise Storage'a yükleme yapmaya gerek yok!
        if (source.type == SourceType.WEBSITE || source.type == SourceType.YOUTUBE) {
            trySend(UploadState.Progress(100))
            trySend(UploadState.Success(source)) // Değişiklik yapmadan geri döndür
            close()
            return@callbackFlow
        }

        val storageRef = storage.reference.child("uploads/${source.type.name.lowercase()}/${source.id}")
        val uploadTask = when (source.type) {
            SourceType.TEXT -> {
                // Stratejik Karar: Paste Text'i ByteArray'e çevirip .txt olarak Storage'a atıyoruz
                val textBytes = source.localUri?.toByteArray(Charsets.UTF_8) ?: ByteArray(0)
                storageRef.child("${source.id}.txt").putBytes(textBytes)
            }
            else -> {
                // PDF ve AUDIO için cihazdaki yerel URI üzerinden yükleme yapıyoruz
                val uri = Uri.parse(source.localUri)
                storageRef.putFile(uri)
            }
        }

        // Yükleme İlerlemesini Dinle
        uploadTask.addOnProgressListener { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
            trySend(UploadState.Progress(progress))
        }.addOnSuccessListener { snapshot ->
            // Yükleme bittiğinde, gs:// yolunu oluşturup Entity'i güncelliyoruz
            val gsPath = "gs://${storage.reference.bucket}/${snapshot.storage.path}"
            val updatedSource = source.copy(storagePath = gsPath)

            trySend(UploadState.Progress(100))
            trySend(UploadState.Success(updatedSource))
            close()
        }.addOnFailureListener { exception ->
            trySend(UploadState.Error(exception))
            close()
        }

        awaitClose { uploadTask.cancel() }
    }
}