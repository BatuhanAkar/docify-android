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
#include <fstream>

#include "../include/fpdfview.h"
#include "../include/fpdf_doc.h"
#include "../include/fpdf_annot.h"
#include "../include/fpdf_edit.h"
#include "../include/fpdf_save.h"
#include "../include/fpdf_text.h"
#include "../include/fpdf_ppo.h"
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
std::vector<uint8_t> globalFontData;  // Font verisini saklamak için global değişken
bool isFontLoaded = false;


extern "C"{

JNI_FUNC(void,icore,nativeLoadFont)(JNIEnv *env , jobject thiz , jbyteArray fontData){

jbyte *byteArray = env->GetByteArrayElements(fontData, nullptr);
jsize length = env->GetArrayLength(fontData);

// Byte array'ı std::vector'a dönüştürme
globalFontData = std::vector<uint8_t>(byteArray, byteArray + length);

env->ReleaseByteArrayElements(fontData, byteArray, 0);

isFontLoaded = true;  // Font başarıyla yüklendi

LOGI("Font successfully loaded");
}

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

    LOGE("Library initialized...");
    }

    JNI_FUNC(void , icore , nativeDestroyLibrary)(JNIEnv* env, jobject thiz){
    FPDF_DestroyLibrary();
    }

    JNI_FUNC(jlong, icore, nativeLoadPage)(JNI_ARGS, jlong docPtr, jint pageIndex){

    LOGE("docPtr: %lld, pageIndex: %d", docPtr, pageIndex);
        DocumentFile *doc = reinterpret_cast<DocumentFile*>(docPtr);
        return loadPageInternal(env, doc, (int)pageIndex);
    }


    JNI_FUNC(jlong, icore, nativeMemPage)(JNIEnv* env, jobject thiz, jlong docPtr, jint pageIndex){


LOGE("docPtr: %lld, pageIndex: %d", docPtr, pageIndex);
        // Döküman handle'ını kontrol et
        FPDF_DOCUMENT document = reinterpret_cast<FPDF_DOCUMENT>(docPtr);
        if (!document) {
        LOGE("Invalid document handle!");
        return 0;
        }

        // Sayfayı yükle
        FPDF_PAGE page = FPDF_LoadPage(document,(int) pageIndex);
        if (!page) {
        LOGE("Failed to load page %d", pageIndex);
        return 0;
        }
        return reinterpret_cast<jlong>(page);
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


JNI_FUNC(jint, icore, nativeInternalGetPageWidthPixel)(JNIEnv* env,jobject thiz, jlong pagePtr, jint dpi){
FPDF_PAGE page = reinterpret_cast<FPDF_PAGE>(pagePtr);
return (jint)(FPDF_GetPageWidth(page) * dpi / 72);
}
JNI_FUNC(jint, icore, nativeInternalGetPageHeightPixel)(JNIEnv* env,jobject thiz, jlong pagePtr, jint dpi){
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

JNI_FUNC(jboolean , icore , nativeDrawPath)(JNIEnv* env , jobject thiz ,
        jstring filePath , jobject PathMapObject , jint dpi){

const char* nativeFilePath = env->GetStringUTFChars(filePath, NULL);

// Döküman handle'ını kontrol et
FPDF_DOCUMENT document = FPDF_LoadDocument(nativeFilePath,"");
if (!document) {
LOGE("Invalid document handle!");
return false;
}

// Log the page count
int pageCount = FPDF_GetPageCount(document);
LOGI("Page count: %d", pageCount);



// get class
jclass mapClass = env->FindClass("java/util/Map");
if (mapClass == NULL) {
// Hata işleme
return false;
}

jmethodID sizeMethod = env->GetMethodID(mapClass, "size", "()I");
if (sizeMethod == NULL) {
// Hata işleme
return false;
}

jint mapSize = env->CallIntMethod(PathMapObject, sizeMethod);
LOGD("File Path Map Size :: %d" , mapSize);

jmethodID entrySetMethod = env->GetMethodID(mapClass, "entrySet", "()Ljava/util/Set;");
if (entrySetMethod == NULL) {
// Hata işleme
return false ;
}

jobject entrySet = env->CallObjectMethod(PathMapObject, entrySetMethod);
if (entrySet == NULL) {
// Hata işleme
return false ;
}

jclass setClass = env->FindClass("java/util/Set");
jmethodID iteratorMethod = env->GetMethodID(setClass, "iterator", "()Ljava/util/Iterator;");
jobject iterator = env->CallObjectMethod(entrySet, iteratorMethod);

jclass iteratorClass = env->FindClass("java/util/Iterator");
jmethodID hasNextMethod = env->GetMethodID(iteratorClass, "hasNext", "()Z");
jmethodID nextMethod = env->GetMethodID(iteratorClass, "next", "()Ljava/lang/Object;");


while (env->CallBooleanMethod(iterator, hasNextMethod)) {
jobject entry = env->CallObjectMethod(iterator, nextMethod);

// Map Entry'den key ve value'yu alın
jclass entryClass = env->FindClass("java/util/Map$Entry");
jmethodID getKeyMethod = env->GetMethodID(entryClass, "getKey", "()Ljava/lang/Object;");
jmethodID getValueMethod = env->GetMethodID(entryClass, "getValue", "()Ljava/lang/Object;");

// İlk olarak, Integer sınıfını alalım
jclass integerClass = env->FindClass("java/lang/Integer");
if (integerClass == nullptr) {
LOGE("Integer class not found!");
return false;
}

// intValue metodunu alalım
jmethodID intValueMethod = env->GetMethodID(integerClass, "intValue", "()I");
if (intValueMethod == nullptr) {
LOGE("intValue method not found!");
return false ;
}

jobject key = env->CallObjectMethod(entry, getKeyMethod); // key

jobject pathsListObj = env->CallObjectMethod(entry, getValueMethod); // value

jclass listClass = env->GetObjectClass(pathsListObj);

jmethodID listSizeMethod = env->GetMethodID(listClass, "size", "()I");

int pathCount = env->CallIntMethod(pathsListObj, listSizeMethod);

jmethodID listGetMethod = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");

jint pageIndex = env->CallIntMethod(key, intValueMethod);

LOGD("Key (Page Index) as Integer: %d", pageIndex);


// PathData class and its fields
jclass pathDataClass = env->FindClass("com/batuscode/pdfium/PathData");
jfieldID pathField = env->GetFieldID(pathDataClass, "path", "Ljava/util/List;");
//jfieldID colorField = env->GetFieldID(pathDataClass, "mcolor", "Landroidx/compose/ui/graphics/Color;");
jfieldID thicknessField = env->GetFieldID(pathDataClass, "thickness", "F");

jmethodID getMcolorValueMethod = env->GetMethodID(pathDataClass, "getMcolorValue", "()J");
if (getMcolorValueMethod == nullptr) {
// Hata: Metot bulunamadı
return false ;
}

// OffsetWrapper class and its methods
jclass offsetWrapperClass = env->FindClass("com/batuscode/pdfium/OffsetWrapper");
jmethodID getXMethod = env->GetMethodID(offsetWrapperClass, "getX", "()F");
jmethodID getYMethod = env->GetMethodID(offsetWrapperClass, "getY", "()F");


// Iterate over paths (List<PathData>)
for (int i = 0; i < pathCount; i++) {

// Sayfayı yükle
FPDF_PAGE page = FPDF_LoadPage(document, pageIndex);
if (!page) {
LOGE("Failed to load page %d", pageIndex);
return false;
}
jobject pathDataObj = env->CallObjectMethod(pathsListObj, listGetMethod, i);

// Get color and thickness for this path
//jobject colorObj = env->GetObjectField(pathDataObj, colorField);
jfloat thickness = env->GetFloatField(pathDataObj, thicknessField);

jobject colorObj = env->CallObjectMethod(pathDataObj, getMcolorValueMethod);


jclass colorClass = env->FindClass("androidx/compose/ui/graphics/Color");
jmethodID toArgbMethod = env->GetMethodID(colorClass, "toArgb", "()I");
int colorInt = env->CallIntMethod(colorObj, toArgbMethod);


// Get the path (List<OffsetWrapper>) from PathData
jobject offsetWrapperList = env->GetObjectField(pathDataObj, pathField);
jint pointCount = env->CallIntMethod(offsetWrapperList, listSizeMethod);

// Create new PDF path object
FPDF_PAGEOBJECT path = FPDFPageObj_CreateNewPath(0,0);
FPDFPath_SetDrawMode(path, false, true);  // Sadece çizgi çiz, içini doldurma
FPDFPageObj_SetLineCap(path,FPDF_LINECAP_ROUND);
FPDFPageObj_SetLineJoin(path,FPDF_LINEJOIN_ROUND);

jint pageWidth = FPDF_GetPageWidth(page)  ;
jint pageHeigth = FPDF_GetPageHeight(page)  ;

float scaleX = pageWidth / 1080.0f ;
float scaleY = pageHeigth / 1528.0f ;

for (int j = 0; j < pointCount; j++) {
jobject offsetWrapperObj = env->CallObjectMethod(offsetWrapperList, listGetMethod, j);

float x = env->CallFloatMethod(offsetWrapperObj, getXMethod)  ;
float y = env->CallFloatMethod(offsetWrapperObj, getYMethod)  ;


if (j == 0) {
FPDFPath_MoveTo(path, x, y);
} else {
//FPDFPath_LineTo(path, x, y);


jobject from = env->CallObjectMethod(offsetWrapperList, listGetMethod, j - 1);

jobject to = env->CallObjectMethod(offsetWrapperList, listGetMethod, j);

float toX = env->CallFloatMethod(to, getXMethod);
float toY = env->CallFloatMethod(to, getYMethod);

float fromX = env->CallFloatMethod(from, getXMethod);
float fromY = env->CallFloatMethod(from, getYMethod);

// İlk kontrol noktası ve ikinci kontrol noktası
float controlX = (fromX + toX) / 2.0f;
float controlY = (fromY + toY) / 2.0f;

float scale = 1.0f;  // Örnek bir değer, scale parametresini burada belirleyin
int smoothness = std::max(static_cast<int>(5 / scale), 1);

float dx = std::abs(fromX - toX);
float dy = std::abs(fromY - toY);

if(dx >= smoothness || dy >= smoothness){
// Cubic Bezier eğrisine ekle
FPDFPath_BezierTo(path, controlX, controlY, toX, toY, toX, toY);
}
}

LOGI("Drawing point at (%f, %f)", x, y);
}
int rgba[4];
rgba[0] = (colorInt >> 16) & 0xFF; // Red
rgba[1] = (colorInt >> 8) & 0xFF;  // Green
rgba[2] = colorInt & 0xFF;         // Blue
rgba[3] = (colorInt >> 24) & 0xFF; // Alpha

FPDFPageObj_SetStrokeColor(path, rgba[0], rgba[1], rgba[2], rgba[3]);
FPDFPageObj_SetStrokeWidth(path, thickness);
// Add path to the PDF page
FPDFPage_InsertObject(page, path);

// Finalize the content generation for the page
FPDFPage_GenerateContent(page);
}

}



/*
// Çizim modunu ayarlıyoruz (örneğin, sadece çizim yapmak için)
FPDFPath_SetDrawMode(path, FPDF_FILLMODE_WINDING, true);

// Çizim yapmaya başlıyoruz
FPDFPath_MoveTo(path, 100.0f, 100.0f); // Başlangıç noktası
FPDFPath_LineTo(path, 200.0f, 100.0f); // Çizgi çiz
FPDFPath_LineTo(path, 200.0f, 200.0f);
FPDFPath_LineTo(path, 100.0f, 200.0f);
FPDFPath_Close(path); // Kapalı yol oluştur
*/

// Path'i sayfaya ekliyoruz
//FPDFPage_InsertObject(page, path);



// Open the file for writing
FILE* outputFile = fopen(nativeFilePath, "wb");
if (!outputFile) {
LOGE("Failed to open file for writing: %s", nativeFilePath);
env->ReleaseStringUTFChars(filePath, nativeFilePath);
return false ;
}

// Initialize CustomFileWriter
CustomFileWriter writer;
writer.fileWrite.version = 1;
writer.fileWrite.WriteBlock = WriteBlock; // Use your existing WriteBlock implementation
writer.file = outputFile;


if (!FPDF_SaveWithVersion(document, reinterpret_cast<FPDF_FILEWRITE*>(&writer), FPDF_NO_INCREMENTAL, 15)) {
LOGE("Failed to save the document!");
return false ;
} else {
LOGI("Document saved successfully with PDF version: %d", 15);
}

fclose(outputFile);
// Temizleme işlemleri
return true ;
}

JNI_FUNC(void, icore, nativeAddTextToPage)(
JNIEnv* env, jobject thiz, jlong docPtr, jint pageIndex, jbyteArray text, jfloat x, jfloat y, jfloat fontSize, jfloat lineHeight) {


// Sabit değerler
const float PAGE_WIDTH = 595.0f;          // A4 sayfa genişliği (nokta)
const float PAGE_HEIGHT = 842.0f;         // A4 sayfa yüksekliği (nokta)
const float TOP_MARGIN = 56.7f;           // Üst kenar boşluğu (nokta)
const float BOTTOM_MARGIN = 56.7f;        // Alt kenar boşluğu (nokta)
const float LEFT_MARGIN = 42.5f;          // Sol kenar boşluğu (nokta)
const float RIGHT_MARGIN = 42.5f;         // Sağ kenar boşluğu (nokta)
const float FONT_SIZE = 12.0f;            // Font boyutu (nokta)
const float LINE_HEIGHT = 14.4f;          // Satır yüksekliği (nokta)
const float MAX_LINE_LENGTH = PAGE_WIDTH - RIGHT_MARGIN ; // Maksimum satır uzunluğu (nokta)

// Döküman handle'ını kontrol et
FPDF_DOCUMENT document = reinterpret_cast<FPDF_DOCUMENT>(docPtr);
if (!document) {
LOGE("Invalid document handle!");
return;
}

// Sayfayı yükle
FPDF_PAGE page = FPDF_LoadPage(document, pageIndex);
if (!page) {
LOGE("Failed to load page %d", pageIndex);
return;
}

// Java byte array'ini C byte array'ine dönüştür
jbyte* utf16Bytes = env->GetByteArrayElements(text, nullptr);
jsize length = env->GetArrayLength(text);

// Fontu yükle
FPDF_FONT font = FPDFText_LoadFont(document, globalFontData.data(), globalFontData.size(), FPDF_FONT_TRUETYPE, true);
if (!font) {
LOGE("Failed to load font into PDF document");
FPDF_CloseDocument(document);
return;
}

// Satırları tutacak vektör
std::vector<std::vector<uint16_t>> lines;
std::vector<uint16_t> currentLine;

// Karakter genişliği (fontSize'a bağlı olarak hesaplanır)
float charWidth = FONT_SIZE * 0.55f;

// Satır uzunluğunu takip et
float lineLength = 0.0f;

// Metni satırlara ayır
for (jsize i = 0; i < length; i += 2) {
uint16_t charCode = static_cast<uint16_t>(utf16Bytes[i] & 0xFF) | (static_cast<uint16_t>(utf16Bytes[i + 1] & 0xFF) << 8);

if (charCode == 0x000A || (lineLength + charWidth > MAX_LINE_LENGTH)) {
// Yeni satıra geç
lines.push_back(currentLine);
currentLine.clear();
lineLength = 0.0f; // Satır uzunluğunu sıfırla

// Satır sonu karakteriyse, bir sonraki karaktere geç
if (charCode == 0x000A) {
continue;
}
}

// Karakteri mevcut satıra ekle
currentLine.push_back(charCode);
lineLength += charWidth; // Satır uzunluğunu güncelle
}

// Son satırı ekle (eğer varsa)
if (!currentLine.empty()) {
lines.push_back(currentLine);
}

// Koordinatlar
float currentX = LEFT_MARGIN; // Başlangıç x koordinatı (sol kenar boşluğu)
float currentY = PAGE_HEIGHT - TOP_MARGIN; // Başlangıç y koordinatı (üst kenar boşluğu)

// Her satırı PDF'e ekle
for (const auto& line : lines) {
// Metin objesi oluştur
FPDF_PAGEOBJECT textObject = FPDFPageObj_CreateTextObj(document, font, FONT_SIZE);
if (!textObject) {
LOGE("Failed to create text object!");
return;
}

// Satırı UTF-16LE byte dizisine dönüştür
std::vector<uint8_t> utf16Line;
for (uint16_t charCode : line) {
utf16Line.push_back(static_cast<uint8_t>(charCode & 0xFF)); // Düşük byte
utf16Line.push_back(static_cast<uint8_t>((charCode >> 8) & 0xFF)); // Yüksek byte
}

// Null-terminator ekle (UTF-16LE için 0x0000)
utf16Line.push_back(0x00); // Düşük byte
utf16Line.push_back(0x00); // Yüksek byte

// Satırı metin nesnesine ekle
if (!utf16Line.empty()) {
FPDFText_SetText(textObject, reinterpret_cast<FPDF_WIDESTRING>(utf16Line.data()));
}

// Metin objesini konumlandır
FPDFPageObj_Transform(textObject, 1, 0, 0, 1, currentX, currentY);

// Metin objesini sayfaya ekle
FPDFPage_InsertObject(page, textObject);

// Y koordinatını bir sonraki satır için azalt
currentY -= LINE_HEIGHT;

// Sayfa yüksekliğini aştıysa yeni bir sayfaya geç
if (currentY < BOTTOM_MARGIN) {
LOGE("Sayfa doldu, yeni bir sayfa oluşturulmalı.");
break;
}
}

// Sayfa içeriğini güncelle
FPDFPage_GenerateContent(page);

// Temizleme işlemleri
env->ReleaseByteArrayElements(text, utf16Bytes, JNI_ABORT);
FPDF_ClosePage(page);
}

JNI_FUNC(jboolean,icore,nativeMergeDocument)(JNIEnv* env, jobject thiz , jobject filePathMapObject , jobject outputStream , jobject context){

    // get class
    jclass mapClass = env->FindClass("java/util/Map");
    if (mapClass == NULL) {
    // Hata işleme
    return false;
    }

    jmethodID sizeMethod = env->GetMethodID(mapClass, "size", "()I");
    if (sizeMethod == NULL) {
    // Hata işleme
    return false;
    }

    jint mapSize = env->CallIntMethod(filePathMapObject, sizeMethod);
    LOGD("File Path Map Size :: %d" , mapSize);

    jmethodID entrySetMethod = env->GetMethodID(mapClass, "entrySet", "()Ljava/util/Set;");
    if (entrySetMethod == NULL) {
    // Hata işleme
    return false ;
    }

    jobject entrySet = env->CallObjectMethod(filePathMapObject, entrySetMethod);
    if (entrySet == NULL) {
    // Hata işleme
    return false ;
    }

    jclass setClass = env->FindClass("java/util/Set");
    jmethodID iteratorMethod = env->GetMethodID(setClass, "iterator", "()Ljava/util/Iterator;");
    jobject iterator = env->CallObjectMethod(entrySet, iteratorMethod);

    jclass iteratorClass = env->FindClass("java/util/Iterator");
    jmethodID hasNextMethod = env->GetMethodID(iteratorClass, "hasNext", "()Z");
    jmethodID nextMethod = env->GetMethodID(iteratorClass, "next", "()Ljava/lang/Object;");


FPDF_DOCUMENT destDocument = nullptr;
int firstIndexToPage = 0;
bool isFirstDocument = true;  // İlk dokümanı tanımlamak için sayaç

while (env->CallBooleanMethod(iterator, hasNextMethod)) {
jobject entry = env->CallObjectMethod(iterator, nextMethod);

// Map Entry'den key ve value'yu alın
jclass entryClass = env->FindClass("java/util/Map$Entry");
jmethodID getKeyMethod = env->GetMethodID(entryClass, "getKey", "()Ljava/lang/Object;");
jmethodID getValueMethod = env->GetMethodID(entryClass, "getValue", "()Ljava/lang/Object;");

jobject key = env->CallObjectMethod(entry, getKeyMethod);
jobject value = env->CallObjectMethod(entry, getValueMethod);

// value'yu string'e dönüştür
const char* nativeFilePath = env->GetStringUTFChars((jstring)value, NULL);

if (isFirstDocument) {
// İlk döküman olarak destDocument'i yükle
destDocument = FPDF_LoadDocument(nativeFilePath, "");
if (!destDocument) {
LOGE("Invalid destination document handle!");
env->ReleaseStringUTFChars((jstring)value, nativeFilePath);  // Bellek temizliği
return false;
}

// İlk dökümanın sayfa sayısını al
firstIndexToPage = FPDF_GetPageCount(destDocument);
LOGI("First document page count: %d", firstIndexToPage);

isFirstDocument = false;  // İlk doküman işlendi, artık srcDocument'ler eklenecek
} else {
// Yeni kaynak dökümanı (srcDocument) yükle
FPDF_DOCUMENT srcDocument = FPDF_LoadDocument(nativeFilePath, "");
if (!srcDocument) {
LOGE("Invalid source document handle!");
env->ReleaseStringUTFChars((jstring)value, nativeFilePath);  // Bellek temizliği
continue;  // Hatalı dokümanı atla ve bir sonrakine geç
}

// Tüm sayfaları destDocument'e ekle
std::string pageRange = "1-" + std::to_string(FPDF_GetPageCount(srcDocument));  // Tüm sayfaları eklemek için dize
if (!FPDF_ImportPages(destDocument, srcDocument, pageRange.c_str(), firstIndexToPage)) {
LOGE("Failed to import pages from source document!");
}

// Sayfa ekleme işleminden sonra destDocument'in sayfa sayısını güncelle
firstIndexToPage = FPDF_GetPageCount(destDocument);
LOGI("New document page count: %d", firstIndexToPage);

// Kaynak dokümanı kapat
FPDF_CloseDocument(srcDocument);
}

// Bellek temizliği: nativeFilePath'i serbest bırak
env->ReleaseStringUTFChars((jstring)value, nativeFilePath);
}

// Java OutputStream sınıfını kullanmak için metodları alın
jclass outputStreamClass = env->GetObjectClass(outputStream);
jmethodID writeMethod = env->GetMethodID(outputStreamClass, "write", "([B)V");

jclass contextClass = env->GetObjectClass(context);
jmethodID getCacheDirMethod = env->GetMethodID(contextClass, "getCacheDir", "()Ljava/io/File;");
jobject cacheDir = env->CallObjectMethod(context, getCacheDirMethod);

jclass fileClass = env->GetObjectClass(cacheDir);
jmethodID getAbsolutePathMethod = env->GetMethodID(fileClass, "getAbsolutePath", "()Ljava/lang/String;");
jstring cacheDirPath = (jstring) env->CallObjectMethod(cacheDir, getAbsolutePathMethod);

// Geçici dosya açın
const char* nativeCacheDir = env->GetStringUTFChars(cacheDirPath, 0);
std::string tempFilePath = std::string(nativeCacheDir) + "/temp_fileXXXXXX";
FILE* tempFile = fopen(tempFilePath.c_str(), "w+b");
if (!tempFile) {
LOGE("Failed to create temp file in cache directory!");
return JNI_FALSE;
}

// CustomFileWriter yapılandırın
CustomFileWriter writer;
writer.fileWrite.version = 1;
writer.fileWrite.WriteBlock = WriteBlock;  // Var olan WriteBlock fonksiyonunuz
writer.file = tempFile;

// PDF dosyasını yazın
if (!FPDF_SaveWithVersion(destDocument, reinterpret_cast<FPDF_FILEWRITE*>(&writer), FPDF_NO_INCREMENTAL, 15)) {
LOGE("Failed to save the document!");
fclose(tempFile);
return JNI_FALSE;
}

// Temp dosyasından okuyun ve OutputStream'e yazın
fseek(tempFile, 0, SEEK_SET);
char buffer[4096];
size_t bytesRead;
while ((bytesRead = fread(buffer, 1, sizeof(buffer), tempFile)) > 0) {
jbyteArray byteArray = env->NewByteArray(bytesRead);
env->SetByteArrayRegion(byteArray, 0, bytesRead, (jbyte*)buffer);
env->CallVoidMethod(outputStream, writeMethod, byteArray);
env->DeleteLocalRef(byteArray);
}

// Temizleme işlemi
fclose(tempFile);

return true;
}

JNI_FUNC(jboolean,icore,nativeSplitDocument)(JNIEnv* env , jobject thiz , jstring filePath , jobject outputStream , jobject context ,jstring range){

// Convert Java string to C string
const char* filePathStr = env->GetStringUTFChars(filePath, nullptr);
if (!filePathStr) {
LOGE("Failed to convert Java string to C string!");
return false ;
}

const char* rangeStr = env->GetStringUTFChars(range, nullptr);
if (!rangeStr) {
LOGE("Failed to convert Java string to C string!");
return false ;
}

FPDF_DOCUMENT destDocument = FPDF_LoadDocument(filePathStr, "");
if (!destDocument) {
LOGE("Failed to create a new PDF document!");
return false;
}
FPDF_DOCUMENT srcDocument = FPDF_CreateNewDocument();
if (!srcDocument) {
LOGE("Failed to create a new PDF document!");
return false;
}

// Tüm sayfaları destDocument'e ekle
std::string pageRange = rangeStr ;
if (!FPDF_ImportPages(srcDocument, destDocument, pageRange.c_str(), 0)) {
LOGE("Failed to import pages from source document!");
}


// Java OutputStream sınıfını kullanmak için metodları alın
jclass outputStreamClass = env->GetObjectClass(outputStream);
jmethodID writeMethod = env->GetMethodID(outputStreamClass, "write", "([B)V");

jclass contextClass = env->GetObjectClass(context);
jmethodID getCacheDirMethod = env->GetMethodID(contextClass, "getCacheDir", "()Ljava/io/File;");
jobject cacheDir = env->CallObjectMethod(context, getCacheDirMethod);

jclass fileClass = env->GetObjectClass(cacheDir);
jmethodID getAbsolutePathMethod = env->GetMethodID(fileClass, "getAbsolutePath", "()Ljava/lang/String;");
jstring cacheDirPath = (jstring) env->CallObjectMethod(cacheDir, getAbsolutePathMethod);

// Geçici dosya açın
const char* nativeCacheDir = env->GetStringUTFChars(cacheDirPath, 0);
std::string tempFilePath = std::string(nativeCacheDir) + "/temp_fileXXXXXX";
FILE* tempFile = fopen(tempFilePath.c_str(), "w+b");
if (!tempFile) {
LOGE("Failed to create temp file in cache directory!");
return JNI_FALSE;
}

// CustomFileWriter yapılandırın
CustomFileWriter writer;
writer.fileWrite.version = 1;
writer.fileWrite.WriteBlock = WriteBlock;  // Var olan WriteBlock fonksiyonunuz
writer.file = tempFile;

// PDF dosyasını yazın
if (!FPDF_SaveWithVersion(srcDocument, reinterpret_cast<FPDF_FILEWRITE*>(&writer), FPDF_NO_INCREMENTAL, 15)) {
LOGE("Failed to save the document!");
fclose(tempFile);
return JNI_FALSE;
}

// Temp dosyasından okuyun ve OutputStream'e yazın
fseek(tempFile, 0, SEEK_SET);
char buffer[4096];
size_t bytesRead;
while ((bytesRead = fread(buffer, 1, sizeof(buffer), tempFile)) > 0) {
jbyteArray byteArray = env->NewByteArray(bytesRead);
env->SetByteArrayRegion(byteArray, 0, bytesRead, (jbyte*)buffer);
env->CallVoidMethod(outputStream, writeMethod, byteArray);
env->DeleteLocalRef(byteArray);
}

// Temizleme işlemi
fclose(tempFile);

return true ;



}


JNI_FUNC(jboolean, icore, nativeSaveDocument)(
        JNIEnv* env, jobject thiz, jlong docPtr, jstring filePath) {
FPDF_DOCUMENT document = reinterpret_cast<FPDF_DOCUMENT>(docPtr);
if (!document) {
LOGE("Invalid document handle!");
return JNI_FALSE;
}

// Convert Java string to C string
const char* filePathStr = env->GetStringUTFChars(filePath, nullptr);
if (!filePathStr) {
LOGE("Failed to convert Java string to C string!");
return JNI_FALSE ;
}

// Open the file for writing
FILE* outputFile = fopen(filePathStr, "wb");
if (!outputFile) {
LOGE("Failed to open file for writing: %s", filePathStr);
env->ReleaseStringUTFChars(filePath, filePathStr);
return JNI_FALSE ;
}

// Initialize CustomFileWriter
CustomFileWriter writer;
writer.fileWrite.version = 1;
writer.fileWrite.WriteBlock = WriteBlock; // Use your existing WriteBlock implementation
writer.file = outputFile;

if (!FPDF_SaveWithVersion(document, reinterpret_cast<FPDF_FILEWRITE*>(&writer), FPDF_NO_INCREMENTAL, 15)) {
LOGE("Failed to save the document!");
return JNI_FALSE ;
} else {
LOGI("Document saved successfully with PDF version: %d", 15);
}
// Clean up
fclose(outputFile);
env->ReleaseStringUTFChars(filePath, filePathStr);
return JNI_TRUE ;

}

JNI_FUNC(jboolean, icore, nativeSaveDocumentAsStream)(
        JNIEnv* env, jobject thiz, jlong docPtr, jobject outputStream , jobject context) {

FPDF_DOCUMENT document = reinterpret_cast<FPDF_DOCUMENT>(docPtr);
if (!document) {
LOGE("Invalid document handle!");
return JNI_FALSE;
}

// Java OutputStream sınıfını kullanmak için metodları alın
jclass outputStreamClass = env->GetObjectClass(outputStream);
jmethodID writeMethod = env->GetMethodID(outputStreamClass, "write", "([B)V");

jclass contextClass = env->GetObjectClass(context);
jmethodID getCacheDirMethod = env->GetMethodID(contextClass, "getCacheDir", "()Ljava/io/File;");
jobject cacheDir = env->CallObjectMethod(context, getCacheDirMethod);

jclass fileClass = env->GetObjectClass(cacheDir);
jmethodID getAbsolutePathMethod = env->GetMethodID(fileClass, "getAbsolutePath", "()Ljava/lang/String;");
jstring cacheDirPath = (jstring) env->CallObjectMethod(cacheDir, getAbsolutePathMethod);

// Geçici dosya açın
const char* nativeCacheDir = env->GetStringUTFChars(cacheDirPath, 0);
std::string tempFilePath = std::string(nativeCacheDir) + "/temp_fileXXXXXX";
FILE* tempFile = fopen(tempFilePath.c_str(), "w+b");
if (!tempFile) {
LOGE("Failed to create temp file in cache directory!");
return JNI_FALSE;
}

// CustomFileWriter yapılandırın
CustomFileWriter writer;
writer.fileWrite.version = 1;
writer.fileWrite.WriteBlock = WriteBlock;  // Var olan WriteBlock fonksiyonunuz
writer.file = tempFile;

// PDF dosyasını yazın
if (!FPDF_SaveWithVersion(document, reinterpret_cast<FPDF_FILEWRITE*>(&writer), FPDF_NO_INCREMENTAL, 15)) {
LOGE("Failed to save the document!");
fclose(tempFile);
return JNI_FALSE;
}

// Temp dosyasından okuyun ve OutputStream'e yazın
fseek(tempFile, 0, SEEK_SET);
char buffer[4096];
size_t bytesRead;
while ((bytesRead = fread(buffer, 1, sizeof(buffer), tempFile)) > 0) {
jbyteArray byteArray = env->NewByteArray(bytesRead);
env->SetByteArrayRegion(byteArray, 0, bytesRead, (jbyte*)buffer);
env->CallVoidMethod(outputStream, writeMethod, byteArray);
env->DeleteLocalRef(byteArray);
}

// Temizleme işlemi
fclose(tempFile);

return JNI_TRUE;
}

}