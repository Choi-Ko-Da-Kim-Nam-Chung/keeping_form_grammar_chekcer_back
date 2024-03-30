package choiKoDaKimNamChung.grammarChecker.service.docx;


import choiKoDaKimNamChung.grammarChecker.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.docx.Paragraph;
import choiKoDaKimNamChung.grammarChecker.docx.Table;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocxParserImp implements DocxParser{

    @Override
    public Docx docxParse(XWPFDocument document, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public IBody iBodyParse(IBodyElement bodyElement, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public Table tableParse(XWPFTable table, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public Paragraph paragraphParse(XWPFParagraph paragraph, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public List<IBody> endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, IBodyType iBodyType) {
        return null;
    }

    @Override
    public List<IBody> headerParse(XWPFHeader header, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public List<IBody> footerParse(XWPFFooter footer, SpellCheckerType spellCheckerType) {
        return null;
    }
}
