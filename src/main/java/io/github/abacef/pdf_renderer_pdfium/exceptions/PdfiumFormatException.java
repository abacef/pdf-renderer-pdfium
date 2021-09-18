package io.github.abacef.pdf_renderer_pdfium.exceptions;

// FPDF_ERR_FORMAT = 3
public class PdfiumFormatException extends PdfiumException {

    PdfiumFormatException() {
        super("PDFium encountered a PDF that is not in the PDF format, or it is corrupted");
    }
}
