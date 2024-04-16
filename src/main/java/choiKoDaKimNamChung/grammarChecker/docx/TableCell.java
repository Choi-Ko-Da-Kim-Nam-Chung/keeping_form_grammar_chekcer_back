package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TableCell{
    int colspan = 1;
    int rowspan = 1;
    public void plusRowSpan(){
        rowspan++;
    }
    List<IBody> iBody = new ArrayList<>();
}
