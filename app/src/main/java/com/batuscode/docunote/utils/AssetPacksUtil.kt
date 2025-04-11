package com.batuscode.docunote.utils

import android.content.Intent
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.mutableStateOf
import com.batuscode.docunote.DocifyAI
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.MainActivity.Companion.context
import com.batuscode.docunote.MainActivity.Companion.llmInference
import com.batuscode.docunote.SystemPrompts
import com.google.android.play.core.assetpacks.AssetPackLocation
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import com.batuscode.docunote.R

object AssetPacksUtil {
    var confirmationDialogShown = mutableStateOf(false)
    var fromExtensions = mutableStateOf(false)
    const val assetPackName = "docifyai"
    val apm = AssetPackManagerFactory.getInstance(context)

    val qlistener = apm.registerListener { assetPackState ->
        when(assetPackState.status()) {
            AssetPackStatus.PENDING -> {
                Log.i("AssetPack", "Pending")
                MainActivity.expdwstatus.value = "Pending"
            }
            AssetPackStatus.DOWNLOADING -> {
                MainActivity.expdwstatus.value = "Downloading"

                val downloaded = assetPackState.bytesDownloaded()
                val totalSize = assetPackState.totalBytesToDownload()
                val percent = 100 * (downloaded.toFloat() / totalSize.toFloat())
                MainActivity.progress.value = percent

                Log.i("AssetPack", "PercentDone=" + String.format("%.2f", percent))
            }
            AssetPackStatus.TRANSFERRING -> {
                // 100% downloaded and assets are being transferred.
                // Notify user to wait until transfer is complete.
                MainActivity.expdwstatus.value = "Transferring"

            }
            AssetPackStatus.COMPLETED -> {
                // Asset pack is ready to use. Start the game.


                val path = getAbsoluteAssetPath(assetPackName , "ai/gemma3_1b_it_int4.task")
                CoroutineScope(Dispatchers.IO).launch{
                    MainActivity.llmInference = SystemPrompts.initInference(path!!, context)

                    if (MainActivity.showDownloadProg.value){
                        MainActivity.waitinit.value = true
                        MainActivity.showDownloadProg.value = false
                    }
                }
            }
            AssetPackStatus.FAILED -> {
                // Request failed. Notify user.
                Log.e("AssetPack", assetPackState.errorCode().toString())
            }
            AssetPackStatus.CANCELED -> {
                // Request canceled. Notify user.
            }
            AssetPackStatus.WAITING_FOR_WIFI,
            AssetPackStatus.REQUIRES_USER_CONFIRMATION -> {
                if (!confirmationDialogShown.value) {
                    apm.showConfirmationDialog(MainActivity.dynamicFeatureLauncher);
                    confirmationDialogShown.value = true
                }
            }
            AssetPackStatus.NOT_INSTALLED -> {
                // Asset pack is not downloaded yet.
                MainActivity.showDFRDialog.value = true
            }
            AssetPackStatus.UNKNOWN -> {
                Log.wtf("AssetPack", "Asset pack status unknown")
            }
        }
    }

    suspend fun isPacksInstalled(){
          withContext(Dispatchers.IO){

              apm.getPackStates(listOf(assetPackName))
                  .addOnSuccessListener { states ->
                      val state = states.packStates()[assetPackName]
                      when (state?.status()) {

                          AssetPackStatus.DOWNLOADING -> {
                              if (!MainActivity.showDownloadProg.value){
                                  MainActivity.showDownloadProg.value = true
                              } else {
                                  CoroutineScope(Dispatchers.Main).launch {
                                      MainActivity.snackbarHostState.showSnackbar(
                                          message = context.getString(R.string.explain_downloading_keepon),
                                          duration = SnackbarDuration.Short
                                      )
                                  }
                              }
                          }
                          AssetPackStatus.COMPLETED -> {

                              val path = getAbsoluteAssetPath(assetPackName , "ai/gemma3_1b_it_int4.task")
                              Log.d("AssetPack" , "path :: ${path}")
                              CoroutineScope(Dispatchers.IO).launch{
                                  MainActivity.llmInference = SystemPrompts.initInference(path!!, context)
                                  MainActivity.waitinit.value = true
                                  if (fromExtensions.value){
                                      val intent = Intent(context, DocifyAI::class.java)
                                      context.startActivity(intent)
                                  }

                              }
                          }
                          else -> {
                              // Asset henüz hiç indirilmemiş → Sor
                              MainActivity.showDFRDialog.value = true
                          }
                      }
                  }

        }
    }

    fun downloadAssetPack(assetPackManager: AssetPackManager, packName: String) {

        assetPackManager.fetch(listOf(packName))
            .addOnSuccessListener {
                Log.d("AssetPack", "Download started.")

            }
            .addOnFailureListener {
                Log.e("AssetPack", "Download failed: ${it.message}")
            }
    }

    private fun getAbsoluteAssetPath(assetPack: String, relativeAssetPath: String): String? {
        val assetPackPath: AssetPackLocation =
            apm.getPackLocation(assetPack)
            // asset pack is not ready
                ?: return null

        val assetsFolderPath = assetPackPath.assetsPath()
        // equivalent to: FilenameUtils.concat(assetPackPath.path(), "assets")
        return FilenameUtils.concat(assetsFolderPath, relativeAssetPath)
    }

}