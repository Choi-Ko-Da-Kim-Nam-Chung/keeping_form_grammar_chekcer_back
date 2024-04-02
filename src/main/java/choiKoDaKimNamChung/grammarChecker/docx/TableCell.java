package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TableCell{
    int colspan = 1;
    int rowspan = 1;
    public void plusRowSpan(){
        rowspan++;
    }
    IBody iBody;
}
