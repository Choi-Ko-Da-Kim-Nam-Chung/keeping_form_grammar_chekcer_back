package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;

@Data
public class EntireInfo {
    private Docx docx;
    private int footnoteEnter = 0;
    private int endnoteEnter = 0;

    public EntireInfo(Docx docx) {
        this.docx = docx;
    }

    public void countFootEnter(){
        this.footnoteEnter++;
    }

    public void countEndEnter(){
        this.endnoteEnter++;
    }
}
