package com.persons.finder.domain;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "persons", indexes = {
        @Index(name = "idx_location", columnList = "latitude, longitude")
})
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String jobTitle;

    private String hobbies;

    // 存储 AI 生成的简介
    @Column(columnDefinition = "TEXT")
    private String bio;

    // 地理位置：纬度和经度
    private Double latitude;
    private Double longitude;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}