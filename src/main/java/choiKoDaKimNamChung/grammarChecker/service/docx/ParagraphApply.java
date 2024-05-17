package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.domain.docx.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public interface ParagraphApply {
    public void paragraphParse(XWPFParagraph paragraph, Paragraph p);
}
