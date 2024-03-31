package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.service.docx.DocxSpellCheckerDTO.*;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

public interface DocxParse {
    public SpellCheckResponseDTO docxParse(XWPFDocument document, SpellCheckerType type);

    public IBodyDTO iBodyParse(IBodyElement bodyElement, SpellCheckerType type);

    public List<List<IBodyDTO>> tableParse(XWPFTable table, SpellCheckerType type);

    public ParagraphDTO paragraphParse(XWPFParagraph paragraph, SpellCheckerType type);

    public String footNoteEndNoteIgnore(String paragraph);

    public List<IBodyDTO> EndNoteParse(XWPFEndnote endNote);

    public List<IBodyDTO> FootNoteParse(XWPFFootnote footnote);

    public List<IBodyDTO> headerParse(XWPFHeader header, SpellCheckerType type);

    public List<IBodyDTO> footerParse(XWPFFooter footer, SpellCheckerType type);

}
