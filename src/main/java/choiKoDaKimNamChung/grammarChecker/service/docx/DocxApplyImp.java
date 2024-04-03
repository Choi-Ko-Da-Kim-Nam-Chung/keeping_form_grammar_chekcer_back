package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.*;
import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocxApplyImp implements DocxApply{
    @Override
    public XWPFDocument docxParse(XWPFDocument document, Docx docx) {
        //header
        List<IBodyElement> bodyElements = document.getBodyElements();
        for(int i=0; i<docx.getBody().size(); i++){
            iBodyParse(bodyElements.get(i), docx.getBody().get(i));
        }
        //footer
        return document;
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
        int runIdx = paragraph.getRuns().size()-1;
        int errorIdx = p.getErrors().size()-1;
        int charIdx = paragraph.getText().length();
        while(errorIdx>=0){
            System.out.println("paragraph = " + paragraph.getRuns().get(runIdx).text());
            if(p.getErrors().get(errorIdx).getReplaceStr() == null){
                errorIdx--;
                continue;
            }
            System.out.println("p.getErrors().get(errorIdx).getOrgStr() = " + p.getErrors().get(errorIdx).getOrgStr());
            while(charIdx - paragraph.getRuns().get(runIdx).text().length() > p.getErrors().get(errorIdx).getEnd() - 1) {
                charIdx -= paragraph.getRuns().get(runIdx).text().length();
                runIdx--;
            }

            System.out.println("(charIdx - paragraph.getRuns().get(runIdx).text().length()) = " + (charIdx - paragraph.getRuns().get(runIdx).text().length()));
            System.out.println("(p.getErrors().get(errorIdx).getStart()) = " + (p.getErrors().get(errorIdx).getStart()));
            System.out.println("charIdx = " + charIdx);
            System.out.println("(p.getErrors().get(errorIdx).getEnd()) = " + (p.getErrors().get(errorIdx).getEnd()));
            if(charIdx - paragraph.getRuns().get(runIdx).text().length() > p.getErrors().get(errorIdx).getStart() && charIdx > p.getErrors().get(errorIdx).getEnd()){
                int length = paragraph.getRuns().get(runIdx).text().length();
                System.out.println("paragraph.getRuns().get(runIdx).text().substring(paragraph.getRuns().get(runIdx).text().length() - (charIdx - p.getErrors().get(errorIdx).getEnd())) = " + paragraph.getRuns().get(runIdx).text().substring(paragraph.getRuns().get(runIdx).text().length() - (charIdx - p.getErrors().get(errorIdx).getEnd())));
                paragraph.getRuns().get(runIdx).setText(paragraph.getRuns().get(runIdx).text().substring(paragraph.getRuns().get(runIdx).text().length() - (charIdx - p.getErrors().get(errorIdx).getEnd())),0);
                System.out.println("paragraph.getRuns().get(runIdx).text() = " + paragraph.getRuns().get(runIdx).text());
                runIdx--;
                charIdx -= length;
            }
            while(charIdx - paragraph.getRuns().get(runIdx).text().length() > p.getErrors().get(errorIdx).getStart()){
                charIdx -= paragraph.getRuns().get(runIdx).text().length();
                paragraph.getRuns().get(runIdx).setText("",0);
                runIdx--;
            }

            String origin = paragraph.getRuns().get(runIdx).text();
            WordError error = p.getErrors().get(errorIdx--);
            String change;
            if(error.getEnd() < charIdx){
                change = origin.substring(0,origin.length() - (charIdx - error.getStart())) + error.getReplaceStr() + origin.substring(origin.length() - (charIdx - error.getEnd()));
            }else{
                System.out.println("error.getEnd() = " + error.getEnd());
                System.out.println("charIdx = " + charIdx);
                System.out.println("origin = " + origin);
                System.out.println("error.getReplaceStr() = " + error.getReplaceStr());

                change = origin.substring(0,origin.length() - (charIdx - error.getStart())) + error.getReplaceStr();
                System.out.println("change = " + change);
            }
            paragraph.getRuns().get(runIdx).setText(change,0);
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
