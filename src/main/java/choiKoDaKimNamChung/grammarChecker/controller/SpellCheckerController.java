package choiKoDaKimNamChung.grammarChecker.controller;

import choiKoDaKimNamChung.grammarChecker.docx.SpellCheckerType;
import choiKoDaKimNamChung.grammarChecker.domain.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.domain.hwp.Hwp;
import choiKoDaKimNamChung.grammarChecker.service.ApplyService;
import choiKoDaKimNamChung.grammarChecker.service.ScanService;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxApply;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxParser;
import choiKoDaKimNamChung.grammarChecker.service.hwp.HwpApply;
import choiKoDaKimNamChung.grammarChecker.service.hwp.HwpParser;
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

    private final ScanService scanService;
    private final ApplyService applyService;
    private final ObjectMapper objectMapper;

    @PostMapping("/grammar-check/scan")
    public ResponseEntity<?> grammarCheckScan(@RequestParam("file") MultipartFile file,
                                              @RequestParam("type") SpellCheckerType type)
            throws ExecutionException, InterruptedException {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
            return scanService.grammarCheckDocxScan(file, type);
        } else if (fileName != null && fileName.toLowerCase().endsWith(".hwp")) {
            return scanService.grammarCheckHwpScan(file, type);
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
            return applyService.grammarCheckDocxApply(file, docx, newFileName);
        } else if (fileName != null && fileName.toLowerCase().endsWith(".hwp")) {
            Hwp hwp = objectMapper.readValue(data, Hwp.class);
            return applyService.grammarCheckHwpApply(file, hwp, newFileName);
        } else {
            throw new RuntimeException("지원하지 않는 파일 형식입니다.");
        }
    }

}
