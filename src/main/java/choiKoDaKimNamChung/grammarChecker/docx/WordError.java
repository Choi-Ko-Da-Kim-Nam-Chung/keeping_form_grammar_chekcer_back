package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;

import java.util.List;

@Data
public class WordError {
    String help;
    String orgStr;
    List<String> candiWord;
    String replaceStr;
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
