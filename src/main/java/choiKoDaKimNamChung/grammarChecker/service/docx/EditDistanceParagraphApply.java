package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class EditDistanceParagraphApply implements ParagraphApply{

    @Override
    public void paragraphParse(XWPFParagraph paragraph, Paragraph p) {
        int runIdx = paragraph.getRuns().size() - 1;

    }
}
