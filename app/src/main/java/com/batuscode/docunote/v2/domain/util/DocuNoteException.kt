package com.batuscode.docunote.v2.domain.util

sealed class DocuNoteException(message: String) : Exception(message) {
    class NetworkException(msg: String = "Bağlantı hatası oluştu.") : DocuNoteException(msg)
    class AuthException(msg: String = "Oturum süresi doldu.") : DocuNoteException(msg)
    class FileTooLargeException(msg: String = "Dosya boyutu çok büyük.") : DocuNoteException(msg)
    class UnknownException(msg: String = "Beklenmedik bir hata oluştu.") : DocuNoteException(msg)
}