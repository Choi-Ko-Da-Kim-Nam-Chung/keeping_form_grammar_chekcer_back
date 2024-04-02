package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.*;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

public interface DocxApply {
    public XWPFDocument docxParse(XWPFDocument document, Docx docx);

    public void iBodyParse(IBodyElement bodyElement, IBody iBody);

    public void tableParse(XWPFTable table, Table t);

    public void paragraphParse(XWPFParagraph paragraph, Paragraph p);

//    public String footNoteEndNoteIgnore(String paragraph); 라이브러리를 직접 수정하는 것을 고려

    public void endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, List<IBody> iBodyList);

    public void headerParse(XWPFHeader header, List<IBody> iBodyList);

    public void footerParse(XWPFFooter footer, List<IBody> iBodyList);
}
