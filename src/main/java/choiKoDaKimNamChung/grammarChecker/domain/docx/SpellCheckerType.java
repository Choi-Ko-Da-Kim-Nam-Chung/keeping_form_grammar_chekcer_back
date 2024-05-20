package choiKoDaKimNamChung.grammarChecker.domain.docx;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SpellCheckerType {
    BUSAN_UNIV("localhost:8000/busan"),
    INCRUIT("localhost:8000/incruit"),
    JOB_KOREA("localhost:8000/jobkorea");

    private String url;
}
