package choiKoDaKimNamChung.grammarChecker.domain.docx;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SpellCheckerType {
    BUSAN_UNIV("http://34.64.157.92:8000/busan"),
    INCRUIT("http://34.64.157.92:8000/incruit"),
    JOB_KOREA("http://34.64.157.92:8000/jobkorea");
    private String url;
}
