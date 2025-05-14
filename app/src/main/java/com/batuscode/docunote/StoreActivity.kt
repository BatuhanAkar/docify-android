package com.batuscode.docunote

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.queryProductDetails
import com.batuscode.docunote.model.Subscription
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.Auth
import com.batuscode.docunote.viewmodel.StoreActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreActivity : ComponentActivity() {

    private val TAG = "StorePurchases"

    companion object {
        lateinit var purchasesUpdatedListener: PurchasesUpdatedListener
        lateinit var billingClient: BillingClient
        lateinit var storeActivityViewModel: StoreActivityViewModel
    }

    suspend fun processQuery() {
        val subsList = listOf(
            Product.newBuilder()
                .setProductId("docify_knowledge_pro")
                .setProductType(ProductType.SUBS)
                .build()
        )

        val subsParams = QueryProductDetailsParams.newBuilder()
        subsParams.setProductList(subsList)

        val subsDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(subsParams.build())
        }


        val subsDetails = subsDetailsResult.productDetailsList?.get(0)

        if (subsDetails != null) {
            val pricingPhase = subsDetails
                .subscriptionOfferDetails
                ?.get(0)
                ?.pricingPhases
                ?.pricingPhaseList
                ?.get(0)


            val formattedPrice = pricingPhase?.formattedPrice ?: ""
            val billingPeriod = parseBillingPeriod(pricingPhase?.billingPeriod ?: "")
            val offerToken = subsDetails.subscriptionOfferDetails?.get(0)?.offerToken
            val offerID = subsDetails.subscriptionOfferDetails?.get(0)?.offerId
            val offertagas = subsDetails.subscriptionOfferDetails?.get(0)?.offerTags
            offertagas?.forEach { Log.d(TAG, "offer tag :: $it") }
            Log.d(TAG, "offerDetails size :: ${subsDetails.subscriptionOfferDetails?.size}")
            Log.d(TAG, "offertoken :: $offerToken")
            Log.d(TAG, "offerId :: $offerID")

            if (subsDetails.productId.equals("docify_knowledge_pro")) {
                val sub = Subscription(
                    image = R.drawable.workspace_premium,
                    name = subsDetails.name,
                    description = subsDetails.description,
                    price = "$formattedPrice / $billingPeriod \n (auto-renews)",
                    benefits = listOf(
                        "3 day free-trial",
                        "Unlimited Smart Summaries",
                        "Unlimited Intelligent Q&A Sessions",
                        "Priority AI Processing",
                        "Designed for Power Users"
                    ),
                    productDetails = subsDetails,
                    offerToken = offerToken!!
                )
                storeActivityViewModel.addSub(sub)

            }
        }


    }

    override fun onStart() {
        super.onStart()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    CoroutineScope(Dispatchers.IO).launch {
                        processQuery()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storeActivityViewModel = ViewModelProvider(this).get(StoreActivityViewModel::class.java)
        storeActivityViewModel.Clear_list()
        purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                -1 -> {
                    // service disconnected .
                    Log.d(TAG, "service disconnected")
                }

                0 -> {
                    // ok .
                    Log.d(TAG, "ok")
                    Auth.updateClaim(true)
                }

                1 -> {
                    // user canceled .
                    Log.d(TAG, "user canceled")

                }

                2 -> {
                    // service unavailable .
                    Log.d(TAG, "service unavailable")

                }

                3 -> {
                    // billing unavailable .
                    Log.d(TAG, "billing unavailable")

                }

                4 -> {
                    // item unavailable .

                    Log.d(TAG, "item unavailable")
                }

                5 -> {
                    // developer error .

                    Log.d(TAG, "developer error")
                }

                6 -> {
                    // error .

                    Log.d(TAG, "error")
                }

                7 -> {
                    // item already owned .

                }

                8 -> {
                    // item not owned .

                }

                12 -> {
                    // network error .
                }


            }


        }

        billingClient = BillingClient.newBuilder(this@StoreActivity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .enableExternalOffer()
            .build()

        enableEdgeToEdge()
        setContent {
            val products = storeActivityViewModel.subs.collectAsState()

            DocuNoteTheme(darkTheme = true) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = innerPadding,
                        verticalArrangement = Arrangement.Center,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products.value) { item: Subscription ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .clickable {
                                        if (Auth.auth.currentUser != null) {

                                            val uid = Auth.auth.currentUser?.uid
                                            var productDetailsParamsList =
                                                emptyList<BillingFlowParams.ProductDetailsParams>()

                                            productDetailsParamsList = listOf(
                                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                    .setProductDetails(item.productDetails!!)
                                                    // For One-time products, "setOfferToken" method shouldn't be called.
                                                    // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                                                    // for a list of offers that are available to the user
                                                    .setOfferToken(item.offerToken)
                                                    .build()
                                            )

                                            if (uid != null) {
                                                val billingFlowParams =
                                                    BillingFlowParams.newBuilder()
                                                        .setProductDetailsParamsList(
                                                            productDetailsParamsList
                                                        )
                                                        .setIsOfferPersonalized(true)
                                                        .setObfuscatedAccountId(uid)
                                                        .build()
// Launch the billing flow
                                                val billingResult = billingClient.launchBillingFlow(
                                                    this@StoreActivity,
                                                    billingFlowParams
                                                )
                                            }

                                        }

                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    tonalElevation = 4.dp,
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(PaddingValues(32.dp)),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Top,
                                    ) {
                                        Image(
                                            painter = painterResource(item.image),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.headlineLarge
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        item.benefits?.forEach { benefit ->
                                            Text(
                                                text = "~ $benefit"
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = item.price,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseBillingPeriod(billingPeriod: String): String {
    return when (billingPeriod) {
        "P1W" -> "weekly"
        "P1M" -> "monthly"
        "P3M" -> "quarterly"
        "P6M" -> "biannually"
        "P1Y" -> "yearly"
        else -> billingPeriod // fallback
    }
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    /*var subsList = listOf(
        Subscription(
            R.drawable.workspace_premium,
            "Knowledge Lite" ,
            "knowledge Lite" ,
            "3.99$/month" ,
            null ,
            null
        ) ,
        Subscription(
            R.drawable.workspace_premium,
            "Knowledge Pro" ,
            "knowledge Pro" ,
            "17.99$/month" ,
            null ,
            null
        )
    )*/
    DocuNoteTheme(darkTheme = true) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red),
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                /* items(subsList){
                         item: Subscription ->
                     Box(
                         modifier = Modifier
                             .fillMaxSize()
                             .padding(16.dp) ,
                         contentAlignment = Alignment.Center
                     ) {
                         Surface(
                             shape = RoundedCornerShape(16.dp),
                             tonalElevation = 4.dp,
                             shadowElevation = 8.dp,
                             modifier = Modifier
                                 .aspectRatio(1f)
                         ) {
                             Column(
                                 modifier = Modifier
                                     .fillMaxSize()
                                     .padding(PaddingValues(32.dp)),
                                 horizontalAlignment = Alignment.CenterHorizontally ,
                                 verticalArrangement = Arrangement.Top ,
                             ) {
                                 Image(
                                     painter = painterResource(item.image) ,
                                     contentDescription = "" ,
                                     modifier = Modifier
                                         .size(48.dp)
                                 )
                                 Spacer(modifier = Modifier.height(8.dp))
                                 Text(
                                     text = item.name ,
                                     style = MaterialTheme.typography.headlineLarge
                                 )
                                 Spacer(modifier = Modifier.height(8.dp))

                                 Text(
                                     text = item.description ,
                                     style = MaterialTheme.typography.bodyLarge,
                                     textAlign = TextAlign.Center
                                 )
                                 Spacer(modifier = Modifier.height(8.dp))

                                 Text(text = "Unlimited Smart Summaries")
                                 Text(text = "Unlimited Smart Summaries")
                                 Text(text = "Unlimited Smart Summaries")
                                 Text(text = "Unlimited Smart Summaries")
                                 Spacer(modifier = Modifier.height(8.dp))

                                 Text(
                                     text = item.price
                                 )
                             }
                         }
                     }
                 }*/
            }
        }
    }
}