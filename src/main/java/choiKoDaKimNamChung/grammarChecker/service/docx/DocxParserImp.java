package choiKoDaKimNamChung.grammarChecker.service.docx;


import choiKoDaKimNamChung.grammarChecker.docx.*;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
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
        if(bodyElement.getElementType() == BodyElementType.PARAGRAPH){
            return (IBody) paragraphParse((XWPFParagraph)bodyElement,spellCheckerType);
//            if(!((XWPFParagraph) bodyElement).getFootnoteText().isEmpty()){}
        }else if(bodyElement.getElementType() == BodyElementType.TABLE){
            return (IBody) tableParse((XWPFTable)bodyElement, spellCheckerType);
        }else{
            System.out.println("bodyElement = " + bodyElement.getElementType());
        }
        return null;
    }

    @Override
    public Table tableParse(XWPFTable table, SpellCheckerType spellCheckerType) {
        Table t = new Table();
        for (XWPFTableRow row : table.getRows()) {
            List<choiKoDaKimNamChung.grammarChecker.docx.IBody> arr = new ArrayList<>();
            for (XWPFTableCell tableCell : row.getTableCells()) {
                for (IBodyElement bodyElement : tableCell.getBodyElements()) {
                    //셀 병합 체크도 필요
                    arr.add(iBodyParse(bodyElement, spellCheckerType));
                }
            }
            t.getTable().add(arr);
        }
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
