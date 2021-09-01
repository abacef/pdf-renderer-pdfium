package io.github.abacef.renderer.exceptions;

// FPDF_ERR_SECURITY = 5
public class PdfiumSecurityException extends Exception implements PdfiumException{

    PdfiumSecurityException() {
        super("Pdfium encountered an unsupported security scheme");
    }
}
