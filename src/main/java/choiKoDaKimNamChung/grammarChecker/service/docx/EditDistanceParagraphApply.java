package choiKoDaKimNamChung.grammarChecker.service.docx;

import choiKoDaKimNamChung.grammarChecker.docx.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.util.LinkedList;
import java.util.Queue;

public class EditDistanceParagraphApply implements ParagraphApply{

    @Override
    public void paragraphParse(XWPFParagraph paragraph, Paragraph p) {
        int runIdx = paragraph.getRuns().size() - 1;

    }


    public Queue<Edit> trackingEditDistance(String origin, String replace) {
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
        Queue<Edit> result = edt.trackingEditDistance("안농하세요", "안녕하세요");
        while (!result.isEmpty()) {
            System.out.println(result.poll());
        }
    }

    private enum Edit {
        ADD,KEEP,DELETE,CASCADE;
    }

}
