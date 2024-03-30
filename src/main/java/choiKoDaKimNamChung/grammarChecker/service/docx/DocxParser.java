package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.docx.Paragraph;
import choiKoDaKimNamChung.grammarChecker.docx.Table;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

public interface DocxParser {
    public Docx docxParse(XWPFDocument document, SpellCheckerType spellCheckerType);

    public IBody iBodyParse(IBodyElement bodyElement, SpellCheckerType spellCheckerType);

    public Table tableParse(XWPFTable table, SpellCheckerType spellCheckerType);

    public Paragraph paragraphParse(XWPFParagraph paragraph, SpellCheckerType spellCheckerType);

//    public String footNoteEndNoteIgnore(String paragraph); 라이브러리를 직접 수정하는 것을 고려

    public List<IBody> endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, IBodyType iBodyType);

    public List<IBody> headerParse(XWPFHeader header, SpellCheckerType spellCheckerType);

    public List<IBody> footerParse(XWPFFooter footer, SpellCheckerType spellCheckerType);

}
