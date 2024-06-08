package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.domain.ParagraphText;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

//@Service
public class DocxFirstCharDocxParagraphApply implements DocxParagraphApply {

    @Override
    public void paragraphParse(XWPFParagraph paragraph, ParagraphText p) {
        int runIdx = paragraph.getRuns().size()-1;
        int errorIdx = p.getErrors().size()-1;
        int charIdx = paragraph.getText().length();
        while(errorIdx>=0){
            if(p.getErrors().get(errorIdx).getReplaceStr() == null){
                errorIdx--;
                continue;
            }
            while(charIdx - paragraph.getRuns().get(runIdx).text().length() > p.getErrors().get(errorIdx).getEnd() - 1) {
                charIdx -= paragraph.getRuns().get(runIdx).text().length();
                runIdx--;
            }

            if(charIdx - paragraph.getRuns().get(runIdx).text().length() > p.getErrors().get(errorIdx).getStart() && charIdx > p.getErrors().get(errorIdx).getEnd()){
                int length = paragraph.getRuns().get(runIdx).text().length();
                paragraph.getRuns().get(runIdx).setText(paragraph.getRuns().get(runIdx).text().substring(paragraph.getRuns().get(runIdx).text().length() - (charIdx - p.getErrors().get(errorIdx).getEnd())),0);
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
                change = origin.substring(0,origin.length() - (charIdx - error.getStart())) + error.getReplaceStr();
            }
            paragraph.getRuns().get(runIdx).setText(change,0);
        }


    }

}
