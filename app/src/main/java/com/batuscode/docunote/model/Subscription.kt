package com.batuscode.docunote.model

import androidx.annotation.DrawableRes
import com.android.billingclient.api.ProductDetails

data class Subscription(
    @DrawableRes
    val image : Int,
    val name : String,
    val description : String,
    val offerprice : String,
    val basePrice : String ,
    val benefits : List<String>? ,
    val productDetails: ProductDetails? ,
    val offerToken : String
    )
