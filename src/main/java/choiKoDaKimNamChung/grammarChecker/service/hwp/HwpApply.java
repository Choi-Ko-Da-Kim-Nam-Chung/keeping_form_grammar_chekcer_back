package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.IBody;
import choiKoDaKimNamChung.grammarChecker.domain.ParagraphText;
import choiKoDaKimNamChung.grammarChecker.domain.SpellData;
import choiKoDaKimNamChung.grammarChecker.domain.Table;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;

import java.util.Iterator;
import java.util.List;

public interface HwpApply {
    public HWPFile hwpApply(HWPFile hwpfile, SpellData hwp);

    public void applyFootnotesEndnotesHeaderFooter(HWPFile hwpFile, Iterator<List<IBody>> footnoteIter, Iterator<List<IBody>> endnoteIter, Iterator<IBody> headerIter, Iterator<IBody> footerIter) ;
    public void controlApply(Paragraph[] paragraphs, IBody iBody);


    public void controlApply(Paragraph paragraph, IBody iBody);

    public void paraTextApply(Paragraph paragraph, ParagraphText paragraphText);

    public void tableApply(ControlTable table, Table t);
}
