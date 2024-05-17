package choiKoDaKimNamChung.grammarChecker.controller;

import choiKoDaKimNamChung.grammarChecker.domain.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.domain.hwp.Hwp;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxApply;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxParser;
import choiKoDaKimNamChung.grammarChecker.domain.SpellCheckerType;
import choiKoDaKimNamChung.grammarChecker.service.hwp.HwpApply;
import choiKoDaKimNamChung.grammarChecker.service.hwp.HwpParser;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.writer.HWPWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SpellCheckerController {

    private final DocxParser docxParser;
    private final DocxApply docxApply;
    private final HwpParser hwpParser;
    private final HwpApply hwpApply;
    private final ObjectMapper objectMapper;

    @PostMapping("/grammar-check/scan")
    public ResponseEntity<?> grammarCheckScan(@RequestParam("file") MultipartFile file,
                                              @RequestParam("type") SpellCheckerType type)
            throws ExecutionException, InterruptedException {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
            return grammarCheckDocxScan(file, type);
        } else if (fileName != null && fileName.toLowerCase().endsWith(".hwp")) {
            return grammarCheckHwpScan(file, type);
        } else {
            throw new RuntimeException("지원하지 않는 파일 형식입니다.");
        }
    }

    @PostMapping("/grammar-check/apply")
    public ResponseEntity<InputStreamResource> grammarCheckApply(@RequestPart("file") MultipartFile file, @RequestPart("data") String data,
                                                                 @RequestPart(value = "fileName", required = false) String newFileName) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
            Docx docx = objectMapper.readValue(data, Docx.class);
            return grammarCheckDocxApply(file, docx, newFileName);
        } else if (fileName != null && fileName.toLowerCase().endsWith(".hwp")) {
            Hwp hwp = objectMapper.readValue(data, Hwp.class);
            return grammarCheckHwpApply(file, hwp, newFileName);
        } else {
            throw new RuntimeException("지원하지 않는 파일 형식입니다.");
        }
    }

//    @PostMapping("/grammar-check/docx/scan")
    public ResponseEntity<Docx> grammarCheckDocxScan(MultipartFile file, SpellCheckerType type)
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

//    @PostMapping("/grammar-check/docx/apply")
    public ResponseEntity<InputStreamResource> grammarCheckDocxApply(MultipartFile file, Docx docx, String newFileName) {
        XWPFDocument document;
        try {
            document = new XWPFDocument(file.getInputStream());

            document = docxApply.docxParse(document, docx);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            System.out.println("file.getOriginalFilename() = " + file.getOriginalFilename());
            HttpHeaders headers = new HttpHeaders();
            String fileName = (newFileName != null && !newFileName.isEmpty()) ? newFileName + ".docx" : "modified_" + file.getOriginalFilename();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(in));

        } catch (IOException e) {
            throw new RuntimeException("파일을 처리하는 도중 오류가 발생했습니다.", e);
        }
    }


//    @PostMapping("/grammar-check/hwp/scan")
    public ResponseEntity<Hwp> grammarCheckHwpScan(MultipartFile file, SpellCheckerType type) {
        HWPFile hwpFile;
        try {
            hwpFile = HWPReader.fromInputStream(file.getInputStream());

        } catch (Exception e) {
            throw new RuntimeException("파일을 처리하는 도중 오류가 발생했습니다.", e);
        }
        Hwp hwp = hwpParser.hwpParse(hwpFile, type);
        return ResponseEntity.ok(hwp);
    }


//    @PostMapping("/grammar-check/hwp/apply")
    public ResponseEntity<InputStreamResource> grammarCheckHwpApply(MultipartFile file, Hwp hwp, String newFileName) {
        HWPFile hwpFile;
        try {
            hwpFile = HWPReader.fromInputStream(file.getInputStream());

            hwpFile = hwpApply.hwpApply(hwpFile, hwp);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HWPWriter.toStream(hwpFile, out);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            HttpHeaders headers = new HttpHeaders();
            String fileName = (newFileName != null && !newFileName.isEmpty()) ? newFileName + ".hwp" : "modified_" + file.getOriginalFilename();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            throw new RuntimeException("파일을 처리하는 도중 오류가 발생했습니다.", e);
        }
    }
}
