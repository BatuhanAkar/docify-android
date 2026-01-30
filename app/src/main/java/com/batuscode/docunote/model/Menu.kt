package com.batuscode.docunote.model

data class Menu(
    var id: Int ,
    var title:String ,
    var buttons: List<ExtensionButton>
)
