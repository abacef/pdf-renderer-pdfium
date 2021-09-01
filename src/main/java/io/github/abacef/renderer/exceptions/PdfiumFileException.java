package io.github.abacef.renderer.exceptions;

// FPDF_ERR_FILE = 2
public class PdfiumFileException extends Exception implements PdfiumException {

    PdfiumFileException() {
        super("Pdfium encountered an error finding the file, or the file could not be opened");
    }
}