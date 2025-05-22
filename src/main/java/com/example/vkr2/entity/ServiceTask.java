package com.example.vkr2.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_task")
public class ServiceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_record_id", nullable = false)
    private ServiceRecord serviceRecord;

    @Column(nullable = false, length = 500)
    private String taskName;

    @Column(length = 2000)
    private String taskDescription;
}