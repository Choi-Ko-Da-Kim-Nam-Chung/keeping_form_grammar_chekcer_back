package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.*;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.ParaText;

import java.util.List;

public interface HwpParser
{
    public SpellData hwpParse(HWPFile hwpfile, SpellCheckerType spellCheckerType);

    // 매 번 전체 본문을 순회하는것보다 미주,각주,머리글,바닥글 한번에 하는게 효율적일 것 같아서 한번에 묶음.
    // 따로 미주와 각주만 뽑아내는건 찾지 못함
    public void extractFootnotesEndnotesHeaderFooter(SpellData hwp, HWPFile hwpFile, SpellCheckerType spellCheckerType);

    public IBody controlParse(Paragraph paragraph, SpellCheckerType spellCheckerType);

    public ParagraphText paraTextParse(ParaText paraText, SpellCheckerType spellCheckerType);

    public void asyncParse(List<IBody> body, Paragraph[] paragraphs, SpellCheckerType spellCheckerType);


    public Table tableParse(ControlTable table, SpellCheckerType spellCheckerType);
}
