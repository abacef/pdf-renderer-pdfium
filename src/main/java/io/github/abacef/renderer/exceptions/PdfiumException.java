package io.github.abacef.renderer.exceptions;

// A marker interface for the various PDFium exceptions
public interface PdfiumException {

    static PdfiumException exceptionNumberToException(int exceptionNumber) {
        switch (exceptionNumber) {
            case 1:
                return new PdfiumUnknownException();
            case 2:
                return new PdfiumFileException();
            case 3:
                return new PdfiumFormatException();
            case 4:
                return new PdfiumPasswordException();
            case 5:
                return new PdfiumSecurityException();
            case 6:
                return new PdfiumPageException();
            default:
                return null;
        }
    }
}
