package choiKoDaKimNamChung.grammarChecker.service;

import choiKoDaKimNamChung.grammarChecker.domain.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.domain.docx.SpellCheckerType;
import choiKoDaKimNamChung.grammarChecker.domain.hwp.Hwp;
import choiKoDaKimNamChung.grammarChecker.service.docx.DocxParser;
import choiKoDaKimNamChung.grammarChecker.service.hwp.HwpParser;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final DocxParser docxParser;
    private final HwpParser hwpParser;

    public ResponseEntity<Docx> grammarCheckDocxScan(MultipartFile file, SpellCheckerType type)
            throws ExecutionException, InterruptedException {
        XWPFDocument document;
        try {
            document = new XWPFDocument(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Docx spellCheckResponseDTO = docxParser.docxParse(document, type);
//        System.out.println("spellCheckResponseDTO = " + spellCheckResponseDTO);
        return ResponseEntity.ok(spellCheckResponseDTO);
    }

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
}
