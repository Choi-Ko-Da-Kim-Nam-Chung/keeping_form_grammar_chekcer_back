package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.domain.IBody;
import choiKoDaKimNamChung.grammarChecker.domain.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocxApplyImp implements DocxApply{

    private final DocxParagraphApply docxParagraphApply;
    @Override
    public XWPFDocument docxParse(XWPFDocument document, SpellData docx) {

        //header
        headerParse(document.getHeaderList(), docx.getHeader());

        //body
        List<IBodyElement> bodyElements = document.getBodyElements();
        for(int i=0; i<docx.getBody().size(); i++){
            iBodyParse(bodyElements.get(i), docx.getBody().get(i));
        }

        //footnote
        int i = 0;
        for (XWPFFootnote footnote : document.getFootnotes()) {
            if (footnote.getCTFtnEdn().toString().contains("<w:continuationSeparator/>") || footnote.getCTFtnEdn().toString().contains("<w:separator/>")){
                continue;
            }
            List<IBody> footNoteElements = docx.getFootNote().get(i);
            for(int j = 0;j<footnote.getBodyElements().size();j++){
                iBodyParse(footnote.getBodyElements().get(j), footNoteElements.get(j));
            }
            i++;
        }

        //endnote
        i = 0;
        for (XWPFEndnote endnote : document.getEndnotes()) {
            if (endnote.getCTFtnEdn().toString().contains("<w:continuationSeparator/>") || endnote.getCTFtnEdn().toString().contains("<w:separator/>")){
                continue;
            }
            List<IBody> endNoteElements = docx.getEndNote().get(i);
            for(int j = 0;j<endnote.getBodyElements().size();j++){
                iBodyParse(endnote.getBodyElements().get(j), endNoteElements.get(j));
            }
            i++;
        }

        //footer
        footerParse(document.getFooterList(), docx.getFooter());

        return document;
    }

    @Override
    public void iBodyParse(IBodyElement bodyElement, IBody iBody) {
        if (bodyElement.getElementType() == BodyElementType.PARAGRAPH){
            docxParagraphApply.paragraphParse((XWPFParagraph) bodyElement,(ParagraphText) iBody);
        }else if(bodyElement.getElementType() == BodyElementType.TABLE){
            tableParse((XWPFTable) bodyElement,(Table) iBody);
        }else{

        }

    }

    @Override
    public void tableParse(XWPFTable table, Table t) {
        Iterator<List<TableCell>> tRow = t.getTable().iterator();
        for (XWPFTableRow row : table.getRows()) {
            Iterator<TableCell> tCell = tRow.next().iterator();
            for (XWPFTableCell cell : row.getTableCells()) {
                if (cell.getCTTc().getTcPr().getVMerge() != null && cell.getCTTc().getTcPr().getVMerge().getVal() == null) {
                    continue;
                }
                Iterator<IBody> iBody = tCell.next().getIBody().iterator();
                for (IBodyElement bodyElement : cell.getBodyElements()) {
                    iBodyParse(bodyElement, iBody.next());
                }
            }
        }
    }

    @Override
    public void endNoteFootNoteParse(XWPFAbstractFootnoteEndnote note, List<IBody> iBodyList) {

    }

    @Override
    public void headerParse(List<XWPFHeader> headerList, List<IBody> parsedHeaderList) {
        for(int i=0; i<parsedHeaderList.size(); i++){
            XWPFHeader header = headerList.get(i);
            IBody parsedHeader = parsedHeaderList.get(i);
            for (IBodyElement element : header.getBodyElements()) {
                iBodyParse(element, parsedHeader);
            }
        }
    }

    @Override
    public void footerParse(List<XWPFFooter> footerList, List<IBody> parsedFooterList) {
        for(int i=0; i<parsedFooterList.size(); i++){
            XWPFFooter footer = footerList.get(i);
            IBody parsedFooter = parsedFooterList.get(i);
            for (IBodyElement element : footer.getBodyElements()) {
                iBodyParse(element, parsedFooter);
            }
        }
    }
}