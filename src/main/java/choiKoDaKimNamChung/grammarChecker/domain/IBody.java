package choiKoDaKimNamChung.grammarChecker.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ParagraphText.class, name = "PARAGRAPH"),
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
