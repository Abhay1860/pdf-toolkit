package com.project.pdf_toolkit.Service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.ByteArrayOutputStream;



@Service
public class PdfService {

    public int getPageCount(byte[] fileBytes) throws IOException {

        PDDocument document = Loader.loadPDF(fileBytes);

        int pages = document.getNumberOfPages();

        document.close();

        return pages;
    }
}