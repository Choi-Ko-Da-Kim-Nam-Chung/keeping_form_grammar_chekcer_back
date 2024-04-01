package choiKoDaKimNamChung.grammarChecker.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class WordError {
    String help;
    String orgStr;
    List<String> candWord;
    int errorIdx;
    int start;
    int end;
//    {
//        "help": "틀린 이유",
//        "orgStr": "원본문자열",
//        "candWord": [
//            "추천문자열"
//        ],
//        "errorIdx": 파라그래프 내 오류번호,
//        "correctMethod": 1,
//        "start": orgStr 시작위치,
//        "end": orgStr 종료위치
//    }
}
