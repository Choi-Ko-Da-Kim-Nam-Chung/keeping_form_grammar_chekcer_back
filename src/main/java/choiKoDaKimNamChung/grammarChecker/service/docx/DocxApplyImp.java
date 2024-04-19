package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.*;

import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DocxApplyImp implements DocxApply{

    private final ParagraphApply paragraphApply;
    @Override
    public XWPFDocument docxParse(XWPFDocument document, Docx docx) {
        EntireInfo entireInfo = new EntireInfo(docx);

        //header
        headerParse(document.getHeaderList(), docx.getHeader(), entireInfo);

        List<IBodyElement> bodyElements = document.getBodyElements();
        for(int i=0; i<docx.getBody().size(); i++){
            iBodyParse(bodyElements.get(i), docx.getBody().get(i), entireInfo);
        }

        //footer
        footerParse(document.getFooterList(), docx.getFooter(), entireInfo);

        return document;
    }

    @Override
    public void iBodyParse(IBodyElement bodyElement, IBody iBody, EntireInfo entireInfo) {
        if (iBody.getType() == IBodyType.PARAGRAPH){
            paragraphApply.paragraphParse((XWPFParagraph) bodyElement,(Paragraph) iBody, entireInfo);
        }else if(iBody.getType() == IBodyType.TABLE){
            tableParse((XWPFTable) bodyElement,(Table) iBody, entireInfo);
        }else{

        }

    }

    @Override
    public void tableParse(XWPFTable table, Table t, EntireInfo entireInfo) {
        Iterator<List<TableCell>> tRow = t.getTable().iterator();
        for (XWPFTableRow row : table.getRows()) {
            Iterator<TableCell> tCell = tRow.next().iterator();
            for (XWPFTableCell cell : row.getTableCells()) {
                if (cell.getCTTc().getTcPr().getVMerge() != null && cell.getCTTc().getTcPr().getVMerge().getVal() == null) {
                    continue;
                }
                Iterator<IBody> iBody = tCell.next().getIBody().iterator();
                for (IBodyElement bodyElement : cell.getBodyElements()) {
                    iBodyParse(bodyElement, iBody.next(), entireInfo);
                }
            }
        }
    }

    @Override
    public void endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, List<IBody> iBodyList, EntireInfo entireInfo) {

    }

    @Override
    public void headerParse(List<XWPFHeader> headerList, List<IBody> parsedHeaderList, EntireInfo entireInfo) {
        for(int i=0; i<parsedHeaderList.size(); i++){
            iBodyParse((IBodyElement) headerList.get(i), parsedHeaderList.get(i), entireInfo);
        }
    }

    @Override
    public void footerParse(List<XWPFFooter> footerList, List<IBody> parsedFooterList, EntireInfo entireInfo) {
        for(int i=0; i<parsedFooterList.size(); i++){
            iBodyParse((IBodyElement) footerList.get(i), parsedFooterList.get(i), entireInfo);
        }
    }
}
