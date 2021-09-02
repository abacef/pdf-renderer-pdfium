package io.github.abacef.renderer.exceptions;

// FPDF_ERR_PASSWORD = 4
public class PdfiumPasswordException extends PdfiumException {

    PdfiumPasswordException() {
        super("Pdfium encountered a PDF that requires a password or the password that was supplied was incorrect");
    }
}
