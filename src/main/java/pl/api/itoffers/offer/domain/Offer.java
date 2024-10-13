package pl.api.itoffers.offer.domain;

import jakarta.persistence.*;
import lombok.*;

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
    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "origin_id", referencedColumnName = "id")
    private Origin origin;
    @Getter
    private final String technology;
    @Getter
    private final String slug;
    @Getter
    private final String title;
    @Getter
    private final String seniority;
    @Embedded
    private final Characteristics characteristics;
    @Getter
    @ManyToMany
    private final Set<Category> categories;
    @NonNull
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Salary> salary;
    @Getter
    @ManyToOne
    private final Company company;
    @Getter
    @NonNull
    private LocalDateTime publishedAt;
    @Getter
    private final LocalDateTime createdAt;

    /**
     * Should be removed after Salaries migration
     */
    @Deprecated
    public void setSalaries(Set<Salary> salaries) {
        this.salary = salaries;
    }

    /**
     * Should be removed after Salaries migration
     */
    @Deprecated
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    /**
     * Should be removed after Provider/MongoDB ID migration
     */
    @Deprecated
    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public Set<Salary> getSalaries() {
        return salary;
    }

    @Override
    public String toString() {
        return title + " (" + slug + ") " + company.getName() + " publishedAt: " + publishedAt;
    }
}
