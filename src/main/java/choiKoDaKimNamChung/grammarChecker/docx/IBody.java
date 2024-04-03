package choiKoDaKimNamChung.grammarChecker.docx;

import choiKoDaKimNamChung.grammarChecker.response.WordError;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Paragraph.class, name = "PARAGRAPH"),
        @JsonSubTypes.Type(value = Table.class, name = "TABLE")
})
@Getter
@NoArgsConstructor
public abstract class IBody {
    @JsonIgnore
    private IBodyType type;

    public IBody(IBodyType iBodyType){
        this.type = iBodyType;
    }
}
