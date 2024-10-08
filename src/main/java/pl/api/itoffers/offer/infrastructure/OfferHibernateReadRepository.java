package pl.api.itoffers.offer.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.api.itoffers.offer.application.dto.outgoing.OfferDto;
import pl.api.itoffers.offer.application.repository.OfferReadRepository;
import pl.api.itoffers.offer.domain.Offer;
import pl.api.itoffers.offer.domain.Salary;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OfferHibernateReadRepository implements OfferReadRepository {

    private final EntityManager em;

    public List<OfferDto> getBySalary(
        int amountTo,
        String currency,
        String employmentType,
        List<String> technologies,
        LocalDateTime from,
        LocalDateTime to
    ) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Offer> query = builder.createQuery(Offer.class);
        Root<Offer> offerRoot = query.from(Offer.class);
        Join<Offer, Salary> salary = offerRoot.join("salary");
        CriteriaBuilder.In<String> technologyInClause = builder.in(offerRoot.get("technology"));
        for (String technology : technologies) {
            technologyInClause.value(technology);
        }
        query.select(offerRoot);
        query.where(
            builder.and(
                builder.greaterThanOrEqualTo(salary.get("amount").get("to"), amountTo),
                builder.equal(salary.get("employmentType"), employmentType),
                builder.equal(salary.get("amount").get("currency"), currency),
                builder.between(offerRoot.get("createdAt"), from, to),
                technologyInClause
            )
        );

        TypedQuery<Offer> typedQuery = em.createQuery(query);
        List<Offer> offers = typedQuery.getResultList();

        List<OfferDto> dtos = new ArrayList<OfferDto>();
        offers.forEach(offer -> dtos.addAll(OfferDto.createFrom(offer, currency, employmentType)));
        sortDesc(dtos);
        return dtos;
    }

    private static void sortDesc(List<OfferDto> offers) {
        offers.sort(new Comparator<OfferDto>() {
            @Override
            public int compare(OfferDto dto1, OfferDto dto2) {
                return dto2.getAmountTo().compareTo(dto1.getAmountTo());
            }
        });
    }
}
