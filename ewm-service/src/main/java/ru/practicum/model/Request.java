package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    User requester;
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    Event event;
    @Enumerated(EnumType.STRING)
    RequestStatus status;
    @CreationTimestamp
    LocalDateTime created;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
}
