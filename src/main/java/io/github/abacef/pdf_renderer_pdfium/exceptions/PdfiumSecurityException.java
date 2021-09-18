package io.github.abacef.pdf_renderer_pdfium.exceptions;

// FPDF_ERR_SECURITY = 5
public class PdfiumSecurityException extends PdfiumException{

    PdfiumSecurityException() {
        super("Pdfium encountered an unsupported security scheme");
    }
}
