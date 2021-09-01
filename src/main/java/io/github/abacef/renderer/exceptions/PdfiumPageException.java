package io.github.abacef.renderer.exceptions;

// FPDF_ERR_PAGE = 6
public class PdfiumPageException extends Exception implements PdfiumException {

    PdfiumPageException() {
        super("Pdfium encountered a PDF that does not have the page specified, or the page's content has an error in it");
    }
}
