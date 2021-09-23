package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import com.sun.jna.Pointer;
import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import io.github.abacef.pdfium.Pdfium;
import lombok.Getter;

public class Page implements AutoCloseable {

    @Getter
    private Pointer pagePointer;

    private Form form;

    private Pdfium pdfium;

    public Page(
            final Pdfium pdfium,
            final Document document,
            final Form form, final int pageNum) throws PdfiumException {
        pagePointer = pdfium.FPDF_LoadPage(document.getDocumentPointer(), pageNum);
        if (pagePointer == null) {
            throw PdfiumException.exceptionNumberToException(pdfium.FPDF_GetLastError().intValue());
        }
        this.form = form;
        this.pdfium = pdfium;

        pdfium.FORM_OnAfterLoadPage(pagePointer, form.getFormPointer());
        pdfium.FORM_DoPageAAction(pagePointer, form.getFormPointer(), Pdfium.FPDFPAGE_AACTION_OPEN);
    }

    @Override
    public void close() {
        if (pdfium != null && form != null && pagePointer != null) {
            pdfium.FORM_DoPageAAction(pagePointer, form.getFormPointer(), Pdfium.FPDFPAGE_AACTION_CLOSE);
            pdfium.FORM_OnBeforeClosePage(pagePointer, form.getFormPointer());
            pdfium.FPDF_ClosePage(pagePointer);

            pdfium = null;
            form = null;
            pagePointer = null;
        }
    }
}
