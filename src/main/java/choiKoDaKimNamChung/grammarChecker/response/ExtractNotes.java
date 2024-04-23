package choiKoDaKimNamChung.grammarChecker.response;

import choiKoDaKimNamChung.grammarChecker.docx.IBody;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExtractNotes {
    private String plainPragraph = "";
    private List<IBody> errorList = new ArrayList<>();
}
