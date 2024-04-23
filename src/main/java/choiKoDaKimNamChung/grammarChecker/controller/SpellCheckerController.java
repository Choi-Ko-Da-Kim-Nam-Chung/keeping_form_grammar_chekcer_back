package choiKoDaKimNamChung.grammarChecker.controller;

import choiKoDaKimNamChung.grammarChecker.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxApply;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxParser;
import choiKoDaKimNamChung.grammarChecker.docx.SpellCheckerType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SpellCheckerController {

    private final DocxParser docxParser;
    private final DocxApply docxApply;

    @PostMapping("/grammar-check/docx/scan")
    public ResponseEntity<Docx> grammarCheckDocxScan(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("type") SpellCheckerType type)
            throws ExecutionException, InterruptedException {
        XWPFDocument document;
        try {
            document = new XWPFDocument(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Docx spellCheckResponseDTO = docxParser.docxParse(document, type);
        System.out.println("spellCheckResponseDTO = " + spellCheckResponseDTO);
        return ResponseEntity.ok(spellCheckResponseDTO);
    }

    @PostMapping("/grammar-check/docx/apply")
    public ResponseEntity<InputStreamResource> grammarCheckDocxApply(@RequestPart("file") MultipartFile file, @RequestPart("data") Docx docx) {
        XWPFDocument document;
        try {
            document = new XWPFDocument(file.getInputStream());

            document = docxApply.docxParse(document, docx);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "modified_" + file.getOriginalFilename());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(in));

        } catch (IOException e) {
            throw new RuntimeException("파일을 처리하는 도중 오류가 발생했습니다.", e);
        }
    }

}
