package choiKoDaKimNamChung.grammarChecker.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SpellCheckerType {
    BUSAN_UNIV("http://3.39.130.18:8000/busan"),
    INCRUIT("http://3.39.130.18:8000/incruit"),
    JOB_KOREA("http://3.39.130.18:8000/jobkorea");
    private String url;
}
