package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.docx.SpellCheckerType;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.Control;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.reader.HWPReader;

public class HwpParser {

    public void hwpParse(HWPFile hwpfile, SpellCheckerType spellCheckerType) {
        for (Section section : hwpfile.getBodyText().getSectionList()) {
            sectionParse(section, spellCheckerType);
        }

    }

    public void sectionParse(Section section, SpellCheckerType spellCheckerType){
        for (Paragraph paragraph : section.getParagraphs()) {
            paragraphParse(paragraph,spellCheckerType);
        }


    }

    public void paragraphParse(Paragraph paragraph, SpellCheckerType spellCheckerType){
        if(paragraph.getControlList() != null) {
            for (Control control : paragraph.getControlList()) {
                controlParse(control, spellCheckerType);
            }
        }
        try {
            System.out.println("paragraph.getNormalString() = " + paragraph.getNormalString());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void controlParse(Control control, SpellCheckerType spellCheckerType){
        if(control.getType()== ControlType.Table){
            tableParse((ControlTable)control, spellCheckerType);
        }
    }

    public void tableParse(ControlTable table, SpellCheckerType spellCheckerType){
        for (Row row : table.getRowList()) {
            for (Cell cell : row.getCellList()) {

//                cell.getListHeader();
                for (Paragraph paragraph : cell.getParagraphList()) {
                    paragraphParse(paragraph, spellCheckerType);
                }

            }
        }

    }

    public static void main(String[] args) {
        String filePath = "/Users/minji/school/전종설/testfile/test.hwp";
        HWPFile hwpFile;
        HwpParser test = new HwpParser();

        try {
            hwpFile = HWPReader.fromFile(filePath);
            test.hwpParse(hwpFile,SpellCheckerType.BUSAN_UNIV);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
