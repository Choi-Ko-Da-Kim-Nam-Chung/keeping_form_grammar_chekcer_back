package choiKoDaKimNamChung.grammarChecker.service.docx;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class DocxSpellCheckerDTO {

    @Data
    public static class SpellCheckDTO {
        List<IBodyDTO> header = new ArrayList<>();
        List<IBodyDTO> body = new ArrayList<>();
        List<IBodyDTO> footer = new ArrayList<>();
    }

    @Data
    public static class IBodyDTO {

    }

    @Data
    public static class ParagraphDTO{}
}
