// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("pdfium");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("pdfium")
//      }
//    }

#include "util.hpp"

extern "C" {
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <string.h>
#include <stdio.h>

}

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/bitmap.h>
#include <sstream> // Add this line
#include <jni.h>

#include "../include/fpdfview.h"
#include "../include/fpdf_doc.h"
#include "../include/fpdf_annot.h"
#include "../include/fpdf_edit.h"
#include "../include/fpdf_save.h"
#include "../include/fpdf_text.h"
#include <string>
#include <vector>

#include <codecvt>
#include <locale>
// Helper function for converting UTF-8 to UTF-16
std::u16string utf8_to_utf16(const std::string& utf8) {
    std::u16string utf16;
    int i = 0;
    while (i < utf8.size()) {
        unsigned char c = utf8[i];
        if (c < 0x80) {
            utf16.push_back(c);
            i++;
        } else if ((c >> 5) == 0x6) {
            char16_t u = ((c & 0x1F) << 6) | (utf8[i + 1] & 0x3F);
            utf16.push_back(u);
            i += 2;
        } else if ((c >> 4) == 0xE) {
            char16_t u = ((c & 0x0F) << 12) | ((utf8[i + 1] & 0x3F) << 6) | (utf8[i + 2] & 0x3F);
            utf16.push_back(u);
            i += 3;
        } else {
            utf16.push_back('?');
            i++;
        }
    }
    return utf16;
}

static void initLibraryIfNeed(){
    FPDF_InitLibrary();
}

static void destroyLibraryIfNeed(){
    FPDF_DestroyLibrary();
}

struct rgb {
    uint8_t red;
    uint8_t green;
    uint8_t blue;
};

class DocumentFile {
private:
    int fileFd;

public:
    FPDF_DOCUMENT pdfDocument = NULL;
    size_t fileSize;

    DocumentFile() { initLibraryIfNeed(); }
    ~DocumentFile();
};
DocumentFile::~DocumentFile(){
    if(pdfDocument != NULL){
        FPDF_CloseDocument(pdfDocument);
    }

    destroyLibraryIfNeed();
}

template <class string_type>
inline typename string_type::value_type* WriteInto(string_type* str, size_t length_with_null) {
    str->reserve(length_with_null);
    str->resize(length_with_null - 1);
    return &((*str)[0]);
}

inline long getFileSize(int fd){
    struct stat file_state;

    if(fstat(fd, &file_state) >= 0){
        return (long)(file_state.st_size);
    }else{
        LOGE("Error getting file size");
        return 0;
    }
}

static char* getErrorDescription(const long error) {
    char* description = NULL;
    switch(error) {
        case FPDF_ERR_SUCCESS:
            asprintf(&description, "No error.");
            break;
        case FPDF_ERR_FILE:
            asprintf(&description, "File not found or could not be opened.");
            break;
        case FPDF_ERR_FORMAT:
            asprintf(&description, "File not in PDF format or corrupted.");
            break;
        case FPDF_ERR_PASSWORD:
            asprintf(&description, "Incorrect password.");
            break;
        case FPDF_ERR_SECURITY:
            asprintf(&description, "Unsupported security scheme.");
            break;
        case FPDF_ERR_PAGE:
            asprintf(&description, "Page not found or content error.");
            break;
        default:
            asprintf(&description, "Unknown error.");
    }

    return description;
}

int jniThrowException(JNIEnv* env, const char* className, const char* message) {
    jclass exClass = env->FindClass(className);
    if (exClass == NULL) {
        LOGE("Unable to find exception class %s", className);
        return -1;
    }

    if(env->ThrowNew(exClass, message ) != JNI_OK) {
        LOGE("Failed throwing '%s' '%s'", className, message);
        return -1;
    }

    return 0;
}

int jniThrowExceptionFmt(JNIEnv* env, const char* className, const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    char msgBuf[512];
    vsnprintf(msgBuf, sizeof(msgBuf), fmt, args);
    return jniThrowException(env, className, msgBuf);
    va_end(args);
}

jobject NewLong(JNIEnv* env, jlong value) {
    jclass cls = env->FindClass("java/lang/Long");
    jmethodID methodID = env->GetMethodID(cls, "<init>", "(J)V");
    return env->NewObject(cls, methodID, value);
}

jobject NewInteger(JNIEnv* env, jint value) {
    jclass cls = env->FindClass("java/lang/Integer");
    jmethodID methodID = env->GetMethodID(cls, "<init>", "(I)V");
    return env->NewObject(cls, methodID, value);
}

uint16_t rgbTo565(rgb *color) {
    return ((color->red >> 3) << 11) | ((color->green >> 2) << 5) | (color->blue >> 3);
}

