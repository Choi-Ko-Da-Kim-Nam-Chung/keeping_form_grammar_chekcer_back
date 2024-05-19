package choiKoDaKimNamChung.grammarChecker.service;

import choiKoDaKimNamChung.grammarChecker.domain.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.domain.hwp.Hwp;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxApply;
import choiKoDaKimNamChung.grammarChecker.service.hwp.HwpApply;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.writer.HWPWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
@RequiredArgsConstructor
public class ApplyService {


    private final DocxApply docxApply;
    private final HwpApply hwpApply;

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