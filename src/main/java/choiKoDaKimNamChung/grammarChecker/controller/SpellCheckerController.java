package choiKoDaKimNamChung.grammarChecker.controller;

import choiKoDaKimNamChung.grammarChecker.domain.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.domain.docx.SpellCheckerType;
import choiKoDaKimNamChung.grammarChecker.domain.hwp.Hwp;
import choiKoDaKimNamChung.grammarChecker.service.ApplyService;
import choiKoDaKimNamChung.grammarChecker.service.ScanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = { "https://api.spell-checker.co.kr", "http://api.spell-checker.co.kr",
        "https://spell-checker.co.kr", "http://spell-checker.co.kr", "localhost:3000" })
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
    public ResponseEntity<InputStreamResource> grammarCheckApply(@RequestPart("file") MultipartFile file, @RequestPart("data") String data) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
            Docx docx = objectMapper.readValue(data, Docx.class);
            return applyService.grammarCheckDocxApply(file, docx);
        } else if (fileName != null && fileName.toLowerCase().endsWith(".hwp")) {
            Hwp hwp = objectMapper.readValue(data, Hwp.class);
            return applyService.grammarCheckHwpApply(file, hwp);
        } else {
            throw new RuntimeException("지원하지 않는 파일 형식입니다.");
        }
    }

}