void rgbBitmapTo565(void *source, int sourceStride, void *dest, AndroidBitmapInfo *info) {
    rgb *srcLine;
    uint16_t *dstLine;
    int y, x;
    for (y = 0; y < info->height; y++) {
        srcLine = (rgb*) source;
        dstLine = (uint16_t*) dest;
        for (x = 0; x < info->width; x++) {
            dstLine[x] = rgbTo565(&srcLine[x]);
        }
        source = (char*) source + sourceStride;
        dest = (char*) dest + info->stride;
    }
}
// Example font data for Helvetica (replace with actual font data)
static const uint8_t helveticaFontData[] = {
        // Insert the actual font data here
};
extern "C"{

    static int getBlock(void* param, unsigned long position, unsigned char* outBuffer,
                        unsigned long size) {
        const int fd = reinterpret_cast<intptr_t>(param);
        const int readCount = pread(fd, outBuffer, size, position);
        if (readCount < 0) {
            LOGE("Cannot read from file descriptor. Error:%d", errno);
            return 0;
        }
        return 1;
    }
    static jlong loadPageInternal(JNIEnv *env, DocumentFile *doc, int pageIndex){
        try{
            if(doc == NULL) throw "Get page document null";

            FPDF_DOCUMENT pdfDoc = doc->pdfDocument;
            if(pdfDoc != NULL){
                FPDF_PAGE page = FPDF_LoadPage(pdfDoc, pageIndex);
                if (page == NULL) {
                    throw "Loaded page is null";
                }
                return reinterpret_cast<jlong>(page);
            }else{
                throw "Get page pdf document null";
            }

        }catch(const char *msg){
            LOGE("%s", msg);

            jniThrowException(env, "java/lang/IllegalStateException",
                              "cannot load page");

            return -1;
        }
    }

    static void closePageInternal(jlong pagePtr) { FPDF_ClosePage(reinterpret_cast<FPDF_PAGE>(pagePtr)); }

    JNI_FUNC(void , icore , nativeInitLibrary)(JNIEnv* env, jobject thiz){
    FPDF_InitLibrary();
    }

    JNI_FUNC(void , icore , nativeDestroyLibrary)(JNIEnv* env, jobject thiz){
    FPDF_DestroyLibrary();
    }

    JNI_FUNC(jlong, icore, nativeLoadPage)(JNI_ARGS, jlong docPtr, jint pageIndex){
        DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);
        return loadPageInternal(env, doc, (int)pageIndex);
    }

    JNI_FUNC(jlong, icore, nativeOpenDocument)(JNI_ARGS, jint fd, jstring password){

        size_t fileLength = (size_t)getFileSize(fd);
        if(fileLength <= 0) {
            jniThrowException(env, "java/io/IOException",
                              "File is empty");
            return -1;
        }

        DocumentFile *docFile = new DocumentFile();

        FPDF_FILEACCESS loader;
        loader.m_FileLen = fileLength;
        loader.m_Param = reinterpret_cast<void*>(intptr_t(fd));
        loader.m_GetBlock = &getBlock;

        const char *cpassword = NULL;
        if(password != NULL) {
            cpassword = env->GetStringUTFChars(password, NULL);
        }

        FPDF_DOCUMENT document = FPDF_LoadCustomDocument(&loader, cpassword);

        if(cpassword != NULL) {
            env->ReleaseStringUTFChars(password, cpassword);
        }

        if (!document) {
            delete docFile;

            const long errorNum = FPDF_GetLastError();
            if(errorNum == FPDF_ERR_PASSWORD) {
                jniThrowException(env, "com/shockwave/pdfium/PdfPasswordException",
                                  "Password required or incorrect password.");
            } else {
                char* error = getErrorDescription(errorNum);
                jniThrowExceptionFmt(env, "java/io/IOException",
                                     "cannot create document: %s", error);

                free(error);
            }

            return -1;
        }

        docFile->pdfDocument = document;

        return reinterpret_cast<jlong>(docFile);
    }

    JNI_FUNC(void, icore, nativeCloseDocument)(JNI_ARGS, jlong documentPtr){
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(documentPtr);
    delete doc;
    }

    JNI_FUNC(void, icore, nativeClosePage)(JNI_ARGS, jlong pagePtr){ closePageInternal(pagePtr); }


    JNI_FUNC(jint, icore, nativeGetPageWidthPixel)(JNI_ARGS, jlong pagePtr, jint dpi){
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
    return (jint)(FPDF_GetPageWidth(page) * dpi / 72);
    }
    JNI_FUNC(jint, icore, nativeGetPageHeightPixel)(JNI_ARGS, jlong pagePtr, jint dpi){
    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
    return (jint)(FPDF_GetPageHeight(page) * dpi / 72);
    }


    JNI_FUNC(jint, icore, nativeGetPageCount)(JNI_ARGS, jlong documentPtr){
    DocumentFile *doc = reinterpret_cast<DocumentFile*>(documentPtr);
    return (jint)FPDF_GetPageCount(doc->pdfDocument);
    }

    JNI_FUNC(void, icore, nativeRenderPageBitmap)(JNI_ARGS, jlong pagePtr, jobject bitmap,
            jint dpi, jint startX, jint startY,
    jint drawSizeHor, jint drawSizeVer,
    jboolean renderAnnot){

    FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);

    if(page == NULL || bitmap == NULL){
    LOGE("Render page pointers invalid");
    return;
    }

    AndroidBitmapInfo info;
    int ret;
    if((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
    LOGE("Fetching bitmap info failed: %s", strerror(ret * -1));
    return;
    }

    int canvasHorSize = info.width;
    int canvasVerSize = info.height;

    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 && info.format != ANDROID_BITMAP_FORMAT_RGB_565){
    LOGE("Bitmap format must be RGBA_8888 or RGB_565");
    return;
    }

    void *addr;
    if( (ret = AndroidBitmap_lockPixels(env, bitmap, &addr)) != 0 ){
    LOGE("Locking bitmap failed: %s", strerror(ret * -1));
    return;
    }

    void *tmp;
    int format;
    int sourceStride;
    if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
    tmp = malloc(canvasVerSize * canvasHorSize * sizeof(rgb));
    sourceStride = canvasHorSize * sizeof(rgb);
    format = FPDFBitmap_BGR;
    } else {
    tmp = addr;
    sourceStride = info.stride;
    format = FPDFBitmap_BGRA;
    }

    FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx( canvasHorSize, canvasVerSize,
                                                 format, tmp, sourceStride);

    /*LOGD("Start X: %d", startX);
    LOGD("Start Y: %d", startY);
    LOGD("Canvas Hor: %d", canvasHorSize);
    LOGD("Canvas Ver: %d", canvasVerSize);
    LOGD("Draw Hor: %d", drawSizeHor);
    LOGD("Draw Ver: %d", drawSizeVer);*/

    if(drawSizeHor < canvasHorSize || drawSizeVer < canvasVerSize){
    FPDFBitmap_FillRect( pdfBitmap, 0, 0, canvasHorSize, canvasVerSize,
    0x848484FF); //Gray
    }

    int baseHorSize = (canvasHorSize < drawSizeHor)? canvasHorSize : (int)drawSizeHor;
    int baseVerSize = (canvasVerSize < drawSizeVer)? canvasVerSize : (int)drawSizeVer;
    int baseX = (startX < 0)? 0 : (int)startX;
    int baseY = (startY < 0)? 0 : (int)startY;
    int flags = FPDF_REVERSE_BYTE_ORDER;

    if(renderAnnot) {
    flags |= FPDF_ANNOT;
    }

    FPDFBitmap_FillRect( pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
    0xFFFFFFFF); //White

    FPDF_RenderPageBitmap( pdfBitmap, page,
            startX, startY,
    (int)drawSizeHor, (int)drawSizeVer,
    0, flags );

    if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
    rgbBitmapTo565(tmp, sourceStride, addr, &info);
    free(tmp);
    }

    AndroidBitmap_unlockPixels(env, bitmap);
