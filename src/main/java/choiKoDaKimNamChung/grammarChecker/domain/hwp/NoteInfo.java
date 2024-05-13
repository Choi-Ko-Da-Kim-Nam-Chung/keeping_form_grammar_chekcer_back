package choiKoDaKimNamChung.grammarChecker.domain.hwp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NoteInfo extends IBody {
    NoteInfoType noteInfoType;
    int noteNum;

    public NoteInfo(NoteInfoType type, int noteNum) {
        this.noteInfoType = type;
        this.noteNum = noteNum;
    }
}

