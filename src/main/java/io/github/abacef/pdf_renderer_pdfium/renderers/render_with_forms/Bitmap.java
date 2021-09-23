package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import com.sun.jna.NativeLong;
import io.github.abacef.pdfium.Pdfium;
import lombok.val;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Bitmap implements AutoCloseable {

    private static final int POINTS_PER_INCH = 72;

    private Page page;

    private Pdfium pdfium;

    private int[] bitmapBuffer;

    private int width;

    private int height;

    public enum OutputImageType {
        PNG,
        JPG
    }

    public Bitmap(
            final Pdfium pdfium,
            final Page page,
            final Form form,
            final int dpi,
            final OutputImageType imageType) {
        if (imageType != OutputImageType.PNG) {
            throw new IllegalArgumentException("This renderer currently only supports PNG images being rendered");
        }

        this.pdfium = pdfium;
        this.page = page;
        val pageWidthInPoints = pdfium.FPDF_GetPageWidthF(page.getPagePointer());
        val pageWidthInInches = pageWidthInPoints / POINTS_PER_INCH;
        width = (int)(pageWidthInInches * dpi);

        val pageHeightInPoints = pdfium.FPDF_GetPageHeightF(page.getPagePointer());
        val pageHeightInInches = pageHeightInPoints / POINTS_PER_INCH;
        height = (int)(pageHeightInInches * dpi);

        val alpha = pdfium.FPDFPage_HasTransparency(page.getPagePointer());

        val bitmap = pdfium.FPDFBitmap_Create(width, height, alpha);
        if (bitmap == null) {
            System.out.println("bitmap is null. Please test me");
        }

        val fillColor = new NativeLong(alpha == 1 ? 0x00000000 : 0xffffffff);
        pdfium.FPDFBitmap_FillRect(bitmap, 0, 0, width, height, fillColor);
        pdfium.FPDF_RenderPageBitmap(bitmap, page.getPagePointer(), 0, 0, width, height, 0, Pdfium.FPDF_ANNOT);
        pdfium.FPDF_FFLDraw(form.getFormPointer(),
                bitmap, page.getPagePointer(), 0, 0, width, height, 0, Pdfium.FPDF_ANNOT);

        val cBuffer = pdfium.FPDFBitmap_GetBuffer(bitmap);
        val pixelCount = width * height;
        bitmapBuffer = cBuffer.getIntArray(0, pixelCount);
    }

    public byte[] convertToImage() {
        val bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        bufferedImage.setRGB(0, 0, width, height, bitmapBuffer, 0, width);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException ioe) {
            // throwing a runtime exception because this should never happen since our byte stream is all in memory
            throw new RuntimeException(
                    "pdf-renderer-pdfium library was unable to convert the bitmap to an image for some unknown reason");
        }
        return baos.toByteArray();
    }

    @Override
    public void close() {
        if (page != null && pdfium != null && bitmapBuffer != null) {
            pdfium.FPDF_RenderPage_Close(page.getPagePointer());

            bitmapBuffer = null;
            pdfium = null;
            page = null;
        }
    }
}
