package pl.api.itoffers.offer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@RequiredArgsConstructor
public class Offer {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Getter
    private final String slug;
    @Getter
    private final String title;
    private final String seniority;
    @Embedded
    private final Salary salary;
    @Embedded
    private final Characteristics characteristics;
    @ManyToMany
    private final Set<Category> categories;
    @Getter
    @ManyToOne
    private final Company company;
    @Getter
    private final LocalDateTime publishedAt;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return title + " (" + slug + ") " + company.getName() + " publishedAt: " + publishedAt;
    }
}
