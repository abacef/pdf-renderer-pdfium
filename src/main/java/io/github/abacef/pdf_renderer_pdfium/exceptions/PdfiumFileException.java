package io.github.abacef.pdf_renderer_pdfium.exceptions;

// FPDF_ERR_FILE = 2
public class PdfiumFileException extends PdfiumException {

    PdfiumFileException() {
        super("Pdfium encountered an error finding the file, or the file could not be opened");
    }
}
