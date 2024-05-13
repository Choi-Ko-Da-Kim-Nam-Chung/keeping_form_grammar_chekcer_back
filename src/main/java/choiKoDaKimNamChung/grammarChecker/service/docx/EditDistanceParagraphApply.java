package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.domain.docx.Paragraph;
import choiKoDaKimNamChung.grammarChecker.response.WordError;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;

@Service
public class EditDistanceParagraphApply implements ParagraphApply{

    @Override
    public void paragraphParse(XWPFParagraph paragraph, Paragraph p) {
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
                if(edit==Edit.CASCADE){
                    sb.setCharAt(runEditIndex--, error.getReplaceStr().charAt(replaceEditIndex--));
                } else if (edit == Edit.DELETE) {
                    sb.deleteCharAt(runEditIndex--);
                } else if(edit == Edit.ADD){
                    sb.insert(runEditIndex + 1, error.getReplaceStr().charAt(replaceEditIndex--));
                } else{
                    runEditIndex--;
                    replaceEditIndex--;
                }

                if(runEditIndex < 0 && runIdx != 0){
                    paragraph.getRuns().get(runIdx).setText(sb.toString(),0);
                    runEditIndex = paragraph.getRuns().get(--runIdx).text().length() - 1;
                    sb = new StringBuilder(paragraph.getRuns().get(runIdx).text());
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

    protected Queue<Edit> trackingEditDistance(String origin, String replace) {
        int m = origin.length();
        int n = replace.length();

        // dp 배열을 사용하여 최소 편집 횟수를 계산
        int[][] dp = new int[m + 1][n + 1];

        // 초기화
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;  // origin을 빈 문자열로 변경
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;  // 빈 문자열을 replace로 변경
        }

        // 편집 거리 계산
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (origin.charAt(i - 1) == replace.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];  // 문자가 같다면 변경 필요 없음
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);  // 삭제, 추가, 대체 중 최소 비용
                }
            }
        }

        // 최소 편집 경로 추적
        Queue<Edit> edits = new LinkedList<>();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (origin.charAt(i - 1) == replace.charAt(j - 1)) {
                edits.add(Edit.KEEP);
                i--;
                j--;
            } else if (dp[i][j] == dp[i - 1][j - 1] + 1) {
                edits.add(Edit.CASCADE);  // 대체
                i--;
                j--;
            } else if (dp[i][j] == dp[i - 1][j] + 1) {
                edits.add(Edit.DELETE);  // 삭제
                i--;
            } else if (dp[i][j] == dp[i][j - 1] + 1) {
                edits.add(Edit.ADD);  // 추가
                j--;
            }
        }

        // 시작까지 도달
        while (i > 0) {
            edits.add(Edit.DELETE);
            i--;
        }
        while (j > 0) {
            edits.add(Edit.ADD);
            j--;
        }

        return edits;
    }

    //편집거리 테스트
    public static void main(String[] args) {
        EditDistanceParagraphApply edt = new EditDistanceParagraphApply();
        Queue<Edit> result = edt.trackingEditDistance("num20", "nUM 20");
        while (!result.isEmpty()) {
            System.out.println(result.poll());
        }
    }

    protected enum Edit {
        ADD,KEEP,DELETE,CASCADE;
    }

}
