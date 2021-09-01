package io.github.abacef.renderer.exceptions;

// FPDF_ERR_FORMAT = 3
public class PdfiumFormatException extends Exception implements PdfiumException {

    PdfiumFormatException() {
        super("PDFium encountered a PDF that is not in the PDF format, or it is corrupted");
    }
}
