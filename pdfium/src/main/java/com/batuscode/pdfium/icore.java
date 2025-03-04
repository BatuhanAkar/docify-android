package com.batuscode.pdfium;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.android.apps.common.testing.accessibility.framework.BuildConfig;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;

public class icore {

    private static final String TAG = icore.class.getName();
    private static final Class FD_CLASS = FileDescriptor.class;
    private static final String FD_FIELD_NAME = "descriptor";

    /* synchronize native methods */
    private static final Object lock = new Object();
    private static Field mFdField = null;
    private int mCurrentDpi;

    static {
        try {
            System.loadLibrary("jpdfium");

        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Native libraries failed to load - " + e);
        }
    }
    public static int getNumFd(ParcelFileDescriptor fdObj) {
        try {
            if (mFdField == null) {
                mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME);
                mFdField.setAccessible(true);
            }

            return mFdField.getInt(fdObj.getFileDescriptor());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return -1;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public icore(Context ctx) {
        mCurrentDpi = ctx.getResources().getDisplayMetrics().densityDpi;
        Log.d(TAG, "Starting PdfiumAndroid " + BuildConfig.VERSION_NAME);
    }

    private native long nativeOpenDocument(int fd, String password);

    private native void nativeRenderPageBitmap(long pagePtr, Bitmap bitmap, int dpi,
                                               int startX, int startY,
                                               int drawSizeHor, int drawSizeVer,
                                               boolean renderAnnot);

    private native void nativeRenderPageSkia(long pagePtr, Canvas skiaCanvas, int dpi,
                                             int startX, int startY,
                                             int drawSizeHor, int drawSizeVer,
                                             boolean renderAnnot);


    private native int nativeGetPageWidthPixel(long pagePtr, int dpi);

    private native int nativeGetPageHeightPixel(long pagePtr, int dpi);

    private native int nativeInternalGetPageWidthPixel(long pagePtr, int dpi);

    private native int nativeInternalGetPageHeightPixel(long pagePtr, int dpi);

    private native void nativeClosePage(long pagePtr);

    private native void nativeCloseDocument(long docPtr);

    private native int nativeGetPageCount(long docPtr);
    private native boolean nativeSaveDocument(long docPtr , String filePath);
    private native boolean nativeSaveDocumentAsStream(long docPtr , OutputStream outputStream);
    public native long nativecreateNewDocument();
    public native long nativeaddPageToDocument(long docPtr, PDFPage page);
    public native void nativeAddTextToPage(long docPtr , int index , byte[] text , float x , float y , float fontSize , float lineHeight);
    public native void nativeInitLibrary();
    public native void nativeDestroyLibrary();
    public native void nativeDrawPath(String filePath , int pageIndex , List<PathData> pathData);
    private native long nativeMemPage(long docPtr , int pageIndex);
    private native void nativeAddAnnotationToPage(long documentPtr, int pageIndex, List<PathData> paths , File file );
    public void addAnnotations(PdfDocument document, int pageIndex, List<PathData> paths , File file) {
        long docPtr = document.mNativeDocPtr; // Assuming you have a way to get the native pointer

        Log.d("saveDrawingsToPDF", "addAnnotations Document pointer: " + docPtr);
        nativeAddAnnotationToPage(docPtr, pageIndex, paths , file );
    }
    private native long nativeLoadPage(long docPtr, int pageIndex);
    /** Create new document from file with password */
    public PdfDocument newDocument(ParcelFileDescriptor fd, String password) throws IOException {
        PdfDocument document = new PdfDocument();
        document.parcelFileDescriptor = fd;
        synchronized (lock) {
            document.mNativeDocPtr = nativeOpenDocument(getNumFd(fd), password);
        }

        return document;
    }


    /**
     * Get page width in pixels. <br>
     * This method requires page to be opened.
     */
    public int getPageWidth(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageWidthPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }


    /**
     * Get page height in pixels. <br>
     * This method requires page to be opened.
     */
    public int getPageHeight(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageHeightPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }




    public int getInternalPageWidth(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeInternalGetPageWidthPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }


    /**
     * Get page height in pixels. <br>
     * This method requires page to be opened.
     */
    public int getInternalPageHeight(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeInternalGetPageHeightPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }

    /** Open page and store native pointer in {@link PdfDocument} */
    public long openPage(PdfDocument doc, int pageIndex) {
        long pagePtr;
        synchronized (lock) {
            pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex);
            doc.mNativePagesPtr.put(pageIndex, pagePtr);
            return pagePtr;
        }

    }

    public long memPage(PdfDocument document , int pageIndex){
        synchronized (lock){
            long docPtr = document.mNativeDocPtr;
            Log.e("nativein" , "document ptr ::: " + docPtr + "pageIndex ::: " + pageIndex);
            long pagePtr = nativeMemPage( docPtr, pageIndex);
            document.mNativePagesPtr.put(pageIndex , pagePtr);
            return pagePtr;
        }
    }

    /** Get total numer of pages in document */
    public int getPageCount(PdfDocument doc) {
        synchronized (lock) {
            return nativeGetPageCount(doc.mNativeDocPtr);
        }
    }

    /** Release native resources and opened file */
    public void closeDocument(PdfDocument doc) {
        synchronized (lock) {
            for (Integer index : doc.mNativePagesPtr.keySet()) {
                nativeClosePage(doc.mNativePagesPtr.get(index));
            }
            doc.mNativePagesPtr.clear();

            nativeCloseDocument(doc.mNativeDocPtr);

            if (doc.parcelFileDescriptor != null) { //if document was loaded from file
                try {
                    doc.parcelFileDescriptor.close();
                } catch (IOException e) {
                    /* ignore */
                }
                doc.parcelFileDescriptor = null;
            }
        }
    }
    /** Create new document from file */
    public PdfDocument newDocument(ParcelFileDescriptor fd) throws IOException {
        return newDocument(fd, null);
    }

    public void renderPageBitmap(PdfDocument doc, Bitmap bitmap, int pageIndex,
                                 int startX, int startY, int drawSizeX, int drawSizeY,
                                 boolean renderAnnot) {
        synchronized (lock) {
            try {
                nativeRenderPageBitmap(doc.mNativePagesPtr.get(pageIndex), bitmap, mCurrentDpi,
                        startX, startY, drawSizeX, drawSizeY, renderAnnot);
            } catch (NullPointerException e) {
                Log.e(TAG, "mContext may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
    }

    public void renderPageBitmap(PdfDocument doc, Bitmap bitmap, int pageIndex,
                                 int startX, int startY, int drawSizeX, int drawSizeY) {
        renderPageBitmap(doc, bitmap, pageIndex, startX, startY, drawSizeX, drawSizeY, true);
    }

    public long createDocument(){
        return nativecreateNewDocument();
    }

    public long addPage(long docptr , PDFPage page){
       return nativeaddPageToDocument(docptr, page);
    }

    public void addText(long docPtr , int index , byte[] text , float x , float y , float fontSize , float lineHeight){
        nativeAddTextToPage(docPtr, index, text, x, y, fontSize, lineHeight);
    }
    public boolean saveDocument(long docptr , String filePath){
        return nativeSaveDocument(docptr, filePath);
    }

    public void drawPath(String filePath , int pageIndex , List<PathData> pathData){
        Log.d("drawPathToPage" , "icore ::: " + "filePath :: " + filePath + " pageIndex :: " + pageIndex);

        nativeDrawPath(filePath, pageIndex, pathData);
    }

    public boolean saveDocumentAsStream(long docPtr , OutputStream outputStream){
        return nativeSaveDocumentAsStream(docPtr,outputStream);
    };

}
