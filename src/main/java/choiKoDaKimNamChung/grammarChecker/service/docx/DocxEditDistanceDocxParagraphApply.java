package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.domain.ParagraphText;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import choiKoDaKimNamChung.grammarChecker.service.EditDistance.Edit;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.util.Queue;

import static choiKoDaKimNamChung.grammarChecker.service.EditDistance.trackingEditDistance;

@Service
public class DocxEditDistanceDocxParagraphApply implements DocxParagraphApply {

    @Override
    public void paragraphParse(XWPFParagraph paragraph, ParagraphText p) {
        int runIdx = paragraph.getRuns().size() - 1;
        int errorIdx = p.getErrors().size() - 1;
        int charIdx = paragraph.getText().length();

        while(errorIdx>=0){
            if(p.getErrors().get(errorIdx).getReplaceStr() == null){
                errorIdx--;
                continue;
            }
            WordError error = p.getErrors().get(errorIdx--);
            Queue<Edit> edits = trackingEditDistance(error.getOrgStr(), error.getReplaceStr());
            while(charIdx - paragraph.getRuns().get(runIdx).text().length() > error.getEnd() - 1) {
                charIdx -= paragraph.getRuns().get(runIdx).text().length();
                runIdx--;
            }

            int runEditIndex = paragraph.getRuns().get(runIdx).text().length() - (charIdx - error.getEnd()) - 1;
            int replaceEditIndex = error.getReplaceStr().length() - 1;
            StringBuilder sb = new StringBuilder(paragraph.getRuns().get(runIdx).text());

            for (Edit edit : edits) {
                while(runEditIndex < 0 && runIdx != 0){
                    paragraph.getRuns().get(runIdx).setText(sb.toString(),0);
                    runEditIndex = paragraph.getRuns().get(--runIdx).text().length() - 1;
                    sb = new StringBuilder(paragraph.getRuns().get(runIdx).text());
                }
                if(edit==Edit.CASCADE){
                    sb.setCharAt(runEditIndex--, error.getReplaceStr().charAt(replaceEditIndex--));
                } else if (edit == Edit.DELETE) {
                    charIdx--;
                    sb.deleteCharAt(runEditIndex--);
                } else if(edit == Edit.ADD){
                    charIdx++;
                    sb.insert(runEditIndex + 1, error.getReplaceStr().charAt(replaceEditIndex--));
                } else{
                    runEditIndex--;
                    replaceEditIndex--;
                }

            }
            paragraph.getRuns().get(runIdx).setText(sb.toString(),0);
        }
    }

//
//    protected String changeString(String str, Integer i, Edit edit, Character c){
//        if(edit==Edit.CASCADE){
//            return str.substring(0, i) + c.toString() + str.substring(i+1);
//        }
//        if(edit==Edit.DELETE){
//            return str.substring(0, i) + str.substring(i+1);
//        }
//        if(edit==Edit.ADD){
//            return str.substring(0, i) + c.toString() + str.substring(i);
//        }
//        return str;
//    }

}
