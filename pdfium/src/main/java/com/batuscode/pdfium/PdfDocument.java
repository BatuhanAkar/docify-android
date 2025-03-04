package com.batuscode.pdfium;

import android.os.ParcelFileDescriptor;
import android.util.ArrayMap;

import java.util.Map;

public class PdfDocument {

    /*package*/ public PdfDocument() {
    }

    /*package*/ public long mNativeDocPtr;
    /*package*/ public ParcelFileDescriptor parcelFileDescriptor;


    /*package*/ public final Map<Integer, Long> mNativePagesPtr = new ArrayMap<>();

    public boolean hasPage(int index) {
        return mNativePagesPtr.containsKey(index);
    }
}
