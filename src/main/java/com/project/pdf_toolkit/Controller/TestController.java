package com.project.pdf_toolkit.Controller;

import com.project.pdf_toolkit.Service.PdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {

    private final PdfService pdfService;

    public TestController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

//    @PostMapping("/upload")
//    public String uploadFile(@RequestParam("file") MultipartFile file){
//
//        try {
//            String projectPath = System.getProperty("user.dir");
//            String uploadDir = projectPath + File.separator + "uploads";
//
//            File dir = new File(uploadDir);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            String fileName = file.getOriginalFilename();
//            File destination = new File(dir, fileName);
//            file.transferTo(destination);
//
//            return "File saved at: " + destination.getAbsolutePath();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Error uploading file";
//        }
//    }

    @PostMapping("/page-count")
    public String pageCount(@RequestParam("file")  MultipartFile file){
        try {

            int pages = pdfService.getPageCount(file.getBytes());

            return "Total pages: "+pages;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading PDF";

        }
    }

    @GetMapping("/pdf-test")
    public String pdfTest() {
        return "PDFBox installed successfully";
    }

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePDFs(
            @RequestParam("files") MultipartFile[] files) {

        try {

            byte[] merged = pdfService.mergePDFs(files);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(merged);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/split")
    public ResponseEntity<byte[]> splitPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ranges") String ranges) throws IOException {

        byte[] zipData = pdfService.splitPdfByRanges(file, ranges);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=split.zip")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipData);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
