package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.service.docx.DocxSpellCheckerDTO.*;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

public interface DocxParser {
    public SpellCheckDTO docxParse(XWPFDocument document, SpellCheckerType spellCheckerType);

    public IBodyDTO iBodyParse(IBodyElement bodyElement, SpellCheckerType spellCheckerType);

    public List<List<IBodyDTO>> tableParse(XWPFTable table, SpellCheckerType spellCheckerType);

    public ParagraphDTO paragraphParse(XWPFParagraph paragraph, SpellCheckerType spellCheckerType);

//    public String footNoteEndNoteIgnore(String paragraph); 라이브러리를 직접 수정하는 것을 고려

    public List<IBodyDTO> endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, IBodyType iBodyType);

    public List<IBodyDTO> headerParse(XWPFHeader header, SpellCheckerType spellCheckerType);

    public List<IBodyDTO> footerParse(XWPFFooter footer, SpellCheckerType spellCheckerType);

}
