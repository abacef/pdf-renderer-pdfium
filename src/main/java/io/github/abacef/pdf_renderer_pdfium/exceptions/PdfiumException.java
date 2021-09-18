package io.github.abacef.pdf_renderer_pdfium.exceptions;

import lombok.NonNull;

// A marker interface for the various PDFium exceptions
public class PdfiumException extends Exception {

    private static final int FPDF_ERR_UNKNOWN = 1;    // Unknown error.
    private static final int FPDF_ERR_FILE = 2;       // File not found or could not be opened.
    private static final int FPDF_ERR_FORMAT = 3;     // File not in PDF format or corrupted.
    private static final int FPDF_ERR_PASSWORD = 4;   // Password required or incorrect password.
    private static final int FPDF_ERR_SECURITY = 5;   // Unsupported security scheme.
    private static final int FPDF_ERR_PAGE = 6;       // Page not found or content error.

    public PdfiumException(final @NonNull String message) {
        super(message);
    }

    public static PdfiumException exceptionNumberToException(final int exceptionNumber) {
        switch (exceptionNumber) {
            case FPDF_ERR_UNKNOWN:
                return new PdfiumUnknownException();
            case FPDF_ERR_FILE:
                return new PdfiumFileException();
            case FPDF_ERR_FORMAT:
                return new PdfiumFormatException();
            case FPDF_ERR_PASSWORD:
                return new PdfiumPasswordException();
            case FPDF_ERR_SECURITY:
                return new PdfiumSecurityException();
            case FPDF_ERR_PAGE:
                return new PdfiumPageException();
            default:
                throw new IllegalArgumentException();
        }
    }
}