FPDFBitmap_Destroy(pdfBitmap);
    }


// Custom file writer structure
struct CustomFileWriter {
    FPDF_FILEWRITE fileWrite;
    FILE* file;
};

// Implementation of WriteBlock for CustomFileWriter
int WriteBlock(struct FPDF_FILEWRITE_* pThis, const void* pData, unsigned long size) {
    CustomFileWriter* writer = reinterpret_cast<CustomFileWriter*>(pThis);
    size_t written = fwrite(pData, 1, size, writer->file);
    return written == size ? 1 : 0;
}

// Create a new PDF document
JNI_FUNC(jlong, icore, nativecreateNewDocument)(JNIEnv* env, jobject thiz) {
FPDF_DOCUMENT document = FPDF_CreateNewDocument();
if (!document) {
LOGE("Failed to create a new PDF document!");
return 0;
}
return reinterpret_cast<jlong>(document);
}

// Add a new page to the document
JNI_FUNC(jlong, icore, nativeaddPageToDocument)(JNIEnv* env, jobject thiz, jlong docPtr, jobject pageSettings) {
FPDF_DOCUMENT document = reinterpret_cast<FPDF_DOCUMENT>(docPtr);
if (!document) {
LOGE("Invalid document handle!");
return 0; // Return false if the document handle is invalid
}

// Get page settings from Java object
jclass pageSettingsClass = env->GetObjectClass(pageSettings);
jmethodID getWidthMethod = env->GetMethodID(pageSettingsClass, "getWidth", "()F");
jmethodID getHeightMethod = env->GetMethodID(pageSettingsClass, "getHeight", "()F");

float width = env->CallFloatMethod(pageSettings, getWidthMethod);
float height = env->CallFloatMethod(pageSettings, getHeightMethod);

// Add a new page
FPDF_PAGE page = FPDFPage_New(document, 0, 595.0, 842.0);
if (!page) {
LOGE("Failed to create a new page!");
return 0; // Return false if page creation fails
}

// Log the page count
int pageCount = FPDF_GetPageCount(document);
LOGI("Page count: %d", pageCount);



return reinterpret_cast<jlong>(document);; // Return true if the page is successfully added
}


