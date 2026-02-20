package com.persons.finder.domain;

import lombok.*;
import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "security_patterns", indexes = {
        @Index(name = "idx_pattern_unique", columnList = "pattern", unique = true),
        @Index(name = "idx_pattern_type", columnList = "type")
})
public class SecurityPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pattern; // 敏感词词条

    @Column(nullable = false)
    private String type; // 'INPUT_FILTER' or 'OUTPUT_FILTER'

    private String description;
}