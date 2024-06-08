package choiKoDaKimNamChung.grammarChecker.domain.docx;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SpellCheckerType {
    BUSAN_UNIV("http://localhost:8000/busan"),
    INCRUIT("http://localhost:8000/incruit"),
    JOB_KOREA("http://localhost:8000/jobkorea");

    private String url;
}

