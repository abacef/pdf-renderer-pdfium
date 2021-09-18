package io.github.abacef.pdf_renderer_pdfium.exceptions;

// FPDF_ERR_PAGE = 6
public class PdfiumPageException extends PdfiumException {

    PdfiumPageException() {
        super("Pdfium encountered a PDF that does not have the page specified, " +
                "or the page's content has an error in it");
    }
}
