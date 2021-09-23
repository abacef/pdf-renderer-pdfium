package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import com.sun.jna.Pointer;
import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import io.github.abacef.pdfium.Pdfium;
import io.github.abacef.pdfium.fpdf_formfill.FPDF_FORMFILLINFO;
import lombok.Getter;
import lombok.val;

public class Form implements AutoCloseable {

    @Getter
    private Pointer formPointer;

    private Pdfium pdfium;

    public Form(final Pdfium pdfium, final Document document) throws PdfiumException {
        formPointer = pdfium.FPDFDOC_InitFormFillEnvironment(document.getDocumentPointer(), makeFormFillInfo());
        if (formPointer == null) {
            throw PdfiumException.exceptionNumberToException(pdfium.FPDF_GetLastError().intValue());
        }
        this.pdfium = pdfium;

        pdfium.FORM_DoDocumentOpenAction(formPointer);
    }

    private static FPDF_FORMFILLINFO makeFormFillInfo() {
        val formFillInfo = new FPDF_FORMFILLINFO();
        formFillInfo.version = 2;
        return formFillInfo;
    }

    @Override
    public void close() {
        if (pdfium != null && formPointer != null) {
            pdfium.FPDFDOC_ExitFormFillEnvironment(formPointer);

            pdfium = null;
            formPointer = null;
        }
    }
}
