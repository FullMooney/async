package com.dev.mq.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class EventVO {
    
    @Schema(example = "Ligue1")
    private String league;
    @Schema(example = "PSG")
    private String team;
    @Schema(example = "Lee Gang-in")
    private String name;
    private int number;
    private int wage;
}
