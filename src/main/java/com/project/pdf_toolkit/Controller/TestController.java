package com.project.pdf_toolkit.Controller;

import com.project.pdf_toolkit.Service.PdfService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

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
}
