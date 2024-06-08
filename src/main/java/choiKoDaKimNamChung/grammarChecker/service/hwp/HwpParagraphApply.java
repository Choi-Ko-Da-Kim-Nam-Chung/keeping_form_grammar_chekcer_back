package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.ParagraphText;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;

public interface HwpParagraphApply {
    public void paragraphParse(Paragraph paragraph, ParagraphText paragraphText);
}
