package choiKoDaKimNamChung.grammarChecker.service.hwp;

import choiKoDaKimNamChung.grammarChecker.domain.hwp.*;
import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.*;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.object.bodytext.paragraph.text.ParaText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        Iterator<IBody> headerIter = hwp.getHeader().iterator();
        Iterator<IBody> footerIter = hwp.getFooter().iterator();
        Iterator<List<IBody>> footnoteIter = hwp.getFootNote().iterator();
        Iterator<List<IBody>> endnoteIter = hwp.getEndNote().iterator();

        applyFootnotesEndnotesHeaderFooter(hwpfile, footnoteIter, endnoteIter, headerIter, footerIter);

        return hwpfile;
    }

    public void applyFootnotesEndnotesHeaderFooter(HWPFile hwpFile, Iterator<List<IBody>> footnoteIter, Iterator<List<IBody>> endnoteIter, Iterator<IBody> headerIter, Iterator<IBody> footerIter) {
        Iterator<IBody> fnIter = footnoteIter.next().iterator();
        Iterator<IBody> enIter = endnoteIter.next().iterator();

        for (Section section : hwpFile.getBodyText().getSectionList()) {
            for (Paragraph paragraph : section.getParagraphs()) {
                if (paragraph.getControlList() != null) {
                    for (Control control : paragraph.getControlList()) {
                        switch (control.getType()) {
                            case Footnote:
                                ControlFootnote footnote = (ControlFootnote) control;
                                controlApply(footnote.getParagraphList().getParagraphs(), fnIter.next());
                                if (footnoteIter.hasNext()) {
                                    fnIter = footnoteIter.next().iterator();
                                }
                                break;
                            case Endnote:
                                ControlEndnote endnote = (ControlEndnote) control;
                                controlApply(endnote.getParagraphList().getParagraphs(), enIter.next());
                                if (endnoteIter.hasNext()) {
                                    enIter = endnoteIter.next().iterator();
                                }
                                break;
                            case Header:
                                ControlHeader header = (ControlHeader) control;
                                controlApply(header.getParagraphList().getParagraphs(), headerIter.next());
                                break;
                            case Footer:
                                ControlFooter footer = (ControlFooter) control;
                                controlApply(footer.getParagraphList().getParagraphs(), footerIter.next());
                                break;
                            default:
                                // 다른 경우에 대한 처리
                                System.out.println("control.getType() = " + control.getType());
                        }
                    }
                }
            }
        }
    }
    public void controlApply(Paragraph[] paragraphs, IBody iBody) {
        for (Paragraph paragraph : paragraphs) {
            controlApply(paragraph, iBody);
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