JNI_FUNC(void, icore, nativeAddTextToPage)(
JNIEnv* env, jobject thiz, jlong docPtr, jint pageIndex, jbyteArray text, jfloat x, jfloat y, jfloat fontSize, jfloat lineHeight , jbyteArray fontData) {
// Döküman handle'ını kontrol et
// Döküman handle'ını kontrol et
FPDF_DOCUMENT document = reinterpret_cast<FPDF_DOCUMENT>(docPtr);
if (!document) {
LOGE("Invalid document handle!");
return;
}

// Sayfa sayısını al
int pageCount = FPDF_GetPageCount(document);
LOGI("Page count: %d", pageCount);

// Sayfa indeksini kontrol et
if (pageIndex < 0 || pageIndex >= pageCount) {
LOGE("Invalid page index: %d (document has %d pages)", pageIndex, pageCount);
return;
}

// Sayfayı yükle
FPDF_PAGE page = FPDF_LoadPage(document, pageIndex);
if (!page) {
LOGE("Failed to load page %d", pageIndex);
return;
}

// UTF-16'ya dönüştürmek için genişletilmiş string'i oluştur
FPDF_WIDESTRING wideText = reinterpret_cast<FPDF_WIDESTRING>(text);

// Java byte array'ini C byte array'ine dönüştür
jbyte* fontDataBytes = env->GetByteArrayElements(fontData, nullptr);
jsize fontDataLength = env->GetArrayLength(fontData);

// Font'u yükle
FPDF_FONT font = FPDFText_LoadFont(
        document,
        reinterpret_cast<const uint8_t*>(fontDataBytes), // Font data
        static_cast<uint32_t>(fontDataLength),           // Size of font data
        FPDF_FONT_TRUETYPE,                             // Font type (TrueType)
        false                                           // Not a CID font
);
if (!font) {
LOGE("Failed to load font!");
env->ReleaseByteArrayElements(fontData, fontDataBytes, JNI_ABORT);
FPDF_ClosePage(page);
return;
}

float currentY = y;

// Metin objesi oluştur
FPDF_PAGEOBJECT textObject = FPDFPageObj_CreateTextObj(document, font, fontSize);
if (!textObject) {
LOGE("Failed to create text object!");
env->ReleaseByteArrayElements(fontData, fontDataBytes, JNI_ABORT);
FPDF_ClosePage(page);
return;
}

// Metni UTF-16 formatında ayarla (boşluk karakterleri korunur)
FPDFText_SetText(textObject, wideText);

// Metin objesini konumlandır
FPDFPageObj_Transform(textObject, 1, 0, 0, 1, x, currentY);

// Metin objesini sayfaya ekle
FPDFPage_InsertObject(page, textObject);
FPDFPage_GenerateContent(page);

// Temizleme işlemleri
env->ReleaseByteArrayElements(fontData, fontDataBytes, JNI_ABORT);
FPDF_ClosePage(page);
}


JNI_FUNC(void, icore, nativeSaveDocument)(
        JNIEnv* env, jobject thiz, jlong docPtr, jstring filePath) {
FPDF_DOCUMENT document = reinterpret_cast<FPDF_DOCUMENT>(docPtr);
if (!document) {
LOGE("Invalid document handle!");
return;
}

// Convert Java string to C string
const char* filePathStr = env->GetStringUTFChars(filePath, nullptr);
if (!filePathStr) {
LOGE("Failed to convert Java string to C string!");
return;
}

// Open the file for writing
FILE* outputFile = fopen(filePathStr, "wb");
if (!outputFile) {
LOGE("Failed to open file for writing: %s", filePathStr);
env->ReleaseStringUTFChars(filePath, filePathStr);
return;
}

// Initialize CustomFileWriter
CustomFileWriter writer;
writer.fileWrite.version = 1;
writer.fileWrite.WriteBlock = WriteBlock; // Use your existing WriteBlock implementation
writer.file = outputFile;

if (!FPDF_SaveWithVersion(document, reinterpret_cast<FPDF_FILEWRITE*>(&writer), FPDF_NO_INCREMENTAL, 15)) {
LOGE("Failed to save the document!");
} else {
LOGI("Document saved successfully with PDF version: %d", 15);
}
// Clean up
fclose(outputFile);
env->ReleaseStringUTFChars(filePath, filePathStr);
}
}