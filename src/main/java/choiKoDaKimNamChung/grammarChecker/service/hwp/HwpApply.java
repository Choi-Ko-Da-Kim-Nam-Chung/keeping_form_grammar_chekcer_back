package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.hwp.*;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlFootnote;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.ParaText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HwpApply {

    private final EditDistanceParagraphTextApply paragraphTextApply;

    public HWPFile hwpApply(HWPFile hwpfile, Hwp hwp) {
        Iterator<IBody> iter = hwp.getBody().iterator();
        for (Section section : hwpfile.getBodyText().getSectionList()) {
            for (Paragraph paragraph : section.getParagraphs()) {
                controlApply(paragraph, iter.next());
            }
        }

        return hwpfile;
    }

    private void applyParagraphs(Paragraph[] paragraphs, List<IBody> bodies) {
        Iterator<IBody> iter = bodies.iterator();
        for (Paragraph paragraph : paragraphs) {
            if (iter.hasNext()) {
                controlApply(paragraph, iter.next());
            }
        }
    }

    public void controlApply(Paragraph paragraph, IBody iBody){
        if(paragraph.getControlList() != null) {
            for (Control control : paragraph.getControlList()) {
                if(control.getType() == ControlType.Table){
                    tableApply((ControlTable) control, (Table) iBody);
                    return;
                }else{
                    System.out.println("control.getType() = " + control.getType());
                }
            }
        }
        paraTextApply(paragraph, (ParagraphText)iBody);
    }

    public void paraTextApply(Paragraph paragraph, ParagraphText paragraphText){
        try{
            paragraphTextApply.paragraphParse(paragraph, paragraphText);
        }catch(Exception e){
            System.out.println("e.getMessage() = " + e.getMessage());
        }
    }

    public void tableApply(ControlTable table, Table t){
//        table.getCaption().getParagraphList();
        Iterator<List<TableCell>> r = t.getTable().iterator();
        for (Row row : table.getRowList()) {
            Iterator<TableCell> c = r.next().iterator();
            for (Cell cell : row.getCellList()) {
                Iterator<IBody> ibody = c.next().getIBody().iterator();
                for (Paragraph paragraph : cell.getParagraphList()) {
                    controlApply(paragraph, ibody.next());
                }
            }
        }
    }
}
