package ro.editii.scriptorium.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @AllArgsConstructor @Builder
public class TeiRepoDto {
    String name;
    List<String> files;
}
