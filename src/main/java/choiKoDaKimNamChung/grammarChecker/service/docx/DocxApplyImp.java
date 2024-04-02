package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.*;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import org.apache.poi.xwpf.usermodel.*;

import java.util.*;

public class DocxApplyImp implements DocxApply{
    @Override
    public XWPFDocument docxParse(XWPFDocument document, Docx docx) {
        //header
        List<IBodyElement> bodyElements = document.getBodyElements();
        for(int i=0; i<docx.getBody().size(); i++){
            iBodyParse(bodyElements.get(i), docx.getBody().get(i));
        }
        //footer
        return null;
    }

    @Override
    public void iBodyParse(IBodyElement bodyElement, IBody iBody) {
        if (iBody.getType() == IBodyType.PARAGRAPH){
            paragraphParse((XWPFParagraph) bodyElement,(Paragraph) iBody);
        }else if(iBody.getType() == IBodyType.TABLE){
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
                for (IBodyElement bodyElement : cell.getBodyElements()) {
                    iBodyParse(bodyElement, tCell.next().getIBody());
                }
            }
        }
    }

    @Override
    public void paragraphParse(XWPFParagraph paragraph, Paragraph p) {
        int runIdx = 0;
        int errorIdx = 0;
        int charIdx = 0;
        while(errorIdx < p.getErrors().size()){
            //변경사항 없음
            if(p.getErrors().get(errorIdx).getReplaceStr() == null){
                errorIdx++;
                continue;
            }
            charIdx += paragraph.getRuns().get(runIdx).text().length();
            if(charIdx >= p.getErrors().get(errorIdx).getStart()){
                paragraph.getRuns().get(runIdx).setText(p.getErrors().get(errorIdx).getReplaceStr());
                while(charIdx < p.getErrors().get(errorIdx).getEnd()){
                    charIdx += paragraph.getRuns().get(++runIdx).text().length();;
                    paragraph.getRuns().get(runIdx).setText("");
                }
                //런 처리
                errorIdx++;
            }else{
                runIdx++;
            }

        }

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
