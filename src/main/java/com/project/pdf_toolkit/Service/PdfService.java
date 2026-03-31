package com.project.pdf_toolkit.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;



@Service
public class PdfService {

    public int getPageCount(byte[] fileBytes) throws IOException {

        PDDocument document = PDDocument.load(fileBytes);
        int pages = document.getNumberOfPages();

        document.close();

        return pages;
    }

    public byte[] mergePDFs(MultipartFile[] files) throws IOException {

        PDFMergerUtility merger = new PDFMergerUtility();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (MultipartFile file : files) {
            merger.addSource(new ByteArrayInputStream(file.getBytes()));
        }

        merger.setDestinationStream(outputStream);

        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        return outputStream.toByteArray();
    }
}