package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.SpellCheckerType;
import choiKoDaKimNamChung.grammarChecker.domain.docx.*;
import choiKoDaKimNamChung.grammarChecker.domain.docx.IBody;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface DocxParser {
    public Docx docxParse(XWPFDocument document, SpellCheckerType spellCheckerType) throws ExecutionException, InterruptedException;

    public IBody iBodyParse(IBodyElement bodyElement, SpellCheckerType spellCheckerType);

    public Table tableParse(XWPFTable table, SpellCheckerType spellCheckerType);

    public Paragraph paragraphParse(XWPFParagraph paragraph, SpellCheckerType spellCheckerType);

//    public String footNoteEndNoteIgnore(String paragraph); 라이브러리를 직접 수정하는 것을 고려

    public List<IBody> headerParse(List<XWPFHeader> headerList, SpellCheckerType spellCheckerType);

    public List<IBody> footerParse(List<XWPFFooter> footerList, SpellCheckerType spellCheckerType);

}