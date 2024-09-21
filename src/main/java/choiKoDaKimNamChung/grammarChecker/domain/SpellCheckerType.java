package choiKoDaKimNamChung.grammarChecker.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SpellCheckerType {
    BUSAN_UNIV("http://127.0.0.1:8000/busan"),
    INCRUIT("http://127.0.0.1:8000/incruit"),
    JOB_KOREA("http://127.0.0.1:8000/jobkorea");
    private String url;
}
