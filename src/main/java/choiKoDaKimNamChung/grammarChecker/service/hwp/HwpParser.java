package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.docx.SpellCheckerType;
import choiKoDaKimNamChung.grammarChecker.domain.hwp.*;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.ParaText;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HwpParser {

    public Hwp hwpParse(HWPFile hwpfile, SpellCheckerType spellCheckerType) {
        Hwp hwp = new Hwp();
        for (Section section : hwpfile.getBodyText().getSectionList()) {
            for (Paragraph paragraph : section.getParagraphs()) {
                hwp.getBody().add(controlParse(paragraph, spellCheckerType));
            }
        }
        return hwp;
    }

    public IBody controlParse(Paragraph paragraph, SpellCheckerType spellCheckerType){
        if(paragraph.getControlList() != null) {
            for (Control control : paragraph.getControlList()) {
                if(control.getType() == ControlType.Table){
                    return tableParse((ControlTable) control, spellCheckerType);
                }else{
                    System.out.println("control.getType() = " + control.getType());
                }
            }
        }

        return paraTextParse(paragraph.getText(), spellCheckerType);
    }

    public ParagraphText paraTextParse(ParaText paraText, SpellCheckerType spellCheckerType){
        ParagraphText paragraphText = new ParagraphText();
        try{
            paragraphText.setOrgStr(paraText.getNormalString(0));

            return paragraphText;
        }catch(Exception e){
            System.out.println("e.getMessage() = " + e.getMessage());
        }
        return new ParagraphText();
    }

//    public IBody controlParse(Control control, SpellCheckerType spellCheckerType){
//        if(control.getType()== ControlType.Table){
//            tableParse((ControlTable)control, spellCheckerType);
//        }else if(control.getType() == ControlType.ColumnDefine){
////            System.out.println("control.getType() = " + ((ControlColumnDefine)control).getCtrlData());
//        }else{
////            System.out.println("control.getType() = " + control.getType());
//        }
//        return null;
//
//    }

    public Table tableParse(ControlTable table, SpellCheckerType spellCheckerType){
        Table t = new Table();
//        table.getCaption().getParagraphList();
        for (Row row : table.getRowList()) {
            List<TableCell> tablecell = new ArrayList<>();
            for (Cell cell : row.getCellList()) {
                TableCell c = new TableCell();
                c.setColspan(cell.getListHeader().getColSpan());
                c.setRowspan(cell.getListHeader().getRowSpan());
                for (Paragraph paragraph : cell.getParagraphList()) {
                    c.getIBody().add(controlParse(paragraph, spellCheckerType));
                }
                tablecell.add(c);
            }
            t.getTable().add(tablecell);
        }

        return t;
    }

//    public static void main(String[] args) {
//        String filePath = "/Users/minji/school/전종설/testfile/test.hwp";
//        HWPFile hwpFile;
//        HwpParser test = new HwpParser();
//
//        try {
//            hwpFile = HWPReader.fromFile(filePath);
//            test.hwpParse(hwpFile,SpellCheckerType.BUSAN_UNIV);
//        }catch(Exception e){
//            System.out.println(e.getMessage());
//        }
//    }
}
