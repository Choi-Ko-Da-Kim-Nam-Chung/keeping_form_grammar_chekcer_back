package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.Docx;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import choiKoDaKimNamChung.grammarChecker.docx.Paragraph;
import choiKoDaKimNamChung.grammarChecker.docx.Table;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

public class DocxApplyImp implements DocxApply{
    @Override
    public XWPFDocument docxParse(XWPFDocument document, Docx docx) {
        return null;
    }

    @Override
    public void iBodyParse(IBodyElement bodyElement, IBody iBody) {

    }

    @Override
    public void tableParse(XWPFTable table, Table t) {

    }

    @Override
    public void paragraphParse(XWPFParagraph paragraph, Paragraph p) {

    }

    @Override
    public void endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, List<IBody> iBodyList) {

    }

    @Override
    public void headerParse(XWPFHeader header, List<IBody> iBodyList) {

    }

    @Override
    public void footerParse(XWPFFooter footer, List<IBody> iBodyList) {

    }
}
