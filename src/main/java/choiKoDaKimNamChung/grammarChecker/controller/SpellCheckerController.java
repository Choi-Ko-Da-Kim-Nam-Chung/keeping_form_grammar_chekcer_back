package choiKoDaKimNamChung.grammarChecker.controller;

import choiKoDaKimNamChung.grammarChecker.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxParser;
import choiKoDaKimNamChung.grammarChecker.docx.SpellCheckerType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SpellCheckerController {

    private final DocxParser docxParser;

    @PostMapping("/grammar-check/docx/scan")
    public ResponseEntity<Docx> grammarCheckDocxScan(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("type") SpellCheckerType type){
        XWPFDocument document;
        try {
            document = new XWPFDocument(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<List<Object>> list = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        Docx spellCheckResponseDTO = docxParser.docxParse(document, type);
        return ResponseEntity.ok(spellCheckResponseDTO);
    }

    @PostMapping("/grammar-check/docx/apply")
    public ResponseEntity<InputStreamResource> grammarCheckDocxApply(@RequestParam("file") MultipartFile file) {
        XWPFDocument document;
        try {
            document = new XWPFDocument(file.getInputStream());
            // document를 변경해서 재할당하는 코드 추가

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
