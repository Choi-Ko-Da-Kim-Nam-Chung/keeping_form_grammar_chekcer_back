package choiKoDaKimNamChung.grammarChecker.service;

import choiKoDaKimNamChung.grammarChecker.domain.SpellData;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxApply;
import choiKoDaKimNamChung.grammarChecker.service.hwp.HwpApply;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.writer.HWPWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


@Service
@RequiredArgsConstructor
public class ApplyService {


    private final DocxApply docxApply;
    private final HwpApply hwpApply;

    public ResponseEntity<InputStreamResource> grammarCheckDocxApply(MultipartFile file, SpellData docx) {
        XWPFDocument document;
        try {
            document = new XWPFDocument(file.getInputStream());

            document = docxApply.docxParse(document, docx);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            HttpHeaders headers = new HttpHeaders();

            headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s" , "response.docx"));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(in));

        } catch (IOException e) {
            throw new RuntimeException("파일을 처리하는 도중 오류가 발생했습니다.", e);
        }
    }


    public ResponseEntity<InputStreamResource> grammarCheckHwpApply(MultipartFile file, SpellData hwp) {
        HWPFile hwpFile;
        try {
            hwpFile = HWPReader.fromInputStream(file.getInputStream());

            hwpFile = hwpApply.hwpApply(hwpFile, hwp);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HWPWriter.toStream(hwpFile, out);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            HttpHeaders headers = new HttpHeaders();

            headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s" , "response.hwp"));
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            throw new RuntimeException("파일을 처리하는 도중 오류가 발생했습니다.", e);
        }
    }

}
