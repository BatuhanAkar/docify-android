package com.batuscode.pdfium;

public class PDFPage {
    private float width;  // Page width in points (1 point = 1/72 inch)
    private float height; // Page height in points

    public PDFPage(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
