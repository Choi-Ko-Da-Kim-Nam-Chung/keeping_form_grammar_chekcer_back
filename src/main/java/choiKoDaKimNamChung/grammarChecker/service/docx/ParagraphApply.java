package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.EntireInfo;
import choiKoDaKimNamChung.grammarChecker.docx.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public interface ParagraphApply {
    public void paragraphParse(XWPFParagraph paragraph, Paragraph p, EntireInfo entireInfo);
}
