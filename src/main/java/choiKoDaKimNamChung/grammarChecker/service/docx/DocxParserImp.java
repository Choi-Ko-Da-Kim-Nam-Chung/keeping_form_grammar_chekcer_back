package choiKoDaKimNamChung.grammarChecker.service.docx;


import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocxParserImp implements DocxParser{
    @Override
    public DocxSpellCheckerDTO.SpellCheckDTO docxParse(XWPFDocument document, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public DocxSpellCheckerDTO.IBodyDTO iBodyParse(IBodyElement bodyElement, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public List<List<DocxSpellCheckerDTO.IBodyDTO>> tableParse(XWPFTable table, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public DocxSpellCheckerDTO.ParagraphDTO paragraphParse(XWPFParagraph paragraph, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public List<DocxSpellCheckerDTO.IBodyDTO> endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, IBodyType iBodyType) {
        return null;
    }

    @Override
    public List<DocxSpellCheckerDTO.IBodyDTO> headerParse(XWPFHeader header, SpellCheckerType spellCheckerType) {
        return null;
    }

    @Override
    public List<DocxSpellCheckerDTO.IBodyDTO> footerParse(XWPFFooter footer, SpellCheckerType spellCheckerType) {
        return null;
    }
}
