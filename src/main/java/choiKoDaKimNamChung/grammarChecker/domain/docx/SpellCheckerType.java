package choiKoDaKimNamChung.grammarChecker.domain.docx;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SpellCheckerType {
    BUSAN_UNIV("http://10.0.134.104:8000/busan"),
    INCRUIT("http://10.0.134.104:8000/incruit"),
    JOB_KOREA("http://10.0.134.104:8000/jobkorea");
    private String url;
}
