package io.github.abacef.renderer.exceptions;

// FPDF_ERR_UNKNOWN  = 1;
public class PdfiumUnknownException extends PdfiumException {

    PdfiumUnknownException() {
        super("Pdfium encountered an unknown error");
    }
}
