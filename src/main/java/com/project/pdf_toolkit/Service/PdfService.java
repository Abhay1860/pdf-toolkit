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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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

    public byte[] splitPdfByRanges(MultipartFile file, String ranges) throws IOException {

        try (PDDocument document = PDDocument.load(file.getInputStream());
             ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(zipBaos)) {

            String[] rangeArray = ranges.split(",");

            int fileIndex = 1;

            for (String range : rangeArray) {
                String[] parts = range.split("-");
                int start = Integer.parseInt(parts[0].trim()) - 1; // zero-based
                int end = Integer.parseInt(parts[1].trim()) - 1;

                PDDocument newDoc = new PDDocument();

                for (int i = start; i <= end; i++) {
                    newDoc.addPage(document.getPage(i));
                }

                ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();
                newDoc.save(pdfBaos);
                newDoc.close();

                ZipEntry zipEntry = new ZipEntry("split_" + fileIndex + ".pdf");
                zipOut.putNextEntry(zipEntry);
                zipOut.write(pdfBaos.toByteArray());
                zipOut.closeEntry();

                fileIndex++;
            }

            zipOut.finish();
            return zipBaos.toByteArray();
        }
    }
}