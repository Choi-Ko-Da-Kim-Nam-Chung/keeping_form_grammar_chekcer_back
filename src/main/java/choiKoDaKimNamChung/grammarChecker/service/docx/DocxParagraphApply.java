package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.domain.ParagraphText;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public interface DocxParagraphApply {
    public void paragraphParse(XWPFParagraph paragraph, ParagraphText p);
}
