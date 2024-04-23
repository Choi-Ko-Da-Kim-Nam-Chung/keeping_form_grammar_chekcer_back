package choiKoDaKimNamChung.grammarChecker.docx;

import lombok.Data;

@Data
public class NoteInfo {
    NoteInfoType type;
    int noteNum;

    public NoteInfo(NoteInfoType type, int noteNum) {
        this.type = type;
        this.noteNum = noteNum;
    }
}

