package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.*;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

public interface DocxApply {
    public XWPFDocument docxParse(XWPFDocument document, Docx docx);

    public void iBodyParse(IBodyElement bodyElement, IBody iBody, EntireInfo entireInfo);

    public void tableParse(XWPFTable table, Table t, EntireInfo entireInfo);

//    public String footNoteEndNoteIgnore(String paragraph); 라이브러리를 직접 수정하는 것을 고려

    public void endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, List<IBody> iBodyList, EntireInfo entireInfo);

    public void headerParse(List<XWPFHeader> headerList, List<IBody> parsedHeaderList, EntireInfo entireInfo);

    public void footerParse(List<XWPFFooter> footerList, List<IBody> parsedFooterList, EntireInfo entireInfo);
}
